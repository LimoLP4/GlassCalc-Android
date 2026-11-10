package com.limonatalp4.glasscalcproject.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.foundation.indication
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    ACTION_CLEAR,
    EQUALS
}

@Composable
fun GlassCalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.NUMBER,
    glassState: LiquidGlassState? = null,
    testTag: String = "btn_$text",
    contentDesc: String = text,
    hapticFeedbackEnabled: Boolean = LocalHapticFeedbackEnabled.current,
    isRepeatable: Boolean = false,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val glassProps = LocalGlassProperties.current
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    var isLongPressTriggered by remember { mutableStateOf(false) }

    fun triggerHapticTap() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
    }

    fun triggerHapticLongPress() {
        if (hapticFeedbackEnabled) {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            vibrator?.let { v ->
                if (v.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else {
                        @Suppress("DEPRECATION")
                        v.vibrate(60L)
                    }
                }
            }
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed || isLongPressTriggered) 0.90f else 1.0f,
        label = "button_scale"
    )

    val colorScheme = MaterialTheme.colorScheme

    val flashColor by animateColorAsState(
        targetValue = if (isLongPressTriggered) colorScheme.error.copy(alpha = 0.85f) else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "flash_color"
    )

    val (bgBrush, textColor, borderColor) = remember(
        type, glassProps.isDark, glassProps.isAmoled, glassProps.isMaterialYou, colorScheme, glassProps.buttonBackgroundAlpha
    ) {
        when (type) {
            ButtonType.EQUALS -> {
                val gradient = Brush.linearGradient(
                    listOf(
                        colorScheme.primary,
                        colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
                Triple(gradient, colorScheme.onPrimary, colorScheme.primary)
            }
            ButtonType.OPERATOR -> {
                val brush = Brush.verticalGradient(
                    listOf(
                        colorScheme.primaryContainer.copy(alpha = 0.80f),
                        colorScheme.primaryContainer.copy(alpha = 0.60f)
                    )
                )
                Triple(brush, colorScheme.onPrimaryContainer, colorScheme.primary.copy(alpha = 0.50f))
            }
            ButtonType.ACTION_CLEAR -> {
                val brush = Brush.verticalGradient(
                    listOf(
                        colorScheme.errorContainer.copy(alpha = 0.80f),
                        colorScheme.errorContainer.copy(alpha = 0.60f)
                    )
                )
                Triple(brush, colorScheme.onErrorContainer, colorScheme.error.copy(alpha = 0.50f))
            }
            ButtonType.FUNCTION -> {
                val brush = Brush.verticalGradient(
                    listOf(
                        colorScheme.tertiaryContainer.copy(alpha = 0.80f),
                        colorScheme.tertiaryContainer.copy(alpha = 0.60f)
                    )
                )
                Triple(brush, colorScheme.onTertiaryContainer, colorScheme.tertiary.copy(alpha = 0.50f))
            }
            ButtonType.NUMBER -> {
                val alpha = glassProps.buttonBackgroundAlpha
                val brush = Brush.verticalGradient(
                    listOf(
                        colorScheme.surfaceVariant.copy(alpha = alpha + 0.10f),
                        colorScheme.surfaceVariant.copy(alpha = alpha)
                    )
                )
                Triple(brush, colorScheme.onSurface, colorScheme.outline.copy(alpha = 0.25f))
            }
        }
    }

    val clickModifier = when {
        isRepeatable -> {
            Modifier
                .indication(interactionSource, ripple(bounded = true, radius = 48.dp, color = textColor))
                .pointerInput(onClick, hapticFeedbackEnabled) {
                    detectTapGestures(
                        onPress = { offset ->
                            val press = PressInteraction.Press(offset)
                            interactionSource.emit(press)
                            
                            // Execute first action immediately on press
                            onClick()
                            triggerHapticTap()
                            
                            val repeatJob = coroutineScope.launch {
                                delay(450) // Wait for long press threshold
                                while (true) {
                                    onClick()
                                    triggerHapticTap()
                                    delay(90) // Repeat interval (50-100ms)
                                }
                            }
                            
                            val released = try {
                                awaitRelease()
                                true
                            } catch (c: Exception) {
                                false
                            }
                            
                            repeatJob.cancel()
                            interactionSource.emit(if (released) PressInteraction.Release(press) else PressInteraction.Cancel(press))
                        }
                    )
                }
        }
        onLongClick != null -> {
            Modifier.pointerInput(onLongClick, onClick, hapticFeedbackEnabled) {
                detectTapGestures(
                    onTap = {
                        triggerHapticTap()
                        onClick()
                    },
                    onLongPress = {
                        isLongPressTriggered = true
                        triggerHapticLongPress()
                        coroutineScope.launch {
                            delay(250)
                            isLongPressTriggered = false
                        }
                        onLongClick()
                    }
                )
            }
        }
        else -> {
            Modifier.clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 48.dp, color = textColor),
                onClick = {
                    triggerHapticTap()
                    onClick()
                }
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(CircleShape)
            .safeLiquidGlass(
                state = if (type != ButtonType.EQUALS) glassState else null,
                shape = CircleShape,
                supportsBlur = glassProps.supportsBlur
            )
            .background(bgBrush, CircleShape)
            .then(
                if (isLongPressTriggered) {
                    Modifier.background(flashColor, CircleShape)
                } else {
                    Modifier
                }
            )
            .border(1.2.dp, if (isLongPressTriggered) colorScheme.error else borderColor, CircleShape)
            .then(clickModifier)
            .semantics { contentDescription = contentDesc }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (glassProps.isAmoled) 0.16f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        val baseFontSize = (maxHeight.value * 0.36f).coerceIn(14f, 32f)
        val calculatedFontSize = when {
            text.length > 4 -> (baseFontSize * 0.65f).coerceAtLeast(10f).sp
            text.length > 3 -> (baseFontSize * 0.78f).coerceAtLeast(11f).sp
            text.length > 2 -> (baseFontSize * 0.88f).coerceAtLeast(12f).sp
            else -> baseFontSize.sp
        }

        Text(
            text = text,
            fontSize = calculatedFontSize,
            fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium,
            color = if (isLongPressTriggered) Color.White else textColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}
