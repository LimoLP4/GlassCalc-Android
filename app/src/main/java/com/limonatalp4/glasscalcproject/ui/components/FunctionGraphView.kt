package com.limonatalp4.glasscalcproject.ui.components

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.PointerInputChange
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
import androidx.compose.material.icons.filled.Download
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.rememberGraphicsLayer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import java.util.Locale
import kotlin.math.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.mXparser

enum class GraphPreset(val label: String, val formula: String) {
    SIN("sin(x)", "sin"),
    COS("cos(x)", "cos"),
    TAN("tan(x)", "tan"),
    LN("ln(x)", "ln"),
    LOG("log(x)", "log"),
    SQUARE("x²", "x^2"),
    SQRT("√x", "sqrt"),
    CUSTOM("f(x)", "custom")
}

data class GraphPoint(val x: Double, val y: Double)

/**
 * Calculates human-readable "nice" grid step intervals (e.g. 1, 2, 5, 10, 0.5, 0.2, etc.)
 * based on the domain/range interval and requested target step count.
 */
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

/**
 * Formats axis numerical labels cleanly without trailing unneeded zeros.
 */
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

@Composable
fun FunctionGraphView(
    expression: String,
    angleMode: AngleMode,
    isGestureLocked: Boolean,
    onGestureLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    glassState: LiquidGlassState? = null
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val localContext = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val hapticFeedback = LocalHapticFeedback.current
    val textMeasurer = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val hapticEnabled = LocalHapticFeedbackEnabled.current

    fun triggerClickHaptic() {
        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
    }

    fun sanitizeForParser(expr: String): String {
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
            .replace("ln", "ln")
            .replace("log", "log10")
    }

    var selectedPreset by remember { mutableStateOf(GraphPreset.SIN) }

    // Range bounds state
    var xMin by remember { mutableDoubleStateOf(-6.28) }
    var xMax by remember { mutableDoubleStateOf(6.28) }
    var yMin by remember { mutableDoubleStateOf(-3.0) }
    var yMax by remember { mutableDoubleStateOf(3.0) }

    var cachedPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    // Asynchronous calculation of curve points in background thread (Dispatchers.Default)
    LaunchedEffect(selectedPreset, expression, angleMode, xMin, xMax) {
        withContext(Dispatchers.Default) {
            if (angleMode == AngleMode.DEG) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            val exprString = when (selectedPreset) {
                GraphPreset.SIN -> "sin(x)"
                GraphPreset.COS -> "cos(x)"
                GraphPreset.TAN -> "tan(x)"
                GraphPreset.LN -> "ln(x)"
                GraphPreset.LOG -> "log10(x)"
                GraphPreset.SQRT -> "sqrt(x)"
                GraphPreset.SQUARE -> "x^2"
                GraphPreset.CUSTOM -> {
                    if (expression.isNotBlank() && (expression.contains("x", ignoreCase = true))) {
                        expression
                    } else if (expression.isNotBlank()) {
                        "($expression) * sin(x)"
                    } else {
                        "sin(x)"
                    }
                }
            }
            val sanitizedExpr = sanitizeForParser(exprString)

            val steps = 600
            val points = ArrayList<Offset>(steps + 1)
            for (i in 0..steps) {
                val t = i.toDouble() / steps
                val x = xMin + t * (xMax - xMin)
                try {
                    val argX = Argument("x = $x")
                    val e = Expression(sanitizedExpr, argX)
                    val y = e.calculate()
                    if (!y.isNaN() && !y.isInfinite() && abs(y) <= 1e10) {
                        points.add(Offset(t.toFloat(), y.toFloat()))
                    } else {
                        points.add(Offset(t.toFloat(), Float.NaN))
                    }
                } catch (_: Exception) {
                    points.add(Offset(t.toFloat(), Float.NaN))
                }
            }
            cachedPoints = points
        }
    }

    fun saveGraphToGallery() {
        scope.launch {
            try {
                val bitmap = Bitmap.createBitmap(view.width.coerceAtLeast(100), view.height.coerceAtLeast(100), Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bitmap)
                view.draw(canvas)

                val resolver = localContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "GlassCalc_Graph_${System.currentTimeMillis()}.png")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/GlassCalc")
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                    Toast.makeText(localContext, "Graph als Bild in der Galerie gespeichert!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(localContext, "Fehler beim Speichern des Bildes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(localContext, "Fehler: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
            triggerClickHaptic()
        }
    }

    // Active touch-to-trace state
    var isTracing by remember { mutableStateOf(false) }
    var inspectedPoint by remember { mutableStateOf<GraphPoint?>(null) }
    var inspectedCanvasPos by remember { mutableStateOf<Offset?>(null) }

    var lastHapticTriggerX by remember { mutableDoubleStateOf(Double.NaN) }
    var lastHapticTime by remember { mutableStateOf(0L) }
    val hapticEnabled = LocalHapticFeedbackEnabled.current

    fun triggerClickHaptic() {
        HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
    }

    // Auto-Zoom & Auto-Lock: Adjust range and re-lock when switching DEG/RAD
    androidx.compose.runtime.LaunchedEffect(angleMode) {
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
        onGestureLockChange(true) // AUTO-LOCK
    }

    // Auto-Lock on Preset change
    androidx.compose.runtime.LaunchedEffect(selectedPreset) {
        onGestureLockChange(true) // AUTO-LOCK
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
        onGestureLockChange(true) // AUTO-LOCK
    triggerClickHaptic()
    }

    fun sanitizeForParser(expr: String): String {
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
            .replace("ln", "ln")
            .replace("log", "log10")
    }

    // Function evaluator using mXparser
    fun evalY(xVal: Double): Double? {
        return try {
            // Set global angle mode for mXparser
            if (angleMode == AngleMode.DEG) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            val exprString = when (selectedPreset) {
                GraphPreset.SIN -> "sin(x)"
                GraphPreset.COS -> "cos(x)"
                GraphPreset.TAN -> "tan(x)"
                GraphPreset.LN -> "ln(x)"
                GraphPreset.LOG -> "log10(x)" // mXparser uses log10 for base 10
                GraphPreset.SQRT -> "sqrt(x)"
                GraphPreset.SQUARE -> "x^2"
                GraphPreset.CUSTOM -> {
                    if (expression.isNotBlank() && (expression.contains("x", ignoreCase = true))) {
                        expression
                    } else if (expression.isNotBlank()) {
                        "($expression) * sin(x)"
                    } else {
                        "sin(x)"
                    }
                }
            }

            val sanitizedExpr = sanitizeForParser(exprString)
            val argX = Argument("x = $xVal")
            val e = Expression(sanitizedExpr, argX)
            val res = e.calculate()

            if (res.isNaN() || res.isInfinite() || abs(res) > 1e10) null
            else res
        } catch (_: Exception) {
            null
        }
    }

    fun updateTrace(offset: Offset, width: Float, height: Float) {
        if (width <= 0 || height <= 0) return
        val touchXFraction = (offset.x / width).coerceIn(0f, 1f)
        val xVal = xMin + touchXFraction * (xMax - xMin)
        val yVal = evalY(xVal)
        if (yVal != null) {
            inspectedPoint = GraphPoint(xVal, yVal)
            val py = (height - ((yVal - yMin) / (yMax - yMin) * height)).toFloat()
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
                                inspectedPoint = null
                                inspectedCanvasPos = null
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

                // LOCK BUTTON
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
                        contentDescription = if (isGestureLocked) "Gesten freigeben" else "Hintergrund sperren",
                        tint = if (isGestureLocked) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Canvas Plot Area with 2D Panning, Throttled Haptics & GraphicsLayer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .graphicsLayer(graphicsLayer)
                    .background(colorScheme.surfaceContainerLow)
                    .border(1.dp, gridColor, RoundedCornerShape(16.dp))
                    .pointerInput(selectedPreset, xMin, xMax, yMin, yMax, angleMode, isGestureLocked) {
                        detectDragGestures(
                            onDragStart = { isTracing = true },
                            onDragEnd = { isTracing = false },
                            onDragCancel = { isTracing = false },
                            onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()

                                if (!isGestureLocked) {
                                    // 2D Panning: Update coordinate bounds only when UNLOCKED
                                    val dx = -(dragAmount.x / width) * (xMax - xMin)
                                    val dy = (dragAmount.y / height) * (yMax - yMin)

                                    xMin += dx
                                    xMax += dx
                                    yMin += dy
                                    yMax += dy
                                }

                                // Update trace info relative to finger position (Always enabled)
                                updateTrace(change.position, width, height)

                                // Throttled Haptic Feedback (80ms)
                                val now = System.currentTimeMillis()
                                if (hapticEnabled && now - lastHapticTime > 80L) {
                                    lastHapticTime = now
                                    HapticUtil.performAppHaptic(localContext, view, hapticEnabled)
                                }

                                // Consume the gesture to prevent background scrolling or system 'Back'
                                change.consume()
                            }
                        )
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

                    // Compute origin pixel positions
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
                    // Vertical grid lines (X axis ticks)
                    val xStep = calculateNiceStep(xMax - xMin, targetSteps = 6)
                    val firstX = floor(xMin / xStep) * xStep
                    var currentX = firstX
                    while (currentX <= xMax + xStep * 0.1) {
                        val gx = ((currentX - xMin) / (xMax - xMin) * width).toFloat()
                        if (gx in 0f..width) {
                            // Grid line
                            drawLine(
                                color = gridColor,
                                start = Offset(gx, 0f),
                                end = Offset(gx, height),
                                strokeWidth = gridStrokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            // Tick mark on X axis
                            val tickY = zeroYpx.coerceIn(12f, height - 16f)
                            drawLine(
                                color = axisColor,
                                start = Offset(gx, tickY - 3.dp.toPx()),
                                end = Offset(gx, tickY + 3.dp.toPx()),
                                strokeWidth = axisStrokeWidth
                            )

                            // Numerical label for X
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

                    // Horizontal grid lines (Y axis ticks)
                    val yStep = calculateNiceStep(yMax - yMin, targetSteps = 4)
                    val firstY = floor(yMin / yStep) * yStep
                    var currentY = firstY
                    while (currentY <= yMax + yStep * 0.1) {
                        val gy = (height - ((currentY - yMin) / (yMax - yMin) * height)).toFloat()
                        if (gy in 0f..height) {
                            // Grid line
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, gy),
                                end = Offset(width, gy),
                                strokeWidth = gridStrokeWidth,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
                            )

                            // Tick mark on Y axis
                            val tickX = zeroXpx.coerceIn(16f, width - 16f)
                            drawLine(
                                color = axisColor,
                                start = Offset(tickX - 3.dp.toPx(), gy),
                                end = Offset(tickX + 3.dp.toPx(), gy),
                                strokeWidth = axisStrokeWidth
                            )

                            // Numerical label for Y
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
                    // X-Axis
                    if (zeroYpx in 0f..height) {
                        drawLine(
                            color = axisColor,
                            start = Offset(0f, zeroYpx),
                            end = Offset(width, zeroYpx),
                            strokeWidth = axisStrokeWidth
                        )
                        // "X" axis symbol at right
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "x",
                            topLeft = Offset(width - 14.dp.toPx(), (zeroYpx - 14.dp.toPx()).coerceAtLeast(4f)),
                            style = axisTitleStyle
                        )
                    }

                    // Y-Axis
                    if (zeroXpx in 0f..width) {
                        drawLine(
                            color = axisColor,
                            start = Offset(zeroXpx, 0f),
                            end = Offset(zeroXpx, height),
                            strokeWidth = axisStrokeWidth
                        )
                        // "Y" axis symbol at top
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "y",
                            topLeft = Offset((zeroXpx + 5.dp.toPx()).coerceAtMost(width - 16.dp.toPx()), 4.dp.toPx()),
                            style = axisTitleStyle
                        )
                    }

                    // Origin "0" label
                    if (zeroXpx in 10f..(width - 10f) && zeroYpx in 10f..(height - 10f)) {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = "0",
                            topLeft = Offset(zeroXpx - 10.dp.toPx(), zeroYpx + 3.dp.toPx()),
                            style = axisTextStyle
                        )
                    }

                    // 3. CURVE EVALUATION & PLOTTING (from cached points)
                    val path = remember(cachedPoints, xMin, xMax, yMin, yMax, width, height) {
                        val p = Path()
                        var isFirstPoint = true
                        var lastPy = 0f

                        for (pt in cachedPoints) {
                            if (pt.y.isNaN()) {
                                isFirstPoint = true
                                continue
                            }
                            val px = pt.x * width
                            val py = (height - ((pt.y.toDouble() - yMin) / (yMax - yMin) * height)).toFloat()

                            // Asymptote / Discontinuity jump filter
                            if (isFirstPoint || abs(py - lastPy) > height * 0.75f) {
                                p.moveTo(px, py)
                                isFirstPoint = false
                            } else {
                                p.lineTo(px, py)
                            }
                            lastPy = py
                        }
                        p
                    }

                    // Draw smooth curve line
                    drawPath(
                        path = path,
                        color = graphColor,
                        style = Stroke(width = strokeWidth)
                    )

                    // 4. TOUCH-TO-TRACE INTERACTION OVERLAYS
                    inspectedCanvasPos?.let { pos ->
                        // Crosshair guide lines
                        val crosshairPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        // Vertical trace guide line (spanning full height)
                        drawLine(
                            color = traceLineColor.copy(alpha = 0.70f),
                            start = Offset(pos.x, 0f),
                            end = Offset(pos.x, height),
                            strokeWidth = 1.4.dp.toPx(),
                            pathEffect = crosshairPathEffect
                        )

                        // Horizontal trace guide line (spanning full width)
                        drawLine(
                            color = traceLineColor.copy(alpha = 0.70f),
                            start = Offset(0f, pos.y),
                            end = Offset(width, pos.y),
                            strokeWidth = 1.4.dp.toPx(),
                            pathEffect = crosshairPathEffect
                        )

                        // Glowing pulsing reticle bead on curve
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
                                val formulaText = when (selectedPreset) {
                                    GraphPreset.SIN -> "sin(x)"
                                    GraphPreset.COS -> "cos(x)"
                                    GraphPreset.TAN -> "tan(x)"
                                    GraphPreset.LN -> "ln(x)"
                                    GraphPreset.LOG -> "log(x)"
                                    GraphPreset.SQUARE -> "x²"
                                    GraphPreset.SQRT -> "√x"
                                    GraphPreset.CUSTOM -> "f(x)"
                                }
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

                            // Dismiss trace button
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

                // Interactive Trace Hint (when not actively traced)
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
                                text = "Gedrückt halten & wischen zum Abtasten",
                                fontSize = 9.sp,
                                color = labelColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Graph Controls Footer Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bounds Legend Text
                Text(
                    text = String.format(Locale.US, "X: [%.1f .. %.1f]  Y: [%.1f .. %.1f]", xMin, xMax, yMin, yMax),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = labelColor
                )

                // Pan, Zoom & Reset Action Buttons
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
                            onGestureLockChange(true) // AUTO-LOCK
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
                            onGestureLockChange(true) // AUTO-LOCK
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
                            onGestureLockChange(true) // AUTO-LOCK
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
                            onGestureLockChange(true) // AUTO-LOCK
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

                    // Export Image Button (⤓)
                    IconButton(
                        onClick = { saveGraphToGallery() },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_export_graph")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Graph als Bild speichern",
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
