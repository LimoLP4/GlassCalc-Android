package com.limonatalp4.glasscalcproject.domain.evaluator

import com.limonatalp4.glasscalcproject.data.model.AngleMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack
import kotlin.math.*

sealed class EvaluationResult {
    data class Success(val value: Double, val formatted: String) : EvaluationResult()
    data class Error(val message: String) : EvaluationResult()
}

class ExpressionEvaluator {

    companion object {
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
            val sanitized = sanitizeExpression(expression)
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return EvaluationResult.Success(0.0, "0")

            val rpn = shuntingYard(tokens)
            val result = evaluateRpn(rpn, angleMode)

            if (result.isNaN()) {
                return EvaluationResult.Error("Nicht definiert")
            }
            if (result.isInfinite()) {
                return EvaluationResult.Error("Division durch 0")
            }

            val formatted = formatResult(result)
            return EvaluationResult.Success(result, formatted)
        } catch (e: ArithmeticException) {
            return EvaluationResult.Error(e.message ?: "Berechnungsfehler")
        } catch (e: IllegalArgumentException) {
            return EvaluationResult.Error(e.message ?: "Syntaxfehler")
        } catch (_: Exception) {
            return EvaluationResult.Error("Ungültiger Ausdruck")
        }
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(",", ".")
            .replace("π", "${Math.PI}")
            .replace("e", "${Math.E}")
            .replace("³√", "cbrt")
            .replace("√", "sqrt")
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        var prevTokenType: TokenType? = null

        while (i < expr.length) {
            val c = expr[i]

            // Numbers
            if (c.isDigit() || c == '.') {
                val sb = StringBuilder()
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                    sb.append(expr[i])
                    i++
                }
                tokens.add(sb.toString())
                prevTokenType = TokenType.NUMBER
                continue
            }

            // Word functions: sin, cos, tan, log, ln, sqrt, cbrt
            if (c.isLetter()) {
                val sb = StringBuilder()
                while (i < expr.length && expr[i].isLetter()) {
                    sb.append(expr[i])
                    i++
                }
                val word = sb.toString()
                tokens.add(word)
                prevTokenType = TokenType.FUNCTION
                continue
            }

            // Check for unary minus vs binary minus
            if (c == '-') {
                val isUnary = prevTokenType == null ||
                        prevTokenType == TokenType.OPERATOR ||
                        prevTokenType == TokenType.LEFT_PAREN ||
                        prevTokenType == TokenType.FUNCTION
                if (isUnary) {
                    tokens.add("u-")
                    prevTokenType = TokenType.OPERATOR
                    i++
                    continue
                }
            }

            // Operators & Parens
            when (c) {
                '+', '-', '*', '/', '^', '!' -> {
                    tokens.add(c.toString())
                    prevTokenType = TokenType.OPERATOR
                }
                '%' -> {
                    tokens.add("%")
                    prevTokenType = TokenType.OPERATOR
                }
                '(' -> {
                    // Implicit multiplication like 2(3) -> 2 * (3)
                    if (prevTokenType == TokenType.NUMBER || prevTokenType == TokenType.RIGHT_PAREN) {
                        tokens.add("*")
                    }
                    tokens.add("(")
                    prevTokenType = TokenType.LEFT_PAREN
                }
                ')' -> {
                    tokens.add(")")
                    prevTokenType = TokenType.RIGHT_PAREN
                }
                else -> {
                    // ignore unmapped character
                }
            }
            i++
        }
        return tokens
    }

    private enum class TokenType {
        NUMBER, OPERATOR, FUNCTION, LEFT_PAREN, RIGHT_PAREN
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "+", "-" -> 1
            "*", "/" -> 2
            "u-" -> 3
            "^" -> 4
            "!", "%" -> 5
            else -> 0
        }
    }

    private fun isRightAssociative(op: String): Boolean {
        return op == "^" || op == "u-"
    }

    private fun isFunction(token: String): Boolean {
        return token in listOf("sin", "cos", "tan", "log", "ln", "sqrt", "cbrt")
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val opStack = Stack<String>()

        for (token in tokens) {
            when {
                // Token is number
                token.toDoubleOrNull() != null -> {
                    output.add(token)
                }
                isFunction(token) -> {
                    opStack.push(token)
                }
                token == "(" -> {
                    opStack.push(token)
                }
                token == ")" -> {
                    while (opStack.isNotEmpty() && opStack.peek() != "(") {
                        output.add(opStack.pop())
                    }
                    if (opStack.isNotEmpty() && opStack.peek() == "(") {
                        opStack.pop()
                    }
                    if (opStack.isNotEmpty() && isFunction(opStack.peek())) {
                        output.add(opStack.pop())
                    }
                }
                else -> {
                    // Operator
                    while (opStack.isNotEmpty() && opStack.peek() != "(" &&
                        ((!isRightAssociative(token) && precedence(token) <= precedence(opStack.peek())) ||
                                (isRightAssociative(token) && precedence(token) < precedence(opStack.peek())))
                    ) {
                        output.add(opStack.pop())
                    }
                    opStack.push(token)
                }
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.pop()
            if (top != "(" && top != ")") {
                output.add(top)
            }
        }

        return output
    }

    private fun evaluateRpn(rpn: List<String>, angleMode: AngleMode): Double {
        val stack = Stack<Double>()

        for (token in rpn) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.push(num)
                continue
            }

            if (token == "u-") {
                if (stack.isEmpty()) throw IllegalArgumentException("Ungültiges Vorzeichen")
                stack.push(-stack.pop())
                continue
            }

            if (token == "!") {
                if (stack.isEmpty()) throw IllegalArgumentException("Fehlender Wert für Fakultät")
                val v = stack.pop()
                if (v < 0 || floor(v) != v || v > 170) {
                    throw ArithmeticException("Fakultät nur für ganze Zahlen [0..170]")
                }
                stack.push(factorial(v.toInt()))
                continue
            }

            if (token == "%") {
                if (stack.isEmpty()) throw IllegalArgumentException("Fehlender Prozentwert")
                val v = stack.pop()
                stack.push(v / 100.0)
                continue
            }

            if (isFunction(token)) {
                if (stack.isEmpty()) throw IllegalArgumentException("Fehlendes Argument für $token")
                val arg = stack.pop()
                val res = when (token) {
                    "sin" -> {
                        val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        sin(rad)
                    }
                    "cos" -> {
                        val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        cos(rad)
                    }
                    "tan" -> {
                        val rad = if (angleMode == AngleMode.DEG) Math.toRadians(arg) else arg
                        val cosVal = cos(rad)
                        if (abs(cosVal) < 1e-12) throw ArithmeticException("tan ist an Polstellen nicht definiert")
                        tan(rad)
                    }
                    "log" -> {
                        if (arg <= 0) throw ArithmeticException("log Argument muss > 0 sein")
                        log10(arg)
                    }
                    "ln" -> {
                        if (arg <= 0) throw ArithmeticException("ln Argument muss > 0 sein")
                        ln(arg)
                    }
                    "sqrt" -> {
                        if (arg < 0) throw ArithmeticException("Negative Wurzel nicht reell")
                        sqrt(arg)
                    }
                    "cbrt" -> cbrt(arg)
                    else -> throw IllegalArgumentException("Unbekannte Funktion $token")
                }
                stack.push(res)
                continue
            }

            // Binary operators
            if (stack.size < 2) throw IllegalArgumentException("Unvollständiger Ausdruck")
            val b = stack.pop()
            val a = stack.pop()

            val res = when (token) {
                "+" -> a + b
                "-" -> a - b
                "*" -> a * b
                "/" -> {
                    if (abs(b) < 1e-15) throw ArithmeticException("Division durch 0 nicht möglich")
                    a / b
                }
                "^" -> a.pow(b)
                else -> throw IllegalArgumentException("Unbekannter Operator $token")
            }
            stack.push(res)
        }

        if (stack.isEmpty()) return 0.0
        return stack.pop()
    }

    private fun factorial(n: Int): Double {
        var res = 1.0
        for (i in 2..n) {
            res *= i
        }
        return res
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Fehler"
        // If whole number and within standard integer range
        if (abs(value - round(value)) < 1e-9 && abs(value) < 1e12) {
            return round(value).toLong().toString()
        }
        // If extremely huge or tiny
        if (abs(value) >= 1e12 || (abs(value) < 1e-6 && abs(value) > 0)) {
            return SCIENTIFIC_FORMAT.format(value)
        }
        // Round nicely
        val str = DECIMAL_FORMAT.format(value)
        return str
    }
}
