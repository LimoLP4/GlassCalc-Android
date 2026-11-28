package com.limonatalp4.glasscalcproject.ui.screens

import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.repository.CurrencyRatesUiState
import com.limonatalp4.glasscalcproject.data.repository.CurrencyRepository
import com.limonatalp4.glasscalcproject.domain.converter.ConversionPair
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.components.ALL_UNIT_ITEMS
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.GlassAutoCenterPillTabBar
import com.limonatalp4.glasscalcproject.ui.components.GlassCardContainer
import com.limonatalp4.glasscalcproject.ui.components.GlassDialogDefaults
import com.limonatalp4.glasscalcproject.ui.components.GlassPillOptionContainer
import com.limonatalp4.glasscalcproject.ui.components.GlassPillTabItem
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.LocalizedUnitSuggestion
import com.limonatalp4.glasscalcproject.ui.components.UnitConverterSearchBar
import com.limonatalp4.glasscalcproject.ui.components.UnitSuggestionsDropdown
import com.limonatalp4.glasscalcproject.ui.components.filterUnitSuggestions
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.components.splitSearchKeywords
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterDialog(
    onImportToCalculator: (String) -> Unit,
    onDismiss: () -> Unit,
    currencyRatesState: CurrencyRatesUiState = CurrencyRatesUiState(),
    onEnsureCurrencyRates: (Boolean) -> Unit = {}
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val focusManager = LocalFocusManager.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategoryIndex by rememberSaveable { mutableIntStateOf(0) }
    val categories = UnitCategory.entries
    val safeCategoryIndex = selectedCategoryIndex.coerceIn(0, categories.lastIndex)

    val currentCategory = categories[safeCategoryIndex]
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
    var showSystemsInfo by rememberSaveable { mutableStateOf(false) }
    var pairsAccordionOpen by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(currentCategory) {
        if (currentCategory == UnitCategory.CURRENCY) {
            onEnsureCurrencyRates(false)
        }
    }

    val convertedResult = remember(inputValue, currentPair, isForward, currencyRatesState.rates) {
        val num = inputValue.replace(",", ".").toDoubleOrNull()
        if (num != null) {
            if (currentPair.category == UnitCategory.CURRENCY) {
                UnitConverter.convertCurrency(num, currentPair, isForward, currencyRatesState.rates)
            } else {
                UnitConverter.convert(num, currentPair, isForward)
            }
        } else {
            "—"
        }
    }

    val dialogBgColor = colorScheme.surfaceContainerHigh
    val onSurfaceColor = colorScheme.onSurface
    val primaryColor = colorScheme.primary
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
                        shape = GlassDialogDefaults.DialogShellShape,
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
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val infoBtnBg by animateColorAsState(
                                    targetValue = if (showSystemsInfo) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                    label = "converter_info_btn_bg"
                                )
                                val infoBtnTint by animateColorAsState(
                                    targetValue = if (showSystemsInfo) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                                    label = "converter_info_btn_tint"
                                )
                                val infoBtnInteraction = remember { MutableInteractionSource() }
                                GlassSurface(
                                    shape = GlassDialogDefaults.PillShape,
                                    customBgColor = infoBtnBg,
                                    borderStroke = if (showSystemsInfo) {
                                        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        glassProps.containerBorderStroke
                                    },
                                    modifier = Modifier.pressScale(infoBtnInteraction)
                                ) {
                                    IconButton(
                                        onClick = {
                                            triggerHaptic()
                                            if (!showSystemsInfo) {
                                                focusManager.clearFocus()
                                            }
                                            showSystemsInfo = !showSystemsInfo
                                        },
                                        interactionSource = infoBtnInteraction,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("btn_converter_info")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(R.string.converter_info_button_desc),
                                            tint = infoBtnTint,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                val closeBtnInteraction = remember { MutableInteractionSource() }
                                GlassSurface(
                                    shape = GlassDialogDefaults.PillShape,
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
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isImeVisible) 8.dp else 12.dp))

                        AnimatedContent(
                            targetState = showSystemsInfo,
                            transitionSpec = {
                                (fadeIn(tween(200, easing = FastOutSlowInEasing)) +
                                    expandVertically(tween(220, easing = FastOutSlowInEasing))).togetherWith(
                                    fadeOut(tween(150, easing = FastOutLinearInEasing)) +
                                        shrinkVertically(tween(180, easing = FastOutLinearInEasing))
                                ).using(SizeTransform(clip = false))
                            },
                            label = "converter_info_toggle",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) { isInfoVisible ->
                            if (isInfoVisible) {
                                SupportedSystemsInfoCard(
                                    isPureBlack = isPureBlack,
                                    onSurfaceColor = onSurfaceColor,
                                    primaryColor = primaryColor,
                                    glassProps = glassProps
                                )
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
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

                // Horizontally Scrollable Pill-Design Segmented Control for Categories (mit Auto-Centering)
                val categoryTabs = listOf(
                    Triple(UnitCategory.LENGTH, Icons.Default.Straighten, R.string.cat_length),
                    Triple(UnitCategory.MASS, Icons.Default.FitnessCenter, R.string.cat_mass),
                    Triple(UnitCategory.TEMPERATURE, Icons.Default.Thermostat, R.string.cat_temp),
                    Triple(UnitCategory.VOLUME, Icons.Default.Opacity, R.string.cat_volume),
                    Triple(UnitCategory.DATA, Icons.Default.Storage, R.string.cat_data),
                    Triple(UnitCategory.TIME, Icons.Default.Schedule, R.string.cat_time),
                    Triple(UnitCategory.VOLTAGE, Icons.Default.Bolt, R.string.cat_voltage),
                    Triple(UnitCategory.CURRENT, Icons.Default.ElectricalServices, R.string.cat_current),
                    Triple(UnitCategory.POWER, Icons.Default.Power, R.string.cat_power),
                    Triple(UnitCategory.CHARGE, Icons.Default.BatteryChargingFull, R.string.cat_charge),
                    Triple(UnitCategory.ANGLE, Icons.Default.RotateRight, R.string.cat_angle),
                    Triple(UnitCategory.SPEED, Icons.Default.Speed, R.string.cat_speed),
                    Triple(UnitCategory.RESISTANCE, Icons.Default.SettingsInputComponent, R.string.cat_resistance),
                    Triple(UnitCategory.FREQUENCY, Icons.Default.GraphicEq, R.string.cat_frequency),
                    Triple(UnitCategory.DATA_RATE, Icons.Default.NetworkCheck, R.string.cat_data_rate),
                    Triple(UnitCategory.ENERGY, Icons.Default.LocalFireDepartment, R.string.cat_energy),
                    Triple(UnitCategory.PRESSURE, Icons.Default.Compress, R.string.cat_pressure),
                    Triple(UnitCategory.CURRENCY, Icons.Default.CurrencyExchange, R.string.cat_currency)
                )
                val categoryPillItems = categoryTabs.map { (cat, icon, shortLabelRes) ->
                    GlassPillTabItem(
                        icon = icon,
                        label = stringResource(shortLabelRes),
                        testTag = "tab_cat_${cat.name}"
                    )
                }

                GlassAutoCenterPillTabBar(
                    items = categoryPillItems,
                    selectedIndex = safeCategoryIndex,
                    onSelectIndex = { index ->
                        val cat = categoryTabs[index].first
                        triggerHaptic()
                        searchQuery = ""
                        selectedCategoryIndex = index
                        selectedPair = UnitConverter.CONVERSIONS.first { it.category == cat }
                    },
                    isTiefschwarz = isPureBlack,
                    glassProps = glassProps,
                    fillMaxWidth = true,
                    minTabHeight = 40.dp,
                    tabPadding = PaddingValues(vertical = 9.dp, horizontal = 12.dp),
                    labelFontSize = 11.5.sp,
                    activeIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.primary,
                    inactiveContentColor = MaterialTheme.colorScheme.primary,
                    animationLabelPrefix = "cat"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                        targetState = safeCategoryIndex,
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
                        val catToDisplay = categories[targetIndex]
                        val pairs = UnitConverter.CONVERSIONS.filter { it.category == catToDisplay }

                        UnitPairsAccordionCard(
                            title = stringResource(catToDisplay.stringResId),
                            expanded = pairsAccordionOpen,
                            onToggle = { pairsAccordionOpen = !pairsAccordionOpen },
                            isPureBlack = isPureBlack,
                            glassProps = glassProps,
                            triggerHaptic = ::triggerHaptic
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                            ) {
                                items(pairs, key = { "${it.unitFrom}_${it.unitTo}" }) { pair ->
                                    val isSelected = pair == currentPair && targetIndex == safeCategoryIndex

                                    GlassPillOptionContainer(
                                        selected = isSelected,
                                        onClick = {
                                            triggerHaptic()
                                            selectedPair = pair
                                        },
                                        glassProps = glassProps,
                                        minHeight = GlassDialogDefaults.PillOptionMinHeight,
                                        contentPadding = GlassDialogDefaults.PillOptionPadding,
                                        animationLabel = "pair_${pair.unitFrom}_${pair.unitTo}",
                                        modifier = Modifier.testTag("pair_${pair.unitFrom}_${pair.unitTo}")
                                    ) { pairTextCol ->
                                        Text(
                                            text = stringResource(pair.labelResId),
                                            fontSize = 14.sp,
                                            lineHeight = 17.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = pairTextCol,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Conversion Box im Settings-Stil (großer harmonischer Radius + feiner BorderStroke)
                GlassCardContainer(
                    glassProps = glassProps,
                    shape = GlassDialogDefaults.LargeCardShape,
                    contentPadding = GlassDialogDefaults.CardContentPadding
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.converter_input_label, fromUnit),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.primary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            )

                            val swapInteraction = remember { MutableInteractionSource() }
                            GlassSurface(
                                shape = GlassDialogDefaults.PillShape,
                                customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderStroke = glassProps.containerBorderStroke,
                                modifier = Modifier.pressScale(swapInteraction)
                            ) {
                                IconButton(
                                    onClick = {
                                        triggerHaptic()
                                        isForward = !isForward
                                    },
                                    interactionSource = swapInteraction,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_swap_units")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SwapHoriz,
                                        contentDescription = stringResource(R.string.converter_swap_direction),
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Eingabefeld im vollen Pill-Shape (CircleShape) passend zu den Einstellungen
                        val inputFocusRequester = remember { FocusRequester() }
                        val inputPillInteraction = remember { MutableInteractionSource() }
                        val inputFieldInteraction = remember { MutableInteractionSource() }
                        val inputBringIntoView = remember { BringIntoViewRequester() }
                        var isInputFocused by remember { mutableStateOf(false) }
                        val inputImeBottomPx = WindowInsets.ime.getBottom(imeDensity)
                        val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
                        val currentHapticIntensity by rememberUpdatedState(hapticIntensity)

                        LaunchedEffect(isInputFocused, inputImeBottomPx) {
                            if (isInputFocused) {
                                delay(80)
                                inputBringIntoView.bringIntoView()
                            }
                        }

                        LaunchedEffect(inputFieldInteraction) {
                            inputFieldInteraction.interactions.collect { interaction ->
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

                        val inputBorderColor by animateColorAsState(
                            targetValue = if (isInputFocused) {
                                primaryColor
                            } else {
                                glassProps.buttonBorderColor
                            },
                            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
                            label = "converter_input_border"
                        )
                        val inputBgColor = GlassDialogDefaults.searchBarBackgroundColor(isPureBlack)
                        val inputTextColor = primaryColor

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 52.dp)
                                .bringIntoViewRequester(inputBringIntoView)
                                .clip(GlassDialogDefaults.PillShape)
                                .background(inputBgColor)
                                .border(
                                    border = BorderStroke(
                                        width = if (isInputFocused) 1.5.dp else 1.dp,
                                        color = inputBorderColor
                                    ),
                                    shape = GlassDialogDefaults.PillShape
                                )
                                .clickable(
                                    interactionSource = inputPillInteraction,
                                    indication = null
                                ) {
                                    triggerHaptic()
                                    inputFocusRequester.requestFocus()
                                }
                                .padding(
                                    horizontal = GlassDialogDefaults.PillHorizontalPadding,
                                    vertical = 12.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                BasicTextField(
                                    value = inputValue,
                                    onValueChange = { inputValue = it },
                                    singleLine = true,
                                    interactionSource = inputFieldInteraction,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = TextStyle(
                                        color = inputTextColor,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    cursorBrush = SolidColor(primaryColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(inputFocusRequester)
                                        .onFocusChanged { focusState ->
                                            isInputFocused = focusState.isFocused
                                        }
                                        .testTag("input_converter_value")
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = fromUnit,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = stringResource(R.string.converter_result_label, toUnit),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        val resultDisplayText = "$convertedResult $toUnit"
                        val textMeasurer = rememberTextMeasurer()
                        val resultScrollState = rememberScrollState()

                        LaunchedEffect(resultDisplayText) {
                            resultScrollState.scrollTo(0)
                        }

                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            val maxWidthPx = constraints.maxWidth
                            val baseResultStyle = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor
                            )
                            val candidateFontSizes = listOf(28.sp, 24.sp, 20.sp, 17.sp, 15.sp)
                            val fittedFontSize = remember(resultDisplayText, maxWidthPx) {
                                candidateFontSizes.firstOrNull { candidateSize ->
                                    textMeasurer.measure(
                                        text = resultDisplayText,
                                        style = baseResultStyle.copy(fontSize = candidateSize),
                                        maxLines = 1,
                                        softWrap = false
                                    ).size.width <= maxWidthPx
                                } ?: 15.sp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(resultScrollState)
                            ) {
                                Text(
                                    text = resultDisplayText,
                                    fontSize = fittedFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    textAlign = TextAlign.Start,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.testTag("text_converted_result")
                                )
                            }
                        }

                        if (currentPair.category == UnitCategory.CURRENCY) {
                            Spacer(modifier = Modifier.height(10.dp))
                            CurrencyRateStatusPill(
                                fromUnit = fromUnit,
                                toUnit = toUnit,
                                currentPair = currentPair,
                                isForward = isForward,
                                ratesState = currencyRatesState,
                                isPureBlack = isPureBlack,
                                glassProps = glassProps,
                                onRefresh = {
                                    triggerHaptic()
                                    onEnsureCurrencyRates(true)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Button to import converted value into calculator (voller Pill-Shape wie in den Einstellungen)
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
                        .defaultMinSize(minHeight = GlassDialogDefaults.ActionButtonMinHeight)
                        .pressScale(importInteraction)
                        .testTag("btn_import_converter"),
                    contentPadding = PaddingValues(
                        horizontal = GlassDialogDefaults.PillHorizontalPadding,
                        vertical = 12.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = primaryColor
                    ),
                    shape = GlassDialogDefaults.PillShape
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.converter_import),
                        fontWeight = FontWeight.Bold
                    )
                }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun UnitPairsAccordionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    isPureBlack: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    triggerHaptic: () -> Unit,
    content: @Composable () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "converter_pairs_chevron_rotation"
    )
    val cardInteraction = remember { MutableInteractionSource() }
    val pillInteraction = remember { MutableInteractionSource() }

    GlassCardContainer(
        glassProps = glassProps,
        shape = GlassDialogDefaults.SettingsCardShape,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        onClick = {
            triggerHaptic()
            onToggle()
        },
        role = Role.Button,
        interactionSource = cardInteraction,
        modifier = Modifier.testTag("card_converter_pairs_accordion")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                GlassSurface(
                    shape = CircleShape,
                    customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke,
                    modifier = Modifier
                        .size(36.dp)
                        .pressScale(pillInteraction)
                ) {
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            onToggle()
                        },
                        interactionSource = pillInteraction,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_toggle_converter_pairs_accordion")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = title,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(18.dp)
                                .graphicsLayer { rotationZ = chevronRotation }
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun SupportedSystemsInfoCard(
    isPureBlack: Boolean,
    onSurfaceColor: Color,
    primaryColor: Color,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties
) {
    val contentScrollState = rememberScrollState()

    GlassCardContainer(
        glassProps = glassProps,
        shape = GlassDialogDefaults.SettingsCardShape,
        contentPadding = GlassDialogDefaults.SettingsCardContentPadding,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_converter_systems_info")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = stringResource(R.string.converter_info_dialog_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(contentScrollState)
            ) {
                Text(
                    text = stringResource(R.string.units_systems_detail),
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                    color = primaryColor,
                    modifier = Modifier.testTag("text_converter_systems_detail")
                )
            }
        }
    }
}

@Composable
private fun CurrencyRateStatusPill(
    fromUnit: String,
    toUnit: String,
    currentPair: ConversionPair,
    isForward: Boolean,
    ratesState: CurrencyRatesUiState,
    isPureBlack: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    onRefresh: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val refreshInteraction = remember { MutableInteractionSource() }
    val statusBg = if (isPureBlack) {
        Color(0xFF0A0A0A)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val statusTextColor = colorScheme.primary

    GlassSurface(
        shape = GlassDialogDefaults.NestedPanelShape,
        customBgColor = statusBg,
        borderStroke = glassProps.containerBorderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("currency_rate_status_pill")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (ratesState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("currency_loading_indicator"),
                            strokeWidth = 2.dp,
                            color = colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.currency_loading),
                            fontSize = 12.sp,
                            color = statusTextColor,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else if (ratesState.hasRates) {
                        val unitRate = UnitConverter.convertCurrency(1.0, currentPair, isForward, ratesState.rates)
                        val dateLabel = ratesState.date.ifBlank { CurrencyRepository.DEFAULT_BASE }
                        val statusMessage = if (ratesState.isOfflineFallback) {
                            stringResource(R.string.currency_cached_offline, fromUnit, unitRate, toUnit, dateLabel)
                        } else {
                            stringResource(R.string.currency_rate_info, fromUnit, unitRate, toUnit, dateLabel)
                        }
                        Text(
                            text = statusMessage,
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = statusTextColor,
                            modifier = Modifier.testTag("text_currency_rate_info")
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.currency_error),
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            color = colorScheme.error,
                            modifier = Modifier.testTag("text_currency_rate_error")
                        )
                    }
                }

                GlassSurface(
                    shape = CircleShape,
                    customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke,
                    modifier = Modifier.pressScale(refreshInteraction)
                ) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !ratesState.isLoading,
                        interactionSource = refreshInteraction,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_refresh_currency_rates")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.currency_refresh_desc),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colorScheme.primary.copy(alpha = 0.85f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = stringResource(R.string.currency_api_disclaimer),
                    fontSize = 10.5.sp,
                    lineHeight = 14.sp,
                    color = statusTextColor.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("text_currency_api_disclaimer")
                )
            }
        }
    }
}
