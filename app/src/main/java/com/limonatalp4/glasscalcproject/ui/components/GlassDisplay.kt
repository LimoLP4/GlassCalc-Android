package com.limonatalp4.glasscalcproject.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.InterceptPlatformTextInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first

private val ALLOWED_EXPRESSION_CHARS: Set<Char> = buildSet {
    addAll('0'..'9')
    // Erlaubte Operatoren, Klammern, Dezimaltrennzeichen & Parser-Symbole
    addAll(listOf('+', '-', '*', '/', '(', ')', '.', ',', '^', '%', '!', ';', '|', ' '))
    // Bestehende Taschenrechner-Operatoren & Konstanten
    addAll(listOf('−', '×', '÷', '⁄', '√', '²', '³', 'ⁿ', '∫', 'π'))
    // Buchstaben für mathematische Funktionen (sin, cos, tan, log, ln, sqrt, ...) & Variablen
    addAll('a'..'z')
    addAll('A'..'Z')
}

internal fun isAllowedCalculatorChar(ch: Char): Boolean = ch in ALLOWED_EXPRESSION_CHARS

internal fun filterCalculatorExpression(
    text: String,
    cursorPosition: Int
): Pair<String, Int> {
    val clampedCursor = cursorPosition.coerceIn(0, text.length)
    val sb = StringBuilder(text.length)
    var newCursor = 0
    text.forEachIndexed { index, ch ->
        if (isAllowedCalculatorChar(ch)) {
            sb.append(ch)
            if (index < clampedCursor) {
                newCursor++
            }
        }
    }
    return sb.toString() to newCursor
}

/**
 * Isolated GlassDisplay collecting from StateFlows directly.
 * Displays cursor navigation for precise editing with robust crash protection.
 */
@Composable
fun GlassDisplay(
    expressionFlow: StateFlow<String>,
    resultFlow: StateFlow<String>,
    cursorPositionFlow: StateFlow<Int>,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessageFlow: StateFlow<String?>,
    onCursorPositionChanged: (Int) -> Unit = {},
    onExpressionChange: (String, Int) -> Unit = { _, newCursor -> onCursorPositionChanged(newCursor) },
    onEvaluate: () -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val expression by expressionFlow.collectAsState()
    val result by resultFlow.collectAsState()
    val cursorPosition by cursorPositionFlow.collectAsState()
    val errorMessage by errorMessageFlow.collectAsState()

    GlassDisplayContent(
        expression = expression,
        result = result,
        cursorPosition = cursorPosition,
        angleMode = angleMode,
        isScientificExpanded = isScientificExpanded,
        errorMessage = errorMessage,
        onCursorPositionChanged = onCursorPositionChanged,
        onExpressionChange = onExpressionChange,
        onEvaluate = onEvaluate,
        isCompact = isCompact,
        modifier = modifier,
        glassState = glassState
    )
}

@Composable
fun GlassDisplay(
    expression: String,
    result: String,
    cursorPosition: Int = expression.length,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessage: String? = null,
    onCursorPositionChanged: (Int) -> Unit = {},
    onExpressionChange: (String, Int) -> Unit = { _, newCursor -> onCursorPositionChanged(newCursor) },
    onEvaluate: () -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    GlassDisplayContent(
        expression = expression,
        result = result,
        cursorPosition = cursorPosition,
        angleMode = angleMode,
        isScientificExpanded = isScientificExpanded,
        errorMessage = errorMessage,
        onCursorPositionChanged = onCursorPositionChanged,
        onExpressionChange = onExpressionChange,
        onEvaluate = onEvaluate,
        isCompact = isCompact,
        modifier = modifier,
        glassState = glassState
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun GlassDisplayContent(
    expression: String,
    result: String,
    cursorPosition: Int,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessage: String? = null,
    onCursorPositionChanged: (Int) -> Unit = {},
    onExpressionChange: (String, Int) -> Unit = { _, newCursor -> onCursorPositionChanged(newCursor) },
    onEvaluate: () -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val context = LocalContext.current
    val view = LocalView.current
    val glassProps = LocalGlassProperties.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var isKeyboardActive by remember { mutableStateOf(false) }
    val currentKeyboardActive by rememberUpdatedState(isKeyboardActive)
    var isTextFieldFocused by remember { mutableStateOf(false) }
    var keyboardRequestToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        withFrameNanos { }
        runCatching { focusRequester.requestFocus() }
    }

    LaunchedEffect(keyboardRequestToken) {
        if (keyboardRequestToken > 0) {
            isKeyboardActive = true
            withFrameNanos { }
            runCatching { focusRequester.requestFocus() }
            keyboardController?.show()
        }
    }

    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var wasImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            wasImeVisible = true
        } else if (wasImeVisible) {
            wasImeVisible = false
            isKeyboardActive = false
            focusManager.clearFocus()
        }
    }

    var isCopyFlashed by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (isCopyFlashed) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 180),
        label = "display_copy_flash"
    )

    // Weiche, sanfte Cursor-Blink-Animation passend zu den Suchleisten (500ms Ein-/Ausblenden)
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    val copiedToastTemplate = stringResource(R.string.toast_copied, "__GLASSCALC_VAL__")

    fun performCopy() {
        val textToCopy = if (result.isNotEmpty() && result != "0") result else if (expression.isNotEmpty()) expression else result
        if (textToCopy.isNotEmpty()) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                if (clipboard != null) {
                    val clip = ClipData.newPlainText("GlassCalc", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, copiedToastTemplate.replace("__GLASSCALC_VAL__", textToCopy), Toast.LENGTH_SHORT).show()
                    isCopyFlashed = true
                }
            } catch (e: Exception) {
                // Ignore silent failures for clipboard in release
            }
        }
    }

    LaunchedEffect(isCopyFlashed) {
        if (isCopyFlashed) {
            delay(280)
            isCopyFlashed = false
        }
    }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val displayShape = RoundedCornerShape(if (isCompact) 20.dp else 28.dp)

    val colorScheme = MaterialTheme.colorScheme
    val flashColor = colorScheme.primary
    val badgeAngleColor = colorScheme.primary
    val badgeSciColor = colorScheme.tertiary
    val errorTextColor = colorScheme.error
    val copyIconColor = colorScheme.primary.copy(alpha = 0.75f)

    val exprColor by animateColorAsState(
        targetValue = if (expression.isEmpty()) {
            colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        } else {
            colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "display_expr_color"
    )

    val resultColor = colorScheme.onSurface

    val displayBorder = BorderStroke(
        width = 1.dp,
        color = if (glassProps.isDark) {
            glassProps.buttonBorderColor
        } else {
            colorScheme.onSurface.copy(alpha = 0.20f)
        }
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_display"),
        glassState = glassState,
        shape = displayShape,
        borderStroke = displayBorder,
        customBgColor = if (glassProps.isAmoled) Color.White.copy(alpha = 0.08f) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(flashColor.copy(alpha = flashAlpha), displayShape)
                .padding(
                    horizontal = if (isCompact) 14.dp else 20.dp,
                    vertical = if (isCompact) 10.dp else 16.dp
                ),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Badges (DEG/RAD, SCI, Copy Indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = angleMode.name,
                        transitionSpec = {
                            (fadeIn(tween(150, easing = FastOutSlowInEasing)) +
                                slideInVertically(tween(150, easing = FastOutSlowInEasing)) { it / 3 })
                                .togetherWith(
                                    fadeOut(tween(100, easing = FastOutLinearInEasing)) +
                                        slideOutVertically(tween(100, easing = FastOutLinearInEasing)) { -it / 3 }
                                )
                                .using(SizeTransform(clip = false))
                        },
                        label = "angle_mode_badge"
                    ) { modeName ->
                        Text(
                            text = modeName,
                            fontSize = if (isCompact) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeAngleColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("badge_angle_mode")
                        )
                    }

                    AnimatedVisibility(
                        visible = isScientificExpanded,
                        enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.92f, animationSpec = tween(160)),
                        exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.92f, animationSpec = tween(110))
                    ) {
                        Text(
                            text = "SCI",
                            fontSize = if (isCompact) 10.sp else 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeSciColor,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.92f, animationSpec = tween(150)),
                        exit = fadeOut(tween(110)) + scaleOut(targetScale = 0.92f, animationSpec = tween(110))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = errorTextColor,
                            modifier = Modifier.testTag("display_error_message")
                        )
                    }

                    // Keyboard button in corner (Pill Glass Design - opens system keyboard only)
                    val keyboardBtnInteraction = remember { MutableInteractionSource() }
                    GlassSurface(
                        shape = RoundedCornerShape(50),
                        customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        borderStroke = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f)),
                        glassState = glassState,
                        modifier = Modifier.pressScale(keyboardBtnInteraction)
                    ) {
                        IconButton(
                            onClick = {
                                HapticUtil.performAppHaptic(
                                    context = context,
                                    view = view,
                                    enabled = hapticFeedbackEnabled,
                                    intensityLevel = hapticIntensity
                                )
                                keyboardRequestToken++
                            },
                            interactionSource = keyboardBtnInteraction,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .testTag("btn_open_keyboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = stringResource(R.string.btn_open_keyboard),
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Copy indicator button in corner (Pill Glass Design)
                    val copyBtnInteraction = remember { MutableInteractionSource() }
                    GlassSurface(
                        shape = RoundedCornerShape(50),
                        customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        borderStroke = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f)),
                        glassState = glassState,
                        modifier = Modifier.pressScale(copyBtnInteraction)
                    ) {
                        IconButton(
                            onClick = {
                                HapticUtil.performAppHaptic(
                                    context = context,
                                    view = view,
                                    enabled = hapticFeedbackEnabled,
                                    intensityLevel = hapticIntensity
                                )
                                performCopy()
                            },
                            interactionSource = copyBtnInteraction,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(4.dp)
                                .testTag("btn_copy_clipboard")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.btn_copy_clipboard),
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Einheitliches Hauptrechenfeld (exakt identische BasicTextField- & Cursor-Logik wie in den Suchleisten)
            val safeSelection = cursorPosition.coerceIn(0, expression.length)
            val exprFontSize = if (isCompact) 19.sp else 24.sp
            val accentCursorColor = MaterialTheme.colorScheme.primary

            InterceptPlatformTextInput(
                interceptor = { request, nextHandler ->
                    if (!currentKeyboardActive) {
                        snapshotFlow { currentKeyboardActive }.first { it }
                    }
                    nextHandler.startInputMethod(request)
                }
            ) {
                BasicTextField(
                    value = TextFieldValue(
                        text = expression,
                        selection = TextRange(safeSelection)
                    ),
                    onValueChange = { newValue ->
                        val (filteredText, filteredCursor) = filterCalculatorExpression(
                            text = newValue.text,
                            cursorPosition = newValue.selection.start
                        )
                        if (filteredText != expression) {
                            onExpressionChange(filteredText, filteredCursor)
                        } else if (filteredCursor != cursorPosition) {
                            onCursorPositionChanged(filteredCursor)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            onEvaluate()
                        }
                    ),
                    textStyle = TextStyle(
                        fontSize = exprFontSize,
                        fontWeight = FontWeight.Normal,
                        color = exprColor,
                        textAlign = TextAlign.End,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    cursorBrush = SolidColor(accentCursorColor),
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (isCompact) 4.dp else 8.dp, end = 8.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { focusState ->
                            isTextFieldFocused = focusState.isFocused
                        }
                        .testTag("display_expression_text"),
                    decorationBox = { innerTextField ->
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = expression.isEmpty(),
                                enter = fadeIn(tween(120, easing = FastOutSlowInEasing)),
                                exit = fadeOut(tween(90, easing = FastOutLinearInEasing))
                            ) {
                                Text(
                                    text = "0",
                                    fontSize = exprFontSize,
                                    fontWeight = FontWeight.Normal,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                    textAlign = TextAlign.End,
                                    style = TextStyle(
                                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                                    )
                                )
                            }
                            innerTextField()
                            if (!isTextFieldFocused) {
                                val layout = textLayoutResult
                                val distanceFromRight = if (expression.isEmpty() || layout == null) {
                                    0.dp
                                } else {
                                    val safePos = cursorPosition.coerceIn(0, layout.layoutInput.text.length)
                                    val rect = runCatching { layout.getCursorRect(safePos) }.getOrNull()
                                    if (rect != null) {
                                        with(density) {
                                            (layout.size.width - rect.left).coerceAtLeast(0f).toDp()
                                        }.coerceIn(0.dp, (maxWidth - 2.dp).coerceAtLeast(0.dp))
                                    } else {
                                        0.dp
                                    }
                                }
                                val fallbackCursorHeight = layout?.let {
                                    val safePos = cursorPosition.coerceIn(0, it.layoutInput.text.length)
                                    runCatching { with(density) { it.getCursorRect(safePos).height.toDp() } }.getOrNull()
                                }?.takeIf { it > 0.dp } ?: if (isCompact) 22.dp else 28.dp

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = -distanceFromRight)
                                        .width(2.dp)
                                        .height(fallbackCursorHeight)
                                        .background(
                                            color = accentCursorColor.copy(alpha = cursorAlpha)
                                        )
                                )
                            }
                        }
                    }
                )
            }

            // Evaluated result (bottom, large & prominent with smooth crossfade & slide)
            AnimatedContent(
                targetState = result,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)) { height -> height / 4 } +
                        scaleIn(initialScale = 0.97f, animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)))
                        .togetherWith(
                            fadeOut(animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)) +
                                slideOutVertically(animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing)) { height -> -height / 4 }
                        )
                        .using(SizeTransform(clip = false))
                },
                label = "result_text_anim",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isCompact) 2.dp else 4.dp)
            ) { targetResult ->
                Text(
                    text = targetResult,
                    fontSize = if (isCompact) 32.sp else 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = resultColor,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("display_result_text")
                )
            }
        }
    }
}
