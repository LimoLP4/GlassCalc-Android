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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import com.limonatalp4.glasscalcproject.BuildConfig
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.Language

@Immutable
data class SettingsSearchVisibility(
    val matchingThemes: List<AppTheme>,
    val showDegCard: Boolean,
    val showRadCard: Boolean,
    val showUnitsSystemsCard: Boolean,
    val showDevScreenCard: Boolean,
    val showDevApiCard: Boolean,
    val showDevHapticCard: Boolean,
    val showDevAutoResultCard: Boolean,
    val showDevLifecycleCard: Boolean,
    val showDevClearCacheCard: Boolean,
    val showDevResetStorageCard: Boolean,
    val showInfoVersionCard: Boolean,
    val showInfoGitHubCard: Boolean,
    val showInfoOssCard: Boolean,
    val showInfoApacheCard: Boolean,
    val matchingLanguages: List<Language>
) {
    val hasAnyAppearanceMatch: Boolean
        get() = matchingThemes.isNotEmpty()

    val hasAnyUnitsMatch: Boolean
        get() = showDegCard || showRadCard || showUnitsSystemsCard

    val hasAnyDeveloperMatch: Boolean
        get() = showDevScreenCard ||
            showDevApiCard ||
            showDevHapticCard ||
            showDevAutoResultCard ||
            showDevLifecycleCard ||
            showDevClearCacheCard ||
            showDevResetStorageCard

    val hasAnyInfoMatch: Boolean
        get() = showInfoVersionCard || showInfoGitHubCard || showInfoOssCard || showInfoApacheCard

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
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return true

    val candidates = searchableTexts.flatMap { text ->
        text.split(',', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
    if (candidates.isEmpty()) return false

    return tokens.all { token ->
        candidates.any { candidate -> candidate.contains(token, ignoreCase = true) }
    }
}

@Composable
fun rememberSettingsSearchVisibility(
    query: String,
    effectiveApiLevel: Int
): SettingsSearchVisibility {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) {
        return SettingsSearchVisibility(
            matchingThemes = AppTheme.entries,
            showDegCard = true,
            showRadCard = true,
            showUnitsSystemsCard = true,
            showDevScreenCard = true,
            showDevApiCard = true,
            showDevHapticCard = true,
            showDevAutoResultCard = true,
            showDevLifecycleCard = true,
            showDevClearCacheCard = true,
            showDevResetStorageCard = true,
            showInfoVersionCard = true,
            showInfoGitHubCard = true,
            showInfoOssCard = true,
            showInfoApacheCard = true,
            matchingLanguages = Language.entries
        )
    }

    val appearanceTabTitle = stringResource(R.string.title_appearance)
    val appearanceKeywords = stringResource(R.string.settings_keywords_appearance)
    val amoledDesc = stringResource(R.string.theme_amoled_desc)
    val materialYouUnsupported = stringResource(R.string.theme_material_you_unsupported, effectiveApiLevel)

    val matchingThemes = AppTheme.entries.filter { theme ->
        val label = stringResource(theme.stringResId)
        val extra = when (theme) {
            AppTheme.AMOLED_DARK -> amoledDesc
            AppTheme.MATERIAL_YOU -> materialYouUnsupported
            else -> ""
        }
        matchesSettingsSearch(
            trimmed,
            label,
            extra,
            appearanceTabTitle,
            appearanceKeywords
        )
    }

    val unitsTabTitle = stringResource(R.string.title_units)
    val unitsHeading = stringResource(R.string.units_angle_heading)
    val unitsKeywords = stringResource(R.string.settings_keywords_units)
    val showDegCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.angle_deg_label),
        stringResource(R.string.angle_deg_desc),
        unitsHeading,
        unitsTabTitle,
        unitsKeywords
    )
    val showRadCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.angle_rad_label),
        stringResource(R.string.angle_rad_desc),
        unitsHeading,
        unitsTabTitle,
        unitsKeywords
    )
    val showUnitsSystemsCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.units_supported_systems),
        stringResource(R.string.units_systems_detail),
        unitsTabTitle,
        unitsKeywords
    )

    val devTabTitle = stringResource(R.string.title_dev_options)
    val showDevScreenCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_screen_scaling),
        "WindowSizeClass",
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_screen)
    )
    val showDevApiCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_simulated_api_title),
        stringResource(R.string.dev_simulate_api_switch),
        stringResource(R.string.dev_feature_vibration_effect),
        stringResource(R.string.dev_feature_edge_to_edge),
        stringResource(R.string.dev_feature_material_you),
        stringResource(R.string.dev_feature_blur_effect),
        stringResource(R.string.dev_feature_swipe_gestures),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_api)
    )
    val showDevHapticCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_haptic_title),
        stringResource(R.string.dev_haptic_desc),
        stringResource(R.string.dev_haptic_detected_motor),
        stringResource(R.string.dev_haptic_intensity_label),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_haptic)
    )
    val showDevAutoResultCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_auto_result_title),
        stringResource(R.string.dev_auto_result_desc),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_auto_result)
    )
    val showDevLifecycleCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_lifecycle_title),
        stringResource(R.string.dev_btn_background),
        stringResource(R.string.dev_btn_crash_test),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_lifecycle)
    )
    val showDevClearCacheCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_clear_cache_title),
        stringResource(R.string.dev_clear_cache_desc),
        stringResource(R.string.dev_btn_clear_cache),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_cache)
    )
    val showDevResetStorageCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.dev_reset_storage_title),
        stringResource(R.string.dev_reset_storage_desc),
        stringResource(R.string.dev_btn_reset_storage),
        devTabTitle,
        stringResource(R.string.settings_keywords_dev_reset)
    )

    val infoTabTitle = stringResource(R.string.title_info)
    val showInfoVersionCard = matchesSettingsSearch(
        trimmed,
        "GlassCalc v${BuildConfig.VERSION_NAME}",
        stringResource(R.string.app_version_subtitle),
        stringResource(R.string.copyright_notice),
        infoTabTitle,
        stringResource(R.string.settings_keywords_info_version)
    )
    val showInfoGitHubCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.info_github_repo),
        "GitHub",
        "LimoLP4/GlassCalc-Android",
        infoTabTitle
    )
    val showInfoOssCard = matchesSettingsSearch(
        trimmed,
        stringResource(R.string.info_oss_licenses),
        stringResource(R.string.info_oss_licenses_desc),
        stringResource(R.string.info_btn_oss_licenses),
        infoTabTitle
    )
    val showInfoApacheCard = matchesSettingsSearch(
        trimmed,
        "Apache License 2.0",
        infoTabTitle,
        stringResource(R.string.settings_keywords_info_license)
    )

    val languageTabTitle = stringResource(R.string.tab_language)
    val languageKeywords = stringResource(R.string.settings_keywords_language)
    val matchingLanguages = Language.entries.filter { lang ->
        val label = stringResource(lang.stringResId)
        matchesSettingsSearch(
            trimmed,
            label,
            languageTabTitle,
            languageKeywords
        )
    }

    return SettingsSearchVisibility(
        matchingThemes = matchingThemes,
        showDegCard = showDegCard,
        showRadCard = showRadCard,
        showUnitsSystemsCard = showUnitsSystemsCard,
        showDevScreenCard = showDevScreenCard,
        showDevApiCard = showDevApiCard,
        showDevHapticCard = showDevHapticCard,
        showDevAutoResultCard = showDevAutoResultCard,
        showDevLifecycleCard = showDevLifecycleCard,
        showDevClearCacheCard = showDevClearCacheCard,
        showDevResetStorageCard = showDevResetStorageCard,
        showInfoVersionCard = showInfoVersionCard,
        showInfoGitHubCard = showInfoGitHubCard,
        showInfoOssCard = showInfoOssCard,
        showInfoApacheCard = showInfoApacheCard,
        matchingLanguages = matchingLanguages
    )
}

@Composable
fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isAmoled: Boolean,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(percent = 50)
    val searchBarBgColor = when {
        isAmoled -> Color(0xFF0A0A0A)
        isTiefschwarz -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val searchBarBorder = when {
        isAmoled -> BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        isTiefschwarz -> BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        else -> BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
    }
    val textColor = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
    val hintColor = if (isAmoled || isTiefschwarz) {
        Color.White.copy(alpha = 0.6f)
    } else {
        colorScheme.onSurfaceVariant
    }

    val context = LocalContext.current
    val view = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
    val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

    val focusRequester = remember { FocusRequester() }
    val pillInteractionSource = remember { MutableInteractionSource() }
    val textFieldInteractionSource = remember { MutableInteractionSource() }

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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
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
                start = 14.dp,
                end = if (query.isNotEmpty()) 4.dp else 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = hintColor,
            modifier = Modifier.size(18.dp)
        )
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
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
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
                                fontSize = 14.sp,
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
            IconButton(
                onClick = onClearClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("btn_clear_settings_search")
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.settings_search_clear),
                    tint = hintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsSearchEmptyState(
    isAmoled: Boolean,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_search_empty"),
        shape = RoundedCornerShape(22.dp),
        borderWidth = 1.dp,
        customBgColor = when {
            isAmoled -> Color.Black
            isTiefschwarz -> Color(0xFF121212)
            else -> colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        customBorderColor = when {
            isAmoled -> Color.White.copy(alpha = 0.15f)
            isTiefschwarz -> Color.White.copy(alpha = 0.1f)
            else -> colorScheme.outline.copy(alpha = 0.25f)
        }
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
                tint = if (isAmoled || isTiefschwarz) {
                    Color.White.copy(alpha = 0.6f)
                } else {
                    colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_search_empty),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isAmoled || isTiefschwarz) {
                    Color.White.copy(alpha = 0.75f)
                } else {
                    colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
