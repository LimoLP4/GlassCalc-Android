package com.limonatalp4.glasscalcproject

import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.domain.evaluator.EvaluationResult
import com.limonatalp4.glasscalcproject.domain.evaluator.ExpressionEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        val cToF = UnitConverter.CONVERSIONS.first { it.unitFrom == "°C" && it.unitTo == "°F" }
        val fVal = UnitConverter.convert(0.0, cToF, forward = true)
        assertEquals("32", fVal)

        // Temperature: °C ↔ K and °F ↔ K in both directions
        val cToK = UnitConverter.CONVERSIONS.first { it.unitFrom == "°C" && it.unitTo == "K" }
        assertEquals("273,15", UnitConverter.convert(0.0, cToK, forward = true))
        assertEquals("373,15", UnitConverter.convert(100.0, cToK, forward = true))
        assertEquals("-273,15", UnitConverter.convert(0.0, cToK, forward = false))
        assertEquals("0", UnitConverter.convert(273.15, cToK, forward = false))

        val fToK = UnitConverter.CONVERSIONS.first { it.unitFrom == "°F" && it.unitTo == "K" }
        assertEquals("273,15", UnitConverter.convert(32.0, fToK, forward = true))
        assertEquals("373,15", UnitConverter.convert(212.0, fToK, forward = true))
        assertEquals("32", UnitConverter.convert(273.15, fToK, forward = false))
        assertEquals("-459,67", UnitConverter.convert(0.0, fToK, forward = false))

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

        // Voltage: V, mV, kV in both directions
        val vToMv = UnitConverter.CONVERSIONS.first { it.unitFrom == "V" && it.unitTo == "mV" }
        assertEquals("1000", UnitConverter.convert(1.0, vToMv, forward = true))
        assertEquals("0,5", UnitConverter.convert(500.0, vToMv, forward = false))

        val kvToV = UnitConverter.CONVERSIONS.first { it.unitFrom == "kV" && it.unitTo == "V" }
        assertEquals("2500", UnitConverter.convert(2.5, kvToV, forward = true))
        assertEquals("1,5", UnitConverter.convert(1500.0, kvToV, forward = false))

        val kvToMv = UnitConverter.CONVERSIONS.first { it.unitFrom == "kV" && it.unitTo == "mV" }
        assertEquals("1000000", UnitConverter.convert(1.0, kvToMv, forward = true))
        assertEquals("0,000001", UnitConverter.convert(1.0, kvToMv, forward = false))

        // Current: A, mA, µA in both directions
        val aToMa = UnitConverter.CONVERSIONS.first { it.unitFrom == "A" && it.unitTo == "mA" }
        assertEquals("1000", UnitConverter.convert(1.0, aToMa, forward = true))
        assertEquals("0,25", UnitConverter.convert(250.0, aToMa, forward = false))

        val maToUa = UnitConverter.CONVERSIONS.first { it.unitFrom == "mA" && it.unitTo == "µA" }
        assertEquals("1000", UnitConverter.convert(1.0, maToUa, forward = true))
        assertEquals("0,5", UnitConverter.convert(500.0, maToUa, forward = false))

        val aToUa = UnitConverter.CONVERSIONS.first { it.unitFrom == "A" && it.unitTo == "µA" }
        assertEquals("1000000", UnitConverter.convert(1.0, aToUa, forward = true))
        assertEquals("0,000001", UnitConverter.convert(1.0, aToUa, forward = false))

        // Power: W, mW, kW, MW in both directions
        val wToMw = UnitConverter.CONVERSIONS.first { it.unitFrom == "W" && it.unitTo == "mW" }
        assertEquals("1000", UnitConverter.convert(1.0, wToMw, forward = true))
        assertEquals("0,75", UnitConverter.convert(750.0, wToMw, forward = false))

        val kwToW = UnitConverter.CONVERSIONS.first { it.unitFrom == "kW" && it.unitTo == "W" }
        assertEquals("3000", UnitConverter.convert(3.0, kwToW, forward = true))
        assertEquals("2,5", UnitConverter.convert(2500.0, kwToW, forward = false))

        val mwToKw = UnitConverter.CONVERSIONS.first { it.unitFrom == "MW" && it.unitTo == "kW" }
        assertEquals("1000", UnitConverter.convert(1.0, mwToKw, forward = true))
        assertEquals("0,5", UnitConverter.convert(500.0, mwToKw, forward = false))

        val mwToW = UnitConverter.CONVERSIONS.first { it.unitFrom == "MW" && it.unitTo == "W" }
        assertEquals("1000000", UnitConverter.convert(1.0, mwToW, forward = true))
        assertEquals("1", UnitConverter.convert(1_000_000.0, mwToW, forward = false))

        // Charge: mAh, Ah in both directions
        val mahToAh = UnitConverter.CONVERSIONS.first { it.unitFrom == "mAh" && it.unitTo == "Ah" }
        assertEquals("5", UnitConverter.convert(5000.0, mahToAh, forward = true))
        assertEquals("2500", UnitConverter.convert(2.5, mahToAh, forward = false))

        // Length: m ↔ cm, m ↔ mm, m ↔ nm in both directions
        val mToCm = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "cm" }
        assertEquals("250", UnitConverter.convert(2.5, mToCm, forward = true))
        assertEquals("1,5", UnitConverter.convert(150.0, mToCm, forward = false))

        val mToMm = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "mm" }
        assertEquals("1500", UnitConverter.convert(1.5, mToMm, forward = true))
        assertEquals("0,5", UnitConverter.convert(500.0, mToMm, forward = false))

        val mToNm = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "nm" }
        assertEquals("1000000000", UnitConverter.convert(1.0, mToNm, forward = true))
        assertEquals("1E-9", UnitConverter.convert(1.0, mToNm, forward = false))
        assertEquals("0,000001", UnitConverter.convert(1000.0, mToNm, forward = false))

        // Mass: kg ↔ t in both directions
        val kgToT = UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "t" }
        assertEquals("1", UnitConverter.convert(1000.0, kgToT, forward = true))
        assertEquals("2500", UnitConverter.convert(2.5, kgToT, forward = false))
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
            assertTrue(formula.keywordsResIds.isNotEmpty())
        }

        // German locale simulation: "Kreis" / "Fläche" match, English "Circle" does not
        val germanEntries = formulas.map { formula ->
            when (formula.id) {
                "circle_area" -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Kreisfläche", listOf("kreis", "fläche", "radius", "pi")
                )
                "pythagoras" -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Pythagoras", listOf("pythagoras", "dreieck")
                )
                else -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Sonstige Formel", emptyList()
                )
            }
        }
        assertEquals(
            listOf("circle_area"),
            com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Fläche", germanEntries).map { it.formula.id }
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Circle", germanEntries).isEmpty()
        )

        // English locale simulation: "Circle" / "Area" match
        val englishEntries = formulas.map { formula ->
            when (formula.id) {
                "circle_area" -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Circle Area", listOf("circle", "area", "radius", "pi")
                )
                else -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Other Formula", emptyList()
                )
            }
        }
        assertEquals(
            listOf("circle_area"),
            com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Area", englishEntries).map { it.formula.id }
        )

        // Spanish locale simulation: "Círculo" / "Área" match
        val spanishEntries = formulas.map { formula ->
            when (formula.id) {
                "circle_area" -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Área del círculo", listOf("círculo", "área", "radio", "pi")
                )
                else -> com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                    formula, "Otra fórmula", emptyList()
                )
            }
        }
        assertEquals(
            listOf("circle_area"),
            com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Círculo", spanishEntries).map { it.formula.id }
        )

        val emptyQuery = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("   ", germanEntries)
        assertTrue(emptyQuery.isEmpty())
    }

    @Test
    fun testSystemLanguageResolutionAndEnglishFallback() {
        val resolve = com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel::resolveEffectiveLanguageTag

        // Supported system languages stay active when System ("") is chosen
        assertEquals("de", resolve("", java.util.Locale("de", "DE")))
        assertEquals("en", resolve("", java.util.Locale("en", "US")))
        assertEquals("es", resolve("", java.util.Locale("es", "MX")))
        assertEquals("fr", resolve("", java.util.Locale("fr", "CA")))
        assertEquals("ja", resolve("", java.util.Locale("ja", "JP")))
        assertEquals("zh", resolve("", java.util.Locale("zh", "CN")))
        assertEquals("tlh", resolve("", java.util.Locale("tlh")))

        // Unsupported system languages must automatically fall back to English ("en")
        assertEquals("en", resolve("", java.util.Locale("ko", "KR")))
        assertEquals("en", resolve("", java.util.Locale("ar", "SA")))
        assertEquals("en", resolve("", java.util.Locale("tr", "TR")))

        // Newly supported system languages (ru, pt, hi) resolve correctly
        assertEquals("ru", resolve("", java.util.Locale("ru", "RU")))
        assertEquals("pt", resolve("", java.util.Locale("pt", "BR")))
        assertEquals("hi", resolve("", java.util.Locale("hi", "IN")))

        // Explicit selections override system locale
        assertEquals("ja", resolve("ja", java.util.Locale("de", "DE")))
        assertEquals("zh", resolve("zh", java.util.Locale("ko", "KR")))
        assertEquals("tlh", resolve("tlh", java.util.Locale("de", "DE")))
        assertEquals("es", resolve("es", java.util.Locale("ko", "KR")))
        assertEquals("fr", resolve("fr", java.util.Locale("ru", "RU")))
        assertEquals("en", resolve("invalid", java.util.Locale("de", "DE")))

        // "SYSTEM" tag behaves identical to blank/system reset
        assertEquals("de", resolve("SYSTEM", java.util.Locale("de", "DE")))
        assertEquals("de", resolve("system", java.util.Locale("de", "DE")))
        assertEquals("ru", resolve("SYSTEM", java.util.Locale("ru", "RU")))
        assertEquals("pt", resolve("SYSTEM", java.util.Locale("pt", "BR")))
        assertEquals("hi", resolve("SYSTEM", java.util.Locale("hi", "IN")))
        assertEquals("en", resolve("SYSTEM", java.util.Locale("tr", "TR")))
    }

    @Test
    fun testSettingsSearchFiltering() {
        // Empty query shows all cards
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "", "Haptisches Feedback", "haptik,intensität"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "   ", "AMOLED"
            )
        )

        // German Haptic search matches haptic card but not language card
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Haptik",
                "Haptisches Feedback",
                "Vibration bei Tastendruck und Schaltern",
                "Intensität",
                "entwickler,haptik,vibration,intensität,motor,lra,erm"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Haptik Intensität",
                "Haptisches Feedback",
                "Intensität",
                "entwickler,haptik,intensität"
            )
        )
        assertTrue(
            !com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Haptik",
                "Deutsch",
                "Sprache",
                "sprache,sprachen,systemsprache"
            )
        )

        // Language search matches all language options when section keyword is included
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Sprache", "Deutsch", "Sprache", "sprache,sprachen,systemsprache"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Sprache", "Español", "Sprache", "sprache,sprachen,systemsprache"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Deutsch", "Deutsch", "Sprache", "sprache,sprachen"
            )
        )
        assertTrue(
            !com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Deutsch", "Español", "Sprache", "sprache,sprachen"
            )
        )

        // Language isolation: searching English "Haptic" should NOT match German-only strings
        assertTrue(
            !com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Haptic",
                "Haptisches Feedback",
                "Vibration bei Tastendruck",
                "entwickler,haptik,vibration,intensität"
            )
        )
        // ...whereas in English it matches cleanly
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Haptic",
                "Haptic Feedback",
                "Vibration on keypress and switches",
                "developer,haptic,haptics,vibration,intensity"
            )
        )
    }

    @Test
    fun testUnitConverterSearchFiltering() {
        val localizedGermanPairs = UnitConverter.CONVERSIONS.map { pair ->
            val label = when (pair.unitFrom to pair.unitTo) {
                "m" to "ft" -> "Meter ↔ Fuß (m ↔ ft)"
                "m" to "cm" -> "Meter ↔ Zentimeter (m ↔ cm)"
                "m" to "mm" -> "Meter ↔ Millimeter (m ↔ mm)"
                "m" to "nm" -> "Meter ↔ Nanometer (m ↔ nm)"
                "cm" to "in" -> "Zentimeter ↔ Zoll (cm ↔ in)"
                "km" to "mi" -> "Kilometer ↔ Meilen (km ↔ mi)"
                "kg" to "lb" -> "Kilogramm ↔ Pfund (kg ↔ lb)"
                "kg" to "t" -> "Kilogramm ↔ Tonnen (kg ↔ t)"
                "°C" to "°F" -> "Celsius ↔ Fahrenheit (°C ↔ °F)"
                "°C" to "K" -> "Celsius ↔ Kelvin (°C ↔ K)"
                "°F" to "K" -> "Fahrenheit ↔ Kelvin (°F ↔ K)"
                "l" to "gal" -> "Liter ↔ Gallonen (l ↔ gal)"
                "B" to "KB" -> "Byte ↔ Kilobyte (B ↔ KB)"
                "KB" to "MB" -> "Kilobyte ↔ Megabyte (KB ↔ MB)"
                "MB" to "GB" -> "Megabyte ↔ Gigabyte (MB ↔ GB)"
                "GB" to "TB" -> "Gigabyte ↔ Terabyte (GB ↔ TB)"
                else -> "${pair.unitFrom} ↔ ${pair.unitTo}"
            }
            val keywords = when (pair.category) {
                UnitCategory.LENGTH -> listOf("länge", "meter", "fuß", "zentimeter", "millimeter", "nanometer", "zoll", "kilometer", "meilen")
                UnitCategory.MASS -> listOf("masse", "gewicht", "kilogramm", "pfund", "tonne")
                UnitCategory.TEMPERATURE -> listOf("temperatur", "celsius", "fahrenheit", "kelvin")
                UnitCategory.VOLUME -> listOf("volumen", "liter", "gallonen")
                UnitCategory.DATA -> listOf("daten", "speicher", "byte", "gigabyte")
                UnitCategory.TIME -> listOf("zeit", "sekunden", "minuten", "stunden")
                UnitCategory.VOLTAGE -> listOf("spannung", "volt", "millivolt", "kilovolt")
                UnitCategory.CURRENT -> listOf("stromstärke", "ampere", "milliampere", "mikroampere")
                UnitCategory.POWER -> listOf("leistung", "watt", "milliwatt", "kilowatt", "megawatt")
                UnitCategory.CHARGE -> listOf("ladung", "akku", "milliamperestunden", "amperestunden")
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedConversionPair(
                pair = pair,
                localizedLabel = label,
                localizedCategoryName = pair.category.name,
                localizedCategoryKeywords = keywords
            )
        }

        // "Meter" matches all meter-based length conversions
        val meterResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("Meter", localizedGermanPairs)
        assertEquals(
            listOf("m" to "ft", "m" to "cm", "m" to "mm", "m" to "nm", "cm" to "in", "km" to "mi"),
            meterResults.map { it.unitFrom to it.unitTo }
        )

        // "m" puts exact symbol match "m ↔ ..." first
        val mResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("m", localizedGermanPairs)
        assertTrue(mResults.isNotEmpty())
        assertEquals("m", mResults.first().unitFrom)

        // "GB" finds data pairs MB↔GB and GB↔TB across categories
        val gbResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("GB", localizedGermanPairs)
        assertEquals(listOf("MB" to "GB", "GB" to "TB"), gbResults.map { it.unitFrom to it.unitTo })

        // "Gewicht" matches Mass pairs (kg↔lb, kg↔t) in German
        val weightResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("Gewicht", localizedGermanPairs)
        assertEquals(listOf("kg" to "lb", "kg" to "t"), weightResults.map { it.unitFrom to it.unitTo })

        // "Tonne" matches kg↔t
        val tonResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("Tonne", localizedGermanPairs)
        assertTrue(tonResults.any { it.unitFrom == "kg" && it.unitTo == "t" })

        // "Kelvin" prioritizes °C↔K and °F↔K
        val kelvinPairResults = com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("Kelvin", localizedGermanPairs)
        assertEquals(listOf("°C" to "K", "°F" to "K"), kelvinPairResults.take(2).map { it.unitFrom to it.unitTo })

        // Empty search returns empty list (dialog switches back to active category view)
        assertTrue(com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs("   ", localizedGermanPairs).isEmpty())
    }

    @Test
    fun testUnitConverterCategorySynchronization() {
        val categories = UnitCategory.entries
        // GB ↔ TB belongs to DATA -> index 4
        val gbPair = UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }
        assertEquals(UnitCategory.DATA, gbPair.category)
        val dataCatIndex = categories.indexOf(gbPair.category)
        assertEquals(4, dataCatIndex)

        // Celsius belongs to TEMPERATURE -> index 2
        val tempPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }
        val tempCatIndex = categories.indexOf(tempPair.category)
        assertEquals(2, tempCatIndex)

        // Length (m ↔ ft) -> index 0
        val lengthPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.LENGTH }
        val lengthCatIndex = categories.indexOf(lengthPair.category)
        assertEquals(0, lengthCatIndex)

        // Mass (kg ↔ lb) -> index 1
        val massPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.MASS }
        val massCatIndex = categories.indexOf(massPair.category)
        assertEquals(1, massCatIndex)

        // Volume (l ↔ gal) -> index 3
        val volPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.VOLUME }
        val volCatIndex = categories.indexOf(volPair.category)
        assertEquals(3, volCatIndex)

        // Time (s ↔ min) -> index 5
        val timePair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TIME }
        val timeCatIndex = categories.indexOf(timePair.category)
        assertEquals(5, timeCatIndex)

        // Voltage (V ↔ mV) -> index 6
        val voltagePair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.VOLTAGE }
        assertEquals(6, categories.indexOf(voltagePair.category))

        // Current (A ↔ mA) -> index 7
        val currentPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.CURRENT }
        assertEquals(7, categories.indexOf(currentPair.category))

        // Power (W ↔ mW) -> index 8
        val powerPair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.POWER }
        assertEquals(8, categories.indexOf(powerPair.category))

        // Charge (mAh ↔ Ah) -> index 9
        val chargePair = UnitConverter.CONVERSIONS.first { it.category == UnitCategory.CHARGE }
        assertEquals(9, categories.indexOf(chargePair.category))
    }

    @Test
    fun testUnitSuggestionsDropdownFiltering() {
        val germanSuggestions = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.map { item ->
            val localizedName = when (item.id) {
                "meter" -> "Meter"
                "feet" -> "Fuß"
                "centimeter" -> "Zentimeter"
                "millimeter" -> "Millimeter"
                "nanometer" -> "Nanometer"
                "inch" -> "Zoll"
                "kilometer" -> "Kilometer"
                "mile" -> "Meilen"
                "kilogram" -> "Kilogramm"
                "pound" -> "Pfund"
                "ton" -> "Tonne"
                "celsius" -> "Celsius"
                "fahrenheit" -> "Fahrenheit"
                "kelvin" -> "Kelvin"
                "liter" -> "Liter"
                "gallon" -> "Gallonen"
                "byte" -> "Byte"
                "kilobyte" -> "Kilobyte"
                "megabyte" -> "Megabyte"
                "gigabyte" -> "Gigabyte"
                "terabyte" -> "Terabyte"
                "second" -> "Sekunden"
                "millisecond" -> "Millisekunden"
                "minute" -> "Minuten"
                "hour" -> "Stunden"
                "day" -> "Tage"
                else -> item.id
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = item,
                localizedName = localizedName,
                symbol = item.symbol
            )
        }

        // 1. "gig" matches "Gigabyte"
        val gigResults = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("gig", germanSuggestions)
        assertEquals(1, gigResults.size)
        assertEquals("Gigabyte", gigResults.first().localizedName)
        assertEquals(UnitCategory.DATA, gigResults.first().item.category)

        // 2. "giga" matches "Gigabyte"
        val gigaResults = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("giga", germanSuggestions)
        assertEquals(1, gigaResults.size)
        assertEquals("Gigabyte", gigaResults.first().localizedName)

        // 3. "m" prioritizes exact match / prefix (Meter, Meilen, Megabyte, etc.)
        val mResults = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("m", germanSuggestions)
        assertTrue(mResults.isNotEmpty())
        assertEquals("Meter", mResults.first().localizedName)

        // 3b. New Length & Mass units in German suggestions (cm, mm, nm, t)
        assertEquals("Zentimeter", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Zentimeter", germanSuggestions).first().localizedName)
        assertEquals("Millimeter", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Millimeter", germanSuggestions).first().localizedName)
        assertEquals("Nanometer", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Nanometer", germanSuggestions).first().localizedName)
        assertEquals("Tonne", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Tonne", germanSuggestions).first().localizedName)
        assertEquals("Kelvin", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Kelvin", germanSuggestions).first().localizedName)
        assertEquals(UnitCategory.TEMPERATURE, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("kel", germanSuggestions).first().item.category)

        // 4. Spanish localization test ("Pulgadas", "Tonelada")
        val spanishSuggestions = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.map { item ->
            val localizedName = when (item.id) {
                "inch" -> "Pulgadas"
                "ton" -> "Tonelada"
                "gigabyte" -> "Gigabyte"
                else -> item.id
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = item,
                localizedName = localizedName,
                symbol = item.symbol
            )
        }
        val pulgResults = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("pulg", spanishSuggestions)
        assertEquals(1, pulgResults.size)
        assertEquals("Pulgadas", pulgResults.first().localizedName)
        assertEquals(UnitCategory.LENGTH, pulgResults.first().item.category)
        assertEquals("Tonelada", com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Tonelada", spanishSuggestions).first().localizedName)

        // 5. French localization test ("Octet")
        val frenchSuggestions = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.map { item ->
            val localizedName = when (item.id) {
                "byte" -> "Octet"
                "gigabyte" -> "Gigaoctet"
                else -> item.id
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = item,
                localizedName = localizedName,
                symbol = item.symbol
            )
        }
        val octResults = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("oct", frenchSuggestions)
        assertTrue(octResults.isNotEmpty())
        assertTrue(octResults.any { it.localizedName == "Octet" })
        assertTrue(octResults.any { it.localizedName == "Gigaoctet" })

        // 6. Electrical & Mass units across languages (German, English, Russian, Spanish)
        val multiLangElectricalSuggestions = listOf(
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.first { it.id == "volt" },
                localizedName = "Вольт",
                symbol = "V",
                localizedKeywords = listOf("вольт", "напряжение", "spannung", "tensión")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.first { it.id == "microampere" },
                localizedName = "Mikroampere",
                symbol = "µA",
                localizedKeywords = listOf("mikroampere", "µa", "ua", "stromstärke")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.first { it.id == "milliampere_hour" },
                localizedName = "Milliamperestunden",
                symbol = "mAh",
                localizedKeywords = listOf("mah", "ladung", "akku")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.first { it.id == "ton" },
                localizedName = "Тонна",
                symbol = "t",
                localizedKeywords = listOf("тонна", "т", "metric ton", "tonne")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.first { it.id == "kelvin" },
                localizedName = "Кельвин",
                symbol = "K",
                localizedKeywords = listOf("кельвин", "kelvin", "температура")
            )
        )
        assertEquals(UnitCategory.VOLTAGE, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Напряжение", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.VOLTAGE, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("tension", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.CURRENT, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("uA", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.CHARGE, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("mAh", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.MASS, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Metric Ton", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.MASS, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Тонна", multiLangElectricalSuggestions).first().item.category)
        assertEquals(UnitCategory.TEMPERATURE, com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Кельвин", multiLangElectricalSuggestions).first().item.category)
    }

    @Test
    fun testDeeplyNestedIntegralAndExtremeConverterValuesDoNotCrash() {
        // Build nested integral deeper than MAX_RECURSION_DEPTH (16)
        var nested = "x"
        repeat(20) {
            nested = "∫($nested; 0; 1)"
        }
        val nestedRes = evaluator.evaluate(nested, AngleMode.RAD)
        assertTrue(nestedRes is EvaluationResult.Error)

        // Non-finite and extreme inputs in UnitConverter
        val mToFt = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }
        assertEquals("—", UnitConverter.convert(Double.NaN, mToFt, forward = true))
        assertEquals("—", UnitConverter.convert(Double.POSITIVE_INFINITY, mToFt, forward = true))
        assertEquals("—", UnitConverter.convert(Double.NEGATIVE_INFINITY, mToFt, forward = true))
        assertEquals("—", UnitConverter.convert(Double.MAX_VALUE, mToFt, forward = true))
        val largeButFinite = UnitConverter.convert(1e15, mToFt, forward = true)
        assertTrue(largeButFinite.contains("E"))

        // System locale support helper
        assertTrue(com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel.isSystemLocaleSupported(java.util.Locale("de", "DE")))
        assertTrue(com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel.isSystemLocaleSupported(java.util.Locale("hi", "IN")))
        assertTrue(!com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel.isSystemLocaleSupported(java.util.Locale("tr", "TR")))
    }

    @Test
    fun testDisplayWhitelistFilter() {
        // Allowed digits, operators, constants, and math functions
        val (validExpr, validCursor) = com.limonatalp4.glasscalcproject.ui.components.filterCalculatorExpression(
            text = "2+sin(π/2)^3-4,5*10%+e",
            cursorPosition = 22
        )
        assertEquals("2+sin(π/2)^3-4,5*10%+e", validExpr)
        assertEquals(22, validCursor)

        // Blocks Cyrillic, umlauts, emojis, and unsafe symbols while keeping cursor position accurate
        val sampleInput = "12@äф😀+3#4"
        val (filtered, cursor) = com.limonatalp4.glasscalcproject.ui.components.filterCalculatorExpression(
            text = sampleInput,
            cursorPosition = sampleInput.indexOf('+') + 1
        )
        assertEquals("12+34", filtered)
        assertEquals(3, cursor) // "12+"

        // Preserves calculator unicode operators during soft-keyboard editing
        val (unicodeKept, unicodeCursor) = com.limonatalp4.glasscalcproject.ui.components.filterCalculatorExpression(
            text = "√(9)×2÷1−3",
            cursorPosition = 10
        )
        assertEquals("√(9)×2÷1−3", unicodeKept)
        assertEquals(10, unicodeCursor)
    }

    @Test
    fun testComprehensiveSearchMatchingAndFaultTolerance() {
        // 1. Substring matching: "hapt" finds "Haptisches Feedback", "gig" finds "Gigabyte"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "hapt",
                "Haptisches Feedback",
                "Vibration bei Tastendruck",
                "haptik,vibration"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "gig",
                "Gigabyte",
                "daten,speicher,gb"
            )
        )

        // 2. Synonyms & Keywords from strings.xml:
        // "Vibration" finds "Haptisches Feedback"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Vibration",
                "Haptisches Feedback",
                "Vibration bei Tastendruck",
                "entwickler,haptik,haptisches,feedback,vibration,vibrieren,intensität,motor,lra,erm,taktil,haptics"
            )
        )
        // "Language" finds German "Sprache"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Language",
                "Sprache",
                "sprache,sprachen,systemsprache,übersetzung,lokalisierung,language,languages"
            )
        )
        // "Sprache" finds English "Language"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Sprache",
                "Language",
                "language,languages,system,locale,translation,localization,sprache"
            )
        )

        // 3. Fault tolerance: Case-insensitivity, leading/trailing whitespace, CJK space, diacritic folding
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "   hApT   ",
                "Haptisches Feedback",
                "haptik"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "\u3000hapt\u3000",
                "Haptisches Feedback",
                "haptik"
            )
        )
        // "Area" finds "Área" (Latin diacritic folding)
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "area",
                "Área del círculo",
                "círculo,área,radio"
            )
        )
        // "Fuss" finds "Fuß"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "fuss",
                "Fuß",
                "fuß,ft,imperial"
            )
        )
        // Cyrillic ё / е normalization
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "объём",
                "Объем цилиндра",
                "цилиндр,объем"
            )
        )

        // 4. Relevance ranking in DrawerFormulaSearch: Exact and prefix matches rank higher
        val dummyCatalog = listOf(
            com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                formula = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS.first(),
                localizedName = "Kreisfläche",
                localizedKeywords = listOf("kreis", "fläche", "radius", "pi")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                formula = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS[1],
                localizedName = "Kreisumfang",
                localizedKeywords = listOf("kreis", "umfang", "durchmesser")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                formula = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS[7],
                localizedName = "Dreiecksfläche",
                localizedKeywords = listOf("dreieck", "fläche", "höhe")
            )
        )
        // "Kreis" matches Kreisfläche & Kreisumfang (prefix match) ahead of Dreiecksfläche (no match)
        val kreisMatches = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Kreis", dummyCatalog)
        assertEquals(2, kreisMatches.size)
        assertEquals(listOf("circle_area", "circle_circumference"), kreisMatches.map { it.formula.id })

        // "Fläche" finds both Kreisfläche and Dreiecksfläche via substring or keywords
        val flaecheMatches = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Fläche", dummyCatalog)
        assertEquals(2, flaecheMatches.size)
        assertTrue(flaecheMatches.any { it.formula.id == "circle_area" })
        assertTrue(flaecheMatches.any { it.formula.id == "triangle_area" })

        // 5. Unit suggestions relevance: "Meter" puts "Meter" first, "m" puts "m" (Meter) first
        val unitItems = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.map { item ->
            val name = when (item.id) {
                "meter" -> "Meter"
                "centimeter" -> "Zentimeter"
                "kilometer" -> "Kilometer"
                "feet" -> "Fuß"
                "gigabyte" -> "Gigabyte"
                else -> item.id
            }
            val kw = when (item.id) {
                "meter" -> listOf("meter", "m", "länge")
                "centimeter" -> listOf("zentimeter", "cm", "länge")
                "kilometer" -> listOf("kilometer", "km", "länge")
                "feet" -> listOf("fuß", "ft", "foot")
                "gigabyte" -> listOf("gigabyte", "gb", "daten")
                else -> emptyList()
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = item,
                localizedName = name,
                symbol = item.symbol,
                localizedKeywords = kw
            )
        }
        val meterSearch = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Meter", unitItems)
        assertEquals("Meter", meterSearch.first().localizedName)

        val gbSearch = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("GB", unitItems)
        assertEquals("Gigabyte", gbSearch.first().localizedName)

        val ftSearch = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("Fuß", unitItems)
        assertEquals("Fuß", ftSearch.first().localizedName)

        val fussSearch = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("fuss", unitItems)
        assertEquals("Fuß", fussSearch.first().localizedName)

        // 6. Spanish formula search examples from user prompt:
        // "Área" -> finds "Círculo", "Teorema" -> finds "Pitágoras"
        val spanishFormulas = listOf(
            com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                formula = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS.first(),
                localizedName = "Área del Círculo",
                localizedKeywords = com.limonatalp4.glasscalcproject.ui.components.splitSearchKeywords("círculo,área,superficie,radio,pi,π,r^2,geometría")
            ),
            com.limonatalp4.glasscalcproject.ui.components.LocalizedFormula(
                formula = com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog.FORMULAS[2],
                localizedName = "Teorema de Pitágoras",
                localizedKeywords = com.limonatalp4.glasscalcproject.ui.components.splitSearchKeywords("pitágoras,teorema,triángulo,rectángulo,hipotenusa,cateto,a^2+b^2")
            )
        )
        val areaMatch = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Área", spanishFormulas)
        assertEquals("circle_area", areaMatch.first().formula.id)

        // Diacritic tolerance: "area" without accent still matches "Área del Círculo"
        val areaNoAccentMatch = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("area", spanishFormulas)
        assertEquals("circle_area", areaNoAccentMatch.first().formula.id)

        val teoremaMatch = com.limonatalp4.glasscalcproject.ui.components.filterFormulas("Teorema", spanishFormulas)
        assertEquals("pythagoras", teoremaMatch.first().formula.id)

        // 7. English settings search examples:
        // "Vibration" -> finds "Haptics", "Language" -> finds "Sprache"
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Vibration",
                "Haptic Feedback",
                "developer,haptic,haptics,feedback,vibration,vibrate,intensity,strength,level,motor,lra,erm,tactile"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Language",
                "Language",
                "language,languages,system,locale,translation,localization,sprache"
            )
        )
        assertTrue(
            com.limonatalp4.glasscalcproject.ui.components.matchesSettingsSearch(
                "Sprache",
                "Language",
                "language,languages,system,locale,translation,localization,sprache"
            )
        )

        // 8. Spanish unit search: "pie" finds Feet (ft)
        val spanishUnits = com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS.map { item ->
            val name = when (item.id) {
                "feet" -> "Pies"
                "meter" -> "Metros"
                "gigabyte" -> "Gigabyte"
                else -> item.id
            }
            val kw = when (item.id) {
                "feet" -> listOf("pie", "pies", "ft", "imperial", "longitud")
                "meter" -> listOf("metro", "metros", "m", "longitud", "distancia", "métrico")
                "gigabyte" -> listOf("gigabyte", "gb", "datos")
                else -> emptyList()
            }
            com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion(
                item = item,
                localizedName = name,
                symbol = item.symbol,
                localizedKeywords = kw
            )
        }
        val pieSearch = com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions("pie", spanishUnits)
        assertEquals("Pies", pieSearch.first().localizedName)
        assertEquals("ft", pieSearch.first().symbol)
    }

    @Test
    fun testGraphDomainsBoundsAndCoordinates() {
        // 1. screenToMath & mathToScreen origin round-trip
        val originScreen = com.limonatalp4.glasscalcproject.ui.components.mathToScreen(
            x = 0.0, y = 0.0, width = 400f, height = 200f,
            xMin = -10.0, xMax = 10.0, yMin = -5.0, yMax = 5.0
        )
        assertEquals(200f, originScreen.x, 1e-3f)
        assertEquals(100f, originScreen.y, 1e-3f)
        val originMath = com.limonatalp4.glasscalcproject.ui.components.screenToMath(
            offsetX = originScreen.x, offsetY = originScreen.y, width = 400f, height = 200f,
            xMin = -10.0, xMax = 10.0, yMin = -5.0, yMax = 5.0
        )
        assertEquals(0.0, originMath.x, 1e-6)
        assertEquals(0.0, originMath.y, 1e-6)

        // 2. arccos & arcsin DEG vs RAD bounds
        val arccosDeg = com.limonatalp4.glasscalcproject.ui.components.defaultBoundsForPreset(
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.ARCCOS,
            AngleMode.DEG
        )
        assertEquals(-15.0, arccosDeg.yMin, 1e-6)
        assertEquals(195.0, arccosDeg.yMax, 1e-6)

        val arccosRad = com.limonatalp4.glasscalcproject.ui.components.defaultBoundsForPreset(
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.ARCCOS,
            AngleMode.RAD
        )
        assertEquals(-0.5, arccosRad.yMin, 1e-6)
        assertEquals(3.5, arccosRad.yMax, 1e-6)

        val customArccosDeg = com.limonatalp4.glasscalcproject.ui.components.defaultBoundsForPreset(
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.CUSTOM,
            AngleMode.DEG,
            "arccos(x)"
        )
        assertEquals(195.0, customArccosDeg.yMax, 1e-6)

        // 3. Domain checks: arccos/arcsin only in [-1, 1], ln/log only > 0, sqrt only >= 0
        assertEquals(180.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("acos(x)", -1.0, AngleMode.DEG)!!, 1e-4)
        assertEquals(90.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("acos(x)", 0.0, AngleMode.DEG)!!, 1e-4)
        assertEquals(0.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("acos(x)", 1.0, AngleMode.DEG)!!, 1e-4)
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("acos(x)", -1.05, AngleMode.DEG))
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("acos(x)", 1.2, AngleMode.DEG))
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("asin(x)", 1.5, AngleMode.RAD))

        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("ln(x)", 0.0, AngleMode.RAD))
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("ln(x)", -2.0, AngleMode.RAD))
        assertEquals(0.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("ln(x)", 1.0, AngleMode.RAD)!!, 1e-6)

        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("log10(x)", -1.0, AngleMode.RAD))
        assertEquals(1.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("log(x)", 10.0, AngleMode.RAD)!!, 1e-6)

        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("sqrt(x)", -0.1, AngleMode.RAD))
        assertEquals(0.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("sqrt(x)", 0.0, AngleMode.RAD)!!, 1e-6)
        assertEquals(3.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("√(x)", 9.0, AngleMode.RAD)!!, 1e-6)

        // 4. All other formulas evaluate accurately
        assertEquals(16.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("x²", -4.0, AngleMode.RAD)!!, 1e-6)
        assertEquals(2.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("³√(x)", 8.0, AngleMode.RAD)!!, 1e-6)
        assertEquals(5.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("|x|", -5.0, AngleMode.RAD)!!, 1e-6)
        assertEquals(45.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("atan(x)", 1.0, AngleMode.DEG)!!, 1e-4)
        assertEquals(1.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("der(sin(x), x)", 0.0, AngleMode.RAD)!!, 1e-4)
        assertEquals(1.0, com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("d/dx(sin(x))", 0.0, AngleMode.RAD)!!, 1e-4)
    }

    @Test
    fun testSafeMathEvaluatorOverflowAndErrorGuards() {
        val safeEval = com.limonatalp4.glasscalcproject.domain.evaluator.SafeMathEvaluator

        // isSafeValue strictly rejects NaN, +/-Infinity, and abs(y) > 1e15
        assertTrue(!safeEval.isSafeValue(Double.NaN))
        assertTrue(!safeEval.isSafeValue(Double.POSITIVE_INFINITY))
        assertTrue(!safeEval.isSafeValue(Double.NEGATIVE_INFINITY))
        assertTrue(!safeEval.isSafeValue(1e16))
        assertTrue(!safeEval.isSafeValue(-1e16))
        assertTrue(safeEval.isSafeValue(1e15))
        assertTrue(safeEval.isSafeValue(-1e15))

        // Domain errors, overflow, and malformed expressions return null cleanly
        assertNull(safeEval.safeEvaluate("1/0"))
        assertNull(safeEval.safeEvaluate("ln(0)"))
        assertNull(safeEval.safeEvaluate("sqrt(-1)"))
        assertNull(safeEval.safeEvaluate("2^5000"))
        assertNull(safeEval.safeEvaluate("10^20"))
        assertNull(safeEval.safeEvaluate("((("))
        assertNull(safeEval.safeEvaluate(""))

        // Variable evaluation overflow and singularity protection
        assertNull(safeEval.safeEvaluateWithVar("x^500", "x", 2.0, AngleMode.RAD))
        assertNull(safeEval.safeEvaluateWithVar("e^x", "x", 100.0, AngleMode.RAD))
        assertNull(safeEval.safeEvaluateWithVar("1/x", "x", 0.0, AngleMode.RAD))
        assertNull(safeEval.safeEvaluateWithVar("x^2", "x", 1e16, AngleMode.RAD))
        assertEquals(16.0, safeEval.safeEvaluateWithVar("x^2", "x", 4.0, AngleMode.RAD)!!, 1e-6)
    }

    @Test
    fun testBoundedIntegrationPoleDetectionAndCancellation() {
        val safeEval = com.limonatalp4.glasscalcproject.domain.evaluator.SafeMathEvaluator

        // Poles inside integration interval must be caught cleanly (null / Error, no Infinity or crash)
        assertNull(safeEval.integrateBounded("1/x", "x", -1.0, 1.0, AngleMode.RAD))
        assertNull(safeEval.integrateBounded("1/(x-0.33)", "x", 0.0, 1.0, AngleMode.RAD))
        assertNull(safeEval.integrateBounded("tan(x)", "x", 0.0, 3.0, AngleMode.RAD))

        // Extreme integrand values and out-of-range bounds return null cleanly
        assertNull(safeEval.integrateBounded("x^500", "x", 0.0, 10.0, AngleMode.RAD))
        assertNull(safeEval.integrateBounded("e^x", "x", 0.0, 100.0, AngleMode.RAD))
        assertNull(safeEval.integrateBounded("x", "x", 0.0, 1e20, AngleMode.RAD))

        // ExpressionEvaluator surfaces pole/overflow integrals cleanly as EvaluationResult.Error
        assertTrue(evaluator.evaluate("∫(1/x; -1; 1)", AngleMode.RAD) is EvaluationResult.Error)
        assertTrue(evaluator.evaluate("∫(tan(x); 0; 3)", AngleMode.RAD) is EvaluationResult.Error)
        assertTrue(evaluator.evaluate("∫(x^500; 0; 10)", AngleMode.RAD) is EvaluationResult.Error)

        // Bounded valid integral with large bounds completes quickly without ANR (capped at 1000 steps)
        val startMs = System.currentTimeMillis()
        val largeBounds = safeEval.integrateBounded("sin(x)", "x", -10000.0, 10000.0, AngleMode.RAD)
        val elapsedMs = System.currentTimeMillis() - startMs
        assertTrue("Bounded integration took too long: ${elapsedMs}ms", elapsedMs < 3000L)
        assertTrue(largeBounds != null && safeEval.isSafeValue(largeBounds))

        // Coroutine cancellation via checkActive aborts integration immediately
        var cancelled = false
        try {
            safeEval.integrateBounded("sin(x)", "x", 0.0, 10.0, AngleMode.RAD) {
                throw kotlinx.coroutines.CancellationException("cancelled")
            }
        } catch (_: kotlinx.coroutines.CancellationException) {
            cancelled = true
        }
        assertTrue(cancelled)
    }

    @Test
    fun testGraphCrashAndAnrProtections() {
        val safeEval = com.limonatalp4.glasscalcproject.domain.evaluator.SafeMathEvaluator

        // 1. Extreme expressions in graph evaluator never crash or leak Infinity/NaN > 1e15
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("x^500", 2.0, AngleMode.RAD))
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("e^x", 100.0, AngleMode.RAD))
        assertNull(com.limonatalp4.glasscalcproject.ui.components.evaluateFormulaAtX("1/x", 0.0, AngleMode.RAD))

        // 2. Curve sampling across extreme functions and wide bounds remains bounded & safe
        val extremeCurves = listOf(
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.CUSTOM to "x^500",
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.CUSTOM to "e^x",
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.CUSTOM to "1/x",
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.TAN to "",
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.INTEGRAL to "1/x",
            com.limonatalp4.glasscalcproject.ui.components.GraphPreset.INTEGRAL to "sin(x)"
        )
        extremeCurves.forEach { (preset, expr) ->
            val startMs = System.currentTimeMillis()
            val pts = com.limonatalp4.glasscalcproject.ui.components.computeGraphCurvePoints(
                preset = preset,
                expression = expr,
                angleMode = AngleMode.RAD,
                xMin = -1000.0,
                xMax = 1000.0
            )
            val elapsedMs = System.currentTimeMillis() - startMs
            assertTrue("Sampling $preset ($expr) took too long: ${elapsedMs}ms", elapsedMs < 3000L)
            assertTrue("Sampling $preset ($expr) exceeded bounded sample budget", pts.size <= 1010)
            pts.forEach { (x, y) ->
                assertTrue(x.isFinite())
                if (!y.isNaN()) {
                    assertTrue("y out of safe bounds: $y", safeEval.isSafeValue(y))
                }
            }
        }

        // 3. mathToScreen returns NaN Offset safely on invalid/infinite coordinates instead of crashing Skia
        val nanScreen = com.limonatalp4.glasscalcproject.ui.components.mathToScreen(
            x = 1.0, y = Double.POSITIVE_INFINITY,
            width = 400f, height = 200f,
            xMin = -10.0, xMax = 10.0, yMin = -10.0, yMax = 10.0
        )
        assertTrue(nanScreen.y.isNaN())

        val zeroSpanScreen = com.limonatalp4.glasscalcproject.ui.components.mathToScreen(
            x = 1.0, y = 1.0,
            width = 0f, height = 200f,
            xMin = -10.0, xMax = 10.0, yMin = -10.0, yMax = 10.0
        )
        assertTrue(zeroSpanScreen.x.isNaN())
    }

    @Test
    fun testConcurrentFormattingAndUnicodeOperators() {
        // Unicode minus and fraction slash evaluate accurately
        val unicodeSub = evaluator.evaluate("5−2")
        assertTrue(unicodeSub is EvaluationResult.Success)
        assertEquals("3", (unicodeSub as EvaluationResult.Success).formatted)

        val unicodeFrac = evaluator.evaluate("6⁄2")
        assertTrue(unicodeFrac is EvaluationResult.Success)
        assertEquals("3", (unicodeFrac as EvaluationResult.Success).formatted)

        // Concurrent formatting via ThreadLocal DecimalFormat must never throw or mix results
        val meterToFeet = UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }
        val threads = (0 until 8).map { threadIdx ->
            java.util.concurrent.Callable {
                repeat(200) { iter ->
                    val n = (threadIdx * 100 + iter + 1).toDouble()
                    val evalRes = evaluator.evaluate("$n+0.5")
                    assertTrue(evalRes is EvaluationResult.Success)
                    val conv = UnitConverter.convert(n, meterToFeet, forward = true)
                    assertTrue(conv.isNotBlank() && conv != "Fehler")
                }
            }
        }
        val executor = java.util.concurrent.Executors.newFixedThreadPool(8)
        try {
            val futures = executor.invokeAll(threads)
            futures.forEach { it.get() }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun testAppThemeEntriesExcludeAmoledAndIncludeTiefschwarz() {
        val names = com.limonatalp4.glasscalcproject.data.model.AppTheme.entries.map { it.name }
        org.junit.Assert.assertFalse(names.contains("AMOLED_DARK"))
        assertTrue(names.contains("DARK_MODE"))
        assertTrue(names.contains("AUTOMATIC"))
        assertEquals(5, names.size)
    }

    @Test
    fun testResolveEffectiveThemeRespectsPowerSaveMode() {
        val automatic = com.limonatalp4.glasscalcproject.data.model.AppTheme.AUTOMATIC
        val darkMode = com.limonatalp4.glasscalcproject.data.model.AppTheme.DARK_MODE
        val standardDark = com.limonatalp4.glasscalcproject.data.model.AppTheme.STANDARD_DARK
        val whiteMode = com.limonatalp4.glasscalcproject.data.model.AppTheme.WHITE_MODE
        val materialYou = com.limonatalp4.glasscalcproject.data.model.AppTheme.MATERIAL_YOU

        // Power Saver ON overrides AUTOMATIC to Tiefschwarz (DARK_MODE) even in system light mode
        assertEquals(
            darkMode,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = automatic,
                systemDark = false,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )
        assertEquals(
            darkMode,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = automatic,
                systemDark = true,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )

        // Power Saver OFF falls back to system dark / white mode
        assertEquals(
            standardDark,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = automatic,
                systemDark = true,
                isPowerSaveMode = false,
                effectiveApiLevel = 35
            )
        )
        assertEquals(
            whiteMode,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = automatic,
                systemDark = false,
                isPowerSaveMode = false,
                effectiveApiLevel = 35
            )
        )

        // Material You MUST keep dynamic colors and NEVER switch to Tiefschwarz on Power Saver
        assertEquals(
            materialYou,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = materialYou,
                systemDark = false,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )
        assertEquals(
            materialYou,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = materialYou,
                systemDark = true,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )
        // On API < 31, falls back to regular light/dark without switching to Tiefschwarz
        assertEquals(
            whiteMode,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = materialYou,
                systemDark = false,
                isPowerSaveMode = true,
                effectiveApiLevel = 30
            )
        )

        // Explicit selections stay intact
        assertEquals(
            whiteMode,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = whiteMode,
                systemDark = true,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )
        assertEquals(
            standardDark,
            com.limonatalp4.glasscalcproject.ui.theme.resolveEffectiveTheme(
                selectedTheme = standardDark,
                systemDark = false,
                isPowerSaveMode = true,
                effectiveApiLevel = 35
            )
        )
    }
}



