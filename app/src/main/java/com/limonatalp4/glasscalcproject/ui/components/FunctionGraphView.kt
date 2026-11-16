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
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
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
import kotlin.math.hypot
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
    DERIV("d/dx", "der(sin(x), x)"),
    INTEGRAL("∫", "int(sin(t), t, 0, x)"),
    CUSTOM("f(x)", "custom")
}

@Immutable
data class GraphPoint(val x: Double, val y: Double)

@Immutable
data class GraphBounds(
    val xMin: Double,
    val xMax: Double,
    val yMin: Double,
    val yMax: Double
)

/**
 * 1. CLEAR COORDINATE TRANSFORMATION FUNCTIONS (Screen to Math & Math to Screen)
 */
fun screenToMath(
    offsetX: Float,
    offsetY: Float,
    width: Float,
    height: Float,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double
): GraphPoint {
    val xSpan = xMax - xMin
    val ySpan = yMax - yMin
    val mathX = if (width > 0f) xMin + (offsetX / width) * xSpan else xMin
    val mathY = if (height > 0f) yMin + ((height - offsetY) / height) * ySpan else yMin
    return GraphPoint(mathX, mathY)
}

fun mathToScreen(
    x: Double,
    y: Double,
    width: Float,
    height: Float,
    xMin: Double,
    xMax: Double,
    yMin: Double,
    yMax: Double
): Offset {
    val xSpan = xMax - xMin
    val ySpan = yMax - yMin
    val px = if (xSpan > 1e-12) ((x - xMin) / xSpan * width).toFloat() else 0f
    val py = if (ySpan > 1e-12) (height - ((y - yMin) / ySpan * height)).toFloat() else 0f
    return Offset(px, py)
}

fun defaultBoundsForPreset(
    preset: GraphPreset,
    angleMode: AngleMode,
    expression: String = ""
): GraphBounds {
    return when (preset) {
        GraphPreset.SIN, GraphPreset.COS, GraphPreset.TAN -> {
            if (angleMode == AngleMode.DEG) {
                GraphBounds(-360.0, 360.0, -2.5, 2.5)
            } else {
                GraphBounds(-6.28, 6.28, -2.0, 2.0)
            }
        }
        GraphPreset.ARCSIN -> {
            if (angleMode == AngleMode.DEG) {
                GraphBounds(-1.2, 1.2, -100.0, 100.0)
            } else {
                GraphBounds(-1.2, 1.2, -2.0, 2.0)
            }
        }
        GraphPreset.ARCCOS -> {
            if (angleMode == AngleMode.DEG) {
                GraphBounds(-1.2, 1.2, -15.0, 195.0)
            } else {
                GraphBounds(-1.2, 1.2, -0.5, 3.5)
            }
        }
        GraphPreset.ARCTAN -> {
            if (angleMode == AngleMode.DEG) {
                GraphBounds(-5.0, 5.0, -100.0, 100.0)
            } else {
                GraphBounds(-5.0, 5.0, -2.0, 2.0)
            }
        }
        GraphPreset.SINH, GraphPreset.COSH -> {
            GraphBounds(-3.5, 3.5, -5.0, 5.0)
        }
        GraphPreset.TANH -> {
            GraphBounds(-4.0, 4.0, -2.0, 2.0)
        }
        GraphPreset.ABS -> {
            GraphBounds(-10.0, 10.0, -2.0, 10.0)
        }
        GraphPreset.LN -> {
            GraphBounds(-0.5, 10.0, -4.0, 4.0)
        }
        GraphPreset.LOG -> {
            GraphBounds(-0.5, 10.0, -3.0, 2.0)
        }
        GraphPreset.SQUARE -> {
            GraphBounds(-10.0, 10.0, -5.0, 100.0)
        }
        GraphPreset.SQRT -> {
            GraphBounds(-0.5, 16.0, -1.0, 5.0)
        }
        GraphPreset.DERIV -> {
            val lower = expression.lowercase()
            if (lower.contains("^2") || lower.contains("²") || lower.contains("x*x")) {
                GraphBounds(-10.0, 10.0, -20.0, 20.0)
            } else {
                GraphBounds(-6.28, 6.28, -2.0, 2.0)
            }
        }
        GraphPreset.INTEGRAL -> {
            if (angleMode == AngleMode.DEG) {
                GraphBounds(-360.0, 360.0, -10.0, 130.0)
            } else {
                GraphBounds(-6.28, 6.28, -3.0, 3.0)
            }
        }
        GraphPreset.CUSTOM -> {
            val lower = expression.lowercase()
            if (lower.contains("arccos") || lower.contains("acos")) {
                if (angleMode == AngleMode.DEG) GraphBounds(-1.2, 1.2, -15.0, 195.0)
                else GraphBounds(-1.2, 1.2, -0.5, 3.5)
            } else if (lower.contains("arcsin") || lower.contains("asin")) {
                if (angleMode == AngleMode.DEG) GraphBounds(-1.2, 1.2, -100.0, 100.0)
                else GraphBounds(-1.2, 1.2, -2.0, 2.0)
            } else if (lower.contains("arctan") || lower.contains("atan")) {
                if (angleMode == AngleMode.DEG) GraphBounds(-5.0, 5.0, -100.0, 100.0)
                else GraphBounds(-5.0, 5.0, -2.0, 2.0)
            } else if (lower.contains("sinh") || lower.contains("cosh")) {
                GraphBounds(-3.5, 3.5, -5.0, 5.0)
            } else if (lower.contains("tanh")) {
                GraphBounds(-4.0, 4.0, -2.0, 2.0)
            } else if (angleMode == AngleMode.DEG && (lower.contains("sin") || lower.contains("cos") || lower.contains("tan"))) {
                GraphBounds(-360.0, 360.0, -2.5, 2.5)
            } else if (lower.contains("^2") || lower.contains("²")) {
                GraphBounds(-10.0, 10.0, -5.0, 100.0)
            } else if (lower.contains("sqrt") || lower.contains("√")) {
                GraphBounds(-0.5, 16.0, -1.0, 5.0)
            } else if (lower.contains("log")) {
                GraphBounds(-0.5, 10.0, -3.0, 2.0)
            } else if (lower.contains("ln")) {
                GraphBounds(-0.5, 10.0, -4.0, 4.0)
            } else {
                GraphBounds(-10.0, 10.0, -10.0, 10.0)
            }
        }
    }
}

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

internal fun sanitizeForGraphParser(expr: String): String {
    var s = expr
        .replace("π", "pi")
        .replace("e", "e")
        .replace("−", "-")
        .replace("×", "*")
        .replace("÷", "/")
        .replace("⁄", "/")
        .replace("³√(", "root(3, ")
        .replace(Regex("""³√([a-zA-Z0-9_.]+)"""), "root(3, $1)")
        .replace("√", "sqrt")
        .replace("²", "^2")
        .replace("³", "^3")
        .replace("⁴", "^4")
        .replace("⁵", "^5")
        .replace("⁶", "^6")
        .replace("⁷", "^7")
        .replace("⁸", "^8")
        .replace("⁹", "^9")
        .replace("⁰", "^0")
        .replace("⁺", "+")
        .replace("⁻", "-")
        .replace("arcsin", "asin")
        .replace("arccos", "acos")
        .replace("arctan", "atan")
        .replace("ln", "ln")
        .replace(Regex("""(?<![a-zA-Z0-9_])log\("""), "log10(")
        .replace(Regex("""\|([^|]+)\|"""), "abs($1)")
        .replace("mod", "#")
        .replace("∫(", "int(")
        .replace("d/dx(", "der(")

    s = s.replace(Regex("""(?<=\d),(?=\d)"""), ".")
    s = s.replace(";", ",")

    s = expandSingleArgDerivatives(s)

    return s
}

private fun expandSingleArgDerivatives(input: String): String {
    val sb = StringBuilder(input.length)
    var i = 0
    while (i < input.length) {
        if (input.startsWith("der(", i) && (i == 0 || !input[i - 1].isLetterOrDigit())) {
            val openParen = i + 3
            var depth = 1
            var j = openParen + 1
            var hasTopLevelComma = false
            while (j < input.length && depth > 0) {
                when (input[j]) {
                    '(' -> depth++
                    ')' -> depth--
                    ',' -> if (depth == 1) hasTopLevelComma = true
                }
                if (depth > 0) j++
            }
            if (depth == 0) {
                val inner = input.substring(openParen + 1, j)
                val expandedInner = expandSingleArgDerivatives(inner)
                if (hasTopLevelComma) {
                    sb.append("der(").append(expandedInner).append(')')
                } else {
                    sb.append("der(").append(expandedInner).append(", x)")
                }
                i = j + 1
                continue
            }
        }
        sb.append(input[i])
        i++
    }
    return sb.toString()
}

internal fun resolveActiveFormula(preset: GraphPreset, expression: String): String {
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
        GraphPreset.DERIV -> {
            if (expression.isNotBlank() && expression.contains("x", ignoreCase = true)) {
                if (expression.contains("d/dx", ignoreCase = true) || expression.contains("der(", ignoreCase = true)) {
                    expression
                } else {
                    "der($expression, x)"
                }
            } else {
                "der(sin(x), x)"
            }
        }
        GraphPreset.INTEGRAL -> {
            if (expression.isNotBlank() && expression.contains("x", ignoreCase = true)) {
                if (expression.contains("int(", ignoreCase = true) || expression.contains("∫", ignoreCase = true)) {
                    expression
                } else {
                    "int($expression, x, 0, x)"
                }
            } else {
                "int(sin(t), t, 0, x)"
            }
        }
        GraphPreset.SQUARE -> "x^2"
        else -> preset.formula
    }
}

internal fun isWithinDomain(sanitizedExpr: String, x: Double): Boolean {
    val lower = sanitizedExpr.lowercase()
    if (lower.contains("asin") || lower.contains("acos") || lower.contains("arcsin") || lower.contains("arccos")) {
        if (x < -1.0 || x > 1.0) return false
    }
    if (lower.contains("log10") || lower.contains("log(") || lower.contains("ln(")) {
        if (x <= 0.0) return false
    }
    if (lower.contains("sqrt") || lower.contains("√")) {
        if (x < 0.0) return false
    }
    return true
}

internal fun evaluateFormulaAtX(
    formula: String,
    x: Double,
    angleMode: AngleMode
): Double? {
    if (!x.isFinite()) return null
    val sanitizedExpr = sanitizeForGraphParser(formula)
    if (!isWithinDomain(sanitizedExpr, x)) return null
    return try {
        val y = synchronized(MxParserLock) {
            try {
                License.iConfirmNonCommercialUse("limonatalp4")
            } catch (_: Exception) {}

            val lower = sanitizedExpr.lowercase()
            val hasTrig = lower.contains("sin") || lower.contains("cos") || lower.contains("tan")
            if (angleMode == AngleMode.DEG && hasTrig && !sanitizedExpr.startsWith("der(")) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            val argX = Argument("x = 0")
            argX.argumentValue = x
            val expr = Expression(sanitizedExpr, argX)
            var result = expr.calculate()

            // Numerical central difference fallback if mXparser returned NaN for derivative
            if ((!result.isFinite() || result.isNaN()) && sanitizedExpr.startsWith("der(")) {
                val match = Regex("""der\((.+),\s*([a-zA-Z_][a-zA-Z0-9_]*)\)""").matchEntire(sanitizedExpr)
                if (match != null) {
                    val fn = match.groupValues[1]
                    val varName = match.groupValues[2]
                    val h = 1e-5
                    val subArg = Argument("$varName = 0")
                    val subExpr = Expression(fn, subArg)
                    subArg.argumentValue = x + h
                    val yPlus = subExpr.calculate()
                    subArg.argumentValue = x - h
                    val yMinus = subExpr.calculate()
                    if (yPlus.isFinite() && yMinus.isFinite()) {
                        result = (yPlus - yMinus) / (2.0 * h)
                    }
                }
            }

            result
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

    // Range bounds state - initialized based on default preset
    val initialBounds = remember { defaultBoundsForPreset(GraphPreset.SIN, angleMode, expression) }
    var xMin by remember { mutableDoubleStateOf(initialBounds.xMin) }
    var xMax by remember { mutableDoubleStateOf(initialBounds.xMax) }
    var yMin by remember { mutableDoubleStateOf(initialBounds.yMin) }
    var yMax by remember { mutableDoubleStateOf(initialBounds.yMax) }

    // Cached curve evaluation points
    var cachedPoints by remember { mutableStateOf<List<Pair<Double, Double>>>(emptyList()) }

    // Two manual inspected points (P1, P2) at mathematical (x, y)
    var point1 by remember { mutableStateOf<GraphPoint?>(null) }
    var point2 by remember { mutableStateOf<GraphPoint?>(null) }
    var nextPointIndex by remember { mutableIntStateOf(1) }
    var lastHapticTime by remember { mutableStateOf(0L) }

    val currentPreset by rememberUpdatedState(selectedPreset)
    val currentExpression by rememberUpdatedState(expression)
    val currentAngleMode by rememberUpdatedState(angleMode)
    val currentIsGraphLocked by rememberUpdatedState(isGraphLocked)
    val currentHapticEnabled by rememberUpdatedState(hapticEnabled)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

    // Auto-bounds and point reset on preset or angleMode change
    LaunchedEffect(selectedPreset, angleMode) {
        val bounds = defaultBoundsForPreset(selectedPreset, angleMode, expression)
        xMin = bounds.xMin
        xMax = bounds.xMax
        yMin = bounds.yMin
        yMax = bounds.yMax
        point1 = null
        point2 = null
        nextPointIndex = 1
    }

    // 4. CURVE DRAWING: Path calculation over visible X range with small step size (0.1 or step resolution)
    LaunchedEffect(selectedPreset, expression, angleMode, xMin, xMax) {
        val computedPoints = withContext(Dispatchers.Default) {
            try {
                if (!xMin.isFinite() || !xMax.isFinite() || xMax <= xMin) {
                    return@withContext emptyList<Pair<Double, Double>>()
                }
                val formulaString = resolveActiveFormula(selectedPreset, expression)
                val sanitizedExpr = sanitizeForGraphParser(formulaString)
                
                // High density steps across xMin..xMax plus exact domain boundary points
                val xSpan = xMax - xMin
                val steps = (xSpan * 50).toInt().coerceIn(300, 1200)
                val xSamples = ArrayList<Double>(steps + 5)
                for (i in 0..steps) {
                    xSamples.add(xMin + (i.toDouble() / steps) * xSpan)
                }
                val lowerSanitized = sanitizedExpr.lowercase()
                if (lowerSanitized.contains("asin") || lowerSanitized.contains("acos")) {
                    if (-1.0 in xMin..xMax) xSamples.add(-1.0)
                    if (1.0 in xMin..xMax) xSamples.add(1.0)
                }
                if (lowerSanitized.contains("sqrt") && 0.0 in xMin..xMax) {
                    xSamples.add(0.0)
                }
                xSamples.sort()

                val calculatedList = ArrayList<Pair<Double, Double>>(xSamples.size)

                synchronized(MxParserLock) {
                    try {
                        License.iConfirmNonCommercialUse("limonatalp4")
                    } catch (_: Exception) {}

                    val hasTrig = lowerSanitized.contains("sin") || lowerSanitized.contains("cos") || lowerSanitized.contains("tan")
                    if (angleMode == AngleMode.DEG && hasTrig && !sanitizedExpr.startsWith("der(")) {
                        mXparser.setDegreesMode()
                    } else {
                        mXparser.setRadiansMode()
                    }

                    val argX = Argument("x = 0")
                    val expr = Expression(sanitizedExpr, argX)

                    val isDeriv = sanitizedExpr.startsWith("der(")
                    val derivMatch = if (isDeriv) Regex("""der\((.+),\s*([a-zA-Z_][a-zA-Z0-9_]*)\)""").matchEntire(sanitizedExpr) else null
                    val subArg = derivMatch?.let { Argument("${it.groupValues[2]} = 0") }
                    val subExpr = derivMatch?.let { Expression(it.groupValues[1], subArg) }
                    val h = 1e-5

                    for (x in xSamples) {
                        // Skip out of domain values
                        if (!isWithinDomain(sanitizedExpr, x)) {
                            calculatedList.add(Pair(x, Double.NaN))
                            continue
                        }

                        argX.argumentValue = x
                        var y = expr.calculate()

                        if ((!y.isFinite() || y.isNaN()) && subExpr != null && subArg != null) {
                            subArg.argumentValue = x + h
                            val yPlus = subExpr.calculate()
                            subArg.argumentValue = x - h
                            val yMinus = subExpr.calculate()
                            if (yPlus.isFinite() && yMinus.isFinite()) {
                                y = (yPlus - yMinus) / (2.0 * h)
                            }
                        }

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
        val bounds = defaultBoundsForPreset(selectedPreset, angleMode, expression)
        xMin = bounds.xMin
        xMax = bounds.xMax
        yMin = bounds.yMin
        yMax = bounds.yMax
        point1 = null
        point2 = null
        nextPointIndex = 1
        triggerClickHaptic()
    }

    // 2. CORRECT CALCULATION OF POINTS (P1/P2): X from tap using screenToMath, Y strictly from function evaluation
    fun tryPlacePointAtTap(tapOffset: Offset, canvasWidth: Float, canvasHeight: Float) {
        if (currentIsGraphLocked || canvasWidth <= 0f || canvasHeight <= 0f) return
        
        // Use screenToMath to get exact math coordinates from tap
        val mathPoint = screenToMath(
            offsetX = tapOffset.x,
            offsetY = tapOffset.y,
            width = canvasWidth,
            height = canvasHeight,
            xMin = xMin,
            xMax = xMax,
            yMin = yMin,
            yMax = yMax
        )
        
        val tappedX = mathPoint.x
        val formula = resolveActiveFormula(currentPreset, currentExpression)
        val evaluatedY = evaluateFormulaAtX(formula, tappedX, currentAngleMode)

        if (evaluatedY != null && evaluatedY.isFinite()) {
            val newPoint = GraphPoint(tappedX, evaluatedY)
            when {
                point1 == null -> {
                    point1 = newPoint
                    nextPointIndex = 2
                }
                point2 == null -> {
                    point2 = newPoint
                    nextPointIndex = 1
                }
                nextPointIndex == 1 -> {
                    point1 = newPoint
                    nextPointIndex = 2
                }
                else -> {
                    point2 = newPoint
                    nextPointIndex = 1
                }
            }
            HapticUtil.performAppHaptic(
                localContext,
                view,
                currentHapticEnabled,
                currentHapticIntensity
            )
        }
    }

    val graphColor = colorScheme.primary
    val point1Color = graphColor
    val point1BadgeTextColor = colorScheme.onPrimary
    val point2Color = colorScheme.tertiary
    val point2BadgeTextColor = if (point2Color.luminance() > 0.5f) Color(0xFF1A1C1E) else Color.White
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

                val lockBtnInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = {
                        isGraphLocked = !isGraphLocked
                        onGestureLockChange(isGraphLocked)
                        triggerClickHaptic()
                    },
                    interactionSource = lockBtnInteraction,
                    modifier = Modifier
                        .size(32.dp)
                        .pressScale(lockBtnInteraction)
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

            // Main Canvas Plot Area with 2D Panning & Zooming
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
                                    tryPlacePointAtTap(offset, size.width.toFloat(), size.height.toFloat())
                                },
                                onLongPress = { offset ->
                                    tryPlacePointAtTap(offset, size.width.toFloat(), size.height.toFloat())
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

                    // Use mathToScreen for origin axes
                    val originOffset = mathToScreen(0.0, 0.0, width, height, xMin, xMax, yMin, yMax)
                    val zeroXpx = originOffset.x
                    val zeroYpx = originOffset.y

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
                        val screenPt = mathToScreen(currentX, 0.0, width, height, xMin, xMax, yMin, yMax)
                        val gx = screenPt.x
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
                        val screenPt = mathToScreen(0.0, currentY, width, height, xMin, xMax, yMin, yMax)
                        val gy = screenPt.y
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

                    // 3. CURVE DRAWING using mathToScreen
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
                            val screenPt = mathToScreen(x, y, width, height, xMin, xMax, yMin, yMax)
                            val px = screenPt.x
                            val py = screenPt.y
                            if (!px.isFinite() || !py.isFinite()) {
                                isFirst = true
                                continue
                            }

                            if (isFirst || abs(py - lastPy) > height * 0.45f) {
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

                    // 4. TWO-POINT MARKER OVERLAY (P1, P2 using mathToScreen)
                    fun drawPointMarker(
                        point: GraphPoint,
                        label: String,
                        markerColor: Color,
                        badgeTextColor: Color,
                        preferBadgeBelow: Boolean = false
                    ) {
                        val screenPt = mathToScreen(point.x, point.y, width, height, xMin, xMax, yMin, yMax)
                        val px = screenPt.x
                        val py = screenPt.y
                        if (!px.isFinite() || !py.isFinite()) return

                        val markerPos = Offset(px, py)
                        val crosshairPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                        if (px in 0f..width) {
                            drawLine(
                                color = markerColor.copy(alpha = 0.45f),
                                start = Offset(px, 0f),
                                end = Offset(px, height),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = crosshairPathEffect
                            )
                        }

                        if (py in 0f..height) {
                            drawLine(
                                color = markerColor.copy(alpha = 0.45f),
                                start = Offset(0f, py),
                                end = Offset(width, py),
                                strokeWidth = 1.2.dp.toPx(),
                                pathEffect = crosshairPathEffect
                            )
                        }

                        if (px in -20f..(width + 20f) && py in -20f..(height + 20f)) {
                            drawCircle(
                                color = markerColor.copy(alpha = 0.28f),
                                radius = 11.dp.toPx(),
                                center = markerPos
                            )
                            drawCircle(
                                color = markerColor,
                                radius = 6.dp.toPx(),
                                center = markerPos
                            )
                            drawCircle(
                                color = badgeTextColor,
                                radius = 2.4.dp.toPx(),
                                center = markerPos
                            )

                            val badgeStyle = TextStyle(
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                            val measuredBadge = textMeasurer.measure(label, style = badgeStyle)
                            val padH = 5.dp.toPx()
                            val padV = 2.dp.toPx()
                            val badgeW = measuredBadge.size.width + padH * 2f
                            val badgeH = measuredBadge.size.height + padV * 2f

                            val rawX = if (px + 10.dp.toPx() + badgeW <= width - 4f) {
                                px + 10.dp.toPx()
                            } else {
                                px - 10.dp.toPx() - badgeW
                            }
                            val rawY = if (!preferBadgeBelow && py - 8.dp.toPx() - badgeH >= 4f) {
                                py - 8.dp.toPx() - badgeH
                            } else if (py + 8.dp.toPx() + badgeH <= height - 4f) {
                                py + 8.dp.toPx()
                            } else {
                                py - 8.dp.toPx() - badgeH
                            }

                            val badgeX = rawX.coerceIn(4f, (width - badgeW - 4f).coerceAtLeast(4f))
                            val badgeY = rawY.coerceIn(4f, (height - badgeH - 4f).coerceAtLeast(4f))

                            drawRoundRect(
                                color = markerColor,
                                topLeft = Offset(badgeX, badgeY),
                                size = Size(badgeW, badgeH),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                            drawText(
                                textMeasurer = textMeasurer,
                                text = label,
                                topLeft = Offset(badgeX + padH, badgeY + padV),
                                style = badgeStyle
                            )
                        }
                    }

                    val p1 = point1
                    val p2 = point2
                    if (p1 != null && p2 != null) {
                        val pt1 = mathToScreen(p1.x, p1.y, width, height, xMin, xMax, yMin, yMax)
                        val pt2 = mathToScreen(p2.x, p2.y, width, height, xMin, xMax, yMin, yMax)
                        if (pt1.x.isFinite() && pt1.y.isFinite() && pt2.x.isFinite() && pt2.y.isFinite()) {
                            drawLine(
                                color = colorScheme.secondary.copy(alpha = 0.80f),
                                start = pt1,
                                end = pt2,
                                strokeWidth = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                            )
                        }
                    }

                    p1?.let {
                        drawPointMarker(
                            point = it,
                            label = "P1",
                            markerColor = point1Color,
                            badgeTextColor = point1BadgeTextColor
                        )
                    }
                    p2?.let {
                        drawPointMarker(
                            point = it,
                            label = "P2",
                            markerColor = point2Color,
                            badgeTextColor = point2BadgeTextColor,
                            preferBadgeBelow = p1 != null && abs(p1.x - it.x) < xSpan * 0.12
                        )
                    }
                }

                if (point1 == null || point2 == null) {
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
                                text = stringResource(
                                    if (point1 == null) R.string.graph_hint_scan else R.string.graph_hint_second_point
                                ),
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
                    val panLeftInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin - shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax - shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        interactionSource = panLeftInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(panLeftInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.graph_pan_left),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Pan Right (▶)
                    val panRightInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin + shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax + shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        interactionSource = panRightInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(panRightInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.graph_pan_right),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom In (+)
                    val zoomInInteraction = remember { MutableInteractionSource() }
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
                        interactionSource = zoomInInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(zoomInInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.graph_zoom_in),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom Out (-)
                    val zoomOutInteraction = remember { MutableInteractionSource() }
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
                        interactionSource = zoomOutInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(zoomOutInteraction)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.graph_zoom_out),
                            tint = graphColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset View (↺)
                    val resetInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { resetView() },
                        interactionSource = resetInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(resetInteraction)
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

            if (point1 != null || point2 != null) {
                Spacer(modifier = Modifier.height(8.dp))

                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("graph_trace_hud"),
                    glassState = glassState,
                    shape = RoundedCornerShape(24.dp),
                    customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedPreset.label} @ [${angleMode.name}]",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = graphColor
                            )

                            val clearPointsInteraction = remember { MutableInteractionSource() }
                            GlassSurface(
                                shape = RoundedCornerShape(50),
                                customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderStroke = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f)),
                                glassState = glassState,
                                modifier = Modifier.pressScale(clearPointsInteraction)
                            ) {
                                IconButton(
                                    onClick = {
                                        point1 = null
                                        point2 = null
                                        nextPointIndex = 1
                                        triggerClickHaptic()
                                    },
                                    interactionSource = clearPointsInteraction,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("btn_clear_graph_points")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.nav_close),
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        point1?.let { p1 ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.testTag("graph_point_1_info")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(point1Color)
                                )
                                Text(
                                    text = String.format(Locale.US, "P1:  x = %.3f  |  y = %.4f", p1.x, p1.y),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = colorScheme.onSurface
                                )
                            }
                        }

                        val p2 = point2
                        if (p2 != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.testTag("graph_point_2_info")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(point2Color)
                                )
                                Text(
                                    text = String.format(Locale.US, "P2:  x = %.3f  |  y = %.4f", p2.x, p2.y),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = colorScheme.onSurface
                                )
                            }

                            point1?.let { p1 ->
                                val dx = p2.x - p1.x
                                val dy = p2.y - p1.y
                                val distance = hypot(dx, dy)
                                val slopeText = if (abs(dx) > 1e-9) {
                                    String.format(Locale.US, "%.4f", dy / dx)
                                } else {
                                    "∞"
                                }
                                val distanceText = String.format(Locale.US, "%.4f", distance)
                                Text(
                                    text = stringResource(R.string.graph_points_analysis, slopeText, distanceText),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = graphColor,
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .testTag("graph_points_analysis")
                                )
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.graph_hint_second_point),
                                fontSize = 10.sp,
                                color = labelColor
                            )
                        }
                    }
                }
            }

            if (onApplyPointToCalculator != null) {
                Spacer(modifier = Modifier.height(10.dp))

                val activePoint = if (nextPointIndex == 1 && point2 != null) point2 else point1 ?: point2
                val hasActivePoint = activePoint != null

                val applyInteraction = remember { MutableInteractionSource() }
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .pressScale(applyInteraction, enabled = hasActivePoint)
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = applyInteraction,
                            indication = androidx.compose.material3.ripple(),
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
