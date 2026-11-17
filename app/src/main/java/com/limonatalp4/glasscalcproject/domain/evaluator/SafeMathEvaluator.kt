package com.limonatalp4.glasscalcproject.domain.evaluator

import com.limonatalp4.glasscalcproject.data.model.AngleMode
import kotlinx.coroutines.CancellationException
import org.mariuszgromada.math.mxparser.Argument
import org.mariuszgromada.math.mxparser.Expression
import org.mariuszgromada.math.mxparser.License
import org.mariuszgromada.math.mxparser.mXparser
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Thread-safe, crash-proof and ANR-proof wrapper around mXparser.
 *
 * Strictly intercepts Double.NaN, +/-Infinity, overflow values (abs(y) > 1e15),
 * ArithmeticException, StackOverflowError, and numerical singularities, returning null
 * instead of propagating invalid values or crashes.
 */
object SafeMathEvaluator {

    const val MAX_SAFE_VALUE: Double = 1e15
    const val MAX_INTEGRAL_STEPS: Int = 1000
    const val MIN_INTEGRAL_STEPS: Int = 100
    private const val INTEGRATION_BATCH_SIZE: Int = 50

    init {
        try {
            License.iConfirmNonCommercialUse("limonatalp4")
        } catch (_: Throwable) {
        }
    }

    fun isSafeValue(value: Double, maxAbs: Double = MAX_SAFE_VALUE): Boolean {
        return !value.isNaN() &&
            value != Double.POSITIVE_INFINITY &&
            value != Double.NEGATIVE_INFINITY &&
            value.isFinite() &&
            abs(value) <= maxAbs
    }

    fun applyAngleMode(angleMode: AngleMode) {
        if (angleMode == AngleMode.DEG) {
            mXparser.setDegreesMode()
        } else {
            mXparser.setRadiansMode()
        }
    }

    /**
     * Evaluates a pre-built mXparser Expression with strict finite and magnitude bounds.
     * Returns null on NaN, Infinity, abs(y) > maxAbs, ArithmeticException, or StackOverflowError.
     */
    fun safeCalculate(
        expression: Expression,
        maxAbs: Double = MAX_SAFE_VALUE
    ): Double? {
        return try {
            val raw = expression.calculate()
            if (isSafeValue(raw, maxAbs)) raw else null
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: ArithmeticException) {
            null
        } catch (_: StackOverflowError) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Evaluates a pre-built mXparser Expression checking finiteness and exceptions without
     * the 1e15 graph magnitude cap (e.g. for standard calculator scientific notation like 50!).
     */
    fun safeCalculateFinite(expression: Expression): Pair<Double?, String?> {
        return try {
            val raw = expression.calculate()
            val err = expression.errorMessage
            val safe = if (!raw.isNaN() && raw.isFinite()) raw else null
            safe to err
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: ArithmeticException) {
            null to "ArithmeticException"
        } catch (_: StackOverflowError) {
            null to "StackOverflowError"
        } catch (_: OutOfMemoryError) {
            null to "OutOfMemoryError"
        } catch (_: Throwable) {
            null to "Error"
        }
    }

    fun safeEvaluate(
        sanitizedExpression: String,
        angleMode: AngleMode = AngleMode.RAD,
        maxAbs: Double = MAX_SAFE_VALUE
    ): Double? {
        if (sanitizedExpression.isBlank()) return null
        return try {
            synchronized(MxParserLock) {
                applyAngleMode(angleMode)
                val expr = Expression(sanitizedExpression)
                safeCalculate(expr, maxAbs)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: ArithmeticException) {
            null
        } catch (_: StackOverflowError) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    fun safeEvaluateWithVar(
        sanitizedExpression: String,
        varName: String,
        x: Double,
        angleMode: AngleMode = AngleMode.RAD,
        maxAbs: Double = MAX_SAFE_VALUE
    ): Double? {
        if (sanitizedExpression.isBlank() || !isSafeValue(x, maxAbs)) return null
        return try {
            synchronized(MxParserLock) {
                applyAngleMode(angleMode)
                val arg = Argument("$varName = 0")
                arg.argumentValue = x
                val expr = Expression(sanitizedExpression, arg)
                safeCalculate(expr, maxAbs)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: ArithmeticException) {
            null
        } catch (_: StackOverflowError) {
            null
        } catch (_: OutOfMemoryError) {
            null
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Adaptive, strictly bounded Composite Simpson integration over [a, b].
     *
     * - Caps subintervals strictly at [maxSteps] (default 1000).
     * - Calls [checkActive] before every lock batch to support fast coroutine cancellation.
     * - Detects singularities, poles (including between-sample asymptotes), NaN, Infinity,
     *   and overflow > [maxAbs], returning null cleanly on divergence.
     */
    fun integrateBounded(
        integrandSanitized: String,
        varName: String,
        a: Double,
        b: Double,
        angleMode: AngleMode,
        maxSteps: Int = MAX_INTEGRAL_STEPS,
        maxAbs: Double = MAX_SAFE_VALUE,
        checkActive: () -> Unit = {}
    ): Double? {
        checkActive()
        if (integrandSanitized.isBlank()) return null
        if (!isSafeValue(a, maxAbs) || !isSafeValue(b, maxAbs)) return null
        if (a == b) return 0.0

        val lower = minOf(a, b)
        val upper = maxOf(a, b)
        val sign = if (a > b) -1.0 else 1.0
        val span = upper - lower
        if (!span.isFinite() || span <= 0.0 || span > maxAbs) return null

        val cappedMax = maxSteps.coerceIn(2, MAX_INTEGRAL_STEPS)
        val desiredSteps = if (span <= cappedMax.toDouble() / 40.0) {
            (span * 40.0).roundToInt()
        } else {
            cappedMax
        }
        var n = desiredSteps.coerceIn(minOf(MIN_INTEGRAL_STEPS, cappedMax), cappedMax)
        if (n % 2 != 0) {
            n = if (n < cappedMax) n + 1 else n - 1
        }
        if (n < 2) n = 2

        val h = span / n
        if (!h.isFinite() || h <= 0.0) return null

        var sum = 0.0
        var prevX = lower
        var prevY = Double.NaN

        try {
            var k = 0
            while (k <= n) {
                checkActive()
                val batchEnd = minOf(k + INTEGRATION_BATCH_SIZE - 1, n)
                val aborted = synchronized(MxParserLock) {
                    applyAngleMode(angleMode)
                    val arg = Argument("$varName = 0")
                    val expr = Expression(integrandSanitized, arg)

                    for (idx in k..batchEnd) {
                        val xVal = if (idx == n) upper else lower + idx * h
                        arg.argumentValue = xVal
                        val yVal = safeCalculate(expr, maxAbs) ?: return@synchronized true

                        if (idx > 0 && hasPoleBetweenSamples(expr, arg, prevX, prevY, xVal, yVal, maxAbs)) {
                            return@synchronized true
                        }

                        val coeff = when {
                            idx == 0 || idx == n -> 1.0
                            idx % 2 == 0 -> 2.0
                            else -> 4.0
                        }
                        val term = coeff * yVal
                        if (!term.isFinite()) return@synchronized true
                        sum += term
                        if (!sum.isFinite()) return@synchronized true

                        prevX = xVal
                        prevY = yVal
                    }
                    false
                }
                if (aborted) return null
                k = batchEnd + 1
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (_: ArithmeticException) {
            return null
        } catch (_: StackOverflowError) {
            return null
        } catch (_: OutOfMemoryError) {
            return null
        } catch (_: Throwable) {
            return null
        }

        val result = sign * (h / 3.0) * sum
        if (!isSafeValue(result, maxAbs)) return null
        return if (abs(result) < 1e-12) 0.0 else result
    }

    /**
     * Checks whether a hidden pole or singularity lies between two adjacent grid samples
     * (e.g. 1/x or tan(x) crossing an asymptote without hitting the exact pole coordinate).
     */
    internal fun hasPoleBetweenSamples(
        expr: Expression,
        arg: Argument,
        x1: Double,
        y1: Double,
        x2: Double,
        y2: Double,
        maxAbs: Double = MAX_SAFE_VALUE
    ): Boolean {
        if (!y1.isFinite() || !y2.isFinite()) return true

        val oppositeSigns = (y1 > 0.0 && y2 < 0.0) || (y1 < 0.0 && y2 > 0.0)
        val jump = abs(y2 - y1)
        val minMag = min(abs(y1), abs(y2))

        // Sign-flip jump with large magnitudes on both sides (e.g. 1/(x-c) or tan(x))
        if (oppositeSigns && minMag > 20.0 && jump > 100.0) {
            var leftX = x1
            var leftY = y1
            var rightX = x2
            var rightY = y2
            repeat(4) {
                val midX = 0.5 * (leftX + rightX)
                arg.argumentValue = midX
                val midY = safeCalculate(expr, maxAbs) ?: return true
                val peak = max(abs(leftY), abs(rightY))
                if (abs(midY) > peak * 1.5) return true
                if ((leftY > 0.0 && midY < 0.0) || (leftY < 0.0 && midY > 0.0)) {
                    rightX = midX
                    rightY = midY
                } else {
                    leftX = midX
                    leftY = midY
                }
            }
        }

        // Same-sign extreme spike (e.g. 1/(x-c)^2 peaking near a grid interval)
        val maxMag = max(abs(y1), abs(y2))
        if (maxMag > 1e6 && jump > 1e5) {
            val midX = 0.5 * (x1 + x2)
            arg.argumentValue = midX
            val midY = safeCalculate(expr, maxAbs) ?: return true
            if (abs(midY) > maxMag * 2.0) return true
        }

        return false
    }
}
