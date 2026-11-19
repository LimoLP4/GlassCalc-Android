package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.semantics.Role
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.evaluator.MxParserLock
import com.limonatalp4.glasscalcproject.domain.evaluator.SafeMathEvaluator
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
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
    if (!x.isFinite() || !y.isFinite() || !width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) {
        return Offset(Float.NaN, Float.NaN)
    }
    val xSpan = xMax - xMin
    val ySpan = yMax - yMin
    if (!xSpan.isFinite() || !ySpan.isFinite()) {
        return Offset(Float.NaN, Float.NaN)
    }
    val pxDouble = if (xSpan > 1e-12) ((x - xMin) / xSpan) * width.toDouble() else 0.0
    val pyDouble = if (ySpan > 1e-12) height.toDouble() - (((y - yMin) / ySpan) * height.toDouble()) else 0.0
    if (!pxDouble.isFinite() || !pyDouble.isFinite()) {
        return Offset(Float.NaN, Float.NaN)
    }
    return Offset(pxDouble.toFloat(), pyDouble.toFloat())
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

private data class ParsedGraphIntegral(
    val integrand: String,
    val varName: String,
    val lowerExpr: String,
    val upperExpr: String
)

private fun findClosingParen(text: String, openParenIndex: Int): Int {
    var depth = 1
    var i = openParenIndex + 1
    while (i < text.length) {
        when (text[i]) {
            '(' -> depth++
            ')' -> {
                depth--
                if (depth == 0) return i
            }
        }
        i++
    }
    return -1
}

private fun splitTopLevelCommaArgs(inner: String): List<String> {
    val args = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    for (ch in inner) {
        when (ch) {
            '(' -> {
                depth++
                current.append(ch)
            }
            ')' -> {
                depth--
                current.append(ch)
            }
            ',' -> {
                if (depth == 0) {
                    args.add(current.toString().trim())
                    current.clear()
                } else {
                    current.append(ch)
                }
            }
            else -> current.append(ch)
        }
    }
    args.add(current.toString().trim())
    return args
}

private fun parseIntegralCall(inner: String): ParsedGraphIntegral? {
    val args = splitTopLevelCommaArgs(inner)
    val varPattern = Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$""")
    return when (args.size) {
        4 -> {
            if (args[0].isBlank() || !varPattern.matches(args[1])) null
            else ParsedGraphIntegral(args[0], args[1], args[2], args[3])
        }
        3 -> {
            if (args[0].isBlank()) null
            else ParsedGraphIntegral(args[0], "x", args[1], args[2])
        }
        1 -> {
            if (args[0].isBlank()) null
            else ParsedGraphIntegral(args[0], "x", "0", "1")
        }
        else -> null
    }
}

private fun extractTopLevelCumulativeIntegral(sanitizedExpr: String): ParsedGraphIntegral? {
    val trimmed = sanitizedExpr.trim()
    if (!trimmed.startsWith("int(") || !trimmed.endsWith(")")) return null
    if (findClosingParen(trimmed, 3) != trimmed.length - 1) return null
    val parsed = parseIntegralCall(trimmed.substring(4, trimmed.length - 1)) ?: return null
    if (!parsed.upperExpr.trim().equals("x", ignoreCase = true)) return null
    return parsed
}

/**
 * Resolves any bounded int(...) tokens at a given x using SafeMathEvaluator.integrateBounded,
 * strictly enforcing max 1000 subintervals, overflow checks, and pole/singularity detection.
 */
private fun resolveGraphIntegralsAtX(
    sanitizedExpr: String,
    x: Double,
    angleMode: AngleMode,
    checkActive: () -> Unit = {}
): String? {
    if (!sanitizedExpr.contains("int(")) return sanitizedExpr
    val sb = StringBuilder()
    var i = 0
    while (i < sanitizedExpr.length) {
        checkActive()
        if (sanitizedExpr.startsWith("int(", i) && (i == 0 || !sanitizedExpr[i - 1].isLetterOrDigit())) {
            val openParen = i + 3
            val closeParen = findClosingParen(sanitizedExpr, openParen)
            if (closeParen == -1) return null
            val parsed = parseIntegralCall(sanitizedExpr.substring(openParen + 1, closeParen)) ?: return null

            val resolvedInnerIntegrand = resolveGraphIntegralsAtX(parsed.integrand, x, angleMode, checkActive)
                ?: return null
            val resolvedLower = resolveGraphIntegralsAtX(parsed.lowerExpr, x, angleMode, checkActive)
                ?: return null
            val resolvedUpper = resolveGraphIntegralsAtX(parsed.upperExpr, x, angleMode, checkActive)
                ?: return null

            val lowerVal = SafeMathEvaluator.safeEvaluateWithVar(resolvedLower, "x", x, angleMode)
                ?: return null
            val upperVal = SafeMathEvaluator.safeEvaluateWithVar(resolvedUpper, "x", x, angleMode)
                ?: return null

            val integralValue = SafeMathEvaluator.integrateBounded(
                integrandSanitized = resolvedInnerIntegrand,
                varName = parsed.varName,
                a = lowerVal,
                b = upperVal,
                angleMode = angleMode,
                maxSteps = SafeMathEvaluator.MAX_INTEGRAL_STEPS,
                maxAbs = SafeMathEvaluator.MAX_SAFE_VALUE,
                checkActive = checkActive
            ) ?: return null

            sb.append('(').append(integralValue).append(')')
            i = closeParen + 1
        } else {
            sb.append(sanitizedExpr[i])
            i++
        }
    }
    return sb.toString()
}

internal fun evaluateFormulaAtX(
    formula: String,
    x: Double,
    angleMode: AngleMode,
    checkActive: () -> Unit = {}
): Double? {
    checkActive()
    if (!SafeMathEvaluator.isSafeValue(x)) return null
    val sanitizedExpr = sanitizeForGraphParser(formula)
    if (!isWithinDomain(sanitizedExpr, x)) return null

    return try {
        val resolvedExpr = resolveGraphIntegralsAtX(sanitizedExpr, x, angleMode, checkActive)
            ?: return null

        val lower = resolvedExpr.lowercase()
        val hasTrig = lower.contains("sin") || lower.contains("cos") || lower.contains("tan")
        val isDeriv = resolvedExpr.startsWith("der(")
        val effectiveAngleMode = if (angleMode == AngleMode.DEG && hasTrig && !isDeriv) {
            AngleMode.DEG
        } else {
            AngleMode.RAD
        }

        val y = SafeMathEvaluator.safeEvaluateWithVar(
            sanitizedExpression = resolvedExpr,
            varName = "x",
            x = x,
            angleMode = effectiveAngleMode,
            maxAbs = SafeMathEvaluator.MAX_SAFE_VALUE
        )
        if (y != null) return y

        // Numerical central difference fallback if mXparser returned null/NaN for derivative
        if (isDeriv) {
            val match = Regex("""der\((.+),\s*([a-zA-Z_][a-zA-Z0-9_]*)\)""").matchEntire(resolvedExpr)
            if (match != null) {
                val fn = match.groupValues[1]
                val varName = match.groupValues[2]
                val h = 1e-5
                val yCenter = SafeMathEvaluator.safeEvaluateWithVar(fn, varName, x, effectiveAngleMode)
                    ?: return null
                if (!SafeMathEvaluator.isSafeValue(yCenter)) return null
                val yPlus = SafeMathEvaluator.safeEvaluateWithVar(fn, varName, x + h, effectiveAngleMode)
                    ?: return null
                val yMinus = SafeMathEvaluator.safeEvaluateWithVar(fn, varName, x - h, effectiveAngleMode)
                    ?: return null
                val deriv = (yPlus - yMinus) / (2.0 * h)
                return if (SafeMathEvaluator.isSafeValue(deriv)) deriv else null
            }
        }
        null
    } catch (ce: CancellationException) {
        throw ce
    } catch (_: Throwable) {
        null
    }
}

internal fun computeGraphCurvePoints(
    preset: GraphPreset,
    expression: String,
    angleMode: AngleMode,
    xMin: Double,
    xMax: Double,
    checkActive: () -> Unit = {}
): List<Pair<Double, Double>> {
    checkActive()
    if (!SafeMathEvaluator.isSafeValue(xMin) || !SafeMathEvaluator.isSafeValue(xMax) || xMax <= xMin) {
        return emptyList()
    }
    val formulaString = resolveActiveFormula(preset, expression)
    val sanitizedExpr = sanitizeForGraphParser(formulaString)
    val xSpan = xMax - xMin
    if (!xSpan.isFinite() || xSpan <= 0.0) return emptyList()

    val cumulativeIntegral = extractTopLevelCumulativeIntegral(sanitizedExpr)
    if (cumulativeIntegral != null) {
        return computeCumulativeIntegralCurve(
            parsed = cumulativeIntegral,
            angleMode = angleMode,
            xMin = xMin,
            xMax = xMax,
            xSpan = xSpan,
            checkActive = checkActive
        )
    }

    val containsNonCumulativeIntegral = sanitizedExpr.contains("int(")
    val maxPoints = if (containsNonCumulativeIntegral) 180 else 1000
    val minPoints = if (containsNonCumulativeIntegral) 80 else 300
    val steps = (xSpan * 50).let { if (it.isFinite()) it.toInt() else maxPoints }
        .coerceIn(minPoints, maxPoints)

    val xSamples = ArrayList<Double>(steps + 5)
    for (i in 0..steps) {
        checkActive()
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

    if (containsNonCumulativeIntegral) {
        val calculatedList = ArrayList<Pair<Double, Double>>(xSamples.size)
        for (x in xSamples) {
            checkActive()
            val y = evaluateFormulaAtX(formulaString, x, angleMode, checkActive)
            calculatedList.add(Pair(x, y ?: Double.NaN))
        }
        return calculatedList
    }

    val calculatedList = ArrayList<Pair<Double, Double>>(xSamples.size)
    val isDeriv = sanitizedExpr.startsWith("der(")
    val derivMatch = if (isDeriv) Regex("""der\((.+),\s*([a-zA-Z_][a-zA-Z0-9_]*)\)""").matchEntire(sanitizedExpr) else null
    val hasTrig = lowerSanitized.contains("sin") || lowerSanitized.contains("cos") || lowerSanitized.contains("tan")
    val effectiveAngleMode = if (angleMode == AngleMode.DEG && hasTrig && !isDeriv) AngleMode.DEG else AngleMode.RAD
    val h = 1e-5
    val batchSize = 30

    for (startIdx in xSamples.indices step batchSize) {
        checkActive()
        val endIdx = minOf(startIdx + batchSize, xSamples.size)
        synchronized(MxParserLock) {
            SafeMathEvaluator.applyAngleMode(effectiveAngleMode)

            val argX = Argument("x = 0")
            val expr = Expression(sanitizedExpr, argX)
            val subArg = derivMatch?.let { Argument("${it.groupValues[2]} = 0") }
            val subExpr = derivMatch?.let { Expression(it.groupValues[1], subArg) }

            for (idx in startIdx until endIdx) {
                val x = xSamples[idx]
                if (!SafeMathEvaluator.isSafeValue(x) || !isWithinDomain(sanitizedExpr, x)) {
                    calculatedList.add(Pair(x, Double.NaN))
                    continue
                }

                argX.argumentValue = x
                var y = SafeMathEvaluator.safeCalculate(expr)

                if (y == null && subExpr != null && subArg != null) {
                    subArg.argumentValue = x
                    val yCenter = SafeMathEvaluator.safeCalculate(subExpr)
                    if (yCenter != null) {
                        subArg.argumentValue = x + h
                        val yPlus = SafeMathEvaluator.safeCalculate(subExpr)
                        subArg.argumentValue = x - h
                        val yMinus = SafeMathEvaluator.safeCalculate(subExpr)
                        if (yPlus != null && yMinus != null) {
                            val deriv = (yPlus - yMinus) / (2.0 * h)
                            if (SafeMathEvaluator.isSafeValue(deriv)) {
                                y = deriv
                            }
                        }
                    }
                }

                calculatedList.add(Pair(x, y ?: Double.NaN))
            }
        }
    }
    return calculatedList
}

/**
 * Bounded O(N) adaptive cumulative integral curve sampler (max 600 steps) that guards against
 * poles, singularities, overflow, and rapid pan/zoom ANRs.
 */
private fun computeCumulativeIntegralCurve(
    parsed: ParsedGraphIntegral,
    angleMode: AngleMode,
    xMin: Double,
    xMax: Double,
    xSpan: Double,
    checkActive: () -> Unit
): List<Pair<Double, Double>> {
    checkActive()
    val lowerStr = parsed.lowerExpr.lowercase()
    val integrandLower = parsed.integrand.lowercase()
    val hasTrig = integrandLower.contains("sin") || integrandLower.contains("cos") || integrandLower.contains("tan") ||
        lowerStr.contains("sin") || lowerStr.contains("cos") || lowerStr.contains("tan")
    val effectiveAngleMode = if (angleMode == AngleMode.DEG && hasTrig) AngleMode.DEG else AngleMode.RAD

    val a = SafeMathEvaluator.safeEvaluate(parsed.lowerExpr, effectiveAngleMode) ?: return emptyList()
    if (!SafeMathEvaluator.isSafeValue(a)) return emptyList()

    val steps = (xSpan * 30).let { if (it.isFinite()) it.toInt() else 600 }.coerceIn(200, 600)
    val xSamples = ArrayList<Double>(steps + 1)
    for (i in 0..steps) {
        checkActive()
        xSamples.add(xMin + (i.toDouble() / steps) * xSpan)
    }

    val yValues = DoubleArray(xSamples.size) { Double.NaN }
    val splitIdx = xSamples.indexOfFirst { it >= a }.let { if (it == -1) xSamples.size else it }

    fun integrateDirection(indices: IntProgression) {
        if (indices.isEmpty()) return
        var started = false
        var prevX = a
        var runningIntegral = 0.0
        var diverged = false

        val indexList = indices.toList()
        val batchSize = 30
        for (start in indexList.indices step batchSize) {
            checkActive()
            val end = minOf(start + batchSize, indexList.size)
            for (pos in start until end) {
                checkActive()
                val sampleIdx = indexList[pos]
                val targetX = xSamples[sampleIdx]
                if (diverged || !SafeMathEvaluator.isSafeValue(targetX)) {
                    yValues[sampleIdx] = Double.NaN
                    continue
                }

                if (!started) {
                    val anchor = SafeMathEvaluator.integrateBounded(
                        integrandSanitized = parsed.integrand,
                        varName = parsed.varName,
                        a = a,
                        b = targetX,
                        angleMode = effectiveAngleMode,
                        maxSteps = 400,
                        maxAbs = SafeMathEvaluator.MAX_SAFE_VALUE,
                        checkActive = checkActive
                    )
                    if (anchor == null) {
                        diverged = true
                        yValues[sampleIdx] = Double.NaN
                    } else {
                        runningIntegral = anchor
                        prevX = targetX
                        started = true
                        yValues[sampleIdx] = anchor
                    }
                    continue
                }

                val dx = targetX - prevX
                if (!dx.isFinite() || dx == 0.0) {
                    yValues[sampleIdx] = runningIntegral
                    continue
                }

                val segValid = synchronized(MxParserLock) {
                    SafeMathEvaluator.applyAngleMode(effectiveAngleMode)
                    val arg = Argument("${parsed.varName} = 0")
                    val expr = Expression(parsed.integrand, arg)

                    arg.argumentValue = prevX
                    val y0 = SafeMathEvaluator.safeCalculate(expr) ?: return@synchronized false
                    val midX = 0.5 * (prevX + targetX)
                    arg.argumentValue = midX
                    val yMid = SafeMathEvaluator.safeCalculate(expr) ?: return@synchronized false
                    arg.argumentValue = targetX
                    val y1 = SafeMathEvaluator.safeCalculate(expr) ?: return@synchronized false

                    if (SafeMathEvaluator.hasPoleBetweenSamples(expr, arg, prevX, y0, midX, yMid) ||
                        SafeMathEvaluator.hasPoleBetweenSamples(expr, arg, midX, yMid, targetX, y1)
                    ) {
                        return@synchronized false
                    }

                    val seg = (dx / 6.0) * (y0 + 4.0 * yMid + y1)
                    if (!SafeMathEvaluator.isSafeValue(seg)) return@synchronized false
                    val nextIntegral = runningIntegral + seg
                    if (!SafeMathEvaluator.isSafeValue(nextIntegral)) return@synchronized false
                    runningIntegral = nextIntegral
                    true
                }

                if (!segValid) {
                    diverged = true
                    yValues[sampleIdx] = Double.NaN
                } else {
                    prevX = targetX
                    yValues[sampleIdx] = runningIntegral
                }
            }
        }
    }

    if (splitIdx < xSamples.size) {
        integrateDirection(splitIdx until xSamples.size)
    }
    if (splitIdx > 0) {
        integrateDirection((splitIdx - 1) downTo 0)
    }

    return xSamples.indices.map { i -> Pair(xSamples[i], yValues[i]) }
}

@Composable
fun FunctionGraphView(
    expression: String,
    angleMode: AngleMode,
    isGestureLocked: Boolean,
    onGestureLockChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    canvasHeight: Dp? = null,
    glassState: LiquidGlassState? = null,
    onApplyPointToCalculator: ((Double, Double) -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassProps = LocalGlassProperties.current
    val isTiefschwarz = glassProps.isTiefschwarz
    val localContext = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val windowContainerSize = LocalWindowInfo.current.containerSize
    val windowWidthDp = with(density) { windowContainerSize.width.toDp() }
    val windowHeightDp = with(density) { windowContainerSize.height.toDp() }
    val isCompactVertical = windowHeightDp < 500.dp || windowWidthDp > windowHeightDp
    val resolvedCanvasHeight = canvasHeight ?: if (isCompactVertical) {
        (windowHeightDp * 0.36f).coerceIn(130.dp, 190.dp)
    } else {
        230.dp
    }
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
    var lastCurveSource by remember { mutableStateOf<Triple<GraphPreset, String, AngleMode>?>(null) }

    // Two manual inspected points (P1, P2) at mathematical (x, y)
    var point1 by remember { mutableStateOf<GraphPoint?>(null) }
    var point2 by remember { mutableStateOf<GraphPoint?>(null) }
    var nextPointIndex by remember { mutableIntStateOf(1) }
    var lastHapticTime by remember { mutableLongStateOf(0L) }

    val currentPreset by rememberUpdatedState(selectedPreset)
    val currentExpression by rememberUpdatedState(expression)
    val currentAngleMode by rememberUpdatedState(angleMode)
    val currentIsGraphLocked by rememberUpdatedState(isGraphLocked)
    val currentHapticEnabled by rememberUpdatedState(hapticEnabled)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)
    val coroutineScope = rememberCoroutineScope()
    var tapEvalJob by remember { mutableStateOf<Job?>(null) }
    var curveEvalJob by remember { mutableStateOf<Job?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            curveEvalJob?.cancel()
            tapEvalJob?.cancel()
        }
    }

    // Auto-bounds and point reset on preset or angleMode change
    LaunchedEffect(selectedPreset, angleMode) {
        tapEvalJob?.cancel()
        curveEvalJob?.cancel()
        val bounds = defaultBoundsForPreset(selectedPreset, angleMode, expression)
        xMin = bounds.xMin
        xMax = bounds.xMax
        yMin = bounds.yMin
        yMax = bounds.yMax
        point1 = null
        point2 = null
        nextPointIndex = 1
    }

    // 4. CURVE DRAWING: Asynchronous sampling on Dispatchers.Default with explicit Job cancellation
    LaunchedEffect(selectedPreset, expression, angleMode, xMin, xMax) {
        val curveSource = Triple(selectedPreset, expression, angleMode)
        if (currentIsGraphLocked && cachedPoints.isNotEmpty() && lastCurveSource == curveSource) {
            return@LaunchedEffect
        }

        curveEvalJob?.cancel()
        val capturedXMin = xMin
        val capturedXMax = xMax
        val job = coroutineScope.launch(Dispatchers.Default) {
            try {
                val computedPoints = computeGraphCurvePoints(
                    preset = selectedPreset,
                    expression = expression,
                    angleMode = angleMode,
                    xMin = capturedXMin,
                    xMax = capturedXMax,
                    checkActive = {
                        ensureActive()
                        if (currentIsGraphLocked && cachedPoints.isNotEmpty() && lastCurveSource == curveSource) {
                            throw CancellationException("Graph locked")
                        }
                    }
                )
                ensureActive()
                withContext(Dispatchers.Main.immediate) {
                    if (!currentIsGraphLocked || cachedPoints.isEmpty() || lastCurveSource != curveSource) {
                        cachedPoints = computedPoints
                        lastCurveSource = curveSource
                    }
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Throwable) {
                // Keep UI stable if evaluation fails
            }
        }
        curveEvalJob = job
    }

    fun resetView() {
        if (currentIsGraphLocked) return
        tapEvalJob?.cancel()
        curveEvalJob?.cancel()
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

    // 2. CORRECT CALCULATION OF POINTS (P1/P2): X from tap using screenToMath, Y strictly from function evaluation on Dispatchers.Default
    fun tryPlacePointAtTap(tapOffset: Offset, canvasWidth: Float, canvasHeight: Float) {
        if (currentIsGraphLocked || canvasWidth <= 0f || canvasHeight <= 0f) return

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
        if (!SafeMathEvaluator.isSafeValue(tappedX)) return
        val formula = resolveActiveFormula(currentPreset, currentExpression)
        val mode = currentAngleMode

        tapEvalJob?.cancel()
        tapEvalJob = coroutineScope.launch {
            try {
                val evaluatedY = withContext(Dispatchers.Default) {
                    evaluateFormulaAtX(formula, tappedX, mode) {
                        ensureActive()
                    }
                }

                if (!currentIsGraphLocked && evaluatedY != null && SafeMathEvaluator.isSafeValue(evaluatedY)) {
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
            } catch (ce: CancellationException) {
                throw ce
            } catch (_: Throwable) {
                // Schutz gegen unerwartete Rechen- oder Zustandsfehler beim Punktsetzen
            }
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
                .padding(
                    horizontal = 14.dp,
                    vertical = if (isCompactVertical) 10.dp else 14.dp
                )
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
                        val onPresetClick = remember(preset) {
                            {
                                selectedPreset = preset
                                HapticUtil.performAppHaptic(
                                    localContext,
                                    view,
                                    currentHapticEnabled,
                                    currentHapticIntensity
                                )
                            }
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = onPresetClick,
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
                        try {
                            val nextLocked = !isGraphLocked
                            isGraphLocked = nextLocked
                            onGestureLockChange(nextLocked)
                            triggerClickHaptic()
                        } catch (_: Throwable) {
                            // Schutz gegen Abstürze oder Blockaden beim Umschalten des Schlosses
                        }
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

            // Main Canvas Plot Area with 2D Panning & Zooming (komplett deaktiviert wenn gesperrt)
            val gestureModifier = if (!isGraphLocked) {
                Modifier
                    .pointerInput(Unit) {
                        try {
                            detectTapGestures(
                                onTap = { offset ->
                                    if (!currentIsGraphLocked) {
                                        tryPlacePointAtTap(offset, size.width.toFloat(), size.height.toFloat())
                                    }
                                },
                                onLongPress = { offset ->
                                    if (!currentIsGraphLocked) {
                                        tryPlacePointAtTap(offset, size.width.toFloat(), size.height.toFloat())
                                    }
                                }
                            )
                        } catch (ce: CancellationException) {
                            throw ce
                        } catch (_: Throwable) {
                        }
                    }
                    .pointerInput(Unit) {
                        try {
                            detectTransformGestures { _, pan, zoom, _ ->
                                if (currentIsGraphLocked) return@detectTransformGestures
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                if (width > 0f && height > 0f && pan.x.isFinite() && pan.y.isFinite()) {
                                    val xSpan = (xMax - xMin).coerceIn(1e-4, 1e6)
                                    val ySpan = (yMax - yMin).coerceIn(1e-4, 1e6)

                                    val dx = -(pan.x / width) * xSpan
                                    val dy = (pan.y / height) * ySpan

                                    if (dx.isFinite() && dy.isFinite()) {
                                        val minDx = -1e7 - xMin
                                        val maxDx = 1e7 - xMax
                                        val clampedDx = if (minDx <= maxDx) dx.coerceIn(minDx, maxDx) else 0.0
                                        val minDy = -1e7 - yMin
                                        val maxDy = 1e7 - yMax
                                        val clampedDy = if (minDy <= maxDy) dy.coerceIn(minDy, maxDy) else 0.0
                                        xMin += clampedDx
                                        xMax += clampedDx
                                        yMin += clampedDy
                                        yMax += clampedDy
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
                        } catch (ce: CancellationException) {
                            throw ce
                        } catch (_: Throwable) {
                        }
                    }
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(resolvedCanvasHeight)
                    .heightIn(max = if (isCompactVertical) 190.dp else 230.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isTiefschwarz) Color.Black else colorScheme.surfaceContainerLow)
                    .border(
                        glassProps.containerBorderStroke,
                        RoundedCornerShape(16.dp)
                    )
                    .then(gestureModifier)
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

                    // 3. CURVE DRAWING using mathToScreen with Skia-safe clamping and pole discontinuity breaks
                    if (cachedPoints.isNotEmpty()) {
                        val path = Path()
                        var isFirst = true
                        var hasPathData = false
                        var lastPy = 0f
                        var lastRawPy = 0f
                        var lastMathY = 0.0
                        val count = cachedPoints.size
                        val safeMinX = -maxOf(width, 500f) * 2f
                        val safeMaxX = width + maxOf(width, 500f) * 2f
                        val safeMinY = -maxOf(height, 500f) * 2f
                        val safeMaxY = height + maxOf(height, 500f) * 2f

                        for (i in 0 until count) {
                            val (x, y) = cachedPoints[i]
                            if (!SafeMathEvaluator.isSafeValue(x) || !SafeMathEvaluator.isSafeValue(y)) {
                                isFirst = true
                                continue
                            }
                            val screenPt = mathToScreen(x, y, width, height, xMin, xMax, yMin, yMax)
                            val rawPx = screenPt.x
                            val rawPy = screenPt.y
                            if (!rawPx.isFinite() || !rawPy.isFinite()) {
                                isFirst = true
                                continue
                            }

                            val px = rawPx.coerceIn(safeMinX, safeMaxX)
                            val py = rawPy.coerceIn(safeMinY, safeMaxY)
                            if (!px.isFinite() || !py.isFinite()) {
                                isFirst = true
                                continue
                            }

                            val crossedSignFlipPole = !isFirst &&
                                ((lastMathY > 0.0 && y < 0.0) || (lastMathY < 0.0 && y > 0.0)) &&
                                (abs(py - lastPy) > height * 0.45f || abs(rawPy - lastRawPy) > height * 0.8f)

                            val largeDiscontinuity = !isFirst && abs(py - lastPy) > height * 0.45f
                            val bothOffScreenSameSide = !isFirst &&
                                ((lastRawPy < -height && rawPy < -height) || (lastRawPy > height * 2f && rawPy > height * 2f))

                            if (isFirst || crossedSignFlipPole || largeDiscontinuity || bothOffScreenSameSide) {
                                path.moveTo(px, py)
                                isFirst = false
                            } else {
                                path.lineTo(px, py)
                                hasPathData = true
                            }
                            lastPy = py
                            lastRawPy = rawPy
                            lastMathY = y
                        }

                        if (hasPathData) {
                            drawPath(
                                path = path,
                                color = graphColor,
                                style = Stroke(width = strokeWidth)
                            )
                        }
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
                        val maxCoord = maxOf(width, height, 500f) * 3f
                        if (pt1.x.isFinite() && pt1.y.isFinite() && pt2.x.isFinite() && pt2.y.isFinite()) {
                            val clampedPt1 = Offset(
                                pt1.x.coerceIn(-maxCoord, width + maxCoord),
                                pt1.y.coerceIn(-maxCoord, height + maxCoord)
                            )
                            val clampedPt2 = Offset(
                                pt2.x.coerceIn(-maxCoord, width + maxCoord),
                                pt2.y.coerceIn(-maxCoord, height + maxCoord)
                            )
                            drawLine(
                                color = colorScheme.secondary.copy(alpha = 0.80f),
                                start = clampedPt1,
                                end = clampedPt2,
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

                val controlTint = if (isGraphLocked) graphColor.copy(alpha = 0.30f) else graphColor
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pan Left (◀)
                    val panLeftInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            if (currentIsGraphLocked) return@IconButton
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin - shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax - shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        enabled = !isGraphLocked,
                        interactionSource = panLeftInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(panLeftInteraction, enabled = !isGraphLocked)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.graph_pan_left),
                            tint = controlTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Pan Right (▶)
                    val panRightInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            if (currentIsGraphLocked) return@IconButton
                            val shift = (xMax - xMin) * 0.25
                            if (shift.isFinite()) {
                                xMin = (xMin + shift).coerceIn(-1e7, 1e7)
                                xMax = (xMax + shift).coerceIn(-1e7, 1e7)
                            }
                            triggerClickHaptic()
                        },
                        enabled = !isGraphLocked,
                        interactionSource = panRightInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(panRightInteraction, enabled = !isGraphLocked)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.graph_pan_right),
                            tint = controlTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom In (+)
                    val zoomInInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            if (currentIsGraphLocked) return@IconButton
                            val xRange = ((xMax - xMin) * 0.75).coerceIn(1e-4, 1e6)
                            val xCenter = ((xMin + xMax) / 2).coerceIn(-1e7, 1e7)
                            xMin = (xCenter - xRange / 2).coerceIn(-1e7, 1e7)
                            xMax = (xCenter + xRange / 2).coerceIn(-1e7, 1e7)
                            val yRange = ((yMax - yMin) * 0.75).coerceIn(1e-4, 1e6)
                            val yCenter = ((yMin + yMax) / 2).coerceIn(-1e7, 1e7)
                            yMin = (yCenter - yRange / 2).coerceIn(-1e7, 1e7)
                            yMax = (yCenter + yRange / 2).coerceIn(-1e7, 1e7)
                            triggerClickHaptic()
                        },
                        enabled = !isGraphLocked,
                        interactionSource = zoomInInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(zoomInInteraction, enabled = !isGraphLocked)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.graph_zoom_in),
                            tint = controlTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Zoom Out (-)
                    val zoomOutInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = {
                            if (currentIsGraphLocked) return@IconButton
                            val xRange = ((xMax - xMin) * 1.35).coerceIn(1e-4, 1e6)
                            val xCenter = ((xMin + xMax) / 2).coerceIn(-1e7, 1e7)
                            xMin = (xCenter - xRange / 2).coerceIn(-1e7, 1e7)
                            xMax = (xCenter + xRange / 2).coerceIn(-1e7, 1e7)
                            val yRange = ((yMax - yMin) * 1.35).coerceIn(1e-4, 1e6)
                            val yCenter = ((yMin + yMax) / 2).coerceIn(-1e7, 1e7)
                            yMin = (yCenter - yRange / 2).coerceIn(-1e7, 1e7)
                            yMax = (yCenter + yRange / 2).coerceIn(-1e7, 1e7)
                            triggerClickHaptic()
                        },
                        enabled = !isGraphLocked,
                        interactionSource = zoomOutInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(zoomOutInteraction, enabled = !isGraphLocked)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = stringResource(R.string.graph_zoom_out),
                            tint = controlTint,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Reset View (↺)
                    val resetInteraction = remember { MutableInteractionSource() }
                    IconButton(
                        onClick = { resetView() },
                        enabled = !isGraphLocked,
                        interactionSource = resetInteraction,
                        modifier = Modifier
                            .size(28.dp)
                            .pressScale(resetInteraction, enabled = !isGraphLocked)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.graph_reset),
                            tint = colorScheme.onSurface.copy(alpha = if (isGraphLocked) 0.25f else 0.70f),
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
                    customBgColor = if (isTiefschwarz) Color(0xFF141414).copy(alpha = 0.85f) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke
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
                                customBgColor = if (isTiefschwarz) Color(0xFF1C1C1E).copy(alpha = 0.85f) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderStroke = glassProps.containerBorderStroke,
                                glassState = glassState,
                                modifier = Modifier.pressScale(clearPointsInteraction)
                            ) {
                                IconButton(
                                    onClick = {
                                        tapEvalJob?.cancel()
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
                        .testTag("btn_apply_graph_point"),
                    glassState = glassState,
                    shape = RoundedCornerShape(50),
                    borderStroke = if (hasActivePoint) {
                        BorderStroke(1.dp, colorScheme.onPrimary.copy(alpha = 0.25f))
                    } else {
                        glassProps.containerBorderStroke
                    },
                    customBgColor = if (hasActivePoint) {
                        colorScheme.primary.copy(alpha = 0.90f)
                    } else {
                        colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    },
                    onClick = {
                        activePoint?.let { pt ->
                            triggerClickHaptic()
                            onApplyPointToCalculator(pt.x, pt.y)
                        }
                    },
                    enabled = hasActivePoint,
                    role = Role.Button,
                    interactionSource = applyInteraction
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
