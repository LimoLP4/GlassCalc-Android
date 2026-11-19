package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.domain.converter.ConversionPair
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import java.util.Locale

@Immutable
data class UnitItem(
    val id: String,
    val symbol: String,
    val category: UnitCategory,
    val nameResId: Int,
    val defaultPair: ConversionPair,
    val keywordsResId: Int
)

val ALL_UNIT_ITEMS: List<UnitItem> by lazy {
    listOf(
        // Length
        UnitItem("meter", "m", UnitCategory.LENGTH, R.string.unit_name_meter, UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }, R.string.unit_keywords_meter),
        UnitItem("feet", "ft", UnitCategory.LENGTH, R.string.unit_name_feet, UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }, R.string.unit_keywords_feet),
        UnitItem("centimeter", "cm", UnitCategory.LENGTH, R.string.unit_name_centimeter, UnitConverter.CONVERSIONS.first { it.unitFrom == "cm" && it.unitTo == "in" }, R.string.unit_keywords_centimeter),
        UnitItem("inch", "in", UnitCategory.LENGTH, R.string.unit_name_inch, UnitConverter.CONVERSIONS.first { it.unitFrom == "cm" && it.unitTo == "in" }, R.string.unit_keywords_inch),
        UnitItem("kilometer", "km", UnitCategory.LENGTH, R.string.unit_name_kilometer, UnitConverter.CONVERSIONS.first { it.unitFrom == "km" && it.unitTo == "mi" }, R.string.unit_keywords_kilometer),
        UnitItem("mile", "mi", UnitCategory.LENGTH, R.string.unit_name_mile, UnitConverter.CONVERSIONS.first { it.unitFrom == "km" && it.unitTo == "mi" }, R.string.unit_keywords_mile),

        // Mass
        UnitItem("kilogram", "kg", UnitCategory.MASS, R.string.unit_name_kilogram, UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "lb" }, R.string.unit_keywords_kilogram),
        UnitItem("pound", "lb", UnitCategory.MASS, R.string.unit_name_pound, UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "lb" }, R.string.unit_keywords_pound),

        // Temperature
        UnitItem("celsius", "°C", UnitCategory.TEMPERATURE, R.string.unit_name_celsius, UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }, R.string.unit_keywords_celsius),
        UnitItem("fahrenheit", "°F", UnitCategory.TEMPERATURE, R.string.unit_name_fahrenheit, UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }, R.string.unit_keywords_fahrenheit),

        // Volume
        UnitItem("liter", "l", UnitCategory.VOLUME, R.string.unit_name_liter, UnitConverter.CONVERSIONS.first { it.unitFrom == "l" && it.unitTo == "gal" }, R.string.unit_keywords_liter),
        UnitItem("gallon", "gal", UnitCategory.VOLUME, R.string.unit_name_gallon, UnitConverter.CONVERSIONS.first { it.unitFrom == "l" && it.unitTo == "gal" }, R.string.unit_keywords_gallon),

        // Data
        UnitItem("byte", "B", UnitCategory.DATA, R.string.unit_name_byte, UnitConverter.CONVERSIONS.first { it.unitFrom == "B" && it.unitTo == "KB" }, R.string.unit_keywords_byte),
        UnitItem("kilobyte", "KB", UnitCategory.DATA, R.string.unit_name_kilobyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "KB" && it.unitTo == "MB" }, R.string.unit_keywords_kilobyte),
        UnitItem("megabyte", "MB", UnitCategory.DATA, R.string.unit_name_megabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "MB" && it.unitTo == "GB" }, R.string.unit_keywords_megabyte),
        UnitItem("gigabyte", "GB", UnitCategory.DATA, R.string.unit_name_gigabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }, R.string.unit_keywords_gigabyte),
        UnitItem("terabyte", "TB", UnitCategory.DATA, R.string.unit_name_terabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }, R.string.unit_keywords_terabyte),

        // Time
        UnitItem("second", "s", UnitCategory.TIME, R.string.unit_name_seconds, UnitConverter.CONVERSIONS.first { it.unitFrom == "s" && it.unitTo == "ms" }, R.string.unit_keywords_second),
        UnitItem("millisecond", "ms", UnitCategory.TIME, R.string.unit_name_milliseconds, UnitConverter.CONVERSIONS.first { it.unitFrom == "s" && it.unitTo == "ms" }, R.string.unit_keywords_millisecond),
        UnitItem("minute", "min", UnitCategory.TIME, R.string.unit_name_minutes, UnitConverter.CONVERSIONS.first { it.unitFrom == "min" && it.unitTo == "s" }, R.string.unit_keywords_minute),
        UnitItem("hour", "h", UnitCategory.TIME, R.string.unit_name_hours, UnitConverter.CONVERSIONS.first { it.unitFrom == "h" && it.unitTo == "min" }, R.string.unit_keywords_hour),
        UnitItem("day", "d", UnitCategory.TIME, R.string.unit_name_days, UnitConverter.CONVERSIONS.first { it.unitFrom == "d" && it.unitTo == "h" }, R.string.unit_keywords_day)
    )
}

@Immutable
data class LocalizedUnitSuggestion(
    val item: UnitItem,
    val localizedName: String,
    val symbol: String,
    val localizedKeywords: List<String> = emptyList()
)

fun filterUnitSuggestions(
    query: String,
    suggestions: List<LocalizedUnitSuggestion>
): List<LocalizedUnitSuggestion> {
    val tokens = tokenizeSearchQuery(query)
    if (tokens.isEmpty()) return emptyList()

    return suggestions.mapNotNull { suggestion ->
        val score = computeMatchScore(
            tokens = tokens,
            primaryNames = listOf(suggestion.localizedName, suggestion.symbol),
            keywords = suggestion.localizedKeywords
        ) ?: return@mapNotNull null
        suggestion to score
    }
    .sortedWith(
        compareBy<Pair<LocalizedUnitSuggestion, Int>> { it.second }
            .thenBy { it.first.localizedName.lowercase(Locale.ROOT) }
    )
    .map { it.first }
}

@Composable
fun UnitConverterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier,
    onSearchSubmit: (() -> Unit)? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = GlassDialogDefaults.PillShape
    val searchBarBgColor = GlassDialogDefaults.searchBarBackgroundColor(isTiefschwarz)
    val searchBarBorder = LocalGlassProperties.current.containerBorderStroke
    val textColor = if (isTiefschwarz) Color.White else colorScheme.onSurface
    val hintColor = if (isTiefschwarz) {
        Color.White.copy(alpha = 0.6f)
    } else {
        colorScheme.onSurfaceVariant
    }

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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchSubmit?.invoke()
                    }
                ),
                textStyle = TextStyle(
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        isSearchFocused = focusState.isFocused
                    }
                    .testTag("converter_search_input"),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.converter_search_placeholder),
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
            val clearInteraction = remember { MutableInteractionSource() }
            IconButton(
                onClick = onClearClick,
                interactionSource = clearInteraction,
                modifier = Modifier
                    .size(48.dp)
                    .pressScale(clearInteraction)
                    .testTag("btn_clear_converter_search")
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = stringResource(R.string.converter_search_clear),
                    tint = hintColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun UnitSuggestionsDropdown(
    suggestions: List<LocalizedUnitSuggestion>,
    onSelectSuggestion: (LocalizedUnitSuggestion) -> Unit,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val textColor = if (isTiefschwarz) Color.White else colorScheme.onSurface
    val dividerColor = if (isTiefschwarz) Color.White.copy(alpha = 0.08f) else colorScheme.outline.copy(alpha = 0.12f)

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unit_suggestions_dropdown"),
        shape = GlassDialogDefaults.NestedPanelShape,
        customBgColor = if (isTiefschwarz) Color(0xFF0A0A0A) else colorScheme.surfaceVariant.copy(alpha = 0.45f),
        borderStroke = LocalGlassProperties.current.containerBorderStroke
    ) {
        val displayedSuggestions = remember(suggestions) { suggestions.take(8) }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = if (isImeVisible) 150.dp else 220.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            if (displayedSuggestions.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.converter_search_empty),
                            color = if (isTiefschwarz) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = displayedSuggestions,
                    key = { _, suggestion -> suggestion.item.id }
                ) { index, suggestion ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (index > 0) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = dividerColor,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSuggestion(suggestion)
                                }
                                .padding(horizontal = 16.dp, vertical = 11.dp)
                                .testTag("unit_suggestion_${suggestion.item.id}"),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = suggestion.localizedName,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// Legacy pair filtering maintained for backwards compatibility and tests
@Immutable
data class LocalizedConversionPair(
    val pair: ConversionPair,
    val localizedLabel: String,
    val localizedCategoryName: String,
    val localizedCategoryKeywords: List<String>
)

fun categoryKeywordsResId(category: UnitCategory): Int = when (category) {
    UnitCategory.LENGTH -> R.string.converter_keywords_length
    UnitCategory.MASS -> R.string.converter_keywords_mass
    UnitCategory.TEMPERATURE -> R.string.converter_keywords_temp
    UnitCategory.VOLUME -> R.string.converter_keywords_volume
    UnitCategory.DATA -> R.string.converter_keywords_data
    UnitCategory.TIME -> R.string.converter_keywords_time
}

fun filterConversionPairs(
    query: String,
    localizedPairs: List<LocalizedConversionPair>
): List<ConversionPair> {
    val tokens = tokenizeSearchQuery(query)
    if (tokens.isEmpty()) return emptyList()

    return localizedPairs
        .mapNotNull { entry ->
            val score = computeMatchScore(
                tokens = tokens,
                primaryNames = listOf(
                    entry.pair.unitFrom,
                    entry.pair.unitTo,
                    entry.localizedLabel
                ),
                secondaryNames = listOf(entry.localizedCategoryName),
                keywords = entry.localizedCategoryKeywords
            ) ?: return@mapNotNull null
            entry.pair to score
        }
        .sortedBy { it.second }
        .map { it.first }
}

@Composable
fun ConverterSearchEmptyState(
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("converter_search_empty"),
        shape = GlassDialogDefaults.SearchEmptyStateShape,
        customBgColor = if (isTiefschwarz) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.45f),
        borderStroke = LocalGlassProperties.current.containerBorderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = if (isTiefschwarz) {
                    Color.White.copy(alpha = 0.6f)
                } else {
                    colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.converter_search_empty),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (isTiefschwarz) {
                    Color.White.copy(alpha = 0.75f)
                } else {
                    colorScheme.onSurfaceVariant
                },
                textAlign = TextAlign.Center
            )
        }
    }
}
