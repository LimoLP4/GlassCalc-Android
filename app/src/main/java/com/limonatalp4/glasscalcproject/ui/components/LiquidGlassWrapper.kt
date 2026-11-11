package com.limonatalp4.glasscalcproject.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import io.github.nadeemiqbal.liquidglass.LiquidGlassQuality
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import io.github.nadeemiqbal.liquidglass.liquidGlass
import io.github.nadeemiqbal.liquidglass.rememberLiquidGlassState

/**
 * Checks whether the host device supports hardware RenderEffect blur safely without
 * causing native SIGSEGV crashes in RenderThread (especially on emulators and Android 17 / Samsung S25).
 */
fun isBlurHardwareSupported(): Boolean {
    // 1. Must be at least Android 12 (API 31)
    if (Build.VERSION.SDK_INT < 31) return false

    // 2. Detect Android emulators / virtualized GPU environments.
    // In emulators (emu64a, goldfish, ranchu), Skia RenderThread segfaults (SEGV_ACCERR)
    // when using GraphicsLayer + AGSL RuntimeShader + RenderEffect.
    val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.startsWith("unknown") ||
            Build.MODEL.contains("google_sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for") ||
            Build.MODEL.contains("sdk_gphone") ||
            Build.HARDWARE.contains("goldfish") ||
            Build.HARDWARE.contains("ranchu") ||
            Build.PRODUCT.contains("sdk_gphone")

    if (isEmulator) {
        return false
    }

    // 3. Android 17 / API 36+ specific guard (as requested for Samsung S25 / Android 17)
    if (Build.VERSION.SDK_INT >= 36) {
        return false
    }

    return try {
        RenderEffect.createBlurEffect(10f, 10f, Shader.TileMode.CLAMP)
        true
    } catch (t: Throwable) {
        false
    }
}

/**
 * Safely initializes LiquidGlassState with hardware tier checking and graceful fallback.
 */
@Composable
fun rememberAdaptiveGlassState(forceFallback: Boolean = false): LiquidGlassState {
    val isHardwareSupported = remember { isBlurHardwareSupported() }
    val quality = if (forceFallback || !isHardwareSupported) {
        LiquidGlassQuality.Fallback
    } else {
        LiquidGlassQuality.Full
    }
    return rememberLiquidGlassState(quality = quality)
}

/**
 * Safely applies the liquidGlass modifier only if hardware blur is supported and tier is not Fallback.
 */
fun Modifier.safeLiquidGlass(
    state: LiquidGlassState?,
    shape: Shape,
    supportsBlur: Boolean
): Modifier {
    if (!supportsBlur || state == null || state.quality == LiquidGlassQuality.Fallback) {
        return this
    }
    return try {
        this.liquidGlass(state = state, shape = shape)
    } catch (t: Throwable) {
        this
    }
}

/**
 * A reusable frosted glass card container with automatic fallback,
 * light reflection highlight, and soft borders.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    borderWidth: Dp = 1.dp,
    customBorderColor: Color? = null,
    customBgColor: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassProps = LocalGlassProperties.current
    val effectiveBorderColor = customBorderColor ?: glassProps.buttonBorderColor
    val effectiveBgColor = customBgColor ?: glassProps.cardBackground

    val state = glassState ?: rememberAdaptiveGlassState(forceFallback = !glassProps.supportsBlur)
    val isBlurActive = glassProps.supportsBlur && state.quality != LiquidGlassQuality.Fallback

    Box(
        modifier = modifier
            .clip(shape)
            .safeLiquidGlass(
                state = state,
                shape = shape,
                supportsBlur = isBlurActive
            )
            .background(effectiveBgColor, shape)
            .border(borderWidth, effectiveBorderColor, shape)
    ) {
        // Specular light reflection on the top-left edge
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (glassProps.isDark) 0.08f else 0.25f),
                            Color.Transparent,
                            Color.White.copy(alpha = if (glassProps.isDark) 0.02f else 0.08f)
                        )
                    )
                )
        )
        content()
    }
}
