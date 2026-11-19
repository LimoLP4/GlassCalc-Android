package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.domain.formulas.FormulaCatalog
import com.limonatalp4.glasscalcproject.domain.formulas.MathFormula
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import java.util.Locale

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
    val suggestionPillShape = CircleShape

    val context = LocalContext.current
    val resources = LocalResources.current
    val configuration = LocalConfiguration.current

    // Namen und Keywords werden pro Sprache/Konfiguration einmalig geladen und gecacht
    val localizedFormulas = remember(configuration, resources) {
        FormulaCatalog.FORMULAS.map { item ->
            val name = resources.getString(item.nameResId)
            val keywords = item.keywordsResIds.flatMap { resId ->
                splitSearchKeywords(resources.getString(resId))
            }
            LocalizedFormula(
                formula = item,
                localizedName = name,
                localizedKeywords = keywords
            )
        }
    }

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
                    border = LocalGlassProperties.current.containerBorderStroke,
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
                val clearInteraction = remember { MutableInteractionSource() }
                IconButton(
                    onClick = onClearClick,
                    interactionSource = clearInteraction,
                    modifier = Modifier
                        .size(48.dp)
                        .pressScale(clearInteraction)
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

        // Schwebende Suchvorschläge im einheitlichen Pill-Design
        AnimatedVisibility(
            visible = trimmedQuery.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            val suggestionBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val suggestionBorder = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .heightIn(max = if (isImeVisible) 160.dp else 232.dp)
                    .testTag("drawer_formula_dropdown"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                if (filteredFormulas.isEmpty()) {
                    item(key = "empty") {
                        Surface(
                            shape = suggestionPillShape,
                            color = colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            contentColor = colorScheme.onSurfaceVariant,
                            border = suggestionBorder,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.formula_search_empty),
                                color = colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                            )
                        }
                    }
                } else {
                    items(
                        items = filteredFormulas,
                        key = { entry -> entry.formula.id }
                    ) { entry ->
                        val itemInteractionSource = remember { MutableInteractionSource() }
                        Surface(
                            onClick = {
                                HapticUtil.performAppHaptic(
                                    context = context,
                                    view = view,
                                    enabled = currentHapticEnabled,
                                    intensityLevel = currentHapticIntensity
                                )
                                onFormulaSelected(entry.formula)
                            },
                            shape = suggestionPillShape,
                            color = suggestionBgColor,
                            contentColor = colorScheme.onSurface,
                            border = suggestionBorder,
                            interactionSource = itemInteractionSource,
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp)
                                .pressScale(itemInteractionSource)
                                .testTag("formula_item_${entry.formula.id}")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 10.dp)
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
    val tokens = tokenizeSearchQuery(query)
    if (tokens.isEmpty()) return emptyList()
    return localizedFormulas
        .mapNotNull { entry ->
            val score = computeMatchScore(
                tokens = tokens,
                primaryNames = listOf(entry.localizedName),
                secondaryNames = listOf(entry.formula.formula),
                keywords = entry.localizedKeywords
            ) ?: return@mapNotNull null
            entry to score
        }
        .sortedWith(
            compareBy<Pair<LocalizedFormula, Int>> { it.second }
                .thenBy { it.first.localizedName.lowercase(Locale.ROOT) }
        )
        .map { it.first }
}

