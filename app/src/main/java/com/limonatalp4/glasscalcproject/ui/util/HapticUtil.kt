package com.limonatalp4.glasscalcproject.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.compositionLocalOf
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.roundToInt
import kotlin.math.roundToLong

val LocalHapticFeedbackEnabled = compositionLocalOf { true }
val LocalHapticIntensity = compositionLocalOf { 5 }

enum class HapticMotorType {
    NONE,
    ERM,
    LRA
}

private data class CachedHapticHardware(
    val contextRef: WeakReference<Context>,
    val vibrator: Vibrator?,
    val motorType: HapticMotorType,
    val supportsPrimitiveClick: Boolean
)

object HapticUtil {

    @Volatile
    var currentIntensityLevel: Int = 5

    @Volatile
    private var cachedHardware: CachedHapticHardware? = null
    private val hardwareLock = Any()

    private val hapticExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "GlassCalc-Haptic").apply {
            isDaemon = true
        }
    }

    fun levelToIntensityFloat(level: Int): Float {
        return level.coerceIn(1, 10) / 10f
    }

    private fun resolveHardware(context: Context): CachedHapticHardware {
        val keyContext = context.applicationContext ?: context
        val existing = cachedHardware
        if (existing != null && existing.contextRef.get() === keyContext) {
            return existing
        }
        return synchronized(hardwareLock) {
            val current = cachedHardware
            if (current != null && current.contextRef.get() === keyContext) {
                return@synchronized current
            }
            val vibrator = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = keyContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    keyContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            } catch (_: Throwable) {
                null
            }
            val hasMotor = try {
                vibrator?.hasVibrator() == true
            } catch (_: Throwable) {
                false
            }
            val motorType = if (!hasMotor || vibrator == null) {
                HapticMotorType.NONE
            } else {
                try {
                    if (vibrator.hasAmplitudeControl()) HapticMotorType.LRA else HapticMotorType.ERM
                } catch (_: Throwable) {
                    HapticMotorType.ERM
                }
            }
            val supportsPrimitiveClick = if (motorType == HapticMotorType.LRA && vibrator != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
                } catch (_: Throwable) {
                    false
                }
            } else {
                false
            }
            CachedHapticHardware(
                contextRef = WeakReference(keyContext),
                vibrator = vibrator,
                motorType = motorType,
                supportsPrimitiveClick = supportsPrimitiveClick
            ).also { cachedHardware = it }
        }
    }

    fun getVibrator(context: Context): Vibrator? {
        return resolveHardware(context).vibrator
    }

    /**
     * Prüft die Hardware des Geräts:
     * - hasVibrator(): Gibt an, ob überhaupt ein Vibrationsmotor vorhanden ist.
     * - hasAmplitudeControl(): Bei true handelt es sich um einen hochwertigen LRA-Motor (Linear Resonant Actuator).
     *   Bei false handelt es sich um einen einfachen ERM-Motor (Eccentric Rotating Mass).
     */
    fun getHapticMotorType(context: Context): HapticMotorType {
        return resolveHardware(context).motorType
    }

    fun hasVibrator(context: Context): Boolean {
        return resolveHardware(context).motorType != HapticMotorType.NONE
    }

    fun performAppHaptic(
        context: Context,
        view: View,
        enabled: Boolean,
        intensityLevel: Int = currentIntensityLevel
    ) {
        if (!enabled) return

        val appCtx = context.applicationContext ?: context
        val viewRef = WeakReference(view)
        try {
            hapticExecutor.execute {
                val hardware = resolveHardware(appCtx)
                val vibrator = hardware.vibrator
                if (vibrator == null || hardware.motorType == HapticMotorType.NONE) {
                    return@execute
                }
                val intensityFloat = levelToIntensityFloat(intensityLevel)
                try {
                    when (hardware.motorType) {
                        HapticMotorType.LRA -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hardware.supportsPrimitiveClick) {
                                val composition = VibrationEffect.startComposition()
                                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, intensityFloat)
                                    .compose()
                                vibrator.vibrate(composition)
                            } else {
                                val amplitude = (intensityFloat * 255f).roundToInt().coerceIn(1, 255)
                                vibrator.vibrate(VibrationEffect.createOneShot(20L, amplitude))
                            }
                        }
                        HapticMotorType.ERM -> {
                            val durationMs = (15L + (intensityFloat * 45f).roundToLong()).coerceIn(15L, 65L)
                            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        HapticMotorType.NONE -> return@execute
                    }
                } catch (_: Throwable) {
                    val v = viewRef.get()
                    if (v != null) {
                        v.post {
                            try {
                                v.performHapticFeedback(
                                    HapticFeedbackConstants.KEYBOARD_TAP,
                                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                )
                            } catch (_: Throwable) {
                            }
                        }
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }

    fun performAppLongPressHaptic(
        context: Context,
        view: View,
        enabled: Boolean,
        intensityLevel: Int = currentIntensityLevel
    ) {
        if (!enabled) return

        val appCtx = context.applicationContext ?: context
        val viewRef = WeakReference(view)
        try {
            hapticExecutor.execute {
                val hardware = resolveHardware(appCtx)
                val vibrator = hardware.vibrator
                if (vibrator == null || hardware.motorType == HapticMotorType.NONE) {
                    return@execute
                }
                val intensityFloat = levelToIntensityFloat(intensityLevel)
                try {
                    when (hardware.motorType) {
                        HapticMotorType.LRA -> {
                            val amplitude = (intensityFloat * 255f).roundToInt().coerceIn(1, 255)
                            vibrator.vibrate(VibrationEffect.createOneShot(45L, amplitude))
                        }
                        HapticMotorType.ERM -> {
                            val durationMs = (40L + (intensityFloat * 50f).roundToLong()).coerceIn(40L, 95L)
                            vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        HapticMotorType.NONE -> return@execute
                    }
                } catch (_: Throwable) {
                    val v = viewRef.get()
                    if (v != null) {
                        v.post {
                            try {
                                v.performHapticFeedback(
                                    HapticFeedbackConstants.LONG_PRESS,
                                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                                )
                            } catch (_: Throwable) {
                            }
                        }
                    }
                }
            }
        } catch (_: Throwable) {
        }
    }
}
