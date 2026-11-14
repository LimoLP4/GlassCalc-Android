package com.limonatalp4.glasscalcproject.ui.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.compositionLocalOf
import kotlin.math.roundToInt
import kotlin.math.roundToLong

val LocalHapticFeedbackEnabled = compositionLocalOf { true }
val LocalHapticIntensity = compositionLocalOf { 5 }

enum class HapticMotorType {
    NONE,
    ERM,
    LRA
}

object HapticUtil {

    @Volatile
    var currentIntensityLevel: Int = 5

    fun levelToIntensityFloat(level: Int): Float {
        return level.coerceIn(1, 10) / 10f
    }

    fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Prüft die Hardware des Geräts:
     * - hasVibrator(): Gibt an, ob überhaupt ein Vibrationsmotor vorhanden ist.
     * - hasAmplitudeControl(): Bei true handelt es sich um einen hochwertigen LRA-Motor (Linear Resonant Actuator).
     *   Bei false handelt es sich um einen einfachen ERM-Motor (Eccentric Rotating Mass).
     */
    fun getHapticMotorType(context: Context): HapticMotorType {
        return try {
            val vibrator = getVibrator(context) ?: return HapticMotorType.NONE
            if (!vibrator.hasVibrator()) {
                return HapticMotorType.NONE
            }
            if (vibrator.hasAmplitudeControl()) {
                HapticMotorType.LRA
            } else {
                HapticMotorType.ERM
            }
        } catch (_: Throwable) {
            HapticMotorType.NONE
        }
    }

    fun hasVibrator(context: Context): Boolean {
        return try {
            getHapticMotorType(context) != HapticMotorType.NONE
        } catch (_: Throwable) {
            false
        }
    }

    fun performAppHaptic(
        context: Context,
        view: View,
        enabled: Boolean,
        intensityLevel: Int = currentIntensityLevel
    ) {
        if (!enabled) return

        try {
            val vibrator = getVibrator(context)
            if (vibrator == null || !vibrator.hasVibrator()) {
                return
            }

            val intensityFloat = levelToIntensityFloat(intensityLevel)

            when (getHapticMotorType(context)) {
                HapticMotorType.LRA -> {
                    // Für LRA-Motoren (Amplitudenkontrolle unterstützt):
                    // Amplitude (0.1f..1.0f) skalieren
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                        vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
                    ) {
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
                    // Für ERM-Motoren (keine Amplitudenkontrolle):
                    // Dauer leicht nach Intensitätsstufe skalieren (Stufe 1: ~20ms, Stufe 10: ~60ms)
                    val durationMs = (15L + (intensityFloat * 45f).roundToLong()).coerceIn(15L, 65L)
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                HapticMotorType.NONE -> return
            }
        } catch (_: Throwable) {
            // Fallback auf Standard View Haptic Feedback bei Hardware- oder Berechtigungsfehlern
            try {
                view.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Throwable) {
            }
        }
    }

    fun performAppLongPressHaptic(
        context: Context,
        view: View,
        enabled: Boolean,
        intensityLevel: Int = currentIntensityLevel
    ) {
        if (!enabled) return

        try {
            val vibrator = getVibrator(context)
            if (vibrator == null || !vibrator.hasVibrator()) {
                return
            }

            val intensityFloat = levelToIntensityFloat(intensityLevel)

            when (getHapticMotorType(context)) {
                HapticMotorType.LRA -> {
                    val amplitude = (intensityFloat * 255f).roundToInt().coerceIn(1, 255)
                    vibrator.vibrate(VibrationEffect.createOneShot(45L, amplitude))
                }
                HapticMotorType.ERM -> {
                    val durationMs = (40L + (intensityFloat * 50f).roundToLong()).coerceIn(40L, 95L)
                    vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                HapticMotorType.NONE -> return
            }
        } catch (_: Throwable) {
            try {
                view.performHapticFeedback(
                    HapticFeedbackConstants.LONG_PRESS,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Throwable) {
            }
        }
    }
}
