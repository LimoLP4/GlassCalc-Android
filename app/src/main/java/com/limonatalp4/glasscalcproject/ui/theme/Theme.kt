package com.limonatalp4.glasscalcproject.ui.theme

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.limonatalp4.glasscalcproject.data.model.AppTheme

internal const val UNIFIED_GLASS_BORDER_ALPHA = 0.25f

@Immutable
data class GlassThemeProperties(
    val backgroundBrush: Brush,
    val buttonBackgroundAlpha: Float,
    val buttonBorderColor: Color,
    val containerBorderStroke: BorderStroke,
    val cardBackground: Color,
    val isDark: Boolean,
    val isTiefschwarz: Boolean,
    val isMaterialYou: Boolean,
    val supportsBlur: Boolean,
    val supportsPredictiveBack: Boolean,
    val effectiveApiLevel: Int
)

val LocalGlassProperties = staticCompositionLocalOf {
    val defaultBorderColor = Color.White.copy(alpha = UNIFIED_GLASS_BORDER_ALPHA)
    GlassThemeProperties(
        backgroundBrush = Brush.verticalGradient(listOf(DarkBackground, Color(0xFF131B2E))),
        buttonBackgroundAlpha = 0.10f,
        buttonBorderColor = defaultBorderColor,
        containerBorderStroke = BorderStroke(1.dp, defaultBorderColor),
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
    onTertiary = Color(0xFF3B1D42),
    tertiaryContainer = Color(0xFF53335A),
    onTertiaryContainer = Color(0xFFFAD8FF),
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
    onTertiary = Color(0xFF3B1D42),
    tertiaryContainer = Color(0xFF53335A),
    onTertiaryContainer = Color(0xFFFAD8FF),
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
    onPrimary = Color(0xFFE8F2FF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = SecondaryBlue,
    tertiary = TertiaryBlue,
    onTertiary = Color(0xFFFDF2FF),
    tertiaryContainer = Color(0xFFF8D8FF),
    onTertiaryContainer = Color(0xFF2B0E32),
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

internal fun resolveAccentSwatchColor(
    colorArgb: Int?,
    isDark: Boolean
): Color {
    if (colorArgb == null) {
        return if (isDark) DarkPrimaryBlue else PrimaryBlue
    }
    return derivePrimaryColor(colorArgb = colorArgb, isDark = isDark)
}

private fun derivePrimaryColor(
    colorArgb: Int,
    isDark: Boolean
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(colorArgb, hsl)
    val hue = hsl[0]
    val baseSat = hsl[1]

    if (isDark) {
        val sat = baseSat.coerceIn(0.60f, 0.90f)
        val argb = ColorUtils.HSLToColor(floatArrayOf(hue, sat, 0.72f))
        return Color(argb)
    }

    val sat = baseSat.coerceIn(0.58f, 0.92f)
    var lightness = 0.38f
    var argb = ColorUtils.HSLToColor(floatArrayOf(hue, sat, lightness))
    val whiteArgb = android.graphics.Color.WHITE
    while (lightness > 0.22f && ColorUtils.calculateContrast(whiteArgb, argb) < 4.5) {
        lightness -= 0.03f
        argb = ColorUtils.HSLToColor(floatArrayOf(hue, sat, lightness))
    }
    return Color(argb)
}

internal fun applyCustomAccentColor(
    baseScheme: ColorScheme,
    customAccentArgb: Int?,
    isDark: Boolean
): ColorScheme {
    if (customAccentArgb == null) return baseScheme

    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(customAccentArgb, hsl)
    val hue = hsl[0]
    val baseSat = hsl[1]

    val primaryColor = derivePrimaryColor(colorArgb = customAccentArgb, isDark = isDark)
    val primaryArgb = primaryColor.toArgb()

    if (isDark) {
        var onPrimaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, (baseSat * 0.85f).coerceIn(0.35f, 0.90f), 0.14f)
        )
        if (ColorUtils.calculateContrast(onPrimaryArgb, primaryArgb) < 4.5) {
            val darkFallback = 0xFF0A0A0A.toInt()
            val lightFallback = android.graphics.Color.WHITE
            onPrimaryArgb = if (
                ColorUtils.calculateContrast(darkFallback, primaryArgb) >=
                ColorUtils.calculateContrast(lightFallback, primaryArgb)
            ) {
                darkFallback
            } else {
                lightFallback
            }
        }

        val containerArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, (baseSat * 0.75f).coerceIn(0.35f, 0.80f), 0.24f)
        )
        val onContainerSat = (baseSat * 0.92f).coerceIn(0.55f, 0.96f)
        var onContainerLightness = 0.78f
        var onContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, onContainerSat, onContainerLightness)
        )
        while (onContainerLightness < 0.95f && ColorUtils.calculateContrast(onContainerArgb, containerArgb) < 4.5) {
            onContainerLightness += 0.02f
            onContainerArgb = ColorUtils.HSLToColor(
                floatArrayOf(hue, onContainerSat, onContainerLightness.coerceAtMost(0.95f))
            )
        }
        if (ColorUtils.calculateContrast(onContainerArgb, containerArgb) < 4.5) {
            onContainerArgb = android.graphics.Color.WHITE
        }
        val inversePrimaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, baseSat.coerceIn(0.55f, 0.88f), 0.40f)
        )

        val tertiaryHue = (hue + 32f) % 360f
        val tertiaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, (baseSat * 0.72f).coerceIn(0.45f, 0.85f), 0.74f)
        )
        val tertiaryContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, (baseSat * 0.65f).coerceIn(0.35f, 0.75f), 0.26f)
        )
        var onTertiaryContainerLightness = 0.82f
        val onTertiaryContainerSat = (baseSat * 0.82f).coerceIn(0.48f, 0.90f)
        var onTertiaryContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, onTertiaryContainerSat, onTertiaryContainerLightness)
        )
        while (onTertiaryContainerLightness < 0.95f && ColorUtils.calculateContrast(onTertiaryContainerArgb, tertiaryContainerArgb) < 4.5) {
            onTertiaryContainerLightness += 0.02f
            onTertiaryContainerArgb = ColorUtils.HSLToColor(
                floatArrayOf(tertiaryHue, onTertiaryContainerSat, onTertiaryContainerLightness.coerceAtMost(0.95f))
            )
        }
        if (ColorUtils.calculateContrast(onTertiaryContainerArgb, tertiaryContainerArgb) < 4.5) {
            onTertiaryContainerArgb = android.graphics.Color.WHITE
        }

        return baseScheme.copy(
            primary = primaryColor,
            onPrimary = Color(onPrimaryArgb),
            primaryContainer = Color(containerArgb),
            onPrimaryContainer = Color(onContainerArgb),
            tertiary = Color(tertiaryArgb),
            tertiaryContainer = Color(tertiaryContainerArgb),
            onTertiaryContainer = Color(onTertiaryContainerArgb),
            inversePrimary = Color(inversePrimaryArgb)
        )
    } else {
        val whiteArgb = android.graphics.Color.WHITE
        val darkFallback = 0xFF0F172A.toInt()
        val onPrimarySat = (baseSat * 0.65f).coerceIn(0.35f, 0.80f)
        var onPrimaryLightness = 0.91f
        var tintedLightOnPrimaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, onPrimarySat, onPrimaryLightness)
        )
        while (onPrimaryLightness < 0.97f && ColorUtils.calculateContrast(tintedLightOnPrimaryArgb, primaryArgb) < 4.5) {
            onPrimaryLightness += 0.02f
            tintedLightOnPrimaryArgb = ColorUtils.HSLToColor(
                floatArrayOf(hue, onPrimarySat, onPrimaryLightness.coerceAtMost(0.97f))
            )
        }
        val onPrimaryColor = when {
            ColorUtils.calculateContrast(tintedLightOnPrimaryArgb, primaryArgb) >= 4.5 -> Color(tintedLightOnPrimaryArgb)
            ColorUtils.calculateContrast(whiteArgb, primaryArgb) >= 4.5 -> Color.White
            else -> Color(darkFallback)
        }

        val containerArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, (baseSat * 0.65f).coerceIn(0.30f, 0.85f), 0.92f)
        )
        var onContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, (baseSat * 0.85f).coerceIn(0.40f, 0.90f), 0.14f)
        )
        if (ColorUtils.calculateContrast(onContainerArgb, containerArgb) < 4.5) {
            onContainerArgb = darkFallback
        }
        val inversePrimaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(hue, baseSat.coerceIn(0.55f, 0.90f), 0.74f)
        )

        val tertiaryHue = (hue + 32f) % 360f
        val tertiaryArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, (baseSat * 0.70f).coerceIn(0.42f, 0.85f), 0.42f)
        )
        val tertiaryContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, (baseSat * 0.60f).coerceIn(0.30f, 0.80f), 0.92f)
        )
        var onTertiaryContainerArgb = ColorUtils.HSLToColor(
            floatArrayOf(tertiaryHue, (baseSat * 0.82f).coerceIn(0.40f, 0.88f), 0.15f)
        )
        if (ColorUtils.calculateContrast(onTertiaryContainerArgb, tertiaryContainerArgb) < 4.5) {
            onTertiaryContainerArgb = darkFallback
        }

        return baseScheme.copy(
            primary = primaryColor,
            onPrimary = onPrimaryColor,
            primaryContainer = Color(containerArgb),
            onPrimaryContainer = Color(onContainerArgb),
            tertiary = Color(tertiaryArgb),
            tertiaryContainer = Color(tertiaryContainerArgb),
            onTertiaryContainer = Color(onTertiaryContainerArgb),
            inversePrimary = Color(inversePrimaryArgb)
        )
    }
}

@Composable
fun GlassCalcTheme(
    selectedTheme: AppTheme = AppTheme.AUTOMATIC,
    simulatedApiLevel: Int = Build.VERSION.SDK_INT,
    powerSaveModeOverride: Boolean? = null,
    customAccentColorArgb: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val systemDark = isSystemInDarkTheme()
    val effectiveApi = simulatedApiLevel

    val appContext = remember(context) { context.applicationContext ?: context }
    val powerManager = remember(appContext) {
        runCatching {
            appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
        }.getOrNull()
    }

    var systemPowerSaveMode by remember(powerManager) {
        mutableStateOf(
            runCatching { powerManager?.isPowerSaveMode == true }.getOrDefault(false)
        )
    }

    DisposableEffect(appContext, lifecycleOwner, powerManager, powerSaveModeOverride, selectedTheme) {
        if (selectedTheme != AppTheme.AUTOMATIC || powerSaveModeOverride != null || powerManager == null) {
            onDispose { }
        } else {
            val refreshPowerSaveState = {
                val latest = runCatching { powerManager.isPowerSaveMode }.getOrDefault(false)
                if (systemPowerSaveMode != latest) {
                    systemPowerSaveMode = latest
                }
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
                    appContext,
                    powerSaveReceiver,
                    IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
                    ContextCompat.RECEIVER_EXPORTED
                )
            }.isSuccess

            val lowPowerUri = runCatching { Settings.Global.getUriFor("low_power") }.getOrNull()
            val lowPowerObserver = if (lowPowerUri != null) {
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        refreshPowerSaveState()
                    }
                }
            } else {
                null
            }
            val observerRegistered = if (lowPowerUri != null && lowPowerObserver != null) {
                runCatching {
                    appContext.contentResolver.registerContentObserver(lowPowerUri, false, lowPowerObserver)
                }.isSuccess
            } else {
                false
            }

            val lifecycleObserver = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START || event == Lifecycle.Event.ON_RESUME) {
                    refreshPowerSaveState()
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

            onDispose {
                if (receiverRegistered) {
                    runCatching { appContext.unregisterReceiver(powerSaveReceiver) }
                }
                if (observerRegistered && lowPowerObserver != null) {
                    runCatching { appContext.contentResolver.unregisterContentObserver(lowPowerObserver) }
                }
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            }
        }
    }

    val isPowerSaveMode = if (selectedTheme == AppTheme.AUTOMATIC) {
        powerSaveModeOverride ?: systemPowerSaveMode
    } else {
        false
    }

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
        isTiefschwarz -> applyCustomAccentColor(
            baseScheme = TiefschwarzColorScheme,
            customAccentArgb = customAccentColorArgb,
            isDark = true
        )
        resolvedTheme == AppTheme.STANDARD_DARK -> applyCustomAccentColor(
            baseScheme = DarkColorScheme.copy(
                background = Color(0xFF1A1C1E),
                surface = Color(0xFF1C1B1F),
                surfaceVariant = Color(0xFF44474E)
            ),
            customAccentArgb = customAccentColorArgb,
            isDark = true
        )
        isDark -> applyCustomAccentColor(
            baseScheme = DarkColorScheme,
            customAccentArgb = customAccentColorArgb,
            isDark = true
        )
        else -> applyCustomAccentColor(
            baseScheme = LightColorScheme,
            customAccentArgb = customAccentColorArgb,
            isDark = false
        )
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

    val buttonBorderColor = colorScheme.outline.copy(alpha = UNIFIED_GLASS_BORDER_ALPHA)
    val containerBorderStroke = remember(buttonBorderColor) {
        BorderStroke(width = 1.dp, color = buttonBorderColor)
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
        containerBorderStroke,
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
            containerBorderStroke = containerBorderStroke,
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
