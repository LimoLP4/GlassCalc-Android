package com.limonatalp4.glasscalcproject.domain.evaluator

import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
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

            // Quick check for division by zero pattern before or after sanitizing
            val sanitized = sanitizeExpression(expression)
            
            // Check if dividing by literal 0 (e.g. /0 or /0.0)
            if (Regex("""/\s*0+(\.0+)?(?!\d)""").containsMatchIn(sanitized)) {
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
        } catch (_: ArithmeticException) {
            return EvaluationResult.Error(R.string.error_calculation)
        } catch (_: IllegalArgumentException) {
            return EvaluationResult.Error(R.string.error_syntax)
        } catch (_: Exception) {
            return EvaluationResult.Error(R.string.error_invalid_expression)
        }
    }

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
