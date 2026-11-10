package com.limonatalp4.glasscalcproject.data.model

import com.limonatalp4.glasscalcproject.R

enum class AppTheme(val stringResId: Int, val requiresMinApi: Int = 0) {
    MATERIAL_YOU(R.string.theme_material_you, requiresMinApi = 31),
    AUTOMATIC(R.string.theme_automatic, requiresMinApi = 0),
    WHITE_MODE(R.string.theme_white_mode, requiresMinApi = 0),
    STANDARD_DARK(R.string.theme_standard_dark, requiresMinApi = 0),
    DARK_MODE(R.string.theme_dark_mode, requiresMinApi = 0),
    AMOLED_DARK(R.string.theme_amoled_dark, requiresMinApi = 0)
}

enum class AngleMode {
    DEG, RAD
}

enum class Language(val stringResId: Int, val localeTag: String) {
    SYSTEM(R.string.language_system, ""),
    GERMAN(R.string.language_german, "de"),
    ENGLISH(R.string.language_english, "en")
}

data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)
