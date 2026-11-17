package com.limonatalp4.glasscalcproject.ui.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.limonatalp4.glasscalcproject.data.model.AppTheme

@Immutable
data class GlassThemeProperties(
    val backgroundBrush: Brush,
    val buttonBackgroundAlpha: Float,
    val buttonBorderColor: Color,
    val cardBackground: Color,
    val isDark: Boolean,
    val isTiefschwarz: Boolean,
    val isMaterialYou: Boolean,
    val supportsBlur: Boolean,
    val supportsPredictiveBack: Boolean,
    val effectiveApiLevel: Int
)

val LocalGlassProperties = staticCompositionLocalOf {
    GlassThemeProperties(
        backgroundBrush = Brush.verticalGradient(listOf(DarkBackground, Color(0xFF131B2E))),
        buttonBackgroundAlpha = 0.10f,
        buttonBorderColor = Color.White.copy(alpha = 0.20f),
        cardBackground = Color.White.copy(alpha = 0.07f),
        isDark = true,
        isTiefschwarz = true,
        isMaterialYou = false,
        supportsBlur = true,
        supportsPredictiveBack = true,
        effectiveApiLevel = Build.VERSION.SDK_INT
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = DarkSecondaryBlue,
    tertiary = DarkTertiaryBlue,
    error = DarkErrorRed,
    background = DarkBackground,
    surface = Color(0xFF14171E),
    onBackground = Color(0xFFF0F4F8),
    onSurface = Color(0xFFF0F4F8),
    surfaceVariant = Color(0xFF202838),
    onSurfaceVariant = Color(0xFFC2C7D0),
    outline = Color(0xFF8C9199)
)

private val TiefschwarzColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = DarkSecondaryBlue,
    tertiary = DarkTertiaryBlue,
    error = DarkErrorRed,
    background = TiefschwarzBackground,
    surface = TiefschwarzBackground,
    surfaceContainerLowest = TiefschwarzBackground,
    surfaceContainerLow = TiefschwarzBackground,
    surfaceContainer = TiefschwarzBackground,
    surfaceContainerHigh = TiefschwarzBackground,
    surfaceContainerHighest = Color(0xFF0E0E0E),
    onBackground = Color(0xFFF0F4F8),
    onSurface = Color(0xFFF0F4F8),
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = Color(0xFFC2C7D0),
    outline = Color(0xFF8C9199)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = SecondaryBlue,
    tertiary = TertiaryBlue,
    error = ErrorRed,
    background = Color(0xFFF3F5FA),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE1E7F0),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF73777F)
)

/**
 * Resolves the effective [AppTheme] while respecting Android API fallbacks and device Power Saver mode.
 *
 * 1. [AppTheme.AUTOMATIC]:
 *    - Wechselt AUSSCHLIESSLICH hier bei aktivem Energiesparmodus zu [AppTheme.DARK_MODE] (Tiefschwarz / Deep Black),
 *      um maximal Akku zu sparen.
 *    - Bei deaktiviertem Energiesparmodus erfolgt der Rückfall auf das normale System-Design
 *      ([AppTheme.STANDARD_DARK] bei System-Dark-Theme, sonst [AppTheme.WHITE_MODE]).
 *
 * 2. [AppTheme.MATERIAL_YOU]:
 *    - Behält strikt die dynamischen Material-You-Farben (Monet) bei. Der Energiesparmodus hat hier
 *      KEINEN Einfluss und darf nicht auf Tiefschwarz wechseln.
 *    - Auf Geräten < API 31 fällt das Theme ohne Energiespar-Umschaltung auf Standard-Dark bzw. Hell zurück.
 *
 * 3. Manuell gewählte Themes ([AppTheme.WHITE_MODE], [AppTheme.STANDARD_DARK], [AppTheme.DARK_MODE]):
 *    - Bleiben stets exakt unverändert, unabhängig vom Energiesparmodus.
 */
internal fun resolveEffectiveTheme(
    selectedTheme: AppTheme,
    systemDark: Boolean,
    isPowerSaveMode: Boolean,
    effectiveApiLevel: Int
): AppTheme {
    return when (selectedTheme) {
        AppTheme.AUTOMATIC -> {
            when {
                isPowerSaveMode -> AppTheme.DARK_MODE
                systemDark -> AppTheme.STANDARD_DARK
                else -> AppTheme.WHITE_MODE
            }
        }
        AppTheme.MATERIAL_YOU -> {
            if (effectiveApiLevel >= 31) {
                AppTheme.MATERIAL_YOU
            } else {
                if (systemDark) AppTheme.STANDARD_DARK else AppTheme.WHITE_MODE
            }
        }
        AppTheme.WHITE_MODE -> AppTheme.WHITE_MODE
        AppTheme.STANDARD_DARK -> AppTheme.STANDARD_DARK
        AppTheme.DARK_MODE -> AppTheme.DARK_MODE
    }
}

@Composable
fun GlassCalcTheme(
    selectedTheme: AppTheme = AppTheme.AUTOMATIC,
    simulatedApiLevel: Int = Build.VERSION.SDK_INT,
    powerSaveModeOverride: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val systemDark = isSystemInDarkTheme()
    val effectiveApi = simulatedApiLevel

    val powerManager = remember(context) {
        runCatching {
            context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        }.getOrNull()
    }

    var systemPowerSaveMode by remember(powerManager) {
        mutableStateOf(
            runCatching { powerManager?.isPowerSaveMode == true }.getOrDefault(false)
        )
    }

    DisposableEffect(context, lifecycleOwner, powerManager, powerSaveModeOverride) {
        if (powerSaveModeOverride != null || powerManager == null) {
            onDispose { }
        } else {
            val refreshPowerSaveState = {
                systemPowerSaveMode = runCatching { powerManager.isPowerSaveMode }.getOrDefault(false)
            }
            refreshPowerSaveState()

            val powerSaveReceiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                        refreshPowerSaveState()
                    }
                }
            }

            val receiverRegistered = runCatching {
                ContextCompat.registerReceiver(
                    context,
                    powerSaveReceiver,
                    IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            }.isSuccess

            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    refreshPowerSaveState()
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                if (receiverRegistered) {
                    runCatching { context.unregisterReceiver(powerSaveReceiver) }
                }
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        }
    }

    val isPowerSaveMode = powerSaveModeOverride ?: systemPowerSaveMode

    val resolvedTheme = resolveEffectiveTheme(
        selectedTheme = selectedTheme,
        systemDark = systemDark,
        isPowerSaveMode = isPowerSaveMode,
        effectiveApiLevel = effectiveApi
    )

    val isTiefschwarz = resolvedTheme == AppTheme.DARK_MODE
    val isMaterialYou = resolvedTheme == AppTheme.MATERIAL_YOU
    val isDark = when (resolvedTheme) {
        AppTheme.DARK_MODE, AppTheme.STANDARD_DARK -> true
        AppTheme.MATERIAL_YOU -> systemDark
        else -> false
    }

    // Native blur supported only on Android 12+ (API 31+)
    val supportsBlur = effectiveApi >= 31

    // Predictive back animations supported on Android 13+ (API 33+)
    val supportsPredictiveBack = effectiveApi >= 33

    val colorScheme = when {
        isMaterialYou -> {
            if (Build.VERSION.SDK_INT >= 31) {
                runCatching {
                    if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }.getOrElse {
                    if (systemDark) DarkColorScheme else LightColorScheme
                }
            } else {
                // This branch shouldn't be reached due to fallback but kept for safety
                if (systemDark) DarkColorScheme else LightColorScheme
            }
        }
        isTiefschwarz -> TiefschwarzColorScheme
        resolvedTheme == AppTheme.STANDARD_DARK -> DarkColorScheme.copy(
            background = Color(0xFF1A1C1E),
            surface = Color(0xFF1C1B1F),
            surfaceVariant = Color(0xFF44474E)
        )
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    // Adaptive background gradient according to theme
    val backgroundBrush = when {
        isMaterialYou -> {
            if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        colorScheme.surface,
                        colorScheme.surfaceVariant.copy(alpha = 0.60f),
                        colorScheme.primaryContainer.copy(alpha = 0.35f),
                        colorScheme.surface
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        colorScheme.surface,
                        colorScheme.primaryContainer.copy(alpha = 0.25f),
                        colorScheme.secondaryContainer.copy(alpha = 0.20f),
                        colorScheme.surface
                    )
                )
            }
        }
        isTiefschwarz -> {
            // Tiefschwarz (Deep Black): Pure #000000 OLED black
            Brush.verticalGradient(
                listOf(
                    Color(0xFF000000),
                    Color(0xFF030303),
                    Color(0xFF000000)
                )
            )
        }
        resolvedTheme == AppTheme.STANDARD_DARK -> {
            // Standard Dark: Material 3 typical charcoal gray gradient
            Brush.verticalGradient(
                listOf(
                    Color(0xFF1A1C1E),
                    Color(0xFF1E2124),
                    Color(0xFF1A1C1E)
                )
            )
        }
        isDark -> {
            // Dark Mode fallback: Deep #0A0A0A with atmospheric midnight indigo & cyan glows
            Brush.verticalGradient(
                listOf(
                    Color(0xFF0A0A0A),
                    Color(0xFF0D1424),
                    Color(0xFF080D1A),
                    Color(0xFF0A0A0A)
                )
            )
        }
        else -> {
            // White Mode: Crisp frosted daylight gradient
            Brush.verticalGradient(
                listOf(
                    Color(0xFFF6F8FC),
                    Color(0xFFEBF1FA),
                    Color(0xFFE2EBF8),
                    Color(0xFFF0F4FA)
                )
            )
        }
    }

    val buttonAlpha = when {
        isMaterialYou -> if (isDark) 0.35f else 0.65f
        isTiefschwarz -> 0.13f
        resolvedTheme == AppTheme.STANDARD_DARK -> 0.12f
        isDark -> 0.08f
        else -> 0.65f
    }

    val buttonBorderColor = when {
        isMaterialYou -> colorScheme.outline.copy(alpha = if (isDark) 0.30f else 0.20f)
        isTiefschwarz -> Color.White.copy(alpha = 0.22f)
        isDark -> Color.White.copy(alpha = 0.18f)
        else -> Color.Black.copy(alpha = 0.08f)
    }

    val cardBackground = when {
        isMaterialYou -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.60f)
        isTiefschwarz -> Color.Black
        isDark -> Color.White.copy(alpha = 0.06f)
        else -> Color.White.copy(alpha = 0.75f)
    }

    val glassProps = remember(
        backgroundBrush,
        buttonAlpha,
        buttonBorderColor,
        cardBackground,
        isDark,
        isTiefschwarz,
        isMaterialYou,
        supportsBlur,
        supportsPredictiveBack,
        effectiveApi
    ) {
        GlassThemeProperties(
            backgroundBrush = backgroundBrush,
            buttonBackgroundAlpha = buttonAlpha,
            buttonBorderColor = buttonBorderColor,
            cardBackground = cardBackground,
            isDark = isDark,
            isTiefschwarz = isTiefschwarz,
            isMaterialYou = isMaterialYou,
            supportsBlur = supportsBlur,
            supportsPredictiveBack = supportsPredictiveBack,
            effectiveApiLevel = effectiveApi
        )
    }

    CompositionLocalProvider(LocalGlassProperties provides glassProps) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
