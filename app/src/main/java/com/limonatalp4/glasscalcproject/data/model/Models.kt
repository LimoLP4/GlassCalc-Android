package com.limonatalp4.glasscalcproject.data.model

import androidx.compose.runtime.Immutable
import com.limonatalp4.glasscalcproject.R

enum class AppTheme(val stringResId: Int, val requiresMinApi: Int = 0) {
    MATERIAL_YOU(R.string.theme_material_you, requiresMinApi = 31),
    AUTOMATIC(R.string.theme_automatic, requiresMinApi = 0),
    WHITE_MODE(R.string.theme_white_mode, requiresMinApi = 0),
    STANDARD_DARK(R.string.theme_standard_dark, requiresMinApi = 0),
    DARK_MODE(R.string.theme_dark_mode, requiresMinApi = 0)
}

enum class AngleMode {
    DEG, RAD
}

enum class Language(val stringResId: Int, val localeTag: String) {
    SYSTEM(R.string.language_system, "SYSTEM"),
    GERMAN(R.string.language_german, "de"),
    ENGLISH(R.string.language_english, "en"),
    SPANISH(R.string.language_spanish, "es"),
    FRENCH(R.string.language_french, "fr"),
    JAPANESE(R.string.language_japanese, "ja"),
    CHINESE(R.string.language_chinese, "zh"),
    KLINGON(R.string.language_klingon, "tlh"),
    RUSSIAN(R.string.language_russian, "ru"),
    PORTUGUESE(R.string.language_portuguese, "pt"),
    HINDI(R.string.language_hindi, "hi")
}

@Immutable
data class CalculationHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AppIconOption(
    val aliasClassName: String,
    val labelResId: Int,
    val descriptionResId: Int? = null,
    val iconResId: Int,
    val foregroundResId: Int,
    val backgroundResId: Int,
    val isMonochromeMaterialYou: Boolean = false,
    val requiresMinApi: Int = 0
) {
    CLASSIC(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityClassic",
        labelResId = R.string.app_icon_classic,
        iconResId = R.mipmap.ic_launcher,
        foregroundResId = R.drawable.ic_launcher_foreground,
        backgroundResId = R.drawable.ic_launcher_background
    ),
    MATERIAL_YOU(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityMaterialYou",
        labelResId = R.string.app_icon_material_you,
        descriptionResId = R.string.app_icon_material_you_desc,
        iconResId = R.mipmap.ic_launcher_material_you,
        foregroundResId = R.drawable.ic_launcher_foreground_material_you,
        backgroundResId = R.drawable.ic_launcher_background_material_you,
        isMonochromeMaterialYou = true,
        requiresMinApi = 33
    ),
    BLUE(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityBlue",
        labelResId = R.string.app_icon_blue,
        iconResId = R.mipmap.ic_launcher_blue,
        foregroundResId = R.drawable.ic_launcher_foreground_blue,
        backgroundResId = R.drawable.ic_launcher_background_blue
    ),
    PURPLE(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityPurple",
        labelResId = R.string.app_icon_purple,
        iconResId = R.mipmap.ic_launcher_purple,
        foregroundResId = R.drawable.ic_launcher_foreground_purple,
        backgroundResId = R.drawable.ic_launcher_background_purple
    ),
    GREEN(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityGreen",
        labelResId = R.string.app_icon_green,
        iconResId = R.mipmap.ic_launcher_green,
        foregroundResId = R.drawable.ic_launcher_foreground_green,
        backgroundResId = R.drawable.ic_launcher_background_green
    ),
    RED(
        aliasClassName = "com.limonatalp4.glasscalcproject.MainActivityRed",
        labelResId = R.string.app_icon_red,
        iconResId = R.mipmap.ic_launcher_red,
        foregroundResId = R.drawable.ic_launcher_foreground_red,
        backgroundResId = R.drawable.ic_launcher_background_red
    )
}

enum class AccentColorOption(
    val colorArgb: Int?,
    val labelResId: Int
) {
    DEFAULT(
        colorArgb = null,
        labelResId = R.string.accent_color_default
    ),
    INDIGO(
        colorArgb = 0xFF4F46E5.toInt(),
        labelResId = R.string.accent_color_indigo
    ),
    PURPLE(
        colorArgb = 0xFF9333EA.toInt(),
        labelResId = R.string.accent_color_purple
    ),
    TEAL(
        colorArgb = 0xFF0D9488.toInt(),
        labelResId = R.string.accent_color_teal
    ),
    GREEN(
        colorArgb = 0xFF16A34A.toInt(),
        labelResId = R.string.accent_color_green
    ),
    AMBER(
        colorArgb = 0xFFD97706.toInt(),
        labelResId = R.string.accent_color_amber
    ),
    CORAL(
        colorArgb = 0xFFEA580C.toInt(),
        labelResId = R.string.accent_color_coral
    ),
    PINK(
        colorArgb = 0xFFDB2777.toInt(),
        labelResId = R.string.accent_color_pink
    );

    companion object {
        fun fromArgb(argb: Int?): AccentColorOption {
            if (argb == null) return DEFAULT
            return entries.firstOrNull { it.colorArgb == argb } ?: DEFAULT
        }

        fun findPresetOrNull(argb: Int?): AccentColorOption? {
            if (argb == null) return DEFAULT
            return entries.firstOrNull { it.colorArgb == argb }
        }
    }
}

