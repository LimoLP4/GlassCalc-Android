package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog
import com.limonatalp4.glasscalcproject.domain.formulas.MathFormula
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity

@Immutable
internal data class LocalizedFormula(
    val formula: MathFormula,
    val localizedName: String,
    val localizedKeywords: List<String>
)

@Composable
fun DrawerFormulaSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearClick: () -> Unit,
    onFormulaSelected: (MathFormula) -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val searchBarShape = RoundedCornerShape(percent = 50)
    val dropdownShape = RoundedCornerShape(22.dp)

    // Namen und Keywords werden dynamisch aus der aktuell aktiven Sprache geladen
    val localizedFormulas = FormulaCatalog.FORMULAS.map { item ->
        val name = stringResource(item.nameResId)
        val keywords = item.keywordsResIds.flatMap { resId ->
            stringResource(resId)
                .split(',', ';')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        }
        LocalizedFormula(
            formula = item,
            localizedName = name,
            localizedKeywords = keywords
        )
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
    val isImeVisible = imeBottomPx > 0

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

    LaunchedEffect(isSearchFocused, imeBottomPx) {
        if (isSearchFocused) {
            delay(80)
            bringIntoViewRequester.bringIntoView()
        }
    }

    val trimmedQuery = query.trim()
    val filteredFormulas = remember(trimmedQuery, localizedFormulas) {
        filterFormulas(trimmedQuery, localizedFormulas)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Liquid-Glass-Suchleiste (ausschließlich dynamische Theme-Variablen)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .clip(searchBarShape)
                .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    width = 1.dp,
                    color = colorScheme.outline.copy(alpha = 0.3f),
                    shape = searchBarShape
                )
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
                tint = colorScheme.onSurfaceVariant,
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
                        color = colorScheme.onSurface,
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
                        .testTag("drawer_formula_search_input"),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.formula_search_placeholder),
                                    color = colorScheme.onSurfaceVariant,
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
                        .testTag("btn_clear_formula_search")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.formula_search_clear),
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Gefiltertes Dropdown unter der Suchleiste
        AnimatedVisibility(
            visible = trimmedQuery.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(dropdownShape)
                    .background(colorScheme.surfaceContainerHigh.copy(alpha = 0.92f))
                    .border(
                        width = 1.dp,
                        color = colorScheme.outline.copy(alpha = 0.3f),
                        shape = dropdownShape
                    )
                    .heightIn(max = if (isImeVisible) 150.dp else 220.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
                    .testTag("drawer_formula_dropdown")
            ) {
                if (filteredFormulas.isEmpty()) {
                    Text(
                        text = stringResource(R.string.formula_search_empty),
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                } else {
                    filteredFormulas.forEachIndexed { index, entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 48.dp)
                                .clickable { onFormulaSelected(entry.formula) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("formula_item_${entry.formula.id}")
                        ) {
                            Text(
                                text = entry.localizedName,
                                color = colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = entry.formula.formula,
                                color = colorScheme.primary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (index < filteredFormulas.lastIndex) {
                            HorizontalDivider(
                                color = colorScheme.outline.copy(alpha = 0.2f),
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun filterFormulas(
    query: String,
    localizedFormulas: List<LocalizedFormula>
): List<LocalizedFormula> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    return localizedFormulas.filter { entry ->
        entry.localizedName.contains(trimmed, ignoreCase = true) ||
            entry.formula.formula.contains(trimmed, ignoreCase = true) ||
            entry.localizedKeywords.any { keyword -> keyword.contains(trimmed, ignoreCase = true) }
    }
}
