package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
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
    visualPadding: Dp = 0.dp,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val glassProps = LocalGlassProperties.current
    val view = LocalView.current
    var isLongPressTriggered by remember { mutableStateOf(false) }

    val localHapticEnabled = LocalHapticFeedbackEnabled.current
    val localHapticIntensity = LocalHapticIntensity.current
    val isHapticActive = hapticFeedbackEnabled && localHapticEnabled

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed || isLongPressTriggered) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "button_scale"
    )

    val colorScheme = MaterialTheme.colorScheme

    val flashColor by animateColorAsState(
        targetValue = if (isLongPressTriggered) colorScheme.error.copy(alpha = 0.85f) else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "flash_color"
    )

    val (bgBrush, textColor, borderColor) = remember(
        type, glassProps.isDark, glassProps.isTiefschwarz, glassProps.isMaterialYou, colorScheme, glassProps.buttonBackgroundAlpha, glassProps.buttonBorderColor
    ) {
        when (type) {
            ButtonType.EQUALS, ButtonType.ACTION_CLEAR -> {
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
                Triple(brush, colorScheme.onSurface, glassProps.buttonBorderColor)
            }
        }
    }

    val currentOnClick by rememberUpdatedState(onClick)
    val currentOnLongClick by rememberUpdatedState(onLongClick)
    val currentIsRepeatable by rememberUpdatedState(isRepeatable)
    val currentHapticActive by rememberUpdatedState(isHapticActive)
    val currentHapticIntensity by rememberUpdatedState(localHapticIntensity)

    val touchModifier = Modifier
        .semantics {
            contentDescription = contentDesc
            role = Role.Button
            semanticsOnClick(label = contentDesc) {
                currentOnClick()
                if (currentHapticActive) {
                    HapticUtil.performAppHaptic(context, view, true, currentHapticIntensity)
                }
                true
            }
        }
        .pointerInput(Unit) {
            coroutineScope {
                awaitPointerEventScope {
                    while (isActive) {
                        var downChange: PointerInputChange? = null
                        while (downChange == null && isActive) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            downChange = event.changes.firstOrNull { change ->
                                !change.isConsumed && change.changedToDownIgnoreConsumed()
                            }
                        }
                        val activeDown = downChange ?: break
                        val trackedId = activeDown.id
                        val downPos = activeDown.position
                        val press = PressInteraction.Press(downPos)
                        interactionSource.tryEmit(press)

                        if (currentIsRepeatable) {
                            // Sofortige Ausführung vor asynchronem Haptik-Dispatch
                            currentOnClick()
                            if (currentHapticActive) {
                                HapticUtil.performAppHaptic(context, view, true, currentHapticIntensity)
                            }
                        }

                        var longPressTriggeredLocal = false
                        var repeatJob: Job? = null
                        var longPressJob: Job? = null

                        if (currentIsRepeatable) {
                            repeatJob = launch {
                                delay(450L)
                                while (isActive) {
                                    currentOnClick()
                                    if (currentHapticActive) {
                                        HapticUtil.performAppHaptic(context, view, true, currentHapticIntensity)
                                    }
                                    delay(90L)
                                }
                            }
                        } else if (currentOnLongClick != null) {
                            val longPressTimeout = viewConfiguration.longPressTimeoutMillis
                            longPressJob = launch {
                                delay(longPressTimeout)
                                longPressTriggeredLocal = true
                                isLongPressTriggered = true
                                if (currentHapticActive) {
                                    HapticUtil.performAppLongPressHaptic(context, view, true, currentHapticIntensity)
                                }
                                launch {
                                    delay(250L)
                                    isLongPressTriggered = false
                                }
                                currentOnLongClick?.invoke()
                            }
                        }

                        var isReleased = false
                        var isCancelled = false
                        val touchSlop = viewConfiguration.touchSlop
                        val width = size.width.toFloat()
                        val height = size.height.toFloat()

                        try {
                            while (!isReleased && !isCancelled && isActive) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val change = event.changes.firstOrNull { it.id == trackedId }
                                if (change == null || change.isConsumed) {
                                    isCancelled = true
                                    break
                                }
                                if (change.changedToUpIgnoreConsumed()) {
                                    change.consume()
                                    isReleased = true
                                    break
                                }
                                if (!change.pressed) {
                                    isCancelled = true
                                    break
                                }
                                val delta = change.position - downPos
                                val outOfBounds = change.position.x < -touchSlop ||
                                    change.position.x > width + touchSlop ||
                                    change.position.y < -touchSlop ||
                                    change.position.y > height + touchSlop
                                if (outOfBounds || delta.getDistance() > touchSlop * 2.5f) {
                                    isCancelled = true
                                    break
                                }
                            }
                        } catch (ce: CancellationException) {
                            repeatJob?.cancel()
                            longPressJob?.cancel()
                            interactionSource.tryEmit(PressInteraction.Cancel(press))
                            throw ce
                        }

                        repeatJob?.cancel()
                        longPressJob?.cancel()

                        if (isReleased) {
                            interactionSource.tryEmit(PressInteraction.Release(press))
                            if (!currentIsRepeatable && !longPressTriggeredLocal) {
                                // State sofort aktualisieren, Haptik non-blocking im Hintergrund
                                currentOnClick()
                                if (currentHapticActive) {
                                    HapticUtil.performAppHaptic(context, view, true, currentHapticIntensity)
                                }
                            }
                        } else {
                            interactionSource.tryEmit(PressInteraction.Cancel(press))
                        }
                    }
                }
            }
        }

    // Äußerer rechteckiger Touch-Container: umfasst die gesamte Tasten-Zelle inklusive Ecken und Rand
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .then(touchModifier)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        // Sichtbarer runder Liquid-Glass-Button mit geclipptem Ripple
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(visualPadding)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .safeLiquidGlass(
                    state = if (type != ButtonType.EQUALS && type != ButtonType.ACTION_CLEAR) glassState else null,
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
                .border(1.dp, if (isLongPressTriggered) colorScheme.error else borderColor, CircleShape)
                .indication(interactionSource, ripple(bounded = true, color = textColor)),
            contentAlignment = Alignment.Center
        ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (glassProps.isTiefschwarz) 0.14f else 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        val hasCjk = remember(text) {
            text.any { ch ->
                when (Character.UnicodeScript.of(ch.code)) {
                    Character.UnicodeScript.HAN,
                    Character.UnicodeScript.HIRAGANA,
                    Character.UnicodeScript.KATAKANA,
                    Character.UnicodeScript.HANGUL -> true
                    else -> false
                }
            }
        }
        val baseFontSize = (maxHeight.value * 0.36f).coerceIn(14f, 32f)
        val cjkScale = if (hasCjk && text.length >= 2) 0.78f else 1f
        val calculatedFontSize = when {
            text.length > 4 -> (baseFontSize * 0.65f * cjkScale).coerceAtLeast(9.5f).sp
            text.length > 3 -> (baseFontSize * 0.78f * cjkScale).coerceAtLeast(10f).sp
            text.length > 2 -> (baseFontSize * 0.88f * cjkScale).coerceAtLeast(11f).sp
            hasCjk && text.length == 2 -> (baseFontSize * 0.82f).coerceAtLeast(11f).sp
            else -> baseFontSize.sp
        }
        val allowWrap = hasCjk && text.length > 2

        Text(
            text = text,
            fontSize = calculatedFontSize,
            lineHeight = (calculatedFontSize.value * 1.15f).sp,
            fontWeight = if (type == ButtonType.EQUALS || type == ButtonType.OPERATOR || type == ButtonType.ACTION_CLEAR) FontWeight.Bold else FontWeight.Medium,
            color = if (isLongPressTriggered) Color.White else textColor,
            textAlign = TextAlign.Center,
            maxLines = if (allowWrap) 2 else 1,
            softWrap = allowWrap,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        }
    }
}
