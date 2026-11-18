package com.limonatalp4.glasscalcproject.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentWidth
import android.view.WindowManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.core.view.WindowCompat
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.domain.converter.ConversionPair
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS
import com.limonatalp4.glasscalcproject.ui.components.ConverterSearchEmptyState
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.LocalizedConversionPair
import com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion
import com.limonatalp4.glasscalcproject.ui.components.UnitConverterSearchBar
import com.limonatalp4.glasscalcproject.ui.components.UnitSuggestionsDropdown
import com.limonatalp4.glasscalcproject.ui.components.categoryKeywordsResId
import com.limonatalp4.glasscalcproject.ui.components.filterConversionPairs
import com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.components.splitSearchKeywords
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.ripple
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterDialog(
    onImportToCalculator: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val context = LocalContext.current
    val view = LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    val categories = UnitCategory.entries

    val currentCategory = categories[selectedCategoryIndex]
    val conversionsInCategory = remember(currentCategory) {
        UnitConverter.CONVERSIONS.filter { it.category == currentCategory }
    }

    val configuration = LocalConfiguration.current
    val resources = LocalResources.current
    val localizedUnitSuggestions = remember(configuration, resources) {
        ALL_UNIT_ITEMS.map { item ->
            LocalizedUnitSuggestion(
                item = item,
                localizedName = resources.getString(item.nameResId),
                symbol = item.symbol,
                localizedKeywords = splitSearchKeywords(resources.getString(item.keywordsResId))
            )
        }
    }

    val filteredUnitSuggestions = remember(searchQuery, localizedUnitSuggestions) {
        filterUnitSuggestions(searchQuery, localizedUnitSuggestions)
    }

    var selectedPair by remember { mutableStateOf(UnitConverter.CONVERSIONS.first()) }
    val currentPair = if (selectedPair in conversionsInCategory) {
        selectedPair
    } else {
        conversionsInCategory.firstOrNull() ?: selectedPair
    }

    var isForward by rememberSaveable { mutableStateOf(true) }
    var inputValue by rememberSaveable { mutableStateOf("1") }

    fun selectUnitSuggestion(suggestion: LocalizedUnitSuggestion) {
        triggerHaptic()
        val targetCatIndex = categories.indexOf(suggestion.item.category)
        if (targetCatIndex >= 0) {
            selectedCategoryIndex = targetCatIndex
        }
        selectedPair = suggestion.item.defaultPair
        isForward = suggestion.item.symbol == suggestion.item.defaultPair.unitFrom
        searchQuery = ""
    }

    fun selectConversionPair(pair: ConversionPair) {
        triggerHaptic()
        selectedPair = pair
        val targetCatIndex = categories.indexOf(pair.category)
        if (targetCatIndex >= 0) {
            selectedCategoryIndex = targetCatIndex
        }
        searchQuery = ""
    }

    val fromUnit = if (isForward) currentPair.unitFrom else currentPair.unitTo
    val toUnit = if (isForward) currentPair.unitTo else currentPair.unitFrom

    val convertedResult = remember(inputValue, currentPair, isForward) {
        val num = inputValue.replace(",", ".").toDoubleOrNull()
        if (num != null) {
            UnitConverter.convert(num, currentPair, isForward)
        } else {
            "—"
        }
    }

    val dialogBgColor = colorScheme.surfaceContainerHigh
    val onSurfaceColor = colorScheme.onSurface
    val primaryColor = colorScheme.primary
    val primaryContainerColor = colorScheme.primaryContainer
    val onPrimaryContainerColor = colorScheme.onPrimaryContainer
    val isTiefschwarz = glassProps.isTiefschwarz
    val isPureBlack = isTiefschwarz

    val dialogTransition = rememberDialogTransitionController(onDismiss)

    @Suppress("DEPRECATION")
    BasicAlertDialog(
        onDismissRequest = dialogTransition.dismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier.testTag("dialog_unit_converter")
    ) {
        val dialogView = LocalView.current
        SideEffect {
            (dialogView.parent as? DialogWindowProvider)?.window?.let { dialogWindow ->
                WindowCompat.setDecorFitsSystemWindows(dialogWindow, false)
                dialogWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }

        val imeDensity = LocalDensity.current
        val isImeVisible = WindowInsets.ime.getBottom(imeDensity) > 0
        val dialogTopPadding = if (isImeVisible) 8.dp else 16.dp
        val dialogBottomPadding = if (isImeVisible) 8.dp else 16.dp
        val dialogScrollState = rememberScrollState()

        Box(modifier = Modifier.fillMaxSize()) {
            // Transparent outside-tap scrim to dismiss dialog
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        dialogTransition.dismiss()
                    }
            )

            Box(
                modifier = Modifier
                    .align(if (isImeVisible) Alignment.TopCenter else Alignment.Center)
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                            .union(WindowInsets.displayCutout)
                            .union(WindowInsets.ime)
                            .union(WindowInsets.navigationBars)
                    )
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = dialogTopPadding,
                        bottom = dialogBottomPadding
                    )
            ) {
                DialogAnimatedContent(visibleState = dialogTransition.visibleState) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .widthIn(max = 400.dp)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        customBgColor = if (isPureBlack) Color.Black else dialogBgColor,
                        borderStroke = glassProps.containerBorderStroke
                    ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (isImeVisible) 14.dp else 20.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.title_converter),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPureBlack) Color.White else onSurfaceColor
                            )
                            val closeBtnInteraction = remember { MutableInteractionSource() }
                            GlassSurface(
                                shape = RoundedCornerShape(50),
                                customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderStroke = glassProps.containerBorderStroke,
                                modifier = Modifier.pressScale(closeBtnInteraction)
                            ) {
                                IconButton(
                                    onClick = {
                                        triggerHaptic()
                                        dialogTransition.dismiss()
                                    },
                                    interactionSource = closeBtnInteraction,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("btn_close_converter")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.nav_close),
                                        tint = if (isPureBlack) Color.White else onSurfaceColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isImeVisible) 8.dp else 12.dp))

                        // Liquid-Glass Einheiten-Suchleiste (direkt unter Titel und über Kategorie-Pill-Leiste)
                        UnitConverterSearchBar(
                            query = searchQuery,
                            onQueryChange = { newValue ->
                                searchQuery = newValue
                            },
                            onClearClick = {
                                HapticUtil.performAppHaptic(
                                    context = context,
                                    view = view,
                                    enabled = hapticFeedbackEnabled,
                                    intensityLevel = hapticIntensity
                                )
                                searchQuery = ""
                            },
                            onSearchSubmit = {
                                val firstMatch = filteredUnitSuggestions.firstOrNull()
                                if (firstMatch != null) {
                                    selectUnitSuggestion(firstMatch)
                                } else {
                                    triggerHaptic()
                                    searchQuery = ""
                                }
                            },
                            isTiefschwarz = isTiefschwarz
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(dialogScrollState)
                        ) {
                            // Dropdown-Vorschläge dezent direkt unter der Suchleiste
                            if (searchQuery.trim().isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                UnitSuggestionsDropdown(
                                    suggestions = filteredUnitSuggestions,
                                    onSelectSuggestion = { suggestion ->
                                        selectUnitSuggestion(suggestion)
                                    },
                                    isTiefschwarz = isTiefschwarz
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                // Horizontally Scrollable Pill-Design Segmented Control for Categories
                val categoryTabs = listOf(
                    Triple(UnitCategory.LENGTH, Icons.Default.Straighten, R.string.cat_length),
                    Triple(UnitCategory.MASS, Icons.Default.FitnessCenter, R.string.cat_mass),
                    Triple(UnitCategory.TEMPERATURE, Icons.Default.Thermostat, R.string.cat_temp),
                    Triple(UnitCategory.VOLUME, Icons.Default.Opacity, R.string.cat_volume),
                    Triple(UnitCategory.DATA, Icons.Default.Storage, R.string.cat_data),
                    Triple(UnitCategory.TIME, Icons.Default.Schedule, R.string.cat_time)
                )

                val tabScrollState = rememberScrollState()
                val density = LocalDensity.current
                val tabWidths = remember { mutableStateMapOf<Int, Dp>() }
                var tabHeight by remember { mutableStateOf(0.dp) }
                val tabSpacing = 4.dp

                val targetPosition = remember(selectedCategoryIndex, tabWidths.toMap()) {
                    var pos = 0.dp
                    for (i in 0 until selectedCategoryIndex) {
                        pos += (tabWidths[i] ?: 0.dp) + tabSpacing
                    }
                    pos
                }
                val targetWidth = tabWidths[selectedCategoryIndex] ?: 0.dp

                val indicatorOffset by animateDpAsState(
                    targetValue = targetPosition,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "cat_indicator_offset"
                )
                val indicatorWidth by animateDpAsState(
                    targetValue = targetWidth,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "cat_indicator_width"
                )
                val activeCatPillColor by animateColorAsState(
                    targetValue = primaryContainerColor,
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    label = "cat_active_pill_bg"
                )

                LaunchedEffect(selectedCategoryIndex, targetPosition) {
                    val targetPx = with(density) { targetPosition.roundToPx() }
                    val offsetPaddingPx = with(density) { 32.dp.roundToPx() }
                    tabScrollState.animateScrollTo((targetPx - offsetPaddingPx).coerceAtLeast(0))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isPureBlack) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .border(
                            border = glassProps.containerBorderStroke,
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(tabScrollState)
                    ) {
                        // Sliding Highlight Pill INSIDE the horizontal scroll container
                        if (targetWidth > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                                    .width(indicatorWidth)
                                    .height(if (tabHeight > 0.dp) tabHeight else 40.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(activeCatPillColor)
                            )
                        }

                        Row(
                            modifier = Modifier.wrapContentWidth(),
                            horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
                            val currentHapticIntensity by rememberUpdatedState(hapticIntensity)
                            categoryTabs.forEachIndexed { index, (cat, icon, shortLabelRes) ->
                                val isSelected = selectedCategoryIndex == index
                                var isPressed by remember { mutableStateOf(false) }
                                val scale by animateFloatAsState(
                                    targetValue = if (isPressed) 0.95f else 1.0f,
                                    animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
                                    label = "cat_tab_scale_$index"
                                )
                                val tabTint by animateColorAsState(
                                    targetValue = if (isSelected) {
                                        if (isPureBlack) Color.White else onPrimaryContainerColor
                                    } else {
                                        if (isPureBlack) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant
                                    },
                                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                    label = "cat_tab_tint_$index"
                                )

                                Box(
                                    modifier = Modifier
                                        .defaultMinSize(minHeight = 40.dp)
                                        .onSizeChanged { size ->
                                            val w = with(density) { size.width.toDp() }
                                            val h = with(density) { size.height.toDp() }
                                            tabWidths[index] = w
                                            if (h > tabHeight) tabHeight = h
                                        }
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        .clip(RoundedCornerShape(50.dp))
                                        .pointerInput(cat, index) {
                                            detectTapGestures(
                                                onPress = {
                                                    isPressed = true
                                                    try { awaitRelease() } finally { isPressed = false }
                                                },
                                                onTap = {
                                                    HapticUtil.performAppHaptic(
                                                        context,
                                                        view,
                                                        currentHapticEnabled,
                                                        currentHapticIntensity
                                                    )
                                                    searchQuery = ""
                                                    selectedCategoryIndex = index
                                                    selectedPair = UnitConverter.CONVERSIONS.first { it.category == cat }
                                                }
                                            )
                                        }
                                        .padding(vertical = 9.dp, horizontal = 12.dp)
                                        .testTag("tab_cat_${cat.name}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = tabTint,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(shortLabelRes),
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = tabTint,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                        targetState = selectedCategoryIndex,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally(tween(200, easing = FastOutSlowInEasing)) { width -> width / 4 } +
                                    fadeIn(tween(200, easing = FastOutSlowInEasing))).togetherWith(
                                    slideOutHorizontally(tween(150, easing = FastOutLinearInEasing)) { width -> -width / 4 } +
                                        fadeOut(tween(150, easing = FastOutLinearInEasing)))
                            } else {
                                (slideInHorizontally(tween(200, easing = FastOutSlowInEasing)) { width -> -width / 4 } +
                                    fadeIn(tween(200, easing = FastOutSlowInEasing))).togetherWith(
                                    slideOutHorizontally(tween(150, easing = FastOutLinearInEasing)) { width -> width / 4 } +
                                        fadeOut(tween(150, easing = FastOutLinearInEasing)))
                            }.using(SizeTransform(clip = false))
                        },
                        label = "cat_transition"
                    ) { targetIndex ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val catToDisplay = categories[targetIndex]
                            val pairs = UnitConverter.CONVERSIONS.filter { it.category == catToDisplay }

                            // Conversion Pairs in this category
                            Text(
                                text = stringResource(R.string.converter_select_pair),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPureBlack) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                items(pairs, key = { "${it.unitFrom}_${it.unitTo}" }) { pair ->
                                    val idx = pairs.indexOf(pair)
                                    val isSelected = pair == currentPair && targetIndex == selectedCategoryIndex
                                    val pairBg by animateColorAsState(
                                        targetValue = if (isSelected) {
                                            if (isPureBlack) primaryContainerColor.copy(alpha = 0.7f) else primaryContainerColor
                                        } else {
                                            if (isPureBlack) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        },
                                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                        label = "pair_bg_$idx"
                                    )
                                    val pairBorder = if (!isPureBlack && isSelected) primaryColor else glassProps.buttonBorderColor
                                    val pairTextCol = if (isPureBlack) Color.White else (if (isSelected) onPrimaryContainerColor else onSurfaceColor)
                                    val pairInteraction = remember { MutableInteractionSource() }

                                    GlassSurface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp)
                                            .pressScale(pairInteraction)
                                            .clickable(
                                                interactionSource = pairInteraction,
                                                indication = ripple()
                                            ) {
                                                triggerHaptic()
                                                selectedPair = pair
                                            }
                                            .testTag("pair_${pair.unitFrom}_${pair.unitTo}"),
                                        shape = RoundedCornerShape(50.dp),
                                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                                        customBorderColor = pairBorder,
                                        customBgColor = pairBg
                                    ) {
                                        Text(
                                            text = stringResource(pair.labelResId),
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = pairTextCol,
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Conversion Box
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    customBgColor = if (isPureBlack) Color.Black else null,
                    borderStroke = glassProps.containerBorderStroke
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.converter_input_label, fromUnit),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isPureBlack) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                            )

                            val swapInteraction = remember { MutableInteractionSource() }
                            IconButton(
                                onClick = {
                                    triggerHaptic()
                                    isForward = !isForward
                                },
                                interactionSource = swapInteraction,
                                modifier = Modifier
                                    .pressScale(swapInteraction)
                                    .testTag("btn_swap_units")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = stringResource(R.string.converter_swap_direction),
                                    tint = primaryColor
                                )
                            }
                        }

                        OutlinedTextField(
                            value = inputValue,
                            onValueChange = { inputValue = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_converter_value"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = if (isPureBlack) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = if (isPureBlack) Color.Black else colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                focusedBorderColor = if (isPureBlack) Color.White else primaryColor,
                                unfocusedBorderColor = glassProps.buttonBorderColor,
                                focusedTextColor = if (isPureBlack) Color.White else onSurfaceColor,
                                unfocusedTextColor = if (isPureBlack) Color.White else onSurfaceColor
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.converter_result_label, toUnit),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPureBlack) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                        )

                        Text(
                            text = "$convertedResult $toUnit",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("text_converted_result")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button to import converted value into calculator
                val importInteraction = remember { MutableInteractionSource() }
                Button(
                    onClick = {
                        triggerHaptic()
                        val cleaned = convertedResult.replace(",", ".").replace(" ", "")
                        if (cleaned != "—") {
                            onImportToCalculator(cleaned)
                            dialogTransition.dismiss()
                        }
                    },
                    interactionSource = importInteraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(importInteraction)
                        .testTag("btn_import_converter"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = colorScheme.onPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.converter_import), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                }
            }
        }
    }
    }
}
