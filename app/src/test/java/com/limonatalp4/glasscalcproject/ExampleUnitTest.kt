package com.limonatalp4.glasscalcproject

import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val evaluator = ExpressionEvaluator()

    @Test
    fun testBasicArithmeticAndPrecedence() {
        // 2 + 3 * 4 should be 14 (Punkt-vor-Strich)
        val res1 = evaluator.evaluate("2+3*4")
        assertTrue(res1 is EvaluationResult.Success)
        assertEquals("14", (res1 as EvaluationResult.Success).formatted)

        // (2 + 3) * 4 should be 20
        val res2 = evaluator.evaluate("(2+3)*4")
        assertTrue(res2 is EvaluationResult.Success)
        assertEquals("20", (res2 as EvaluationResult.Success).formatted)

        // Decimal operations
        val res3 = evaluator.evaluate("10/4")
        assertTrue(res3 is EvaluationResult.Success)
        assertEquals("2,5", (res3 as EvaluationResult.Success).formatted)
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val res = evaluator.evaluate("10/0")
        assertTrue(res is EvaluationResult.Error)
        assertEquals(R.string.error_division_by_zero, (res as EvaluationResult.Error).messageResId)
    }

    @Test
    fun testScientificFunctionsAndTrigonometry() {
        // sin(90) in DEG = 1
        val sinDeg = evaluator.evaluate("sin(90)", AngleMode.DEG)
        assertTrue(sinDeg is EvaluationResult.Success)
        assertEquals("1", (sinDeg as EvaluationResult.Success).formatted)

        // cos(0) = 1
        val cosDeg = evaluator.evaluate("cos(0)", AngleMode.DEG)
        assertTrue(cosDeg is EvaluationResult.Success)
        assertEquals("1", (cosDeg as EvaluationResult.Success).formatted)

        // log(100) = 2
        val logRes = evaluator.evaluate("log(100)")
        assertTrue(logRes is EvaluationResult.Success)
        assertEquals("2", (logRes as EvaluationResult.Success).formatted)

        // Factorial 5! = 120
        val factRes = evaluator.evaluate("5!")
        assertTrue(factRes is EvaluationResult.Success)
        assertEquals("120", (factRes as EvaluationResult.Success).formatted)

        // Square root √(16) = 4
        val sqrtRes = evaluator.evaluate("√(16)")
        assertTrue(sqrtRes is EvaluationResult.Success)
        assertEquals("4", (sqrtRes as EvaluationResult.Success).formatted)

        // Powers 2^3 = 8
        val powRes = evaluator.evaluate("2^3")
        assertTrue(powRes is EvaluationResult.Success)
        assertEquals("8", (powRes as EvaluationResult.Success).formatted)

        // Percentage 50 * 10% = 5
        val pctRes = evaluator.evaluate("50*10%")
        assertTrue(pctRes is EvaluationResult.Success)
        assertEquals("5", (pctRes as EvaluationResult.Success).formatted)
    }

    @Test
    fun testUnitConversions() {
        // Meter to feet
        val mToFt = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }
        val ftVal = UnitConverter.convert(1.0, mToFt, forward = true)
        assertTrue(ftVal.startsWith("3,28"))

        // Kg to lb
        val kgToLb = UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "lb" }
        val lbVal = UnitConverter.convert(1.0, kgToLb, forward = true)
        assertTrue(lbVal.startsWith("2,20"))

        // Celsius to Fahrenheit (0°C -> 32°F)
        val cToF = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }
        val fVal = UnitConverter.convert(0.0, cToF, forward = true)
        assertEquals("32", fVal)

        // Data: Binary standard (1024) in both directions
        val mbToGb = UnitConverter.CONVERSIONS.first { it.unitFrom == "MB" && it.unitTo == "GB" }
        assertEquals("1", UnitConverter.convert(1024.0, mbToGb, forward = true))
        assertEquals("1024", UnitConverter.convert(1.0, mbToGb, forward = false))

        val bToKb = UnitConverter.CONVERSIONS.first { it.unitFrom == "B" && it.unitTo == "KB" }
        assertEquals("0,5", UnitConverter.convert(512.0, bToKb, forward = true))
        assertEquals("2048", UnitConverter.convert(2.0, bToKb, forward = false))

        val gbToTb = UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }
        assertEquals("1", UnitConverter.convert(1024.0, gbToTb, forward = true))
        assertEquals("1024", UnitConverter.convert(1.0, gbToTb, forward = false))

        // Time: ms, s, min, h, d in both directions
        val hToMin = UnitConverter.CONVERSIONS.first { it.unitFrom == "h" && it.unitTo == "min" }
        assertEquals("60", UnitConverter.convert(1.0, hToMin, forward = true))
        assertEquals("2", UnitConverter.convert(120.0, hToMin, forward = false))

        val sToMs = UnitConverter.CONVERSIONS.first { it.unitFrom == "s" && it.unitTo == "ms" }
        assertEquals("1500", UnitConverter.convert(1.5, sToMs, forward = true))
        assertEquals("1", UnitConverter.convert(1000.0, sToMs, forward = false))

        val dToH = UnitConverter.CONVERSIONS.first { it.unitFrom == "d" && it.unitTo == "h" }
        assertEquals("48", UnitConverter.convert(2.0, dToH, forward = true))
        assertEquals("1", UnitConverter.convert(24.0, dToH, forward = false))
    }

    @Test
    fun testGraphCurveEvaluations() {
        // Test trigonometric values across common points
        val sinZero = evaluator.evaluate("sin(0)", AngleMode.RAD)
        assertTrue(sinZero is EvaluationResult.Success)
        assertEquals(0.0, (sinZero as EvaluationResult.Success).value, 1e-6)

        val sinHalfPi = evaluator.evaluate("sin(${Math.PI / 2})", AngleMode.RAD)
        assertTrue(sinHalfPi is EvaluationResult.Success)
        assertEquals(1.0, (sinHalfPi as EvaluationResult.Success).value, 1e-6)

        // Test logarithmic curve evaluations
        val lnE = evaluator.evaluate("ln(e)")
        assertTrue(lnE is EvaluationResult.Success)
        assertEquals(1.0, (lnE as EvaluationResult.Success).value, 1e-6)

        val logTen = evaluator.evaluate("log(10)")
        assertTrue(logTen is EvaluationResult.Success)
        assertEquals(1.0, (logTen as EvaluationResult.Success).value, 1e-6)

        // Test quadratic x^2
        val sq = evaluator.evaluate("(2.5)^2")
        assertTrue(sq is EvaluationResult.Success)
        assertEquals(6.25, (sq as EvaluationResult.Success).value, 1e-6)
    }

    @Test
    fun testGraphPointTransferExpressionEvaluation() {
        val positive = evaluator.evaluate("1,5708 + 1")
        assertTrue(positive is EvaluationResult.Success)
        assertEquals(2.5708, (positive as EvaluationResult.Success).value, 1e-4)

        val negativeY = evaluator.evaluate("1,5708 + (−0,5)")
        assertTrue(negativeY is EvaluationResult.Success)
        assertEquals(1.0708, (negativeY as EvaluationResult.Success).value, 1e-4)
    }

    @Test
    fun testEulerNthRootPowerAndFractionSlash() {
        // Euler's number e
        val eulerRes = evaluator.evaluate("ln(e^2)")
        assertTrue(eulerRes is EvaluationResult.Success)
        assertEquals(2.0, (eulerRes as EvaluationResult.Success).value, 1e-6)

        // General power x^y
        val powRes = evaluator.evaluate("2^5")
        assertTrue(powRes is EvaluationResult.Success)
        assertEquals("32", (powRes as EvaluationResult.Success).formatted)

        // n-th root: infix & prefix
        val infixRoot = evaluator.evaluate("3ⁿ√(27)")
        assertTrue(infixRoot is EvaluationResult.Success)
        assertEquals(3.0, (infixRoot as EvaluationResult.Success).value, 1e-6)

        val plainInfixRoot = evaluator.evaluate("4ⁿ√16")
        assertTrue(plainInfixRoot is EvaluationResult.Success)
        assertEquals(2.0, (plainInfixRoot as EvaluationResult.Success).value, 1e-6)

        val prefixRoot = evaluator.evaluate("ⁿ√(3; 64)")
        assertTrue(prefixRoot is EvaluationResult.Success)
        assertEquals(4.0, (prefixRoot as EvaluationResult.Success).value, 1e-6)

        // Fraction slash and decimal denominator
        val frac = evaluator.evaluate("3/4")
        assertTrue(frac is EvaluationResult.Success)
        assertEquals("0,75", (frac as EvaluationResult.Success).formatted)

        val halfDiv = evaluator.evaluate("10/0,5")
        assertTrue(halfDiv is EvaluationResult.Success)
        assertEquals("20", (halfDiv as EvaluationResult.Success).formatted)
    }

    @Test
    fun testNumericalSimpsonIntegrationAndDerivative() {
        // ∫_0^3 x^2 dx = 9
        val cubicInt = evaluator.evaluate("∫(x^2; 0; 3)")
        assertTrue(cubicInt is EvaluationResult.Success)
        assertEquals(9.0, (cubicInt as EvaluationResult.Success).value, 1e-5)

        // Reversed bounds: ∫_3^0 x^2 dx = -9
        val reversedInt = evaluator.evaluate("∫(x^2; 3; 0)")
        assertTrue(reversedInt is EvaluationResult.Success)
        assertEquals(-9.0, (reversedInt as EvaluationResult.Success).value, 1e-5)

        // ∫_0^π sin(x) dx = 2 (RAD)
        val sinInt = evaluator.evaluate("∫(sin(x); 0; π)", AngleMode.RAD)
        assertTrue(sinInt is EvaluationResult.Success)
        assertEquals(2.0, (sinInt as EvaluationResult.Success).value, 1e-6)

        // 4-arg int(t; t; 0; 2) = 2
        val fourArgInt = evaluator.evaluate("int(t; t; 0; 2)")
        assertTrue(fourArgInt is EvaluationResult.Success)
        assertEquals(2.0, (fourArgInt as EvaluationResult.Success).value, 1e-6)

        // 1-arg default [0, 1]: ∫_0^1 x^2 dx = 1/3
        val oneArgInt = evaluator.evaluate("∫(x^2)")
        assertTrue(oneArgInt is EvaluationResult.Success)
        assertEquals(1.0 / 3.0, (oneArgInt as EvaluationResult.Success).value, 1e-6)

        // Composite outer expression: 2 * ∫_1^3 x dx + 1 = 2 * 4 + 1 = 9
        val composite = evaluator.evaluate("2*∫(x; 1; 3)+1")
        assertTrue(composite is EvaluationResult.Success)
        assertEquals(9.0, (composite as EvaluationResult.Success).value, 1e-6)

        // Numerical derivative d/dx(x^3; 2) = 12
        val deriv = evaluator.evaluate("d/dx(x^3; 2)")
        assertTrue(deriv is EvaluationResult.Success)
        assertEquals(12.0, (deriv as EvaluationResult.Success).value, 1e-4)
    }

    @Test
    fun testFormulaCatalogSearchAndEvaluation() {
        val formulas = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS
        assertTrue(formulas.isNotEmpty())

        // Every formula in the catalog must evaluate cleanly in the calculator
        formulas.forEach { formula ->
            val res = evaluator.evaluate(formula.insertExpression, AngleMode.DEG)
            assertTrue(
                "Formula '${formula.id}' (${formula.insertExpression}) failed to evaluate",
                res is EvaluationResult.Success
            )
        }

        // Verify search filtering matches titles, previews, and keywords
        val pairs = formulas.map { formula ->
            val title = when (formula.id) {
                "circle_area" -> "Kreisfläche"
                "pythagoras" -> "Pythagoras"
                "compound_interest" -> "Zinseszins"
                else -> formula.id
            }
            formula to title
        }

        val pythMatches = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("pyth", pairs)
        assertEquals(listOf("pythagoras"), pythMatches.map { it.first.id })

        val kreisMatches = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Kreis", pairs)
        assertTrue(kreisMatches.any { it.first.id == "circle_area" })
        assertTrue(kreisMatches.any { it.first.id == "circle_circumference" })

        val zinsMatches = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("zins", pairs)
        assertEquals(listOf("compound_interest"), zinsMatches.map { it.first.id })

        val emptyQuery = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("   ", pairs)
        assertTrue(emptyQuery.isEmpty())
    }
}
