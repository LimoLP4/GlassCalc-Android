package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import com.limonatalp4.glasscalcproject.BuildConfig
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AccentColorOption
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.Language
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties

enum class SettingsUnitCard { DEG, RAD }

enum class SettingsDevCard { SCREEN, QUICK_SETTINGS_TILE, API, HAPTIC, AUTO_RESULT, LIFECYCLE, CLEAR_CACHE, RESET_STORAGE }

enum class SettingsInfoCard { VERSION, GITHUB, FRANKFURTER_API, OSS, APACHE }

@Immutable
data class SettingsSearchVisibility(
    val matchingThemes: List<AppTheme>,
    val showAccentColorCard: Boolean = true,
    val matchingAppIcons: List<AppIconOption> = AppIconOption.entries,
    val showDegCard: Boolean,
    val showRadCard: Boolean,
    val showDevScreenCard: Boolean,
    val showDevApiCard: Boolean,
    val showDevQsTileCard: Boolean = true,
    val showDevHapticCard: Boolean,
    val showDevAutoResultCard: Boolean,
    val showDevLifecycleCard: Boolean,
    val showDevClearCacheCard: Boolean,
    val showDevResetStorageCard: Boolean,
    val showInfoVersionCard: Boolean,
    val showInfoGitHubCard: Boolean,
    val showInfoFrankfurterCard: Boolean = true,
    val showInfoOssCard: Boolean,
    val showInfoApacheCard: Boolean,
    val matchingLanguages: List<Language>,
    val orderedUnitCards: List<SettingsUnitCard> = SettingsUnitCard.entries,
    val orderedDevCards: List<SettingsDevCard> = SettingsDevCard.entries,
    val orderedInfoCards: List<SettingsInfoCard> = SettingsInfoCard.entries,
    val bestTabIndex: Int? = null
) {
    val hasAnyAppearanceMatch: Boolean
        get() = matchingThemes.isNotEmpty() || showAccentColorCard || matchingAppIcons.isNotEmpty()

    val hasAnyUnitsMatch: Boolean
        get() = showDegCard || showRadCard

    val hasAnyDeveloperMatch: Boolean
        get() = showDevScreenCard ||
            showDevApiCard ||
            showDevQsTileCard ||
            showDevHapticCard ||
            showDevAutoResultCard ||
            showDevLifecycleCard ||
            showDevClearCacheCard ||
            showDevResetStorageCard

    val hasAnyInfoMatch: Boolean
        get() = showInfoVersionCard ||
            showInfoGitHubCard ||
            showInfoFrankfurterCard ||
            showInfoOssCard ||
            showInfoApacheCard

    val hasAnyLanguageMatch: Boolean
        get() = matchingLanguages.isNotEmpty()

    val hasAnyMatch: Boolean
        get() = hasAnyAppearanceMatch ||
            hasAnyUnitsMatch ||
            hasAnyDeveloperMatch ||
            hasAnyInfoMatch ||
            hasAnyLanguageMatch
}

fun matchesSettingsSearch(
    query: String,
    vararg searchableTexts: String
): Boolean {
    val tokens = tokenizeSearchQuery(query)
    if (tokens.isEmpty()) return true
    val expanded = searchableTexts.flatMap { splitSearchKeywords(it) }
    if (expanded.isEmpty()) return false

    return computeMatchScore(
        tokens = tokens,
        primaryNames = listOf(expanded.first()),
        secondaryNames = expanded.drop(1)
    ) != null
}

private fun scoreSettingsEntry(
    tokens: List<String>,
    primaryNames: List<String>,
    secondaryTexts: List<String> = emptyList(),
    keywordStrings: List<String> = emptyList()
): Int? {
    val keywords = keywordStrings.flatMap(::splitSearchKeywords)
    return computeMatchScore(
        tokens = tokens,
        primaryNames = primaryNames,
        secondaryNames = secondaryTexts,
        keywords = keywords
    )
}

@Composable
fun rememberSettingsSearchVisibility(
    query: String,
    effectiveApiLevel: Int,
    isMaterialYouActive: Boolean = false
): SettingsSearchVisibility {
    val tokens = tokenizeSearchQuery(query)
    if (tokens.isEmpty()) {
        return SettingsSearchVisibility(
            matchingThemes = AppTheme.entries,
            showAccentColorCard = !isMaterialYouActive,
            matchingAppIcons = AppIconOption.entries,
            showDegCard = true,
            showRadCard = true,
            showDevScreenCard = true,
            showDevApiCard = true,
            showDevQsTileCard = true,
            showDevHapticCard = true,
            showDevAutoResultCard = true,
            showDevLifecycleCard = true,
            showDevClearCacheCard = true,
            showDevResetStorageCard = true,
            showInfoVersionCard = true,
            showInfoGitHubCard = true,
            showInfoFrankfurterCard = true,
            showInfoOssCard = true,
            showInfoApacheCard = true,
            matchingLanguages = Language.entries,
            orderedUnitCards = SettingsUnitCard.entries,
            orderedDevCards = SettingsDevCard.entries,
            orderedInfoCards = SettingsInfoCard.entries,
            bestTabIndex = null
        )
    }

    val appearanceTabTitle = stringResource(R.string.title_appearance)
    val themeAccordionTitle = stringResource(R.string.theme_accordion_title)
    val appearanceKeywords = stringResource(R.string.settings_keywords_appearance)
    val materialYouUnsupported = stringResource(R.string.theme_material_you_unsupported, effectiveApiLevel)
    val amoledDesc = stringResource(R.string.theme_amoled_desc)

    val themeKwMaterialYou = stringResource(R.string.settings_keywords_theme_material_you)
    val themeKwAutomatic = stringResource(R.string.settings_keywords_theme_automatic)
    val themeKwWhite = stringResource(R.string.settings_keywords_theme_white)
    val themeKwStandardDark = stringResource(R.string.settings_keywords_theme_standard_dark)
    val themeKwDark = stringResource(R.string.settings_keywords_theme_dark)

    val scoredThemes = AppTheme.entries.mapIndexedNotNull { index, theme ->
        val label = stringResource(theme.stringResId)
        val extra = when (theme) {
            AppTheme.MATERIAL_YOU -> listOf(materialYouUnsupported)
            AppTheme.DARK_MODE -> listOf(amoledDesc)
            else -> emptyList()
        }
        val themeKw = when (theme) {
            AppTheme.MATERIAL_YOU -> themeKwMaterialYou
            AppTheme.AUTOMATIC -> themeKwAutomatic
            AppTheme.WHITE_MODE -> themeKwWhite
            AppTheme.STANDARD_DARK -> themeKwStandardDark
            AppTheme.DARK_MODE -> themeKwDark
        }
        val score = scoreSettingsEntry(
            tokens = tokens,
            primaryNames = listOf(label),
            secondaryTexts = extra + listOf(themeAccordionTitle, appearanceTabTitle),
            keywordStrings = listOf(themeKw, appearanceKeywords)
        ) ?: return@mapIndexedNotNull null
        Triple(theme, score, index)
    }.sortedWith(compareBy<Triple<AppTheme, Int, Int>> { it.second }.thenBy { it.third })
    val matchingThemes = scoredThemes.map { it.first }
    val bestThemeScore = scoredThemes.minOfOrNull { it.second }

    val appIconAccordionTitle = stringResource(R.string.app_icon_accordion_title)
    val appIconHeading = stringResource(R.string.app_icon_select_heading)
    val appIconMaterialYouUnsupported = stringResource(
        R.string.app_icon_material_you_unsupported,
        minOf(android.os.Build.VERSION.SDK_INT, effectiveApiLevel)
    )
    val appIconKeywords = stringResource(R.string.settings_keywords_app_icon)
    val appIconKwClassic = stringResource(R.string.settings_keywords_app_icon_classic)
    val appIconKwMaterialYou = stringResource(R.string.settings_keywords_app_icon_material_you)
    val appIconKwBlue = stringResource(R.string.settings_keywords_app_icon_blue)
    val appIconKwPurple = stringResource(R.string.settings_keywords_app_icon_purple)
    val appIconKwGreen = stringResource(R.string.settings_keywords_app_icon_green)
    val appIconKwRed = stringResource(R.string.settings_keywords_app_icon_red)

    val scoredAppIcons = AppIconOption.entries.mapIndexedNotNull { index, iconOption ->
        val label = stringResource(iconOption.labelResId)
        val extraDesc = buildList {
            iconOption.descriptionResId?.let { add(stringResource(it)) }
            if (iconOption.requiresMinApi > 0) {
                add(appIconMaterialYouUnsupported)
            }
        }
        val iconKw = when (iconOption) {
            AppIconOption.CLASSIC -> appIconKwClassic
            AppIconOption.MATERIAL_YOU -> appIconKwMaterialYou
            AppIconOption.BLUE -> appIconKwBlue
            AppIconOption.PURPLE -> appIconKwPurple
            AppIconOption.GREEN -> appIconKwGreen
            AppIconOption.RED -> appIconKwRed
        }
        val score = scoreSettingsEntry(
            tokens = tokens,
            primaryNames = listOf(label),
            secondaryTexts = extraDesc + listOf(appIconAccordionTitle, appIconHeading, appearanceTabTitle),
            keywordStrings = listOf(iconKw, appIconKeywords, appearanceKeywords)
        ) ?: return@mapIndexedNotNull null
        Triple(iconOption, score, index)
    }.sortedWith(compareBy<Triple<AppIconOption, Int, Int>> { it.second }.thenBy { it.third })
    val matchingAppIcons = scoredAppIcons.map { it.first }
    val bestAppIconScore = scoredAppIcons.minOfOrNull { it.second }

    val accentColorAccordionTitle = stringResource(R.string.accent_color_accordion_title)
    val accentColorDesc = stringResource(R.string.accent_color_desc)
    val accentColorHexLabel = stringResource(R.string.accent_color_hex_label)
    val accentColorRecentLabel = stringResource(R.string.accent_color_recent_label)
    val accentColorKeywords = stringResource(R.string.settings_keywords_accent_color)
    val accentColorNames = AccentColorOption.entries.map { stringResource(it.labelResId) }
    val accentColorScore = if (!isMaterialYouActive) {
        scoreSettingsEntry(
            tokens = tokens,
            primaryNames = listOf(accentColorAccordionTitle),
            secondaryTexts = accentColorNames + listOf(accentColorDesc, accentColorHexLabel, accentColorRecentLabel, appearanceTabTitle),
            keywordStrings = listOf(accentColorKeywords, appearanceKeywords)
        )
    } else {
        null
    }

    val bestAppearanceScore = listOfNotNull(bestThemeScore, accentColorScore, bestAppIconScore).minOfOrNull { it }

    val unitsTabTitle = stringResource(R.string.title_units)
    val unitsHeading = stringResource(R.string.units_angle_heading)
    val angleModeAccordionTitle = stringResource(R.string.angle_mode_accordion_title)
    val unitsKeywords = stringResource(R.string.settings_keywords_units)

    val degScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.angle_deg_label)),
        secondaryTexts = listOf(
            stringResource(R.string.angle_deg_desc),
            angleModeAccordionTitle,
            unitsHeading,
            unitsTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_angle_deg), unitsKeywords)
    )
    val radScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.angle_rad_label)),
        secondaryTexts = listOf(
            stringResource(R.string.angle_rad_desc),
            angleModeAccordionTitle,
            unitsHeading,
            unitsTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_angle_rad), unitsKeywords)
    )
    val scoredUnitCards = listOfNotNull(
        degScore?.let { Triple(SettingsUnitCard.DEG, it, 0) },
        radScore?.let { Triple(SettingsUnitCard.RAD, it, 1) }
    ).sortedWith(compareBy<Triple<SettingsUnitCard, Int, Int>> { it.second }.thenBy { it.third })
    val orderedUnitCards = scoredUnitCards.map { it.first }
    val bestUnitsScore = scoredUnitCards.minOfOrNull { it.second }

    val devTabTitle = stringResource(R.string.title_dev_options)
    val devAccordionSystemApi = stringResource(R.string.dev_accordion_system_api)
    val devAccordionHapticFeedback = stringResource(R.string.dev_accordion_haptic_feedback)
    val devAccordionAppBehavior = stringResource(R.string.dev_accordion_app_behavior)
    val devAccordionStorageCache = stringResource(R.string.dev_accordion_storage_cache)
    val devScreenScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_screen_scaling)),
        secondaryTexts = listOf("WindowSizeClass", devAccordionSystemApi, devTabTitle),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_screen))
    )
    val devApiScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_simulated_api_title)),
        secondaryTexts = listOf(
            stringResource(R.string.dev_simulate_api_switch),
            stringResource(R.string.dev_feature_vibration_effect),
            stringResource(R.string.dev_feature_edge_to_edge),
            stringResource(R.string.dev_feature_material_you),
            stringResource(R.string.dev_feature_blur_effect),
            stringResource(R.string.dev_feature_swipe_gestures),
            devAccordionSystemApi,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_api))
    )
    val devHapticScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_haptic_title)),
        secondaryTexts = listOf(
            stringResource(R.string.dev_haptic_desc),
            stringResource(R.string.dev_haptic_detected_motor),
            stringResource(R.string.dev_haptic_intensity_label),
            devAccordionHapticFeedback,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_haptic))
    )
    val devAutoResultScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_auto_result_title)),
        secondaryTexts = listOf(stringResource(R.string.dev_auto_result_desc), devAccordionAppBehavior, devTabTitle),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_auto_result))
    )
    val devLifecycleScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_lifecycle_title)),
        secondaryTexts = listOf(
            stringResource(R.string.dev_btn_background),
            stringResource(R.string.dev_btn_crash_test),
            devAccordionAppBehavior,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_lifecycle))
    )
    val devClearCacheScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_clear_cache_title)),
        secondaryTexts = listOf(
            stringResource(R.string.dev_clear_cache_desc),
            stringResource(R.string.dev_btn_clear_cache),
            devAccordionStorageCache,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_cache))
    )
    val devResetStorageScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.dev_reset_storage_title)),
        secondaryTexts = listOf(
            stringResource(R.string.dev_reset_storage_desc),
            stringResource(R.string.dev_btn_reset_storage),
            devAccordionStorageCache,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_dev_reset))
    )
    val devQsTileScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(
            stringResource(R.string.info_qs_tile_title),
            stringResource(R.string.info_qs_tile_btn)
        ),
        secondaryTexts = listOf(
            stringResource(R.string.info_qs_tile_desc),
            devAccordionSystemApi,
            devTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_info_qs_tile))
    )
    val scoredDevCards = listOfNotNull(
        devScreenScore?.let { Triple(SettingsDevCard.SCREEN, it, 0) },
        devQsTileScore?.let { Triple(SettingsDevCard.QUICK_SETTINGS_TILE, it, 1) },
        devApiScore?.let { Triple(SettingsDevCard.API, it, 2) },
        devHapticScore?.let { Triple(SettingsDevCard.HAPTIC, it, 3) },
        devAutoResultScore?.let { Triple(SettingsDevCard.AUTO_RESULT, it, 4) },
        devLifecycleScore?.let { Triple(SettingsDevCard.LIFECYCLE, it, 5) },
        devClearCacheScore?.let { Triple(SettingsDevCard.CLEAR_CACHE, it, 6) },
        devResetStorageScore?.let { Triple(SettingsDevCard.RESET_STORAGE, it, 7) }
    ).sortedWith(compareBy<Triple<SettingsDevCard, Int, Int>> { it.second }.thenBy { it.third })
    val orderedDevCards = scoredDevCards.map { it.first }
    val bestDevScore = scoredDevCards.minOfOrNull { it.second }

    val infoTabTitle = stringResource(R.string.title_info)
    val infoVersionScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf("GlassCalc v${BuildConfig.VERSION_NAME}"),
        secondaryTexts = listOf(
            stringResource(R.string.app_version_subtitle),
            stringResource(R.string.copyright_notice),
            infoTabTitle
        ),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_info_version))
    )
    val infoGitHubScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.info_github_repo), "GitHub"),
        secondaryTexts = listOf("LimoLP4/GlassCalc-Android", infoTabTitle),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_info_github))
    )
    val infoFrankfurterScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.info_oss_frankfurter_title)),
        secondaryTexts = listOf(
            stringResource(R.string.info_frankfurter_desc),
            stringResource(R.string.info_oss_frankfurter_license),
            "https://www.frankfurter.app",
            infoTabTitle
        ),
        keywordStrings = listOf(
            stringResource(R.string.settings_keywords_info_license),
            "frankfurter,api,währung,currency,ecb,ezb,mit"
        )
    )
    val infoOssScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf(stringResource(R.string.info_oss_licenses)),
        secondaryTexts = listOf(
            stringResource(R.string.info_oss_licenses_desc),
            stringResource(R.string.info_btn_oss_licenses),
            infoTabTitle
        ),
        keywordStrings = listOf(
            stringResource(R.string.settings_keywords_info_oss),
            stringResource(R.string.settings_keywords_info_license)
        )
    )
    val infoApacheScore = scoreSettingsEntry(
        tokens = tokens,
        primaryNames = listOf("Apache License 2.0"),
        secondaryTexts = listOf(infoTabTitle),
        keywordStrings = listOf(stringResource(R.string.settings_keywords_info_license))
    )
    val scoredInfoCards = listOfNotNull(
        infoVersionScore?.let { Triple(SettingsInfoCard.VERSION, it, 0) },
        infoGitHubScore?.let { Triple(SettingsInfoCard.GITHUB, it, 1) },
        infoFrankfurterScore?.let { Triple(SettingsInfoCard.FRANKFURTER_API, it, 2) },
        infoOssScore?.let { Triple(SettingsInfoCard.OSS, it, 3) },
        infoApacheScore?.let { Triple(SettingsInfoCard.APACHE, it, 4) }
    ).sortedWith(compareBy<Triple<SettingsInfoCard, Int, Int>> { it.second }.thenBy { it.third })
    val orderedInfoCards = scoredInfoCards.map { it.first }
    val bestInfoScore = scoredInfoCards.minOfOrNull { it.second }

    val languageTabTitle = stringResource(R.string.tab_language)
    val languageAccordionTitle = stringResource(R.string.language_accordion_title)
    val languageKeywords = stringResource(R.string.settings_keywords_language)
    val scoredLanguages = Language.entries.mapIndexedNotNull { index, lang ->
        val label = stringResource(lang.stringResId)
        val nativeTokens = when (lang) {
            Language.SYSTEM -> "system,default"
            Language.GERMAN -> "deutsch,de"
            Language.ENGLISH -> "english,en"
            Language.SPANISH -> "español,es"
            Language.FRENCH -> "français,fr"
            Language.JAPANESE -> "日本語,ja"
            Language.CHINESE -> "中文,zh"
            Language.KLINGON -> "tlhIngan Hol,klingon,tlh"
            Language.RUSSIAN -> "русский,ru"
            Language.PORTUGUESE -> "português,pt"
            Language.HINDI -> "हिन्दी,hi"
        }
        val score = scoreSettingsEntry(
            tokens = tokens,
            primaryNames = listOf(label),
            secondaryTexts = listOf(languageAccordionTitle, languageTabTitle),
            keywordStrings = listOf(nativeTokens, languageKeywords)
        ) ?: return@mapIndexedNotNull null
        Triple(lang, score, index)
    }.sortedWith(compareBy<Triple<Language, Int, Int>> { it.second }.thenBy { it.third })
    val matchingLanguages = scoredLanguages.map { it.first }
    val bestLanguageScore = scoredLanguages.minOfOrNull { it.second }

    val bestTabIndex = listOfNotNull(
        bestAppearanceScore?.let { 0 to it },
        bestUnitsScore?.let { 1 to it },
        bestDevScore?.let { 2 to it },
        bestInfoScore?.let { 3 to it },
        bestLanguageScore?.let { 4 to it }
    ).minByOrNull { it.second }?.first

    return SettingsSearchVisibility(
        matchingThemes = matchingThemes,
        showAccentColorCard = accentColorScore != null,
        matchingAppIcons = matchingAppIcons,
        showDegCard = degScore != null,
        showRadCard = radScore != null,
        showDevScreenCard = devScreenScore != null,
        showDevApiCard = devApiScore != null,
        showDevQsTileCard = devQsTileScore != null,
        showDevHapticCard = devHapticScore != null,
        showDevAutoResultCard = devAutoResultScore != null,
        showDevLifecycleCard = devLifecycleScore != null,
        showDevClearCacheCard = devClearCacheScore != null,
        showDevResetStorageCard = devResetStorageScore != null,
        showInfoVersionCard = infoVersionScore != null,
        showInfoGitHubCard = infoGitHubScore != null,
        showInfoFrankfurterCard = infoFrankfurterScore != null,
        showInfoOssCard = infoOssScore != null,
        showInfoApacheCard = infoApacheScore != null,
        matchingLanguages = matchingLanguages,
        orderedUnitCards = orderedUnitCards,
        orderedDevCards = orderedDevCards,
        orderedInfoCards = orderedInfoCards,
        bestTabIndex = bestTabIndex
    )
}

@Composable
fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = GlassDialogDefaults.PillShape
    val searchBarBgColor = GlassDialogDefaults.searchBarBackgroundColor(isTiefschwarz)
    val searchBarBorder = LocalGlassProperties.current.containerBorderStroke
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant

    val context = LocalContext.current
    val view = LocalView.current
    val density = LocalDensity.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

    val focusRequester = remember { FocusRequester() }
    val pillInteractionSource = remember { MutableInteractionSource() }
    val textFieldInteractionSource = remember { MutableInteractionSource() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isSearchFocused by remember { mutableStateOf(false) }
    val imeBottomPx = WindowInsets.ime.getBottom(density)

    LaunchedEffect(isSearchFocused, imeBottomPx) {
        if (isSearchFocused) {
            delay(80)
            bringIntoViewRequester.bringIntoView()
        }
    }

    LaunchedEffect(textFieldInteractionSource) {
        textFieldInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                HapticUtil.performAppHaptic(
                    context = context,
                    view = view,
                    enabled = currentHapticEnabled,
                    intensityLevel = currentHapticIntensity
                )
            }
        }
    }

    val iconPillShape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(GlassDialogDefaults.SearchBarHeight)
            .bringIntoViewRequester(bringIntoViewRequester)
            .clip(shape)
            .background(searchBarBgColor)
            .border(searchBarBorder, shape = shape)
            .clickable(
                interactionSource = pillInteractionSource,
                indication = null
            ) {
                HapticUtil.performAppHaptic(
                    context = context,
                    view = view,
                    enabled = currentHapticEnabled,
                    intensityLevel = currentHapticIntensity
                )
                focusRequester.requestFocus()
            }
            .padding(
                start = 6.dp,
                end = if (query.isNotEmpty()) 0.dp else 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(iconPillShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = iconPillShape
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                interactionSource = textFieldInteractionSource,
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                    }
                    .testTag("settings_search_input"),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.settings_search_placeholder),
                                color = hintColor,
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                style = TextStyle(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        if (query.isNotEmpty()) {
            val clearInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onClearClick,
                interactionSource = clearInteraction,
                modifier = Modifier
                    .size(48.dp)
                    .pressScale(clearInteraction)
                    .testTag("btn_clear_settings_search")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(iconPillShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            shape = iconPillShape
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.settings_search_clear),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSearchEmptyState(
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_search_empty"),
        shape = GlassDialogDefaults.SearchEmptyStateShape,
        customBgColor = if (isTiefschwarz) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.45f),
        borderStroke = LocalGlassProperties.current.containerBorderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_search_empty),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
