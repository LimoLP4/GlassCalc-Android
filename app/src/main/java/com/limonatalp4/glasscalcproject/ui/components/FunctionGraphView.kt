package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.limonatalp4.glasscalcproject.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.evaluator.MxParserLock
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import org.mariuszgromada.math.mxparser.mXparser
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

enum class GraphPreset(val label: String, val formula: String) {
    SIN("sin(x)", "sin(x)"),
    COS("cos(x)", "cos(x)"),
    TAN("tan(x)", "tan(x)"),
    ARCSIN("arcsin", "asin(x)"),
    ARCCOS("arccos", "acos(x)"),
    ARCTAN("arctan", "atan(x)"),
    SINH("sinh(x)", "sinh(x)"),
    COSH("cosh(x)", "cosh(x)"),
    TANH("tanh(x)", "tanh(x)"),
    ABS("|x|", "abs(x)"),
    LN("ln(x)", "ln(x)"),
    LOG("log(x)", "log10(x)"),
    SQUARE("x²", "x^2"),
    SQRT("√x", "sqrt(x)"),
    DERIV("d/dx", "der(sin(t), t, x)"),
    INTEGRAL("∫", "int(sin(t), t, 0, x)"),
    CUSTOM("f(x)", "custom")
}

@Immutable
data class GraphPoint(val x: Double, val y: Double)

private fun calculateNiceStep(range: Double, targetSteps: Int): Double {
    if (!range.isFinite() || range <= 1e-12) return 1.0
    val rawStep = range / targetSteps.coerceAtLeast(1)
    if (!rawStep.isFinite() || rawStep <= 1e-12) return 1.0
    val exponent = floor(log10(rawStep))
    val magnitude = 10.0.pow(exponent)
    val normalized = rawStep / magnitude
    val niceFactor = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    val step = niceFactor * magnitude
    return if (step.isFinite() && step > 1e-12) step else 1.0
}

private fun formatAxisLabel(value: Double): String {
    val rounded = (value * 1000.0).roundToLong() / 1000.0
    if (abs(rounded - rounded.toLong()) < 1e-4) {
        return rounded.toLong().toString()
    }
    return if (abs(rounded) < 10.0) {
        String.format(Locale.US, "%.1f", rounded)
    } else {
        String.format(Locale.US, "%.0f", rounded)
    }
}

private fun sanitizeForGraphParser(expr: String): String {
    return expr
        .replace("π", "pi")
        .replace("e", "e")
        .replace("−", "-")
        .replace("×", "*")
        .replace("÷", "/")
        .replace("⁄", "/")
        .replace(",", ".")
        .replace("²", "^2")
        .replace("³", "^3")
        .replace("√", "sqrt")
        .replace("³√", "cbrt")
        .replace("arcsin", "asin")
        .replace("arccos", "acos")
        .replace("arctan", "atan")
        .replace("ln", "ln")
        .replace("log", "log10")
        .replace("|", "abs")
        .replace("mod", "#")
}

private fun resolveActiveFormula(preset: GraphPreset, expression: String): String {
    return when (preset) {
        GraphPreset.CUSTOM -> {
            if (expression.isNotBlank() && expression.contains("x", ignoreCase = true)) {
                expression
            } else if (expression.isNotBlank()) {
                "($expression) * sin(x)"
            } else {
                "sin(x)"
            }
        }
        else -> preset.formula
    }
}

private fun evaluateFormulaAtX(
    formula: String,
    x: Double,
    angleMode: AngleMode
): Double? {
    if (!x.isFinite()) return null
    return try {
        val sanitizedExpr = sanitizeForGraphParser(formula)
        val y = synchronized(MxParserLock) {
            try {
                License.iConfirmNonCommercialUse("limonatalp4")
            } catch (_: Exception) {}

            if (angleMode == AngleMode.DEG) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            val argX = Argument("x = 0")
            argX.argumentValue = x
            val expr = Expression(sanitizedExpr, argX)
            expr.calculate()
        }

        if (!y.isFinite() || abs(y) > 1e8) {
            null
        } else {
            y
        }
    } catch (_: Throwable) {
        null
    }
}

@Composable
fun FunctionGraphView(
    expression: String,
    angleMode: AngleMode,
    isGestureLocked: Boolean,
    onGestureLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null,
    onApplyPointToCalculator: ((Double, Double) -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val localContext = LocalContext.current
    val view = LocalView.current
    val textMeasurer = rememberTextMeasurer()
    val hapticEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current

    fun triggerClickHaptic() {
        HapticUtil.performAppHaptic(localContext, view, hapticEnabled, hapticIntensity)
    }

    var selectedPreset by remember { mutableStateOf(GraphPreset.SIN) }
    var isGraphLocked by remember { mutableStateOf(isGestureLocked) }

    LaunchedEffect(isGestureLocked) {
        isGraphLocked = isGestureLocked
    }

    // Range bounds state
    var xMin by remember { mutableDoubleStateOf(-6.28) }
    var xMax by remember { mutableDoubleStateOf(6.28) }
    var yMin by remember { mutableDoubleStateOf(-3.0) }
    var yMax by remember { mutableDoubleStateOf(3.0) }

    // Cached curve evaluation points (normalized t from 0..1, y value)
    var cachedPoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }

    // Active inspected point state (single marker at mathematical (x, y))
    var inspectedPoint by remember { mutableStateOf<GraphPoint?>(null) }
    var lastHapticTime by remember { mutableStateOf(0L) }

    // Keep latest parameters accessible inside long-lived pointerInput blocks
    val currentPreset by rememberUpdatedState(selectedPreset)
    val currentExpression by rememberUpdatedState(expression)
    val currentAngleMode by rememberUpdatedState(angleMode)
    val currentIsGraphLocked by rememberUpdatedState(isGraphLocked)
    val currentHapticEnabled by rememberUpdatedState(hapticEnabled)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

    // Auto-Zoom & Auto-Lock: Adjust range and re-lock when switching DEG/RAD
    LaunchedEffect(angleMode) {
        if (angleMode == AngleMode.DEG) {
            xMin = -360.0
            xMax = 360.0
        } else {
            xMin = -6.28
            xMax = 6.28
        }
        yMin = -2.0
        yMax = 2.0
        inspectedPoint = null
    }

    // Auto-reset inspected point on Preset change
    LaunchedEffect(selectedPreset) {
        inspectedPoint = null
    }

    // Asynchronous background computation of curve points (Dispatchers.Default)
    LaunchedEffect(selectedPreset, expression, angleMode, xMin, xMax) {
        val computedPoints = withContext(Dispatchers.Default) {
            try {
                if (!xMin.isFinite() || !xMax.isFinite() || xMax <= xMin) {
                    return@withContext emptyList<Pair<Double, Double>>()
                }
                val formulaString = resolveActiveFormula(selectedPreset, expression)
                val sanitizedExpr = sanitizeForGraphParser(formulaString)
                val steps = 360 // High quality, low CPU footprint
                val calculatedList = ArrayList<Pair<Double, Double>>(steps + 1)

                synchronized(MxParserLock) {
                    try {
                        License.iConfirmNonCommercialUse("limonatalp4")
                    } catch (_: Exception) {}

                    if (angleMode == AngleMode.DEG) {
                        mXparser.setDegreesMode()
                    } else {
                        mXparser.setRadiansMode()
                    }

                    val argX = Argument("x = 0")
                    val expr = Expression(sanitizedExpr, argX)

                    for (i in 0..steps) {
                        val t = i.toDouble() / steps
                        val x = xMin + t * (xMax - xMin)
                        argX.argumentValue = x
                        val y = expr.calculate()
                        if (y.isFinite() && abs(y) <= 1e8) {
                            calculatedList.add(Pair(x, y))
                        } else {
                            calculatedList.add(Pair(x, Double.NaN))
                        }
                    }
                }
                calculatedList
            } catch (_: Throwable) {
                emptyList()
            }
        }
        cachedPoints = computedPoints
    }

    fun resetView() {
        if (angleMode == AngleMode.DEG) {
            xMin = -360.0
            xMax = 360.0
        } else {
            xMin = -6.28
            xMax = 6.28
        }
        yMin = -3.0
        yMax = 3.0
        inspectedPoint = null
        triggerClickHaptic()
    }

    fun tryPlacePointAtTap(tapOffset: Offset, canvasWidth: Float) {
        if (currentIsGraphLocked || canvasWidth <= 0f) return
        val xSpan = xMax - xMin
        if (xSpan <= 0.0) return

        val fraction = (tapOffset.x / canvasWidth).coerceIn(0f, 1f).toDouble()
        val tappedX = xMin + fraction * xSpan
        val formula = resolveActiveFormula(currentPreset, currentExpression)
        val evaluatedY = evaluateFormulaAtX(formula, tappedX, currentAngleMode)

        if (evaluatedY != null) {
            inspectedPoint = GraphPoint(tappedX, evaluatedY)
            HapticUtil.performAppHaptic(
                localContext,
                view,
                currentHapticEnabled,
                currentHapticIntensity
            )
        }
    }

    val graphColor = colorScheme.primary
    val gridColor = colorScheme.onSurface.copy(alpha = 0.16f)
    val axisColor = colorScheme.onSurface.copy(alpha = 0.55f)
    val labelColor = colorScheme.onSurface.copy(alpha = 0.70f)

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("function_graph_view"),
        glassState = glassState,
        shape = RoundedCornerShape(24.dp),
        borderWidth = 1.2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Preset Selector Chips + Lock Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GraphPreset.entries.forEach { preset ->
                        val isSelected = selectedPreset == preset
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset
                                triggerClickHaptic()
                            },
                            label = {
                                Text(
                                    text = preset.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = CircleShape,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colorScheme.primary,
                                selectedLabelColor = colorScheme.onPrimary,
                                labelColor = colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                selectedBorderColor = colorScheme.primary,
                                borderColor = gridColor
                            ),
                            modifier = Modifier.height(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // LOCK BUTTON (Locks graph 2D panning/zooming gesture on canvas)
                IconButton(
                    onClick = {
                        isGraphLocked = !isGraphLocked
                        onGestureLockChange(isGraphLocked)
                        triggerClickHaptic()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_lock_graph")
                ) {
                    Icon(
                        imageVector = if (isGraphLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isGraphLocked) stringResource(R.string.graph_gesture_unlock) else stringResource(R.string.graph_gesture_lock),
                        tint = if (isGraphLocked) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Canvas Plot Area with 2D Panning & Throttled Haptics
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colorScheme.surfaceContainerLow)
                    .border(1.dp, gridColor, RoundedCornerShape(16.dp))
                    .pointerInput(isGraphLocked) {
                        if (!isGraphLocked) {
                            detectTapGestures(
                                onTap = { offset ->
                                    tryPlacePointAtTap(offset, size.width.toFloat())
                                },
                                onLongPress = { offset ->
                                    tryPlacePointAtTap(offset, size.width.toFloat())
                                }
                            )
                        }
                    }
                    .pointerInput(isGraphLocked) {
                        if (!isGraphLocked) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                if (width > 0f && height > 0f && pan.x.isFinite() && pan.y.isFinite()) {
                                    val xSpan = (xMax - xMin).coerceIn(1e-4, 1e6)
                                    val ySpan = (yMax - yMin).coerceIn(1e-4, 1e6)

                                    val dx = -(pan.x / width) * xSpan
                                    val dy = (pan.y / height) * ySpan

                                    if (dx.isFinite() && dy.isFinite()) {
                                        xMin = (xMin + dx).coerceIn(-1e7, 1e7)
                                        xMax = (xMax + dx).coerceIn(-1e7, 1e7)
                                        yMin = (yMin + dy).coerceIn(-1e7, 1e7)
                                        yMax = (yMax + dy).coerceIn(-1e7, 1e7)
                                    }

                                    if (zoom.isFinite() && zoom != 1f && zoom > 0f) {
                                        val xCenter = ((xMin + xMax) / 2.0).coerceIn(-1e7, 1e7)
                                        val yCenter = ((yMin + yMax) / 2.0).coerceIn(-1e7, 1e7)
                                        val newXSpan = (xSpan / zoom).coerceIn(1e-4, 1e6)
                                        val newYSpan = (ySpan / zoom).coerceIn(1e-4, 1e6)
                                        xMin = (xCenter - newXSpan / 2.0).coerceIn(-1e7, 1e7)
                                        xMax = (xCenter + newXSpan / 2.0).coerceIn(-1e7, 1e7)
                                        yMin = (yCenter - newYSpan / 2.0).coerceIn(-1e7, 1e7)
                                        yMax = (yCenter + newYSpan / 2.0).coerceIn(-1e7, 1e7)
                                    }

                                    val now = System.currentTimeMillis()
                                    if (currentHapticEnabled && now - lastHapticTime > 80L) {
                                        lastHapticTime = now
                                        HapticUtil.performAppHaptic(
                                            localContext,
                                            view,
                                            currentHapticEnabled,
                                            currentHapticIntensity
                                        )
                                    }
                                }
                            }
                        }
                    }
                    .testTag("graph_canvas_container")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    if (width <= 0 || height <= 0) return@Canvas
                    val xSpan = xMax - xMin
                    val ySpan = yMax - yMin
                    if (!xSpan.isFinite() || xSpan <= 1e-12 || !ySpan.isFinite() || ySpan <= 1e-12) return@Canvas

                    val strokeWidth = 2.5.dp.toPx()
                    val gridStrokeWidth = 1.dp.toPx()
                    val axisStrokeWidth = 1.8.dp.toPx()

                    val zeroXpx = ((0.0 - xMin) / xSpan * width).toFloat()
                    val zeroYpx = (height - ((0.0 - yMin) / ySpan * height)).toFloat()

                    val axisTextStyle = TextStyle(
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        color = labelColor
                    )
                    val axisTitleStyle = TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = graphColor
                    )

                    // 1. DYNAMIC GRID LINES & AXIS LABELS
                    val xStep = calculateNiceStep(xSpan, targetSteps = 6)
                    val firstX = floor(xMin / xStep) * xStep
                    var currentX = firstX
                    var xGridCount = 0
                    while (currentX <= xMax + xStep * 0.1 && xGridCount++ < 80) {
                        val gx = ((currentX - xMin) / xSpan * width).toFloat()
                        if (gx.isFinite() && gx in 0f..width) {
                            drawLine(
                                color = gridColor,
                                start = Offset(gx, 0f),
                                end = Offset(gx, height),
                                strokeWidth = gridStrokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            val tickY = zeroYpx.coerceIn(12f, height - 16f)
                            drawLine(
                                color = axisColor,
                                start = Offset(gx, tickY - 3.dp.toPx()),
                                end = Offset(gx, tickY + 3.dp.toPx()),
                                strokeWidth = axisStrokeWidth
                            )

                            if (abs(currentX) > 1e-4) {
                                val labelText = formatAxisLabel(currentX)
                                val measured = textMeasurer.measure(labelText, style = axisTextStyle)
                                val textX = (gx - measured.size.width / 2f).coerceIn(4f, (width - measured.size.width - 4f).coerceAtLeast(4f))
                                val textY = if (zeroYpx in 10f..(height - 24f)) {
                                    zeroYpx + 4.dp.toPx()
                                } else {
                                    (height - measured.size.height - 4.dp.toPx()).coerceAtLeast(0f)
                                }
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    topLeft = Offset(textX, textY),
                                    style = axisTextStyle
                                )
                            }
                        }
                        val nextX = currentX + xStep
                        if (!nextX.isFinite() || nextX <= currentX) break
                        currentX = nextX
                    }

                    val yStep = calculateNiceStep(ySpan, targetSteps = 4)
                    val firstY = floor(yMin / yStep) * yStep
                    var currentY = firstY
                    var yGridCount = 0
                    while (currentY <= yMax + yStep * 0.1 && yGridCount++ < 80) {
                        val gy = (height - ((currentY - yMin) / ySpan * height)).toFloat()
                        if (gy.isFinite() && gy in 0f..height) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, gy),
                                end = Offset(width, gy),
                                strokeWidth = gridStrokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            val tickX = zeroXpx.coerceIn(16f, (width - 16f).coerceAtLeast(16f))
                            drawLine(
                                color = axisColor,
                                start = Offset(tickX - 3.dp.toPx(), gy),
                                end = Offset(tickX + 3.dp.toPx(), gy),
                                strokeWidth = axisStrokeWidth
                            )

                            if (abs(currentY) > 1e-4) {
                                val labelText = formatAxisLabel(currentY)
                                val measured = textMeasurer.measure(labelText, style = axisTextStyle)
                                val textX = if (zeroXpx in 24f..(width - 10f)) {
                                    (zeroXpx - measured.size.width - 4.dp.toPx()).coerceAtLeast(4f)
                                } else {
                                    4.dp.toPx()
                                }
                                val textY = (gy - measured.size.height / 2f).coerceIn(4f, (height - measured.size.height - 4f).coerceAtLeast(4f))
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    topLeft = Offset(textX, textY),
                                    style = axisTextStyle
                                )
                            }
                        }
                        val nextY = currentY + yStep
                        if (!nextY.isFinite() || nextY <= currentY) break
                        currentY = nextY
                    }

                    // 2. MAIN COORDINATE AXES (X = 0 & Y = 0)
                    if (zeroYpx in 0f..height) {
                        drawLine(
                            color = axisColor,
                            start = Offset(0f, zeroYpx),
                            end = Offset(width, zeroYpx),
                            strokeWidth = axisStrokeWidth
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "x",
                            topLeft = Offset(width - 14.dp.toPx(), (zeroYpx - 14.dp.toPx()).coerceAtLeast(4f)),
                            style = axisTitleStyle
                        )
                    }

                    if (zeroXpx in 0f..width) {
                        drawLine(
                            color = axisColor,
                            start = Offset(zeroXpx, 0f),
                            end = Offset(zeroXpx, height),
                            strokeWidth = axisStrokeWidth
                        )
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "y",
                            topLeft = Offset((zeroXpx + 5.dp.toPx()).coerceAtMost(width - 16.dp.toPx()), 4.dp.toPx()),
                            style = axisTitleStyle
                        )
                    }

                    if (zeroXpx in 10f..(width - 10f) && zeroYpx in 10f..(height - 10f)) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "0",
                            topLeft = Offset(zeroXpx - 10.dp.toPx(), zeroYpx + 3.dp.toPx()),
                            style = axisTextStyle
                        )
                    }

                    // 3. CURVE DRAWING (Instantly rendered from cached list with zero allocation in draw pass)
                    if (cachedPoints.isNotEmpty()) {
                        val path = Path()
                        var isFirst = true
                        var lastPy = 0f
                        val count = cachedPoints.size

                        for (i in 0 until count) {
                            val (x, y) = cachedPoints[i]
                            if (!x.isFinite() || !y.isFinite()) {
                                isFirst = true
                                continue
                            }
                            val px = ((x - xMin) / xSpan * width).toFloat()
                            val py = (height - ((y - yMin) / ySpan * height)).toFloat()
                            if (!px.isFinite() || !py.isFinite()) {
                                isFirst = true
                                continue
                            }

                            if (isFirst || abs(py - lastPy) > height * 0.75f) {
                                path.moveTo(px, py)
                                isFirst = false
                            } else {
                                path.lineTo(px, py)
                            }
                            lastPy = py
                        }

                        drawPath(
                            path = path,
                            color = graphColor,
                            style = Stroke(width = strokeWidth)
                        )
                    }

                    // 4. POINT MARKER OVERLAY (Dynamically projected from mathematical (x, y) point)
                    inspectedPoint?.let { pt ->
                        val px = ((pt.x - xMin) / xSpan * width).toFloat()
                        val py = (height - ((pt.y - yMin) / ySpan * height)).toFloat()
                        if (!px.isFinite() || !py.isFinite()) return@let
                        val markerPos = Offset(px, py)
                        val crosshairPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        if (px in 0f..width) {
                            drawLine(
                                color = graphColor.copy(alpha = 0.55f),
                                start = Offset(px, 0f),
                                end = Offset(px, height),
                                strokeWidth = 1.3.dp.toPx(),
                                pathEffect = crosshairPathEffect
                            )
                        }

                        if (py in 0f..height) {
                            drawLine(
                                color = graphColor.copy(alpha = 0.55f),
                                start = Offset(0f, py),
                                end = Offset(width, py),
                                strokeWidth = 1.3.dp.toPx(),
                                pathEffect = crosshairPathEffect
                            )
                        }

                        if (px in -20f..(width + 20f) && py in -20f..(height + 20f)) {
                            drawCircle(
                                color = graphColor.copy(alpha = 0.28f),
                                radius = 11.dp.toPx(),
                                center = markerPos
                            )
                            drawCircle(
                                color = graphColor,
                                radius = 6.dp.toPx(),
                                center = markerPos
                            )
                            drawCircle(
                                color = colorScheme.onPrimary,
                                radius = 2.6.dp.toPx(),
                                center = markerPos
                            )
                        }
                    }
                }

                // Interactive Point HUD Tooltip
                inspectedPoint?.let { pt ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
                            .border(1.2.dp, graphColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("graph_trace_hud")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column {
                                val formulaText = selectedPreset.label
                                Text(
                                    text = "$formulaText @ [${angleMode.name}]",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = graphColor
                                )
                                Text(
                                    text = String.format(Locale.US, "x: %.3f  |  y: %.4f", pt.x, pt.y),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = {
                                    inspectedPoint = null
                                    triggerClickHaptic()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.nav_close),
                                    tint = graphColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                if (inspectedPoint == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorScheme.surface.copy(alpha = 0.50f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = graphColor.copy(alpha = 0.85f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = stringResource(R.string.graph_hint_scan),
                                fontSize = 9.sp,
                                color = labelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Graph Controls Footer Bar (Pan, Zoom, Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format(Locale.US, "X: [%.1f .. %.1f]  Y: [%.1f .. %.1f]", xMin, xMax, yMin, yMax),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = labelColor
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pan Left (◀)
                    IconButton(
                        onClick = {
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin - shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax - shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.graph_pan_left),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Pan Right (▶)
                    IconButton(
                        onClick = {
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin + shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax + shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.graph_pan_right),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom In (+)
                    IconButton(
                        onClick = {
                            val xRange = ((xMax - xMin) * 0.75).coerceIn(1e-4, 1e6)
                            val xCenter = (xMin + xMax) / 2
                            xMin = xCenter - xRange / 2
                            xMax = xCenter + xRange / 2
                            val yRange = ((yMax - yMin) * 0.75).coerceIn(1e-4, 1e6)
                            val yCenter = (yMin + yMax) / 2
                            yMin = yCenter - yRange / 2
                            yMax = yCenter + yRange / 2
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.graph_zoom_in),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom Out (-)
                    IconButton(
                        onClick = {
                            val xRange = ((xMax - xMin) * 1.35).coerceIn(1e-4, 1e6)
                            val xCenter = (xMin + xMax) / 2
                            xMin = xCenter - xRange / 2
                            xMax = xCenter + xRange / 2
                            val yRange = ((yMax - yMin) * 1.35).coerceIn(1e-4, 1e6)
                            val yCenter = (yMin + yMax) / 2
                            yMin = yCenter - yRange / 2
                            yMax = yCenter + yRange / 2
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.graph_zoom_out),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset View (↺)
                    IconButton(
                        onClick = { resetView() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.graph_reset),
                            tint = colorScheme.onSurface.copy(alpha = 0.70f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (onApplyPointToCalculator != null) {
                Spacer(modifier = Modifier.height(10.dp))

                val activePoint = inspectedPoint
                val hasActivePoint = activePoint != null

                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            enabled = hasActivePoint,
                            role = Role.Button
                        ) {
                            activePoint?.let { pt ->
                                triggerClickHaptic()
                                onApplyPointToCalculator(pt.x, pt.y)
                            }
                        }
                        .testTag("btn_apply_graph_point"),
                    glassState = glassState,
                    shape = RoundedCornerShape(50),
                    borderStroke = if (hasActivePoint) {
                        BorderStroke(1.dp, colorScheme.onPrimary.copy(alpha = 0.25f))
                    } else {
                        BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.2f))
                    },
                    customBgColor = if (hasActivePoint) {
                        colorScheme.primary.copy(alpha = 0.90f)
                    } else {
                        colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val contentColor = if (hasActivePoint) {
                            colorScheme.onPrimary
                        } else {
                            colorScheme.onSurface.copy(alpha = 0.45f)
                        }
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.graph_btn_apply_to_calc),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
