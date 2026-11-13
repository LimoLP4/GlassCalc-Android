package com.limonatalp4.glasscalcproject.domain.evaluator

import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import org.mariuszgromada.math.mxparser.mXparser
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.*

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val messageResId: Int) : EvaluationResult()
}

private class EvalStepException(val messageResId: Int) : RuntimeException()

class ExpressionEvaluator {

    companion object {
        init {
            try {
                License.iConfirmNonCommercialUse("limonatalp4")
            } catch (_: Exception) {}
        }

        private val DECIMAL_FORMAT = DecimalFormat("#,##0.########", DecimalFormatSymbols(Locale.GERMANY)).apply {
            isGroupingUsed = false
        }
        private val SCIENTIFIC_FORMAT = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
    }

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): EvaluationResult {
        if (expression.isBlank()) {
            return EvaluationResult.Success(0.0, "0")
        }

        try {
            if (angleMode == AngleMode.DEG) {
                mXparser.setDegreesMode()
            } else {
                mXparser.setRadiansMode()
            }

            // Preprocess integrals (Simpson's rule), derivatives, and n-th roots
            val resolvedIntegrals = resolveIntegrals(expression, angleMode)
            val resolvedDerivatives = resolveDerivatives(resolvedIntegrals, angleMode)
            val resolvedNthRoots = resolveNthRoots(resolvedDerivatives)

            // Quick check for division by zero pattern after sanitizing
            val sanitized = sanitizeExpression(resolvedNthRoots)

            // Check if dividing by literal 0 (e.g. /0 or /0.0, without blocking /0.5)
            if (Regex("""/\s*0+(\.0+)?(?![\d.])""").containsMatchIn(sanitized)) {
                return EvaluationResult.Error(R.string.error_division_by_zero)
            }

            val e = Expression(sanitized)
            val result = e.calculate()

            if (result.isInfinite()) {
                return EvaluationResult.Error(R.string.error_division_by_zero)
            }

            if (result.isNaN()) {
                val err = e.errorMessage
                val userResId = if (!err.isNullOrBlank() && !err.contains("no errors", ignoreCase = true)) {
                    R.string.error_syntax
                } else {
                    R.string.error_not_defined
                }
                return EvaluationResult.Error(userResId)
            }

            val formatted = formatResult(result)
            return EvaluationResult.Success(result, formatted)
        } catch (ex: EvalStepException) {
            return EvaluationResult.Error(ex.messageResId)
        } catch (_: ArithmeticException) {
            return EvaluationResult.Error(R.string.error_calculation)
        } catch (_: IllegalArgumentException) {
            return EvaluationResult.Error(R.string.error_syntax)
        } catch (_: Exception) {
            return EvaluationResult.Error(R.string.error_invalid_expression)
        }
    }

    /**
     * Evaluates definite integrals using Composite Simpson's Rule:
     * - ∫(f(x); a; b) or int(f(x); a; b) -> variable defaults to x
     * - ∫(f(t); t; a; b) or int(f(t); t; a; b) -> custom variable
     * - ∫(f(x)) or int(f(x)) -> definite integral over [0, 1]
     */
    private fun resolveIntegrals(expr: String, angleMode: AngleMode): String {
        val sb = StringBuilder()
        var i = 0
        while (i < expr.length) {
            val tokenLen = when {
                expr.startsWith("∫(", i) -> 2
                expr.startsWith("int(", i) && (i == 0 || !expr[i - 1].isLetterOrDigit()) -> 4
                else -> 0
            }

            if (tokenLen > 0) {
                val openParen = i + tokenLen - 1
                val closeParen = findMatchingParen(expr, openParen)
                if (closeParen == -1) {
                    throw EvalStepException(R.string.error_syntax)
                }

                val inner = expr.substring(openParen + 1, closeParen)
                val args = splitTopLevelArgs(inner)
                val (integrandRaw, varName, lowerRaw, upperRaw) = when (args.size) {
                    4 -> Quad(args[0], args[1].trim(), args[2], args[3])
                    3 -> Quad(args[0], "x", args[1], args[2])
                    1 -> {
                        if (args[0].isBlank()) throw EvalStepException(R.string.error_syntax)
                        Quad(args[0], "x", "0", "1")
                    }
                    else -> throw EvalStepException(R.string.error_syntax)
                }

                if (integrandRaw.isBlank() || !Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$""").matches(varName)) {
                    throw EvalStepException(R.string.error_syntax)
                }

                val lowerVal = when (val lowerEval = evaluate(lowerRaw, angleMode)) {
                    is EvaluationResult.Success -> lowerEval.value
                    is EvaluationResult.Error -> throw EvalStepException(lowerEval.messageResId)
                }
                val upperVal = when (val upperEval = evaluate(upperRaw, angleMode)) {
                    is EvaluationResult.Success -> upperEval.value
                    is EvaluationResult.Error -> throw EvalStepException(upperEval.messageResId)
                }

                val resolvedInnerIntegrals = resolveIntegrals(integrandRaw, angleMode)
                val resolvedInnerDerivatives = resolveDerivatives(resolvedInnerIntegrals, angleMode)
                val resolvedInnerRoots = resolveNthRoots(resolvedInnerDerivatives)

                val integralValue = simpsonIntegrate(
                    integrandSanitized = sanitizeExpression(resolvedInnerRoots),
                    varName = varName,
                    a = lowerVal,
                    b = upperVal,
                    angleMode = angleMode
                )

                sb.append('(').append(integralValue).append(')')
                i = closeParen + 1
            } else {
                sb.append(expr[i])
                i++
            }
        }
        return sb.toString()
    }

    private fun simpsonIntegrate(
        integrandSanitized: String,
        varName: String,
        a: Double,
        b: Double,
        angleMode: AngleMode
    ): Double {
        if (!a.isFinite() || !b.isFinite()) throw EvalStepException(R.string.error_not_defined)
        if (a == b) return 0.0

        val lower = minOf(a, b)
        val upper = maxOf(a, b)
        val sign = if (a > b) -1.0 else 1.0

        val n = 1000 // Even number of subintervals for Simpson's rule
        val h = (upper - lower) / n

        if (angleMode == AngleMode.DEG) {
            mXparser.setDegreesMode()
        } else {
            mXparser.setRadiansMode()
        }

        val arg = Argument("$varName = 0")
        val expression = Expression(integrandSanitized, arg)

        fun evalAt(t: Double): Double {
            arg.argumentValue = t
            val v = expression.calculate()
            if (v.isNaN() || v.isInfinite()) {
                val err = expression.errorMessage
                val resId = if (!err.isNullOrBlank() && !err.contains("no errors", ignoreCase = true)) {
                    R.string.error_syntax
                } else {
                    R.string.error_not_defined
                }
                throw EvalStepException(resId)
            }
            return v
        }

        var sum = evalAt(lower) + evalAt(upper)
        for (k in 1 until n) {
            val xVal = lower + k * h
            val coeff = if (k % 2 == 0) 2.0 else 4.0
            sum += coeff * evalAt(xVal)
        }

        val result = sign * (h / 3.0) * sum
        return if (abs(result) < 1e-12) 0.0 else result
    }

    /**
     * Supports central-difference numerical derivative for:
     * - der(f(x); x0) / d/dx(f(x); x0)
     * - der(f(t); t; x0) / d/dx(f(t); t; x0)
     */
    private fun resolveDerivatives(expr: String, angleMode: AngleMode): String {
        val sb = StringBuilder()
        var i = 0
        while (i < expr.length) {
            val tokenLen = when {
                expr.startsWith("d/dx(", i) -> 5
                expr.startsWith("der(", i) && (i == 0 || !expr[i - 1].isLetterOrDigit()) -> 4
                else -> 0
            }

            if (tokenLen > 0) {
                val openParen = i + tokenLen - 1
                val closeParen = findMatchingParen(expr, openParen)
                if (closeParen == -1) {
                    throw EvalStepException(R.string.error_syntax)
                }

                val inner = expr.substring(openParen + 1, closeParen)
                val args = splitTopLevelArgs(inner)
                val (fnRaw, varName, pointRaw) = when (args.size) {
                    3 -> Triple(args[0], args[1].trim(), args[2])
                    2 -> Triple(args[0], "x", args[1])
                    else -> throw EvalStepException(R.string.error_syntax)
                }

                if (fnRaw.isBlank() || !Regex("""^[a-zA-Z_][a-zA-Z0-9_]*$""").matches(varName)) {
                    throw EvalStepException(R.string.error_syntax)
                }

                val x0 = when (val ptEval = evaluate(pointRaw, angleMode)) {
                    is EvaluationResult.Success -> ptEval.value
                    is EvaluationResult.Error -> throw EvalStepException(ptEval.messageResId)
                }

                val sanitizedFn = sanitizeExpression(resolveNthRoots(resolveDerivatives(fnRaw, angleMode)))
                if (angleMode == AngleMode.DEG) mXparser.setDegreesMode() else mXparser.setRadiansMode()

                val arg = Argument("$varName = 0")
                val expression = Expression(sanitizedFn, arg)
                val h = 1e-6

                arg.argumentValue = x0 + h
                val fPlus = expression.calculate()
                arg.argumentValue = x0 - h
                val fMinus = expression.calculate()

                if (fPlus.isNaN() || fPlus.isInfinite() || fMinus.isNaN() || fMinus.isInfinite()) {
                    throw EvalStepException(R.string.error_not_defined)
                }

                val deriv = (fPlus - fMinus) / (2.0 * h)
                val cleanedDeriv = if (abs(deriv) < 1e-11) 0.0 else deriv
                sb.append('(').append(cleanedDeriv).append(')')
                i = closeParen + 1
            } else {
                sb.append(expr[i])
                i++
            }
        }
        return sb.toString()
    }

    /**
     * Resolves n-th roots in both infix and prefix forms:
     * - Prefix: ⁿ√(n; x) -> ((x)^(1/(n)))
     * - Infix:  nⁿ√(x) or nⁿ√x or (expr)ⁿ√(x) -> ((x)^(1/(n)))
     */
    private fun resolveNthRoots(expr: String): String {
        var current = expr
        var guard = 0
        while (guard++ < 32) {
            val idx = current.indexOf("ⁿ√")
            if (idx == -1) break

            val afterToken = idx + 2
            if (afterToken >= current.length) {
                throw EvalStepException(R.string.error_syntax)
            }

            // Check if there is an index operand directly before ⁿ√
            val beforeOperandStart = findLeftOperandStart(current, idx)
            if (beforeOperandStart < idx) {
                val nExpr = current.substring(beforeOperandStart, idx)
                val (xExpr, afterRight) = extractRightOperand(current, afterToken)
                val replacement = "(($xExpr)^(1/($nExpr)))"
                current = current.substring(0, beforeOperandStart) + replacement + current.substring(afterRight)
            } else if (current[afterToken] == '(') {
                val closeParen = findMatchingParen(current, afterToken)
                if (closeParen == -1) throw EvalStepException(R.string.error_syntax)
                val inner = current.substring(afterToken + 1, closeParen)
                val args = splitTopLevelArgs(inner)
                if (args.size != 2 || args[0].isBlank() || args[1].isBlank()) {
                    throw EvalStepException(R.string.error_syntax)
                }
                val replacement = "((${args[1]})^(1/(${args[0]})))"
                current = current.substring(0, idx) + replacement + current.substring(closeParen + 1)
            } else {
                throw EvalStepException(R.string.error_syntax)
            }
        }
        return current
    }

    private fun findLeftOperandStart(expr: String, opStart: Int): Int {
        var pos = opStart - 1
        while (pos >= 0 && expr[pos] == ' ') pos--
        if (pos < 0) return opStart

        if (expr[pos] == ')') {
            var depth = 1
            pos--
            while (pos >= 0 && depth > 0) {
                when (expr[pos]) {
                    ')' -> depth++
                    '(' -> depth--
                }
                if (depth > 0) pos--
            }
            return if (depth == 0) pos else opStart
        }

        if (expr[pos].isDigit() || expr[pos] == ',' || expr[pos] == '.' || expr[pos] == 'π' || expr[pos] == 'e') {
            while (pos > 0 && (expr[pos - 1].isDigit() || expr[pos - 1] == ',' || expr[pos - 1] == '.')) {
                pos--
            }
            return pos
        }

        if (expr[pos].isLetter()) {
            while (pos > 0 && expr[pos - 1].isLetterOrDigit()) {
                pos--
            }
            return pos
        }

        return opStart
    }

    private fun extractRightOperand(expr: String, start: Int): Pair<String, Int> {
        var pos = start
        while (pos < expr.length && expr[pos] == ' ') pos++
        if (pos >= expr.length) throw EvalStepException(R.string.error_syntax)

        if (expr[pos] == '(') {
            val close = findMatchingParen(expr, pos)
            if (close == -1) throw EvalStepException(R.string.error_syntax)
            return expr.substring(pos + 1, close) to (close + 1)
        }

        var end = pos
        if (expr[end] == '-' || expr[end] == '−') end++
        while (end < expr.length && (expr[end].isLetterOrDigit() || expr[end] == ',' || expr[end] == '.' || expr[end] == 'π')) {
            end++
        }
        if (end == pos) throw EvalStepException(R.string.error_syntax)
        return expr.substring(pos, end) to end
    }

    private fun findMatchingParen(expr: String, openParenIndex: Int): Int {
        var depth = 1
        var i = openParenIndex + 1
        while (i < expr.length) {
            when (expr[i]) {
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

    private fun splitTopLevelArgs(inner: String): List<String> {
        val hasTopLevelSemicolon = containsTopLevelChar(inner, ';')
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var depth = 0

        for (idx in inner.indices) {
            val ch = inner[idx]
            when (ch) {
                '(' -> {
                    depth++
                    current.append(ch)
                }
                ')' -> {
                    depth--
                    current.append(ch)
                }
                ';' -> {
                    if (depth == 0) {
                        result.add(current.toString().trim())
                        current.clear()
                    } else {
                        current.append(ch)
                    }
                }
                ',' -> {
                    if (depth == 0 && !hasTopLevelSemicolon && !isDecimalComma(inner, idx)) {
                        result.add(current.toString().trim())
                        current.clear()
                    } else {
                        current.append(ch)
                    }
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    private fun containsTopLevelChar(text: String, target: Char): Boolean {
        var depth = 0
        for (ch in text) {
            when (ch) {
                '(' -> depth++
                ')' -> depth--
                target -> if (depth == 0) return true
            }
        }
        return false
    }

    private fun isDecimalComma(text: String, commaIndex: Int): Boolean {
        val prev = text.getOrNull(commaIndex - 1) ?: return false
        val next = text.getOrNull(commaIndex + 1) ?: return false
        return prev.isDigit() && next.isDigit()
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

    fun sanitizeExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("⁄", "/")
            .replace("−", "-")
            .replace(",", ".")
            .replace("π", "pi")
            .replace("e", "e")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("³√", "cbrt")
            .replace("√", "sqrt")
            .replace("arcsin", "asin")
            .replace("arccos", "acos")
            .replace("arctan", "atan")
            .replace("d/dx", "der")
            .replace("∫", "int")
            .replace("mod", "#")
            .replace("|", "abs")
            .replace(Regex("""(?<![a-zA-Z0-9_])log\("""), "log10(")
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        // Whole number check
        if (abs(value - round(value)) < 1e-9 && abs(value) < 1e12) {
            return round(value).toLong().toString()
        }
        // Huge or very tiny numbers
        if (abs(value) >= 1e12 || (abs(value) < 1e-6 && abs(value) > 0)) {
            return SCIENTIFIC_FORMAT.format(value)
        }
        return DECIMAL_FORMAT.format(value)
    }
}
