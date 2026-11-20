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
    TIME(R.string.cat_time),
    VOLTAGE(R.string.cat_voltage),
    CURRENT(R.string.cat_current),
    POWER(R.string.cat_power),
    CHARGE(R.string.cat_charge)
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
    private val FORMAT = ThreadLocal.withInitial {
        DecimalFormat("#,##0.######", DecimalFormatSymbols(Locale.GERMANY)).apply {
            isGroupingUsed = false
        }
    }
    private val SCIENTIFIC_FORMAT = ThreadLocal.withInitial {
        DecimalFormat("0.######E0", DecimalFormatSymbols(Locale.US))
    }

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
            unitFrom = "m",
            unitTo = "cm",
            labelResId = R.string.unit_m_cm,
            formulaFromTo = { it * 100.0 },
            formulaToFrom = { it / 100.0 }
        ),
        ConversionPair(
            category = UnitCategory.LENGTH,
            unitFrom = "m",
            unitTo = "mm",
            labelResId = R.string.unit_m_mm,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.LENGTH,
            unitFrom = "m",
            unitTo = "nm",
            labelResId = R.string.unit_m_nm,
            formulaFromTo = { it * 1_000_000_000.0 },
            formulaToFrom = { it / 1_000_000_000.0 }
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
        ConversionPair(
            category = UnitCategory.MASS,
            unitFrom = "kg",
            unitTo = "t",
            labelResId = R.string.unit_kg_t,
            formulaFromTo = { it / 1000.0 },
            formulaToFrom = { it * 1000.0 }
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
        ConversionPair(
            category = UnitCategory.TEMPERATURE,
            unitFrom = "°C",
            unitTo = "K",
            labelResId = R.string.unit_c_k,
            formulaFromTo = { it + 273.15 },
            formulaToFrom = { it - 273.15 }
        ),
        ConversionPair(
            category = UnitCategory.TEMPERATURE,
            unitFrom = "°F",
            unitTo = "K",
            labelResId = R.string.unit_f_k,
            formulaFromTo = { (it - 32.0) * 5.0 / 9.0 + 273.15 },
            formulaToFrom = { (it - 273.15) * 9.0 / 5.0 + 32.0 }
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
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "ml",
            labelResId = R.string.unit_l_ml,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "cl",
            labelResId = R.string.unit_l_cl,
            formulaFromTo = { it * 100.0 },
            formulaToFrom = { it / 100.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "dl",
            labelResId = R.string.unit_l_dl,
            formulaFromTo = { it * 10.0 },
            formulaToFrom = { it / 10.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "m³",
            unitTo = "l",
            labelResId = R.string.unit_m3_l,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "dl",
            unitTo = "ml",
            labelResId = R.string.unit_dl_ml,
            formulaFromTo = { it * 100.0 },
            formulaToFrom = { it / 100.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "cl",
            unitTo = "ml",
            labelResId = R.string.unit_cl_ml,
            formulaFromTo = { it * 10.0 },
            formulaToFrom = { it / 10.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "qt",
            labelResId = R.string.unit_l_qt,
            formulaFromTo = { it / 0.946352946 },
            formulaToFrom = { it * 0.946352946 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "l",
            unitTo = "pt",
            labelResId = R.string.unit_l_pt,
            formulaFromTo = { it / 0.473176473 },
            formulaToFrom = { it * 0.473176473 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "ml",
            unitTo = "cup",
            labelResId = R.string.unit_ml_cup,
            formulaFromTo = { it / 236.5882365 },
            formulaToFrom = { it * 236.5882365 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "ml",
            unitTo = "fl oz",
            labelResId = R.string.unit_ml_floz,
            formulaFromTo = { it / 29.5735295625 },
            formulaToFrom = { it * 29.5735295625 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "ml",
            unitTo = "tbsp",
            labelResId = R.string.unit_ml_tbsp,
            formulaFromTo = { it / 14.78676478125 },
            formulaToFrom = { it * 14.78676478125 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "ml",
            unitTo = "tsp",
            labelResId = R.string.unit_ml_tsp,
            formulaFromTo = { it / 4.92892159375 },
            formulaToFrom = { it * 4.92892159375 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "gal",
            unitTo = "qt",
            labelResId = R.string.unit_gal_qt,
            formulaFromTo = { it * 4.0 },
            formulaToFrom = { it / 4.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "qt",
            unitTo = "pt",
            labelResId = R.string.unit_qt_pt,
            formulaFromTo = { it * 2.0 },
            formulaToFrom = { it / 2.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "pt",
            unitTo = "cup",
            labelResId = R.string.unit_pt_cup,
            formulaFromTo = { it * 2.0 },
            formulaToFrom = { it / 2.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "cup",
            unitTo = "fl oz",
            labelResId = R.string.unit_cup_floz,
            formulaFromTo = { it * 8.0 },
            formulaToFrom = { it / 8.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLUME,
            unitFrom = "tbsp",
            unitTo = "tsp",
            labelResId = R.string.unit_tbsp_tsp,
            formulaFromTo = { it * 3.0 },
            formulaToFrom = { it / 3.0 }
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
        ),
        // Voltage
        ConversionPair(
            category = UnitCategory.VOLTAGE,
            unitFrom = "V",
            unitTo = "mV",
            labelResId = R.string.unit_v_mv,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLTAGE,
            unitFrom = "kV",
            unitTo = "V",
            labelResId = R.string.unit_kv_v,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.VOLTAGE,
            unitFrom = "kV",
            unitTo = "mV",
            labelResId = R.string.unit_kv_mv,
            formulaFromTo = { it * 1_000_000.0 },
            formulaToFrom = { it / 1_000_000.0 }
        ),
        // Current
        ConversionPair(
            category = UnitCategory.CURRENT,
            unitFrom = "A",
            unitTo = "mA",
            labelResId = R.string.unit_a_ma,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.CURRENT,
            unitFrom = "mA",
            unitTo = "µA",
            labelResId = R.string.unit_ma_ua,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.CURRENT,
            unitFrom = "A",
            unitTo = "µA",
            labelResId = R.string.unit_a_ua,
            formulaFromTo = { it * 1_000_000.0 },
            formulaToFrom = { it / 1_000_000.0 }
        ),
        // Power
        ConversionPair(
            category = UnitCategory.POWER,
            unitFrom = "W",
            unitTo = "mW",
            labelResId = R.string.unit_w_mw,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.POWER,
            unitFrom = "kW",
            unitTo = "W",
            labelResId = R.string.unit_kw_w,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.POWER,
            unitFrom = "MW",
            unitTo = "kW",
            labelResId = R.string.unit_mw_kw,
            formulaFromTo = { it * 1000.0 },
            formulaToFrom = { it / 1000.0 }
        ),
        ConversionPair(
            category = UnitCategory.POWER,
            unitFrom = "MW",
            unitTo = "W",
            labelResId = R.string.unit_mw_w,
            formulaFromTo = { it * 1_000_000.0 },
            formulaToFrom = { it / 1_000_000.0 }
        ),
        // Charge
        ConversionPair(
            category = UnitCategory.CHARGE,
            unitFrom = "mAh",
            unitTo = "Ah",
            labelResId = R.string.unit_mah_ah,
            formulaFromTo = { it / 1000.0 },
            formulaToFrom = { it * 1000.0 }
        )
    )

    fun convert(value: Double, pair: ConversionPair, forward: Boolean): String {
        if (!value.isFinite()) return "—"
        val res = if (forward) pair.formulaFromTo(value) else pair.formulaToFrom(value)
        if (!res.isFinite()) return "—"
        if (abs(res) >= 1e12 || (abs(res) < 1e-6 && res != 0.0)) {
            return SCIENTIFIC_FORMAT.get()!!.format(res)
        }
        return FORMAT.get()!!.format(res)
    }
}
