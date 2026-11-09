package com.limonatalp4.glasscalcproject.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow

/**
 * Isolated GlassDisplay collecting from StateFlows directly.
 * This prevents the entire parent Screen and all 25 keyboard buttons
 * from recomposing on every single keystroke, eliminating typing lags.
 */
@Composable
fun GlassDisplay(
    expressionFlow: StateFlow<String>,
    resultFlow: StateFlow<String>,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessageFlow: StateFlow<String?>,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val expression by expressionFlow.collectAsState()
    val result by resultFlow.collectAsState()
    val errorMessage by errorMessageFlow.collectAsState()

    GlassDisplayContent(
        expression = expression,
        result = result,
        angleMode = angleMode,
        isScientificExpanded = isScientificExpanded,
        errorMessage = errorMessage,
        isCompact = isCompact,
        modifier = modifier,
        glassState = glassState
    )
}

@Composable
fun GlassDisplay(
    expression: String,
    result: String,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessage: String? = null,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    GlassDisplayContent(
        expression = expression,
        result = result,
        angleMode = angleMode,
        isScientificExpanded = isScientificExpanded,
        errorMessage = errorMessage,
        isCompact = isCompact,
        modifier = modifier,
        glassState = glassState
    )
}

@Composable
private fun GlassDisplayContent(
    expression: String,
    result: String,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    errorMessage: String? = null,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val context = LocalContext.current
    val glassProps = LocalGlassProperties.current
    val exprScrollState = rememberScrollState()

    var isCopyFlashed by remember { mutableStateOf(false) }
    val flashAlpha by animateFloatAsState(
        targetValue = if (isCopyFlashed) 0.35f else 0.0f,
        animationSpec = tween(durationMillis = 180),
        label = "display_copy_flash"
    )

    fun performCopy() {
        val textToCopy = if (result.isNotEmpty() && result != "0") result else if (expression.isNotEmpty()) expression else result
        if (textToCopy.isNotEmpty()) {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                if (clipboard != null) {
                    val clip = ClipData.newPlainText("GlassCalc", textToCopy)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "In Zwischenablage kopiert: $textToCopy", Toast.LENGTH_SHORT).show()
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

    // Auto-scroll to end on new typing
    LaunchedEffect(expression) {
        exprScrollState.animateScrollTo(exprScrollState.maxValue)
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
            .pointerInput(expression, result) {
                detectTapGestures(
                    onTap = { performCopy() },
                    onLongPress = { performCopy() }
                )
            }
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

                    // Copy indicator icon
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Ergebnis in Zwischenablage kopieren",
                        tint = copyIconColor,
                        modifier = Modifier
                            .size(16.dp)
                            .testTag("icon_copy_clipboard")
                    )
                }
            }

            // Running expression input (top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isCompact) 4.dp else 8.dp)
                    .horizontalScroll(exprScrollState),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    fontSize = if (isCompact) 19.sp else 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = exprColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.testTag("display_expression_text")
                )
            }

            // Evaluated result (bottom, large & prominent)
            Text(
                text = result,
                fontSize = if (isCompact) 32.sp else 42.sp,
                fontWeight = FontWeight.Bold,
                color = resultColor,
                textAlign = TextAlign.End,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (isCompact) 2.dp else 4.dp)
                    .testTag("display_result_text")
            )
        }
    }
}
