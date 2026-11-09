package com.limonatalp4.glasscalcproject.ui.util

import android.content.Context
import android.os.*
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.compositionLocalOf

val LocalHapticFeedbackEnabled = compositionLocalOf { true }

object HapticUtil {
    fun performAppHaptic(context: Context, view: View, enabled: Boolean) {
        if (!enabled) return

        // SCHRITT A (Samsung / Standard View-Haptic)
        // Nutzt die Systemeinstellungen für haptisches Feedback des Nutzers
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
        )

        // Fallback-Kette für Vibrator-Direktansteuerung (AOSP, Xiaomi etc.)
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        vibrator?.let { v ->
            if (v.hasVibrator()) {
                try {
                    when {
                        // SCHRITT C (Xiaomi HyperOS / API 30+)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                            val composition = VibrationEffect.startComposition()
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                                .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
                                .compose()
                            v.vibrate(composition)
                        }
                        // SCHRITT B (AOSP Android 10+)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        }
                        // Android 8+ (API 26+) Oreo
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                            v.vibrate(VibrationEffect.createOneShot(20L, VibrationEffect.DEFAULT_AMPLITUDE))
                        }
                        // Legacy Fallback
                        else -> {
                            @Suppress("DEPRECATION")
                            v.vibrate(20L)
                        }
                    }
                } catch (e: Exception) {
                    // Falls eine Vibration fehlschlägt (z.B. fehlende Permission, was bei internen Apps passieren kann)
                }
            }
        }
    }
}
