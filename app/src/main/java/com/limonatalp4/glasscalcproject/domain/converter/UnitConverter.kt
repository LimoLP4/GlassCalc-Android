package com.limonatalp4.glasscalcproject.domain.converter

import com.limonatalp4.glasscalcproject.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val stringResId: Int) {
    LENGTH(R.string.cat_length),
    MASS(R.string.cat_mass),
    TEMPERATURE(R.string.cat_temp),
    VOLUME(R.string.cat_volume)
}

data class ConversionPair(
    val category: UnitCategory,
    val unitFrom: String,
    val unitTo: String,
    val labelResId: Int,
    val formulaFromTo: (Double) -> Double,
    val formulaToFrom: (Double) -> Double
)

object UnitConverter {
    private val FORMAT = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.GERMANY))

    val CONVERSIONS = listOf(
        // Length
        ConversionPair(
            category = UnitCategory.LENGTH,
            unitFrom = "m",
            unitTo = "ft",
            labelResId = R.string.unit_m_ft,
            formulaFromTo = { it * 3.280839895 },
            formulaToFrom = { it / 3.280839895 }
        ),
        ConversionPair(
            category = UnitCategory.LENGTH,
            unitFrom = "cm",
            unitTo = "in",
            labelResId = R.string.unit_cm_in,
            formulaFromTo = { it * 0.3937007874 },
            formulaToFrom = { it / 0.3937007874 }
        ),
        ConversionPair(
            category = UnitCategory.LENGTH,
            unitFrom = "km",
            unitTo = "mi",
            labelResId = R.string.unit_km_mi,
            formulaFromTo = { it * 0.6213711922 },
            formulaToFrom = { it / 0.6213711922 }
        ),
        // Mass
        ConversionPair(
            category = UnitCategory.MASS,
            unitFrom = "kg",
            unitTo = "lb",
            labelResId = R.string.unit_kg_lb,
            formulaFromTo = { it * 2.2046226218 },
            formulaToFrom = { it / 2.2046226218 }
        ),
        // Temperature
        ConversionPair(
            category = UnitCategory.TEMPERATURE,
            unitFrom = "°C",
            unitTo = "°F",
            labelResId = R.string.unit_c_f,
            formulaFromTo = { (it * 9.0 / 5.0) + 32.0 },
            formulaToFrom = { (it - 32.0) * 5.0 / 9.0 }
        ),
        // Volume
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "gal",
            labelResId = R.string.unit_l_gal,
            formulaFromTo = { it * 0.2641720524 },
            formulaToFrom = { it / 0.2641720524 }
        )
    )

    fun convert(value: Double, pair: ConversionPair, forward: Boolean): String {
        val res = if (forward) pair.formulaFromTo(value) else pair.formulaToFrom(value)
        return FORMAT.format(res)
    }
}
