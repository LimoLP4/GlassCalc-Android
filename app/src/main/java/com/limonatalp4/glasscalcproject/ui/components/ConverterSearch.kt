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

@Immutable
data class UnitItem(
    val id: String,
    val symbol: String,
    val category: UnitCategory,
    val nameResId: Int,
    val defaultPair: ConversionPair
)

val ALL_UNIT_ITEMS: List<UnitItem> by lazy {
    listOf(
        // Length
        UnitItem("meter", "m", UnitCategory.LENGTH, R.string.unit_name_meter, UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }),
        UnitItem("feet", "ft", UnitCategory.LENGTH, R.string.unit_name_feet, UnitConverter.CONVERSIONS.first { it.unitFrom == "m" && it.unitTo == "ft" }),
        UnitItem("centimeter", "cm", UnitCategory.LENGTH, R.string.unit_name_centimeter, UnitConverter.CONVERSIONS.first { it.unitFrom == "cm" && it.unitTo == "in" }),
        UnitItem("inch", "in", UnitCategory.LENGTH, R.string.unit_name_inch, UnitConverter.CONVERSIONS.first { it.unitFrom == "cm" && it.unitTo == "in" }),
        UnitItem("kilometer", "km", UnitCategory.LENGTH, R.string.unit_name_kilometer, UnitConverter.CONVERSIONS.first { it.unitFrom == "km" && it.unitTo == "mi" }),
        UnitItem("mile", "mi", UnitCategory.LENGTH, R.string.unit_name_mile, UnitConverter.CONVERSIONS.first { it.unitFrom == "km" && it.unitTo == "mi" }),

        // Mass
        UnitItem("kilogram", "kg", UnitCategory.MASS, R.string.unit_name_kilogram, UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "lb" }),
        UnitItem("pound", "lb", UnitCategory.MASS, R.string.unit_name_pound, UnitConverter.CONVERSIONS.first { it.unitFrom == "kg" && it.unitTo == "lb" }),

        // Temperature
        UnitItem("celsius", "°C", UnitCategory.TEMPERATURE, R.string.unit_name_celsius, UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }),
        UnitItem("fahrenheit", "°F", UnitCategory.TEMPERATURE, R.string.unit_name_fahrenheit, UnitConverter.CONVERSIONS.first { it.category == UnitCategory.TEMPERATURE }),

        // Volume
        UnitItem("liter", "l", UnitCategory.VOLUME, R.string.unit_name_liter, UnitConverter.CONVERSIONS.first { it.unitFrom == "l" && it.unitTo == "gal" }),
        UnitItem("gallon", "gal", UnitCategory.VOLUME, R.string.unit_name_gallon, UnitConverter.CONVERSIONS.first { it.unitFrom == "l" && it.unitTo == "gal" }),

        // Data
        UnitItem("byte", "B", UnitCategory.DATA, R.string.unit_name_byte, UnitConverter.CONVERSIONS.first { it.unitFrom == "B" && it.unitTo == "KB" }),
        UnitItem("kilobyte", "KB", UnitCategory.DATA, R.string.unit_name_kilobyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "KB" && it.unitTo == "MB" }),
        UnitItem("megabyte", "MB", UnitCategory.DATA, R.string.unit_name_megabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "MB" && it.unitTo == "GB" }),
        UnitItem("gigabyte", "GB", UnitCategory.DATA, R.string.unit_name_gigabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }),
        UnitItem("terabyte", "TB", UnitCategory.DATA, R.string.unit_name_terabyte, UnitConverter.CONVERSIONS.first { it.unitFrom == "GB" && it.unitTo == "TB" }),

        // Time
        UnitItem("second", "s", UnitCategory.TIME, R.string.unit_name_seconds, UnitConverter.CONVERSIONS.first { it.unitFrom == "s" && it.unitTo == "ms" }),
        UnitItem("millisecond", "ms", UnitCategory.TIME, R.string.unit_name_milliseconds, UnitConverter.CONVERSIONS.first { it.unitFrom == "s" && it.unitTo == "ms" }),
        UnitItem("minute", "min", UnitCategory.TIME, R.string.unit_name_minutes, UnitConverter.CONVERSIONS.first { it.unitFrom == "min" && it.unitTo == "s" }),
        UnitItem("hour", "h", UnitCategory.TIME, R.string.unit_name_hours, UnitConverter.CONVERSIONS.first { it.unitFrom == "h" && it.unitTo == "min" }),
        UnitItem("day", "d", UnitCategory.TIME, R.string.unit_name_days, UnitConverter.CONVERSIONS.first { it.unitFrom == "d" && it.unitTo == "h" })
    )
}

@Immutable
data class LocalizedUnitSuggestion(
    val item: UnitItem,
    val localizedName: String,
    val symbol: String
)

fun filterUnitSuggestions(
    query: String,
    suggestions: List<LocalizedUnitSuggestion>
): List<LocalizedUnitSuggestion> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()

    return suggestions.mapNotNull { suggestion ->
        val nameMatch = suggestion.localizedName.contains(trimmed, ignoreCase = true)
        val symbolMatch = suggestion.symbol.contains(trimmed, ignoreCase = true)
        val idMatch = suggestion.item.id.contains(trimmed, ignoreCase = true)

        if (!nameMatch && !symbolMatch && !idMatch) {
            null
        } else {
            val priority = when {
                suggestion.localizedName.equals(trimmed, ignoreCase = true) ||
                    suggestion.symbol.equals(trimmed, ignoreCase = true) -> 0
                suggestion.localizedName.startsWith(trimmed, ignoreCase = true) ||
                    suggestion.symbol.startsWith(trimmed, ignoreCase = true) -> 1
                else -> 2
            }
            suggestion to priority
        }
    }
    .sortedWith(compareBy({ it.second }, { it.first.localizedName }))
    .map { it.first }
}

@Composable
fun UnitConverterSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    isAmoled: Boolean,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier,
    onSearchSubmit: (() -> Unit)? = null
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(searchBarBgColor)
            .border(searchBarBorder, shape = shape)
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
                cursorBrush = SolidColor(colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
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
            IconButton(
                onClick = onClearClick,
                modifier = Modifier
                    .size(48.dp)
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
    isAmoled: Boolean,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val textColor = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
    val dividerColor = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.08f) else colorScheme.outline.copy(alpha = 0.12f)

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("unit_suggestions_dropdown"),
        shape = RoundedCornerShape(18.dp),
        borderWidth = 1.dp,
        customBgColor = when {
            isAmoled -> Color(0xFF0A0A0A)
            isTiefschwarz -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        customBorderColor = when {
            isAmoled -> Color.White.copy(alpha = 0.12f)
            isTiefschwarz -> Color.White.copy(alpha = 0.1f)
            else -> colorScheme.outline.copy(alpha = 0.25f)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            if (suggestions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = stringResource(R.string.converter_search_empty),
                        color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            } else {
                suggestions.take(6).forEachIndexed { index, suggestion ->
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
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()

    val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (tokens.isEmpty()) return emptyList()

    return localizedPairs
        .mapNotNull { entry ->
            val candidates = buildList {
                add(entry.pair.unitFrom)
                add(entry.pair.unitTo)
                add(entry.localizedLabel)
                add(entry.localizedCategoryName)
                addAll(entry.localizedCategoryKeywords)
            }
            val matchesAllTokens = tokens.all { token ->
                candidates.any { candidate -> candidate.contains(token, ignoreCase = true) }
            }
            if (!matchesAllTokens) {
                null
            } else {
                val exactUnitMatch = entry.pair.unitFrom.equals(trimmed, ignoreCase = true) ||
                    entry.pair.unitTo.equals(trimmed, ignoreCase = true)
                val prefixUnitMatch = entry.pair.unitFrom.startsWith(trimmed, ignoreCase = true) ||
                    entry.pair.unitTo.startsWith(trimmed, ignoreCase = true) ||
                    entry.localizedLabel.startsWith(trimmed, ignoreCase = true)
                val priority = when {
                    exactUnitMatch -> 0
                    prefixUnitMatch -> 1
                    else -> 2
                }
                entry.pair to priority
            }
        }
        .sortedBy { it.second }
        .map { it.first }
}

@Composable
fun ConverterSearchEmptyState(
    isAmoled: Boolean,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("converter_search_empty"),
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
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
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.converter_search_empty),
                fontSize = 13.5.sp,
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
