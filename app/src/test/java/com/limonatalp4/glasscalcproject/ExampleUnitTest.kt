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
}
