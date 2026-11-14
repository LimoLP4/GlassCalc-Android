package com.limonatalp4.glasscalcproject.domain.converter

import androidx.compose.runtime.Immutable
import com.limonatalp4.glasscalcproject.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

enum class UnitCategory(val stringResId: Int) {
    LENGTH(R.string.cat_length),
    MASS(R.string.cat_mass),
    TEMPERATURE(R.string.cat_temp),
    VOLUME(R.string.cat_volume),
    DATA(R.string.cat_data),
    TIME(R.string.cat_time)
}

@Immutable
data class ConversionPair(
    val category: UnitCategory,
    val unitFrom: String,
    val unitTo: String,
    val labelResId: Int,
    val formulaFromTo: (Double) -> Double,
    val formulaToFrom: (Double) -> Double
)

object UnitConverter {
    private val FORMAT = DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.GERMANY)).apply {
        isGroupingUsed = false
    }
    private val SCIENTIFIC_FORMAT = DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))

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
        ),
        // Data (Binary standard: 1024)
        ConversionPair(
            category = UnitCategory.DATA,
            unitFrom = "B",
            unitTo = "KB",
            labelResId = R.string.unit_b_kb,
            formulaFromTo = { it / 1024.0 },
            formulaToFrom = { it * 1024.0 }
        ),
        ConversionPair(
            category = UnitCategory.DATA,
            unitFrom = "KB",
            unitTo = "MB",
            labelResId = R.string.unit_kb_mb,
            formulaFromTo = { it / 1024.0 },
            formulaToFrom = { it * 1024.0 }
        ),
        ConversionPair(
            category = UnitCategory.DATA,
            unitFrom = "MB",
            unitTo = "GB",
            labelResId = R.string.unit_mb_gb,
            formulaFromTo = { it / 1024.0 },
            formulaToFrom = { it * 1024.0 }
        ),
        ConversionPair(
            category = UnitCategory.DATA,
            unitFrom = "GB",
            unitTo = "TB",
            labelResId = R.string.unit_gb_tb,
            formulaFromTo = { it / 1024.0 },
            formulaToFrom = { it * 1024.0 }
        ),
        // Time
        ConversionPair(
            category = UnitCategory.TIME,
            unitFrom = "s",
            unitTo = "ms",
            labelResId = R.string.unit_s_ms,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.TIME,
            unitFrom = "min",
            unitTo = "s",
            labelResId = R.string.unit_min_s,
            formulaFromTo = { it * 60.0 },
            formulaToFrom = { it / 60.0 }
        ),
        ConversionPair(
            category = UnitCategory.TIME,
            unitFrom = "h",
            unitTo = "min",
            labelResId = R.string.unit_h_min,
            formulaFromTo = { it * 60.0 },
            formulaToFrom = { it / 60.0 }
        ),
        ConversionPair(
            category = UnitCategory.TIME,
            unitFrom = "d",
            unitTo = "h",
            labelResId = R.string.unit_d_h,
            formulaFromTo = { it * 24.0 },
            formulaToFrom = { it / 24.0 }
        )
    )

    fun convert(value: Double, pair: ConversionPair, forward: Boolean): String {
        if (!value.isFinite()) return "—"
        val res = if (forward) pair.formulaFromTo(value) else pair.formulaToFrom(value)
        if (!res.isFinite()) return "—"
        if (abs(res) >= 1e12 || (abs(res) < 1e-6 && res != 0.0)) {
            return SCIENTIFIC_FORMAT.format(res)
        }
        return FORMAT.format(res)
    }
}
