package com.limonatalp4.glasscalcproject.ui.util

import android.content.Context
import android.os.*
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.compositionLocalOf

val LocalHapticFeedbackEnabled = compositionLocalOf { true }

enum class HapticMotorType {
    NONE,
    ERM,
    LRA
}

object HapticUtil {

    fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Prüft die Hardware des Geräts:
     * - hasVibrator(): Gibt an, ob überhaupt ein Vibrationsmotor vorhanden ist.
     * - hasAmplitudeControl(): Bei true handelt es sich um einen hochwertigen LRA-Motor (Linear Resonant Actuator).
     *   Bei false handelt es sich um einen einfachen ERM-Motor (Eccentric Rotating Mass).
     */
    fun getHapticMotorType(context: Context): HapticMotorType {
        val vibrator = getVibrator(context) ?: return HapticMotorType.NONE
        if (!vibrator.hasVibrator()) {
            return HapticMotorType.NONE
        }
        return if (vibrator.hasAmplitudeControl()) {
            HapticMotorType.LRA
        } else {
            HapticMotorType.ERM
        }
    }

    fun hasVibrator(context: Context): Boolean {
        return getHapticMotorType(context) != HapticMotorType.NONE
    }

    fun performAppHaptic(context: Context, view: View, enabled: Boolean) {
        if (!enabled) return

        val vibrator = getVibrator(context)
        if (vibrator == null || !vibrator.hasVibrator()) {
            return
        }

        // Standard View Haptic Feedback als Ergänzung
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )

        try {
            val isLra = vibrator.hasAmplitudeControl()

            if (isLra) {
                // Für LRA-Motoren (hasAmplitudeControl() == true):
                // Feine, moderne Haptik-Effekte (z.B. EFFECT_CLICK oder Composition)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(20L)
                }
            } else {
                // Für ERM-Motoren (hasAmplitudeControl() == false):
                // Robuster, einfacher Impuls (z.B. 30ms DEFAULT_AMPLITUDE), damit es auf günstigen Geräten
                // und AOSP-ROMs nicht zu Aussetzern kommt.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(30L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30L)
                }
            }
        } catch (e: Exception) {
            // Ignoriere Hardware- oder Berechtigungsfehler sicher
        }
    }

    fun performAppLongPressHaptic(context: Context, view: View, enabled: Boolean) {
        if (!enabled) return

        val vibrator = getVibrator(context)
        if (vibrator == null || !vibrator.hasVibrator()) {
            return
        }

        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )

        try {
            val isLra = vibrator.hasAmplitudeControl()
            if (isLra) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50L)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(60L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(60L)
                }
            }
        } catch (e: Exception) {
            // Ignoriere Fehler
        }
    }
}

