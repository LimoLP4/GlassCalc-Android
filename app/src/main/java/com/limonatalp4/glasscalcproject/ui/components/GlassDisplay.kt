package com.limonatalp4.glasscalcproject.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

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
        isCompact = isCompact,
        modifier = modifier,
        glassState = glassState
    )
}

@Composable
private fun GlassDisplayContent(
    expression: String,
    result: String,
    cursorPosition: Int,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessage: String? = null,
    onCursorPositionChanged: (Int) -> Unit = {},
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val context = LocalContext.current
    val glassProps = LocalGlassProperties.current
    val exprScrollState = rememberScrollState()
    val density = LocalDensity.current

    var isCopyFlashed by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (isCopyFlashed) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 180),
        label = "display_copy_flash"
    )

    // Soft, smooth cursor breathing animation (calmer 650ms half-cycle)
    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursor_alpha"
    )

    fun performCopy() {
        val textToCopy = if (result.isNotEmpty() && result != "0") result else if (expression.isNotEmpty()) expression else result
        if (textToCopy.isNotEmpty()) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                if (clipboard != null) {
                    val clip = ClipData.newPlainText("GlassCalc", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.toast_copied, textToCopy), Toast.LENGTH_SHORT).show()
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

    // Auto-scroll to keep cursor visible safely
    LaunchedEffect(expression, cursorPosition, textLayoutResult) {
        textLayoutResult?.let { layout ->
            runCatching {
                val maxLen = layout.layoutInput.text.length
                val safePos = cursorPosition.coerceIn(0, maxLen)
                val cursorX = layout.getCursorRect(safePos).left.toInt()
                val targetScroll = (cursorX - 100).coerceAtLeast(0)
                exprScrollState.animateScrollTo(targetScroll)
            }
        }
    }

    val displayShape = RoundedCornerShape(if (isCompact) 20.dp else 28.dp)

    val colorScheme = MaterialTheme.colorScheme
    val flashColor = colorScheme.primary
    val badgeAngleColor = colorScheme.primary
    val badgeSciColor = colorScheme.tertiary
    val errorTextColor = colorScheme.error
    val copyIconColor = colorScheme.primary.copy(alpha = 0.75f)

    val exprColor = if (expression.isEmpty()) {
        colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    } else {
        colorScheme.onSurfaceVariant
    }

    val resultColor = colorScheme.onSurface

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calculator_display"),
        glassState = glassState,
        shape = displayShape,
        borderWidth = 1.2.dp,
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
                    Text(
                        text = angleMode.name,
                        fontSize = if (isCompact) 10.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeAngleColor,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("badge_angle_mode")
                    )

                    if (isScientificExpanded) {
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
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            fontSize = if (isCompact) 11.sp else 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = errorTextColor,
                            modifier = Modifier.testTag("display_error_message")
                        )
                    }

                    // Copy indicator button in corner (Pill Glass Design)
                    GlassSurface(
                        shape = RoundedCornerShape(50),
                        customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        customBorderColor = colorScheme.outline.copy(alpha = 0.3f),
                        borderWidth = 1.dp,
                        glassState = glassState
                    ) {
                        IconButton(
                            onClick = { performCopy() },
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

            // Running expression input with Cursor (top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isCompact) 4.dp else 8.dp)
                    .horizontalScroll(exprScrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier.pointerInput(expression) {
                        detectTapGestures { tapOffset ->
                            val layout = textLayoutResult
                            if (layout != null && expression.isNotEmpty()) {
                                runCatching {
                                    val tappedPos = layout.getOffsetForPosition(tapOffset)
                                    onCursorPositionChanged(tappedPos.coerceIn(0, expression.length))
                                }.onFailure {
                                    onCursorPositionChanged(expression.length)
                                }
                            } else {
                                onCursorPositionChanged(0)
                            }
                        }
                    }
                ) {
                    val textToShow = if (expression.isEmpty()) "0" else expression

                    Text(
                        text = textToShow,
                        fontSize = if (isCompact) 19.sp else 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = exprColor,
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("display_expression_text")
                    )

                    // Blinking cursor bar with layout-bounds validation and Z-index overlay
                    textLayoutResult?.let { layout ->
                        val layoutTextLen = layout.layoutInput.text.length
                        val safePos = cursorPosition.coerceIn(0, layoutTextLen)

                        val cursorRect = runCatching {
                            layout.getCursorRect(safePos)
                        }.getOrNull()

                        if (cursorRect != null) {
                            val cursorX = with(density) { cursorRect.left.toDp() }
                            val cursorY = with(density) { cursorRect.top.toDp() }
                            val cursorH = with(density) { cursorRect.height.toDp() }

                            Box(
                                modifier = Modifier
                                    .zIndex(2f)
                                    .offset(x = cursorX, y = cursorY)
                                    .width(2.5.dp)
                                    .height(if (cursorH > 0.dp) cursorH else if (isCompact) 20.dp else 26.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha),
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // Evaluated result (bottom, large & prominent with smooth animation)
            AnimatedContent(
                targetState = result,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(160)) + slideInVertically(animationSpec = tween(160)) { height -> height / 3 })
                        .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { height -> -height / 3 })
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
