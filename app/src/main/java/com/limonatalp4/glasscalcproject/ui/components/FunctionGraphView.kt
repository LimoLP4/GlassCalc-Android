package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
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

data class GraphPoint(val x: Double, val y: Double)

private fun calculateNiceStep(range: Double, targetSteps: Int): Double {
    if (range <= 0.0) return 1.0
    val rawStep = range / targetSteps.coerceAtLeast(1)
    val exponent = floor(log10(rawStep))
    val magnitude = 10.0.pow(exponent)
    val normalized = rawStep / magnitude
    val niceFactor = when {
        normalized < 1.5 -> 1.0
        normalized < 3.5 -> 2.0
        normalized < 7.5 -> 5.0
        else -> 10.0
    }
    return niceFactor * magnitude
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

@Composable
fun FunctionGraphView(
    expression: String,
    angleMode: AngleMode,
    isGestureLocked: Boolean,
    onGestureLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val localContext = LocalContext.current
    val view = LocalView.current
    val textMeasurer = rememberTextMeasurer()
    val hapticEnabled = LocalHapticFeedbackEnabled.current

    fun triggerClickHaptic() {
        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
    }

    var selectedPreset by remember { mutableStateOf(GraphPreset.SIN) }

    // Range bounds state
    var xMin by remember { mutableDoubleStateOf(-6.28) }
    var xMax by remember { mutableDoubleStateOf(6.28) }
    var yMin by remember { mutableDoubleStateOf(-3.0) }
    var yMax by remember { mutableDoubleStateOf(3.0) }

    // Cached curve evaluation points (normalized t from 0..1, y value)
    var cachedPoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }

    // Active touch-to-trace state
    var isTracing by remember { mutableStateOf(false) }
    var inspectedPoint by remember { mutableStateOf<GraphPoint?>(null) }
    var inspectedCanvasPos by remember { mutableStateOf<Offset?>(null) }
    var lastHapticTime by remember { mutableStateOf(0L) }

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
        inspectedCanvasPos = null
        onGestureLockChange(true)
    }

    // Auto-Lock on Preset change
    LaunchedEffect(selectedPreset) {
        inspectedPoint = null
        inspectedCanvasPos = null
        onGestureLockChange(true)
    }

    // Asynchronous background computation of curve points (Dispatchers.Default)
    // Only re-runs when the function or viewport boundaries change, NEVER when tracing!
    LaunchedEffect(selectedPreset, expression, angleMode, xMin, xMax) {
        withContext(Dispatchers.Default) {
            try {
                License.iConfirmNonCommercialUse("limonatalp4")
            } catch (_: Exception) {}

            if (angleMode == AngleMode.DEG) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            val formulaString = when (selectedPreset) {
                GraphPreset.CUSTOM -> {
                    if (expression.isNotBlank() && expression.contains("x", ignoreCase = true)) {
                        expression
                    } else if (expression.isNotBlank()) {
                        "($expression) * sin(x)"
                    } else {
                        "sin(x)"
                    }
                }
                else -> selectedPreset.formula
            }

            val sanitizedExpr = sanitizeForGraphParser(formulaString)
            val steps = 360 // High quality, low CPU footprint
            val calculatedList = ArrayList<Pair<Double, Double>>(steps + 1)

            val argX = Argument("x = 0")
            val expr = Expression(sanitizedExpr, argX)

            for (i in 0..steps) {
                val t = i.toDouble() / steps
                val x = xMin + t * (xMax - xMin)
                argX.argumentValue = x
                val y = expr.calculate()
                if (!y.isNaN() && !y.isInfinite() && abs(y) <= 1e8) {
                    calculatedList.add(Pair(x, y))
                } else {
                    calculatedList.add(Pair(x, Double.NaN))
                }
            }
            cachedPoints = calculatedList
        }
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
        inspectedCanvasPos = null
        isTracing = false
        onGestureLockChange(true)
        triggerClickHaptic()
    }

    // High performance trace lookup: interpolates from already calculated cached points (instant O(1))
    fun updateTraceFromCache(offset: Offset, width: Float, height: Float) {
        if (width <= 0 || height <= 0 || cachedPoints.isEmpty()) return
        val touchFraction = (offset.x / width).coerceIn(0f, 1f)
        val index = (touchFraction * (cachedPoints.size - 1)).toInt().coerceIn(0, cachedPoints.size - 1)
        val pt = cachedPoints[index]
        if (!pt.second.isNaN()) {
            inspectedPoint = GraphPoint(pt.first, pt.second)
            val py = (height - ((pt.second - yMin) / (yMax - yMin) * height)).toFloat()
            inspectedCanvasPos = Offset(offset.x.coerceIn(0f, width), py.coerceIn(0f, height))
        }
    }

    val graphColor = colorScheme.primary
    val gridColor = colorScheme.onSurface.copy(alpha = 0.16f)
    val axisColor = colorScheme.onSurface.copy(alpha = 0.55f)
    val labelColor = colorScheme.onSurface.copy(alpha = 0.70f)
    val traceLineColor = colorScheme.tertiary

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
                        onGestureLockChange(!isGestureLocked)
                        triggerClickHaptic()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("btn_lock_graph")
                ) {
                    Icon(
                        imageVector = if (isGestureLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isGestureLocked) "Graph-Gesten entsperren" else "Graph-Gesten sperren",
                        tint = if (isGestureLocked) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.4f),
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
                    .pointerInput(selectedPreset, xMin, xMax, yMin, yMax, angleMode, isGestureLocked) {
                        if (isGestureLocked) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val width = size.width.toFloat()
                                    val height = size.height.toFloat()
                                    isTracing = true
                                    updateTraceFromCache(offset, width, height)
                                    if (hapticEnabled) {
                                        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
                                    }
                                    tryAwaitRelease()
                                    isTracing = false
                                },
                                onTap = { offset ->
                                    val width = size.width.toFloat()
                                    val height = size.height.toFloat()
                                    updateTraceFromCache(offset, width, height)
                                    if (hapticEnabled) {
                                        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
                                    }
                                }
                            )
                        } else {
                            detectDragGestures(
                                onDragStart = { isTracing = true },
                                onDragEnd = { isTracing = false },
                                onDragCancel = { isTracing = false },
                                onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                    val width = size.width.toFloat()
                                    val height = size.height.toFloat()

                                    // 2D Panning when unlocked
                                    val dx = -(dragAmount.x / width) * (xMax - xMin)
                                    val dy = (dragAmount.y / height) * (yMax - yMin)

                                    xMin += dx
                                    xMax += dx
                                    yMin += dy
                                    yMax += dy

                                    // Instant O(1) trace info from cached points
                                    updateTraceFromCache(change.position, width, height)

                                    // Throttled Haptic Feedback (80ms)
                                    val now = System.currentTimeMillis()
                                    if (hapticEnabled && now - lastHapticTime > 80L) {
                                        lastHapticTime = now
                                        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
                                    }

                                    change.consume()
                                }
                            )
                        }
                    }
                    .testTag("graph_canvas_container")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    if (width <= 0 || height <= 0) return@Canvas

                    val strokeWidth = 2.5.dp.toPx()
                    val gridStrokeWidth = 1.dp.toPx()
                    val axisStrokeWidth = 1.8.dp.toPx()

                    val zeroXpx = ((0.0 - xMin) / (xMax - xMin) * width).toFloat()
                    val zeroYpx = (height - ((0.0 - yMin) / (yMax - yMin) * height)).toFloat()

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
                    val xStep = calculateNiceStep(xMax - xMin, targetSteps = 6)
                    val firstX = floor(xMin / xStep) * xStep
                    var currentX = firstX
                    while (currentX <= xMax + xStep * 0.1) {
                        val gx = ((currentX - xMin) / (xMax - xMin) * width).toFloat()
                        if (gx in 0f..width) {
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
                                val textX = (gx - measured.size.width / 2f).coerceIn(4f, width - measured.size.width - 4f)
                                val textY = if (zeroYpx in 10f..(height - 24f)) {
                                    zeroYpx + 4.dp.toPx()
                                } else {
                                    height - measured.size.height - 4.dp.toPx()
                                }
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    topLeft = Offset(textX, textY),
                                    style = axisTextStyle
                                )
                            }
                        }
                        currentX += xStep
                    }

                    val yStep = calculateNiceStep(yMax - yMin, targetSteps = 4)
                    val firstY = floor(yMin / yStep) * yStep
                    var currentY = firstY
                    while (currentY <= yMax + yStep * 0.1) {
                        val gy = (height - ((currentY - yMin) / (yMax - yMin) * height)).toFloat()
                        if (gy in 0f..height) {
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, gy),
                                end = Offset(width, gy),
                                strokeWidth = gridStrokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            val tickX = zeroXpx.coerceIn(16f, width - 16f)
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
                                    zeroXpx - measured.size.width - 4.dp.toPx()
                                } else {
                                    4.dp.toPx()
                                }
                                val textY = (gy - measured.size.height / 2f).coerceIn(4f, height - measured.size.height - 4f)
                                drawText(
                                    textMeasurer = textMeasurer,
                                    text = labelText,
                                    topLeft = Offset(textX, textY),
                                    style = axisTextStyle
                                )
                            }
                        }
                        currentY += yStep
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
                            if (y.isNaN()) {
                                isFirst = true
                                continue
                            }
                            val px = ((x - xMin) / (xMax - xMin) * width).toFloat()
                            val py = (height - ((y - yMin) / (yMax - yMin) * height)).toFloat()

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

                    // 4. TOUCH-TO-TRACE INTERACTION OVERLAYS
                    inspectedCanvasPos?.let { pos ->
                        val crosshairPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        drawLine(
                            color = traceLineColor.copy(alpha = 0.70f),
                            start = Offset(pos.x, 0f),
                            end = Offset(pos.x, height),
                            strokeWidth = 1.4.dp.toPx(),
                            pathEffect = crosshairPathEffect
                        )

                        drawLine(
                            color = traceLineColor.copy(alpha = 0.70f),
                            start = Offset(0f, pos.y),
                            end = Offset(width, pos.y),
                            strokeWidth = 1.4.dp.toPx(),
                            pathEffect = crosshairPathEffect
                        )

                        drawCircle(
                            color = traceLineColor.copy(alpha = 0.28f),
                            radius = 11.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = traceLineColor,
                            radius = 5.5.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.8.dp.toPx(),
                            center = pos
                        )
                    }
                }

                // Interactive Touch-To-Trace HUD Tooltip
                inspectedPoint?.let { pt ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
                            .border(1.2.dp, traceLineColor.copy(alpha = 0.8f), RoundedCornerShape(10.dp))
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
                                    color = traceLineColor
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
                                    inspectedCanvasPos = null
                                    triggerClickHaptic()
                                },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Schließen",
                                    tint = traceLineColor,
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
                                text = "Wischen zum Abtasten",
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
                            xMin -= shift
                            xMax -= shift
                            onGestureLockChange(true)
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Links schwenken",
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Pan Right (▶)
                    IconButton(
                        onClick = {
                            val shift = (xMax - xMin) * 0.25
                            xMin += shift
                            xMax += shift
                            onGestureLockChange(true)
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "Rechts schwenken",
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom In (+)
                    IconButton(
                        onClick = {
                            val xRange = (xMax - xMin) * 0.75
                            val xCenter = (xMin + xMax) / 2
                            xMin = xCenter - xRange / 2
                            xMax = xCenter + xRange / 2
                            val yRange = (yMax - yMin) * 0.75
                            val yCenter = (yMin + yMax) / 2
                            yMin = yCenter - yRange / 2
                            yMax = yCenter + yRange / 2
                            onGestureLockChange(true)
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom Out (-)
                    IconButton(
                        onClick = {
                            val xRange = (xMax - xMin) * 1.35
                            val xCenter = (xMin + xMax) / 2
                            xMin = xCenter - xRange / 2
                            xMax = xCenter + xRange / 2
                            val yRange = (yMax - yMin) * 1.35
                            val yCenter = (yMin + yMax) / 2
                            yMin = yCenter - yRange / 2
                            yMax = yCenter + yRange / 2
                            onGestureLockChange(true)
                            triggerClickHaptic()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
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
                            contentDescription = "Zurücksetzen",
                            tint = colorScheme.onSurface.copy(alpha = 0.70f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
