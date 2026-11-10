package com.limonatalp4.glasscalcproject.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.limonatalp4.glasscalcproject.data.model.AppTheme

data class GlassThemeProperties(
    val backgroundBrush: Brush,
    val buttonBackgroundAlpha: Float,
    val buttonBorderColor: Color,
    val cardBackground: Color,
    val isDark: Boolean,
    val isTiefschwarz: Boolean,
    val isAmoled: Boolean,
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
        isAmoled = false,
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

private val AmoledColorScheme = darkColorScheme(
    primary = AmoledPrimaryBlue,
    onPrimary = Color(0xFF003554),
    primaryContainer = Color(0xFF004D73),
    onPrimaryContainer = Color(0xFFE0F3FF),
    secondary = AmoledSecondaryBlue,
    tertiary = DarkTertiaryBlue,
    error = DarkErrorRed,
    background = AmoledBackground,
    surface = Color(0xFF0D0D0D),
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF181F2B),
    onSurfaceVariant = Color(0xFFBCC2CD),
    outline = Color(0xFF8A909A)
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

@Composable
fun GlassCalcTheme(
    selectedTheme: AppTheme = AppTheme.AUTOMATIC,
    simulatedApiLevel: Int = Build.VERSION.SDK_INT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val effectiveApi = simulatedApiLevel

    // 1. Handle Fallback for Material You if not supported
    val themeAfterFallback = when (selectedTheme) {
        AppTheme.MATERIAL_YOU -> if (effectiveApi >= 31) AppTheme.MATERIAL_YOU else AppTheme.AUTOMATIC
        else -> selectedTheme
    }

    // 2. Fully resolve AUTOMATIC to a concrete theme
    val resolvedTheme = if (themeAfterFallback == AppTheme.AUTOMATIC) {
        if (systemDark) AppTheme.DARK_MODE else AppTheme.WHITE_MODE
    } else {
        themeAfterFallback
    }

    val isAmoled = resolvedTheme == AppTheme.AMOLED_DARK
    val isTiefschwarz = resolvedTheme == AppTheme.DARK_MODE
    val isMaterialYou = resolvedTheme == AppTheme.MATERIAL_YOU
    val isDark = when (resolvedTheme) {
        AppTheme.DARK_MODE, AppTheme.AMOLED_DARK, AppTheme.STANDARD_DARK -> true
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
                if (systemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                // This branch shouldn't be reached due to fallback but kept for safety
                if (systemDark) DarkColorScheme else LightColorScheme
            }
        }
        isAmoled -> AmoledColorScheme
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
        isAmoled -> {
            // AMOLED: Pure black with very subtle deep dark blue gradient at the top/bottom
            Brush.verticalGradient(
                listOf(
                    Color(0xFF000000),
                    Color(0xFF020408),
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
            // Dark Mode: Deep #0A0A0A with atmospheric midnight indigo & cyan glows
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

    // Button alphas: For AMOLED, 10-15% white (0.13f) as requested so they remain visible
    val buttonAlpha = when {
        isMaterialYou -> if (isDark) 0.35f else 0.65f
        isAmoled -> 0.14f
        resolvedTheme == AppTheme.STANDARD_DARK -> 0.12f
        isDark -> 0.08f
        else -> 0.65f
    }

    val buttonBorderColor = when {
        isMaterialYou -> colorScheme.outline.copy(alpha = if (isDark) 0.30f else 0.20f)
        isAmoled -> Color.White.copy(alpha = 0.28f)
        isDark -> Color.White.copy(alpha = 0.18f)
        else -> Color.Black.copy(alpha = 0.08f)
    }

    val cardBackground = when {
        isMaterialYou -> colorScheme.surfaceVariant.copy(alpha = if (isDark) 0.35f else 0.60f)
        isAmoled -> Color.White.copy(alpha = 0.09f)
        isDark -> Color.White.copy(alpha = 0.06f)
        else -> Color.White.copy(alpha = 0.75f)
    }

    val glassProps = GlassThemeProperties(
        backgroundBrush = backgroundBrush,
        buttonBackgroundAlpha = buttonAlpha,
        buttonBorderColor = buttonBorderColor,
        cardBackground = cardBackground,
        isDark = isDark,
        isTiefschwarz = isTiefschwarz,
        isAmoled = isAmoled,
        isMaterialYou = isMaterialYou,
        supportsBlur = supportsBlur,
        supportsPredictiveBack = supportsPredictiveBack,
        effectiveApiLevel = effectiveApi
    )

    CompositionLocalProvider(LocalGlassProperties provides glassProps) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
