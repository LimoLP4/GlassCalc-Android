package com.limonatalp4.glasscalcproject.ui.screens

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import androidx.core.net.toUri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import android.view.WindowManager
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.core.view.WindowCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.graphics.ColorUtils
import androidx.window.core.layout.WindowSizeClass
import com.limonatalp4.glasscalcproject.data.model.AccentColorOption
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppIconOption
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.Language
import com.limonatalp4.glasscalcproject.BuildConfig
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.GlassAutoCenterPillTabBar
import com.limonatalp4.glasscalcproject.ui.components.GlassCardContainer
import com.limonatalp4.glasscalcproject.ui.components.GlassDialogDefaults
import com.limonatalp4.glasscalcproject.ui.components.GlassPillOptionContainer
import com.limonatalp4.glasscalcproject.ui.components.GlassPillTabItem
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.SettingsDevCard
import com.limonatalp4.glasscalcproject.ui.components.SettingsInfoCard
import com.limonatalp4.glasscalcproject.ui.components.SettingsSearchBar
import com.limonatalp4.glasscalcproject.ui.components.SettingsSearchEmptyState
import com.limonatalp4.glasscalcproject.ui.components.SettingsSearchVisibility
import com.limonatalp4.glasscalcproject.ui.components.SettingsUnitCard
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.components.rememberSettingsSearchVisibility
import com.limonatalp4.glasscalcproject.service.GlassCalcTileService
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.theme.resolveAccentSwatchColor
import com.limonatalp4.glasscalcproject.ui.util.HapticMotorType
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentTheme: AppTheme,
    currentAccentColor: Int? = null,
    recentAccentColors: List<Int> = emptyList(),
    accentHexInput: String = "",
    isAccentHexInvalid: Boolean = false,
    currentAppIcon: AppIconOption = AppIconOption.CLASSIC,
    currentLanguage: String,
    effectiveApiLevel: Int,
    currentAngleMode: AngleMode,
    onSelectLanguage: (String) -> Unit,
    windowSizeClass: WindowSizeClass?,
    simulatedApiEnabled: Boolean,
    simulatedApiLevel: Int,
    hapticFeedbackEnabled: Boolean,
    hapticIntensity: Int = 5,
    autoResultEnabled: Boolean,
    lifecycleStatus: String,
    onSelectTheme: (AppTheme) -> Unit,
    onSelectAccentColor: (Int?, Int?) -> Unit = { _, _ -> },
    onAccentHexChanged: (String) -> Unit = {},
    onSelectAppIcon: (AppIconOption) -> Unit = {},
    onSelectAngleMode: (AngleMode) -> Unit,
    onSetSimulatedApi: (Boolean, Int) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onSetHapticIntensity: (Int) -> Unit = {},
    onToggleAutoResult: (Boolean) -> Unit,
    onSimulateBackground: () -> Unit,
    onTestCrashProtection: () -> Unit,
    onResetApp: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val config = LocalConfiguration.current
    val context = LocalContext.current
    val view = LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    var showResetConfirmDialog by rememberSaveable { mutableStateOf(false) }
    val realDeviceApi = Build.VERSION.SDK_INT
    val sliderMin = 26f
    val sliderMax = realDeviceApi.toFloat().coerceAtLeast(sliderMin)
    val sliderSteps = if (sliderMax > sliderMin) (sliderMax - sliderMin).toInt() - 1 else 0

    var currentSliderApi by remember(simulatedApiLevel, sliderMax) { 
        mutableFloatStateOf(simulatedApiLevel.toFloat().coerceIn(sliderMin, sliderMax)) 
    }

    // Accordion expansion states for Design, Units & Language tabs
    var themeAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var accentColorAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var appIconAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var angleModeAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var languageAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var selectedAppIconPreview by rememberSaveable(currentAppIcon) { mutableStateOf(currentAppIcon) }

    // Accordion expansion states for Info tab
    var apacheAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var mxparserAccordionOpen by rememberSaveable { mutableStateOf(false) }

    // Accordion expansion states for Developer tab
    var devSystemApiAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var devHapticAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var devBehaviorAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var devStorageAccordionOpen by rememberSaveable { mutableStateOf(false) }

    val effectiveApi = if (simulatedApiEnabled) simulatedApiLevel else Build.VERSION.SDK_INT
    val isTiefschwarz = glassProps.isTiefschwarz
    val searchVisibility = rememberSettingsSearchVisibility(
        query = searchQuery,
        effectiveApiLevel = effectiveApi,
        isMaterialYouActive = glassProps.isMaterialYou
    )
    val isSearchActive = searchQuery.trim().isNotEmpty()
    var preSearchTab by remember { mutableIntStateOf(0) }
    var wasSearchActive by remember { mutableStateOf(false) }
    var clearedByTabTap by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, searchVisibility.bestTabIndex) {
        if (isSearchActive) {
            if (!wasSearchActive) {
                clearedByTabTap = false
                preSearchTab = selectedTab
                wasSearchActive = true
            }
            searchVisibility.bestTabIndex?.let { selectedTab = it }
        } else if (wasSearchActive) {
            wasSearchActive = false
            if (!clearedByTabTap) {
                selectedTab = preSearchTab
            }
            clearedByTabTap = false
        }
    }

    val dialogTransition = rememberDialogTransitionController(onDismiss)
    val coroutineScope = rememberCoroutineScope()

    @Suppress("DEPRECATION")
    BasicAlertDialog(
        onDismissRequest = dialogTransition.dismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier.testTag("dialog_settings")
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
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val pinTabBar = !(isLandscape && isImeVisible)
        val dialogTopPadding = if (isImeVisible) 8.dp else 16.dp
        val dialogBottomPadding = if (isImeVisible) 8.dp else 16.dp
        val dialogScrollState = rememberScrollState()

        LaunchedEffect(selectedTab) {
            dialogScrollState.scrollTo(0)
        }

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
                        customBgColor = if (isTiefschwarz) Color.Black else colorScheme.surfaceContainerHigh,
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
                                text = stringResource(R.string.title_settings),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
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
                                        .testTag("btn_close_settings")
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

                        Spacer(modifier = Modifier.height(if (isImeVisible) 8.dp else 12.dp))

                        // Liquid-Glass Settings Search Bar (direkt unter Titel "Einstellungen" und über Pill-Leiste)
                        SettingsSearchBar(
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
                            isTiefschwarz = isTiefschwarz
                        )

                        Spacer(modifier = Modifier.height(if (isImeVisible) 8.dp else 12.dp))

                        val currentIsSearchActive by rememberUpdatedState(isSearchActive)
                        val currentHapticEnabled by rememberUpdatedState(hapticFeedbackEnabled)
                        val currentHapticIntensity by rememberUpdatedState(hapticIntensity)
                        val handleTabSelect = remember {
                            { index: Int ->
                                HapticUtil.performAppHaptic(
                                    context,
                                    view,
                                    currentHapticEnabled,
                                    currentHapticIntensity
                                )
                                if (currentIsSearchActive) {
                                    clearedByTabTap = true
                                }
                                selectedTab = index
                                searchQuery = ""
                            }
                        }

                        if (pinTabBar) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                SettingsTabBar(
                                    selectedTab = selectedTab,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    onSelectTab = handleTabSelect
                                )
                            }
                            Spacer(modifier = Modifier.height(if (isImeVisible) 10.dp else 16.dp))
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(dialogScrollState)
                        ) {
                            if (!pinTabBar) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SettingsTabBar(
                                        selectedTab = selectedTab,
                                        isTiefschwarz = isTiefschwarz,
                                        colorScheme = colorScheme,
                                        onSelectTab = handleTabSelect
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                if (isSearchActive && !searchVisibility.hasAnyMatch) {
                    SettingsSearchEmptyState(
                        isTiefschwarz = isTiefschwarz
                    )
                } else {
                    AnimatedContent(
                        targetState = selectedTab,
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
                        label = "tab_transition",
                        modifier = Modifier.fillMaxWidth()
                    ) { targetTab ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            when (targetTab) {
                                0 -> AppearanceSection(
                                    currentTheme = currentTheme,
                                    currentAccentColor = currentAccentColor,
                                    recentAccentColors = recentAccentColors,
                                    accentHexInput = accentHexInput,
                                    isAccentHexInvalid = isAccentHexInvalid,
                                    currentAppIcon = selectedAppIconPreview,
                                    effectiveApiLevel = effectiveApiLevel,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    themeAccordionOpen = isSearchActive || themeAccordionOpen,
                                    accentColorAccordionOpen = isSearchActive || accentColorAccordionOpen,
                                    appIconAccordionOpen = isSearchActive || appIconAccordionOpen,
                                    onToggleThemeAccordion = { themeAccordionOpen = !themeAccordionOpen },
                                    onToggleAccentColorAccordion = { accentColorAccordionOpen = !accentColorAccordionOpen },
                                    onToggleAppIconAccordion = { appIconAccordionOpen = !appIconAccordionOpen },
                                    onSelectTheme = onSelectTheme,
                                    onSelectAccentColor = onSelectAccentColor,
                                    onAccentHexChanged = onAccentHexChanged,
                                    onSelectAppIcon = { icon ->
                                        selectedAppIconPreview = icon
                                        onSelectAppIcon(icon)
                                    },
                                    triggerHaptic = ::triggerHaptic,
                                    visibleThemes = searchVisibility.matchingThemes,
                                    showAccentColorCard = searchVisibility.showAccentColorCard && !glassProps.isMaterialYou,
                                    isMaterialYouActive = glassProps.isMaterialYou,
                                    visibleAppIcons = searchVisibility.matchingAppIcons
                                )
                                1 -> UnitsSection(
                                    currentAngleMode = currentAngleMode,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    angleModeAccordionOpen = isSearchActive || angleModeAccordionOpen,
                                    onToggleAngleModeAccordion = { angleModeAccordionOpen = !angleModeAccordionOpen },
                                    onSelectAngleMode = onSelectAngleMode,
                                    triggerHaptic = ::triggerHaptic,
                                    orderedCards = searchVisibility.orderedUnitCards
                                )
                                2 -> DeveloperOptionsSection(
                                    windowSizeClass = windowSizeClass,
                                    simulatedApiEnabled = simulatedApiEnabled,
                                    effectiveApi = effectiveApi,
                                    realDeviceApi = realDeviceApi,
                                    currentSliderApi = currentSliderApi,
                                    sliderMin = sliderMin,
                                    sliderMax = sliderMax,
                                    sliderSteps = sliderSteps,
                                    hapticFeedbackEnabled = hapticFeedbackEnabled,
                                    hapticIntensity = hapticIntensity,
                                    autoResultEnabled = autoResultEnabled,
                                    lifecycleStatus = lifecycleStatus,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    systemApiAccordionOpen = isSearchActive || devSystemApiAccordionOpen,
                                    hapticAccordionOpen = isSearchActive || devHapticAccordionOpen,
                                    behaviorAccordionOpen = isSearchActive || devBehaviorAccordionOpen,
                                    storageAccordionOpen = isSearchActive || devStorageAccordionOpen,
                                    onToggleSystemApiAccordion = { devSystemApiAccordionOpen = !devSystemApiAccordionOpen },
                                    onToggleHapticAccordion = { devHapticAccordionOpen = !devHapticAccordionOpen },
                                    onToggleBehaviorAccordion = { devBehaviorAccordionOpen = !devBehaviorAccordionOpen },
                                    onToggleStorageAccordion = { devStorageAccordionOpen = !devStorageAccordionOpen },
                                    onSetSimulatedApi = onSetSimulatedApi,
                                    onToggleHapticFeedback = onToggleHapticFeedback,
                                    onSetHapticIntensity = onSetHapticIntensity,
                                    onToggleAutoResult = onToggleAutoResult,
                                    onSimulateBackground = onSimulateBackground,
                                    onTestCrashProtection = onTestCrashProtection,
                                    onResetAppRequest = { showResetConfirmDialog = true },
                                    triggerHaptic = ::triggerHaptic,
                                    context = context,
                                    onSliderValueChange = { currentSliderApi = it },
                                    searchVisibility = searchVisibility
                                )
                                3 -> InformationSection(
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    apacheAccordionOpen = apacheAccordionOpen,
                                    mxparserAccordionOpen = mxparserAccordionOpen,
                                    onToggleApache = { apacheAccordionOpen = !apacheAccordionOpen },
                                    onToggleMxparser = { mxparserAccordionOpen = !mxparserAccordionOpen },
                                    triggerHaptic = ::triggerHaptic,
                                    context = context,
                                    searchVisibility = searchVisibility
                                )
                                4 -> LanguageSection(
                                    currentLanguage = currentLanguage,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    languageAccordionOpen = isSearchActive || languageAccordionOpen,
                                    onToggleLanguageAccordion = { languageAccordionOpen = !languageAccordionOpen },
                                    onSelectLanguage = onSelectLanguage,
                                    onDismiss = dialogTransition.dismiss,
                                    triggerHaptic = ::triggerHaptic,
                                    coroutineScope = coroutineScope,
                                    onSetDismissing = { if (it) dialogTransition.dismiss() },
                                    visibleLanguages = searchVisibility.matchingLanguages
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

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colorScheme.error) },
            title = { Text(stringResource(R.string.dialog_reset_title), color = colorScheme.primary) },
            text = { Text(stringResource(R.string.dialog_reset_text), color = colorScheme.primary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        showResetConfirmDialog = false
                        onResetApp()
                        dialogTransition.dismiss()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text(stringResource(R.string.dialog_reset_confirm), color = colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    triggerHaptic()
                    showResetConfirmDialog = false
                }) {
                    Text(stringResource(R.string.dialog_reset_cancel), color = colorScheme.primary)
                }
            }
        )
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppearanceSection(
    currentTheme: AppTheme,
    currentAccentColor: Int?,
    recentAccentColors: List<Int> = emptyList(),
    accentHexInput: String,
    isAccentHexInvalid: Boolean,
    currentAppIcon: AppIconOption,
    effectiveApiLevel: Int,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    themeAccordionOpen: Boolean,
    accentColorAccordionOpen: Boolean,
    appIconAccordionOpen: Boolean,
    onToggleThemeAccordion: () -> Unit,
    onToggleAccentColorAccordion: () -> Unit,
    onToggleAppIconAccordion: () -> Unit,
    onSelectTheme: (AppTheme) -> Unit,
    onSelectAccentColor: (Int?, Int?) -> Unit,
    onAccentHexChanged: (String) -> Unit,
    onSelectAppIcon: (AppIconOption) -> Unit,
    triggerHaptic: () -> Unit,
    visibleThemes: List<AppTheme> = AppTheme.entries,
    showAccentColorCard: Boolean = true,
    isMaterialYouActive: Boolean = false,
    visibleAppIcons: List<AppIconOption> = AppIconOption.entries
) {
    val matchedPreset = AccentColorOption.findPresetOrNull(currentAccentColor)
    val defaultSwatchColorArgb = remember(glassProps.isDark) {
        resolveAccentSwatchColor(colorArgb = null, isDark = glassProps.isDark).toArgb()
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (visibleThemes.isNotEmpty()) {
            SettingsAccordionCard(
                title = stringResource(id = R.string.theme_accordion_title),
                expanded = themeAccordionOpen,
                onToggle = onToggleThemeAccordion,
                isTiefschwarz = isTiefschwarz,
                colorScheme = colorScheme,
                glassProps = glassProps,
                triggerHaptic = triggerHaptic,
                cardTestTag = "card_theme_accordion",
                toggleButtonTestTag = "btn_toggle_theme_accordion",
                chevronLabel = "theme_chevron_rotation"
            ) {
                visibleThemes.forEach { themeOption ->
                    val isMaterialYou = themeOption == AppTheme.MATERIAL_YOU
                    val isSupported = !isMaterialYou || effectiveApiLevel >= 31
                    val isSelected = currentTheme == themeOption && isSupported

                    val themeLabel = when (themeOption) {
                        AppTheme.MATERIAL_YOU -> stringResource(id = R.string.theme_material_you)
                        AppTheme.AUTOMATIC -> stringResource(id = R.string.theme_automatic)
                        AppTheme.WHITE_MODE -> stringResource(id = R.string.theme_white_mode)
                        AppTheme.STANDARD_DARK -> stringResource(id = R.string.theme_standard_dark)
                        AppTheme.DARK_MODE -> stringResource(id = R.string.theme_dark_mode)
                    }
                    val amoledDesc = if (themeOption == AppTheme.DARK_MODE) {
                        stringResource(id = R.string.theme_amoled_desc)
                    } else null
                    val unsupportedError = if (!isSupported) {
                        stringResource(id = R.string.theme_material_you_unsupported, effectiveApiLevel)
                    } else null

                    SettingsPillOption(
                        selected = isSelected,
                        onClick = {
                            triggerHaptic()
                            onSelectTheme(themeOption)
                        },
                        title = themeLabel,
                        description = amoledDesc,
                        errorText = unsupportedError,
                        enabled = isSupported,
                        glassProps = glassProps,
                        testTag = "radio_theme_${themeOption.name}",
                        descriptionTestTag = if (amoledDesc != null) "text_theme_amoled_desc" else null,
                        errorTestTag = if (unsupportedError != null) "text_material_you_unsupported" else null
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isMaterialYouActive && showAccentColorCard,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            SettingsAccordionCard(
                title = stringResource(id = R.string.accent_color_accordion_title),
                expanded = accentColorAccordionOpen,
                onToggle = onToggleAccentColorAccordion,
                isTiefschwarz = isTiefschwarz,
                colorScheme = colorScheme,
                glassProps = glassProps,
                triggerHaptic = triggerHaptic,
                cardTestTag = "card_accent_color_accordion",
                toggleButtonTestTag = "btn_toggle_accent_color_accordion",
                chevronLabel = "accent_color_chevron_rotation"
            ) {
                Text(
                    text = stringResource(id = R.string.accent_color_desc),
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colorScheme.primary
                )

                val activeAccentLabel = if (matchedPreset != null) {
                    stringResource(id = matchedPreset.labelResId)
                } else {
                    CalculatorViewModel.formatAccentHex(currentAccentColor!!)
                }
                Text(
                    text = stringResource(id = R.string.accent_color_selected, activeAccentLabel),
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                    modifier = Modifier.testTag("text_active_accent_color")
                )

                SettingsGlassCard(
                    glassProps = glassProps,
                    shape = GlassDialogDefaults.NestedPanelShape,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 12.dp,
                        vertical = 12.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AccentColorOption.entries.forEach { option ->
                                val isSelected = matchedPreset == option
                                val label = stringResource(id = option.labelResId)
                                val swatchColor = resolveAccentSwatchColor(
                                    colorArgb = option.colorArgb,
                                    isDark = glassProps.isDark
                                )

                                AccentColorPillButton(
                                    option = option,
                                    swatchColor = swatchColor,
                                    label = label,
                                    isSelected = isSelected,
                                    isTiefschwarz = isTiefschwarz,
                                    glassProps = glassProps,
                                    onClick = {
                                        triggerHaptic()
                                        onSelectAccentColor(
                                            option.colorArgb,
                                            if (option.colorArgb == null) defaultSwatchColorArgb else null
                                        )
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = recentAccentColors.isNotEmpty(),
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.accent_color_recent_label),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.primary,
                                    modifier = Modifier
                                        .padding(start = 6.dp)
                                        .testTag("label_recent_accent_colors")
                                )
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.Start),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    recentAccentColors.take(5).forEachIndexed { index, argb ->
                                        RecentAccentColorMiniButton(
                                            color = Color(argb),
                                            hexLabel = CalculatorViewModel.formatAccentHex(argb),
                                            isSelected = currentAccentColor == argb,
                                            isTiefschwarz = isTiefschwarz,
                                            glassProps = glassProps,
                                            testTag = "recent_accent_color_$index",
                                            onClick = {
                                                triggerHaptic()
                                                onSelectAccentColor(argb, null)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        AccentColorHexInput(
                            hexInput = accentHexInput,
                            isInvalid = isAccentHexInvalid,
                            currentAccentColor = currentAccentColor,
                            defaultHexPlaceholder = CalculatorViewModel.formatAccentHex(defaultSwatchColorArgb),
                            isTiefschwarz = isTiefschwarz,
                            glassProps = glassProps,
                            triggerHaptic = triggerHaptic,
                            onHexInputChanged = onAccentHexChanged
                        )
                    }
                }
            }
        }

        if (visibleAppIcons.isNotEmpty()) {
            SettingsAccordionCard(
                title = stringResource(id = R.string.app_icon_accordion_title),
                expanded = appIconAccordionOpen,
                onToggle = onToggleAppIconAccordion,
                isTiefschwarz = isTiefschwarz,
                colorScheme = colorScheme,
                glassProps = glassProps,
                triggerHaptic = triggerHaptic,
                cardTestTag = "card_app_icon_accordion",
                toggleButtonTestTag = "btn_toggle_app_icon_accordion",
                chevronLabel = "app_icon_chevron_rotation"
            ) {
                visibleAppIcons.forEach { iconOption ->
                    val minApi = iconOption.requiresMinApi
                    val activeApi = minOf(Build.VERSION.SDK_INT, effectiveApiLevel)
                    val isSupported = minApi == 0 || (Build.VERSION.SDK_INT >= minApi && effectiveApiLevel >= minApi)
                    val isSelected = currentAppIcon == iconOption && isSupported
                    val unsupportedError = if (!isSupported) {
                        stringResource(id = R.string.app_icon_material_you_unsupported, activeApi)
                    } else null

                    AppIconPillOption(
                        iconOption = iconOption,
                        selected = isSelected,
                        enabled = isSupported,
                        errorText = unsupportedError,
                        errorTestTag = if (unsupportedError != null) "text_app_icon_material_you_unsupported" else null,
                        onClick = {
                            triggerHaptic()
                            onSelectAppIcon(iconOption)
                        },
                        glassProps = glassProps
                    )
                }
            }
        }
    }
}

@Composable
private fun AccentColorPillButton(
    option: AccentColorOption,
    swatchColor: Color,
    label: String,
    isSelected: Boolean,
    isTiefschwarz: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            glassProps.buttonBorderColor
        },
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "accent_ring_${option.name}"
    )
    val ringWidth by animateDpAsState(
        targetValue = if (isSelected) 2.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "accent_ring_width_${option.name}"
    )
    val checkTint = remember(swatchColor) {
        if (ColorUtils.calculateContrast(android.graphics.Color.WHITE, swatchColor.toArgb()) >= 3.0) {
            Color.White
        } else {
            Color(0xFF0A0A0A)
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .semantics {
                selected = isSelected
                contentDescription = label
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.RadioButton,
                onClick = onClick
            )
            .testTag("accent_color_${option.name}"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(
                    width = ringWidth,
                    color = ringColor,
                    shape = CircleShape
                )
                .padding(if (isSelected) 3.dp else 0.dp)
                .clip(CircleShape)
                .background(swatchColor)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.14f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = checkTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentAccentColorMiniButton(
    color: Color,
    hexLabel: String,
    isSelected: Boolean,
    isTiefschwarz: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val ringColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            glassProps.buttonBorderColor
        },
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "recent_accent_ring_$hexLabel"
    )
    val checkTint = remember(color) {
        if (ColorUtils.calculateContrast(android.graphics.Color.WHITE, color.toArgb()) >= 3.0) {
            Color.White
        } else {
            Color(0xFF0A0A0A)
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .pressScale(interactionSource)
            .clip(CircleShape)
            .semantics {
                selected = isSelected
                contentDescription = hexLabel
            }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                role = Role.RadioButton,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = ringColor,
                    shape = CircleShape
                )
                .padding(if (isSelected) 2.dp else 0.dp)
                .clip(CircleShape)
                .background(color)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.34f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.14f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = checkTint,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun AccentColorHexInput(
    hexInput: String,
    isInvalid: Boolean,
    currentAccentColor: Int?,
    defaultHexPlaceholder: String,
    isTiefschwarz: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    triggerHaptic: () -> Unit,
    onHexInputChanged: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = GlassDialogDefaults.PillShape
    val bgColor = GlassDialogDefaults.searchBarBackgroundColor(isTiefschwarz)
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val pillInteractionSource = remember { MutableInteractionSource() }
    val textFieldInteractionSource = remember { MutableInteractionSource() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val imeBottomPx = WindowInsets.ime.getBottom(density)

    var hexFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = hexInput,
                selection = TextRange(hexInput.length)
            )
        )
    }

    LaunchedEffect(hexInput) {
        if (hexFieldValue.text != hexInput) {
            hexFieldValue = TextFieldValue(
                text = hexInput,
                selection = TextRange(hexInput.length)
            )
        }
    }

    LaunchedEffect(isFocused, imeBottomPx) {
        if (isFocused) {
            delay(80)
            bringIntoViewRequester.bringIntoView()
        }
    }

    val currentTriggerHaptic by rememberUpdatedState(triggerHaptic)
    LaunchedEffect(textFieldInteractionSource) {
        textFieldInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                currentTriggerHaptic()
            }
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (isInvalid) colorScheme.error else glassProps.buttonBorderColor,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "accent_hex_border_color"
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isInvalid) 1.5.dp else 1.dp,
        animationSpec = tween(durationMillis = 160, easing = FastOutSlowInEasing),
        label = "accent_hex_border_width"
    )

    val parsedValidArgb = CalculatorViewModel.parseValidAccentHexOrNull(hexFieldValue.text)
    val previewColor = parsedValidArgb?.let { Color(it) }
        ?: resolveAccentSwatchColor(colorArgb = currentAccentColor, isDark = glassProps.isDark)

    val textColor = colorScheme.primary
    val hintColor = colorScheme.primary.copy(alpha = 0.5f)
    val hexLabel = stringResource(id = R.string.accent_color_hex_label)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = hexLabel,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isInvalid) colorScheme.error else colorScheme.primary,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .clip(shape)
                .background(bgColor)
                .border(width = borderWidth, color = borderColor, shape = shape)
                .clickable(
                    interactionSource = pillInteractionSource,
                    indication = null
                ) {
                    triggerHaptic()
                    focusRequester.requestFocus()
                }
                .padding(
                    start = 12.dp,
                    end = if (hexFieldValue.text.isNotEmpty()) 4.dp else 14.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(previewColor)
                    .border(
                        width = 1.dp,
                        color = glassProps.buttonBorderColor,
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = hexFieldValue,
                    onValueChange = { nextValue ->
                        val sanitized = CalculatorViewModel.sanitizeAccentHexInput(nextValue.text)
                        val cursorEnd = nextValue.selection.end.coerceIn(0, nextValue.text.length)
                        val hexCountBeforeCursor = nextValue.text.substring(0, cursorEnd).count {
                            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F'
                        }
                        val nextSelectionPos = if (sanitized.isEmpty()) {
                            0
                        } else {
                            (1 + hexCountBeforeCursor).coerceIn(
                                if (sanitized.startsWith("#")) 1 else 0,
                                sanitized.length
                            )
                        }
                        hexFieldValue = TextFieldValue(
                            text = sanitized,
                            selection = TextRange(nextSelectionPos)
                        )
                        onHexInputChanged(sanitized)
                    },
                    singleLine = true,
                    interactionSource = textFieldInteractionSource,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { state -> isFocused = state.isFocused }
                        .semantics { contentDescription = hexLabel }
                        .testTag("input_accent_hex"),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (hexFieldValue.text.isEmpty()) {
                                Text(
                                    text = defaultHexPlaceholder,
                                    color = hintColor,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            if (hexFieldValue.text.isNotEmpty()) {
                val clearInteraction = remember { MutableInteractionSource() }
                val clearPillShape = RoundedCornerShape(50)
                val clearPillBorder = BorderStroke(
                    width = 1.dp,
                    color = if (isInvalid) {
                        colorScheme.error.copy(alpha = 0.45f)
                    } else {
                        colorScheme.outline.copy(alpha = 0.3f)
                    }
                )
                IconButton(
                    onClick = {
                        triggerHaptic()
                        hexFieldValue = TextFieldValue(text = "", selection = TextRange(0))
                        onHexInputChanged("")
                    },
                    interactionSource = clearInteraction,
                    modifier = Modifier
                        .size(48.dp)
                        .pressScale(clearInteraction)
                        .testTag("btn_clear_accent_hex")
                ) {
                    Box(
                        modifier = Modifier
                            .clip(clearPillShape)
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(border = clearPillBorder, shape = clearPillShape)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.accent_color_hex_clear),
                            tint = if (isInvalid) {
                                colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isInvalid,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Text(
                text = stringResource(id = R.string.accent_color_hex_invalid),
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = colorScheme.error,
                modifier = Modifier
                    .padding(start = 8.dp, top = 5.dp)
                    .testTag("text_accent_hex_error")
            )
        }
    }
}

@Composable
private fun SettingsAccordionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    triggerHaptic: () -> Unit,
    cardTestTag: String,
    toggleButtonTestTag: String,
    chevronLabel: String,
    content: @Composable () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = chevronLabel
    )
    val cardInteraction = remember { MutableInteractionSource() }
    val pillInteraction = remember { MutableInteractionSource() }

    SettingsGlassCard(
        glassProps = glassProps,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        onClick = {
            triggerHaptic()
            onToggle()
        },
        role = Role.Button,
        interactionSource = cardInteraction,
        modifier = Modifier.testTag(cardTestTag)
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                )
                GlassSurface(
                    shape = CircleShape,
                    customBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                            .testTag(toggleButtonTestTag)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
private fun AppIconPillOption(
    iconOption: AppIconOption,
    selected: Boolean,
    onClick: () -> Unit,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    enabled: Boolean = true,
    errorText: String? = null,
    errorTestTag: String? = null
) {
    val label = stringResource(id = iconOption.labelResId)
    val description = iconOption.descriptionResId?.let { stringResource(id = it) }
    val hasSecondaryText = description != null || errorText != null

    GlassPillOptionContainer(
        selected = selected,
        onClick = onClick,
        glassProps = glassProps,
        modifier = Modifier.testTag("radio_app_icon_${iconOption.name}"),
        enabled = enabled,
        minHeight = GlassDialogDefaults.PillOptionExpandedMinHeight,
        contentPadding = GlassDialogDefaults.PillOptionPadding,
        animationLabel = "pill_app_icon_${iconOption.name}"
    ) { textColor ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    },
                    disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                enabled = enabled
            )

            Spacer(modifier = Modifier.width(10.dp))

            AppIconPreviewBadge(
                iconOption = iconOption,
                selected = selected,
                glassProps = glassProps
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp)
            ) {
                Text(
                    text = label,
                    fontSize = 14.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    maxLines = if (hasSecondaryText) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = errorText,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (errorTestTag != null) Modifier.testTag(errorTestTag) else Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun AppIconPreviewBadge(
    iconOption: AppIconOption,
    selected: Boolean,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties
) {
    val previewShape = RoundedCornerShape(12.dp)
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        glassProps.buttonBorderColor
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(previewShape)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = previewShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconOption.backgroundResId),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = iconOption.foregroundResId),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.32f
                    scaleY = 1.32f
                }
        )
    }
}

@Composable
private fun UnitsSection(
    currentAngleMode: AngleMode,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    angleModeAccordionOpen: Boolean,
    onToggleAngleModeAccordion: () -> Unit,
    onSelectAngleMode: (AngleMode) -> Unit,
    triggerHaptic: () -> Unit,
    orderedCards: List<SettingsUnitCard> = SettingsUnitCard.entries
) {
    if (orderedCards.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingsAccordionCard(
            title = stringResource(id = R.string.angle_mode_accordion_title),
            expanded = angleModeAccordionOpen,
            onToggle = onToggleAngleModeAccordion,
            isTiefschwarz = isTiefschwarz,
            colorScheme = colorScheme,
            glassProps = glassProps,
            triggerHaptic = triggerHaptic,
            cardTestTag = "card_angle_mode_accordion",
            toggleButtonTestTag = "btn_toggle_angle_mode_accordion",
            chevronLabel = "angle_mode_chevron_rotation"
        ) {
            orderedCards.forEach { angleCard ->
                when (angleCard) {
                    SettingsUnitCard.DEG -> {
                        SettingsPillOption(
                            selected = currentAngleMode == AngleMode.DEG,
                            onClick = {
                                triggerHaptic()
                                onSelectAngleMode(AngleMode.DEG)
                            },
                            title = stringResource(id = R.string.angle_deg_label),
                            description = stringResource(id = R.string.angle_deg_desc),
                            glassProps = glassProps,
                            testTag = "radio_angle_deg"
                        )
                    }
                    SettingsUnitCard.RAD -> {
                        SettingsPillOption(
                            selected = currentAngleMode == AngleMode.RAD,
                            onClick = {
                                triggerHaptic()
                                onSelectAngleMode(AngleMode.RAD)
                            },
                            title = stringResource(id = R.string.angle_rad_label),
                            description = stringResource(id = R.string.angle_rad_desc),
                            glassProps = glassProps,
                            testTag = "radio_angle_rad"
                        )
                    }
                }
            }
        }
    }
}

private enum class SettingsDevGroup {
    SYSTEM_API,
    HAPTIC_FEEDBACK,
    APP_BEHAVIOR,
    STORAGE_CACHE
}

private fun SettingsDevCard.group(): SettingsDevGroup = when (this) {
    SettingsDevCard.SCREEN, SettingsDevCard.QUICK_SETTINGS_TILE, SettingsDevCard.API -> SettingsDevGroup.SYSTEM_API
    SettingsDevCard.HAPTIC -> SettingsDevGroup.HAPTIC_FEEDBACK
    SettingsDevCard.AUTO_RESULT, SettingsDevCard.LIFECYCLE -> SettingsDevGroup.APP_BEHAVIOR
    SettingsDevCard.CLEAR_CACHE, SettingsDevCard.RESET_STORAGE -> SettingsDevGroup.STORAGE_CACHE
}

@Composable
private fun DeveloperOptionsSection(
    windowSizeClass: WindowSizeClass?,
    simulatedApiEnabled: Boolean,
    effectiveApi: Int,
    realDeviceApi: Int,
    currentSliderApi: Float,
    sliderMin: Float,
    sliderMax: Float,
    sliderSteps: Int,
    hapticFeedbackEnabled: Boolean,
    hapticIntensity: Int,
    autoResultEnabled: Boolean,
    lifecycleStatus: String,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    systemApiAccordionOpen: Boolean,
    hapticAccordionOpen: Boolean,
    behaviorAccordionOpen: Boolean,
    storageAccordionOpen: Boolean,
    onToggleSystemApiAccordion: () -> Unit,
    onToggleHapticAccordion: () -> Unit,
    onToggleBehaviorAccordion: () -> Unit,
    onToggleStorageAccordion: () -> Unit,
    onSetSimulatedApi: (Boolean, Int) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onSetHapticIntensity: (Int) -> Unit,
    onToggleAutoResult: (Boolean) -> Unit,
    onSimulateBackground: () -> Unit,
    onTestCrashProtection: () -> Unit,
    onResetAppRequest: () -> Unit,
    triggerHaptic: () -> Unit,
    context: android.content.Context,
    onSliderValueChange: (Float) -> Unit,
    searchVisibility: SettingsSearchVisibility? = null
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidthDp = with(density) { containerSize.width.toDp().value.roundToInt() }
    val screenHeightDp = with(density) { containerSize.height.toDp().value.roundToInt() }
    val glassProps = LocalGlassProperties.current

    val hasVibratorMotor = remember(context) { HapticUtil.hasVibrator(context) }
    val hapticMotorType = remember(context) { HapticUtil.getHapticMotorType(context) }
    val isSupportedBySimulatedApi = effectiveApi >= 26
    val isHapticUsable = hasVibratorMotor && isSupportedBySimulatedApi
    val isIntensitySliderEnabled = hapticMotorType != HapticMotorType.NONE && isSupportedBySimulatedApi && hapticFeedbackEnabled
    var sliderIntensity by remember(hapticIntensity) {
        mutableFloatStateOf(hapticIntensity.toFloat().coerceIn(1f, 10f))
    }

    val orderedDevCards = searchVisibility?.orderedDevCards ?: SettingsDevCard.entries
    if (orderedDevCards.isEmpty()) return

    val groupedCards = orderedDevCards.groupBy { it.group() }
    val orderedGroups = orderedDevCards.map { it.group() }.distinct()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        orderedGroups.forEach { group ->
            val cardsInGroup = groupedCards[group].orEmpty()
            val title = when (group) {
                SettingsDevGroup.SYSTEM_API -> stringResource(R.string.dev_accordion_system_api)
                SettingsDevGroup.HAPTIC_FEEDBACK -> stringResource(R.string.dev_accordion_haptic_feedback)
                SettingsDevGroup.APP_BEHAVIOR -> stringResource(R.string.dev_accordion_app_behavior)
                SettingsDevGroup.STORAGE_CACHE -> stringResource(R.string.dev_accordion_storage_cache)
            }
            val expanded = when (group) {
                SettingsDevGroup.SYSTEM_API -> systemApiAccordionOpen
                SettingsDevGroup.HAPTIC_FEEDBACK -> hapticAccordionOpen
                SettingsDevGroup.APP_BEHAVIOR -> behaviorAccordionOpen
                SettingsDevGroup.STORAGE_CACHE -> storageAccordionOpen
            }
            val onToggle = when (group) {
                SettingsDevGroup.SYSTEM_API -> onToggleSystemApiAccordion
                SettingsDevGroup.HAPTIC_FEEDBACK -> onToggleHapticAccordion
                SettingsDevGroup.APP_BEHAVIOR -> onToggleBehaviorAccordion
                SettingsDevGroup.STORAGE_CACHE -> onToggleStorageAccordion
            }
            val cardTestTag = when (group) {
                SettingsDevGroup.SYSTEM_API -> "card_dev_system_api_accordion"
                SettingsDevGroup.HAPTIC_FEEDBACK -> "card_dev_haptic_accordion"
                SettingsDevGroup.APP_BEHAVIOR -> "card_dev_behavior_accordion"
                SettingsDevGroup.STORAGE_CACHE -> "card_dev_storage_accordion"
            }
            val toggleButtonTestTag = when (group) {
                SettingsDevGroup.SYSTEM_API -> "btn_toggle_dev_system_api_accordion"
                SettingsDevGroup.HAPTIC_FEEDBACK -> "btn_toggle_dev_haptic_accordion"
                SettingsDevGroup.APP_BEHAVIOR -> "btn_toggle_dev_behavior_accordion"
                SettingsDevGroup.STORAGE_CACHE -> "btn_toggle_dev_storage_accordion"
            }
            val chevronLabel = when (group) {
                SettingsDevGroup.SYSTEM_API -> "dev_system_api_chevron_rotation"
                SettingsDevGroup.HAPTIC_FEEDBACK -> "dev_haptic_chevron_rotation"
                SettingsDevGroup.APP_BEHAVIOR -> "dev_behavior_chevron_rotation"
                SettingsDevGroup.STORAGE_CACHE -> "dev_storage_chevron_rotation"
            }

            SettingsAccordionCard(
                title = title,
                expanded = expanded,
                onToggle = onToggle,
                isTiefschwarz = isTiefschwarz,
                colorScheme = colorScheme,
                glassProps = glassProps,
                triggerHaptic = triggerHaptic,
                cardTestTag = cardTestTag,
                toggleButtonTestTag = toggleButtonTestTag,
                chevronLabel = chevronLabel
            ) {
                cardsInGroup.forEach { devCard ->
                    when (devCard) {
                        SettingsDevCard.SCREEN -> {
                            // Card 1: Screen & WindowSizeClass
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.dev_screen_scaling),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    @Suppress("DEPRECATION")
                                    val widthClassName = windowSizeClass?.windowWidthSizeClass ?: "Compact"
                                    Text(
                                        text = "WindowSizeClass: $widthClassName",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.dev_resolution, screenWidthDp, screenHeightDp),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.primary
                                    )
                                }
                            }
                        }
                        SettingsDevCard.QUICK_SETTINGS_TILE -> {
                            // Card 2: Quick Settings Tile (StatusBarManager Android 13+)
                            val qsTileBtnInteraction = remember { MutableInteractionSource() }
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape,
                                modifier = Modifier.testTag("card_qs_tile")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.info_qs_tile_title),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary
                                            )
                                            Text(
                                                text = stringResource(R.string.info_qs_tile_desc),
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp,
                                                color = colorScheme.primary
                                            )
                                        }
                                        GlassSurface(
                                            shape = CircleShape,
                                            customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            borderStroke = glassProps.containerBorderStroke
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_qs_tile_gc),
                                                    contentDescription = stringResource(R.string.info_qs_tile_title),
                                                    tint = colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    GlassSurface(
                                        shape = GlassDialogDefaults.PillShape,
                                        customBgColor = colorScheme.primaryContainer.copy(alpha = if (isTiefschwarz) 0.55f else 0.72f),
                                        borderStroke = glassProps.containerBorderStroke,
                                        onClick = {
                                            triggerHaptic()
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                val statusBarManager = context.getSystemService(StatusBarManager::class.java)
                                                if (statusBarManager != null) {
                                                    statusBarManager.requestAddTileService(
                                                        ComponentName(context, GlassCalcTileService::class.java),
                                                        context.getString(R.string.app_name),
                                                        Icon.createWithResource(context, R.drawable.ic_qs_tile_gc),
                                                        context.mainExecutor
                                                    ) { resultCode ->
                                                        val msgRes = when (resultCode) {
                                                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> R.string.qs_tile_added
                                                            StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.qs_tile_already_added
                                                            else -> R.string.qs_tile_not_added
                                                        }
                                                        Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        context.getString(R.string.qs_tile_not_added),
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.qs_tile_requires_android_13),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        role = Role.Button,
                                        interactionSource = qsTileBtnInteraction,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                            .pressScale(qsTileBtnInteraction)
                                            .testTag("btn_add_qs_tile")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = stringResource(R.string.info_qs_tile_btn),
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        SettingsDevCard.API -> {
                            // Card 3: Simulated API Level
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.dev_simulated_api_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.dev_effective_ui_api, effectiveApi),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (effectiveApi < 31) colorScheme.error else colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = stringResource(R.string.dev_simulate_api_switch),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Switch(
                                            checked = simulatedApiEnabled,
                                            onCheckedChange = { checked ->
                                                triggerHaptic()
                                                onSetSimulatedApi(checked, currentSliderApi.toInt())
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = colorScheme.onPrimary,
                                                checkedTrackColor = colorScheme.primary,
                                                uncheckedThumbColor = colorScheme.outline,
                                                uncheckedTrackColor = colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.testTag("switch_simulate_api")
                                        )
                                    }

                                    if (simulatedApiEnabled) {
                                        val apiSliderPillShape = GlassDialogDefaults.NestedPanelShape
                                        val apiSliderPillBg = GlassDialogDefaults.cardBackgroundColor()
                                        val apiSliderPillBorder = glassProps.containerBorderStroke
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                                .clip(apiSliderPillShape)
                                                .background(apiSliderPillBg)
                                                .border(apiSliderPillBorder, shape = apiSliderPillShape)
                                                .padding(horizontal = 20.dp, vertical = 16.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.dev_api_level_slider, currentSliderApi.roundToInt()),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Slider(
                                                value = currentSliderApi,
                                                onValueChange = { newValue ->
                                                    val newApi = newValue.roundToInt()
                                                    val previousApi = currentSliderApi.roundToInt()
                                                    onSliderValueChange(newApi.toFloat())
                                                    if (newApi != previousApi && hapticFeedbackEnabled) {
                                                        HapticUtil.performAppHaptic(
                                                            context = context,
                                                            view = view,
                                                            enabled = hapticFeedbackEnabled,
                                                            intensityLevel = hapticIntensity
                                                        )
                                                    }
                                                },
                                                onValueChangeFinished = {
                                                    onSetSimulatedApi(true, currentSliderApi.roundToInt())
                                                },
                                                valueRange = sliderMin..sliderMax,
                                                steps = sliderSteps,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = MaterialTheme.colorScheme.primary,
                                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                    activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                                                    inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                                ),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("slider_simulated_api")
                                            )
                                        }
                                    }

                                    // Dynamic API Feature Legend
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(R.string.dev_api_feature_compatibility, realDeviceApi),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val apiFeatures = remember {
                                        listOf(
                                            R.string.dev_feature_vibration_effect to 26,
                                            R.string.dev_feature_edge_to_edge to 29,
                                            R.string.dev_feature_material_you to 31,
                                            R.string.dev_feature_blur_effect to 31,
                                            R.string.dev_feature_swipe_gestures to 33
                                        )
                                    }

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        apiFeatures.forEach { (nameResId, minApi) ->
                                            val isAvailable = effectiveApi >= minApi
                                            val pillBg = if (isAvailable) colorScheme.primaryContainer.copy(alpha = 0.85f) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            val textColor = if (isAvailable) colorScheme.primary else colorScheme.primary.copy(alpha = 0.3f)
                                            val featureName = stringResource(nameResId)
                                            val apiSuffix = stringResource(R.string.dev_api_level_plus, minApi)

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(percent = 50))
                                                    .background(pillBg)
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                    modifier = Modifier.wrapContentWidth()
                                                ) {
                                                    Icon(
                                                        imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Close,
                                                        contentDescription = null,
                                                        tint = if (isAvailable) colorScheme.primary else colorScheme.primary.copy(alpha = 0.3f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "$featureName ($apiSuffix)",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Medium,
                                                        color = textColor,
                                                        softWrap = true,
                                                        lineHeight = 13.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        SettingsDevCard.HAPTIC -> {
                            // Card 3: Haptic Feedback with hardware motor detection
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            val hapticTextColor = if (isHapticUsable) {
                                                colorScheme.primary
                                            } else {
                                                colorScheme.primary.copy(alpha = 0.3f)
                                            }
                                            Text(
                                                text = stringResource(R.string.dev_haptic_title),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = hapticTextColor
                                            )
                                            Text(
                                                text = stringResource(R.string.dev_haptic_desc),
                                                fontSize = 12.sp,
                                                color = hapticTextColor
                                            )
                                        }
                                        Switch(
                                            checked = if (isHapticUsable) hapticFeedbackEnabled else false,
                                            onCheckedChange = if (isHapticUsable) {
                                                { enabled ->
                                                    onToggleHapticFeedback(enabled)
                                                    if (enabled) {
                                                        HapticUtil.performAppHaptic(
                                                            context,
                                                            view,
                                                            true,
                                                            sliderIntensity.roundToInt().coerceIn(1, 10)
                                                        )
                                                    }
                                                }
                                            } else null,
                                            enabled = isHapticUsable,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = colorScheme.onPrimary,
                                                checkedTrackColor = colorScheme.primary,
                                                uncheckedThumbColor = colorScheme.outline,
                                                uncheckedTrackColor = colorScheme.surfaceVariant,
                                                disabledCheckedThumbColor = colorScheme.onSurface.copy(alpha = 0.38f),
                                                disabledCheckedTrackColor = colorScheme.surfaceVariant.copy(alpha = 0.38f),
                                                disabledUncheckedThumbColor = colorScheme.outline.copy(alpha = 0.38f),
                                                disabledUncheckedTrackColor = colorScheme.surfaceVariant.copy(alpha = 0.24f)
                                            ),
                                            modifier = Modifier.testTag("switch_haptic_feedback")
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val motorLabel = when (hapticMotorType) {
                                        HapticMotorType.LRA -> "LRA (Linear Resonant Actuator)"
                                        HapticMotorType.ERM -> "ERM (Eccentric Rotating Mass)"
                                        else -> "NONE"
                                    }

                                    Text(
                                        text = "${stringResource(R.string.dev_haptic_detected_motor)}: $motorLabel",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isHapticUsable) {
                                            colorScheme.primary
                                        } else {
                                            colorScheme.primary.copy(alpha = 0.3f)
                                        }
                                    )

                                    // Haptic Intensity Slider Pill (10 levels: 1..10, default 5)
                                    val currentLevel = sliderIntensity.roundToInt().coerceIn(1, 10)
                                    val sliderPillShape = RoundedCornerShape(28.dp)
                                    val sliderPillBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    val sliderPillBorder = glassProps.containerBorderStroke
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .clip(sliderPillShape)
                                            .background(sliderPillBg)
                                            .border(sliderPillBorder, shape = sliderPillShape)
                                            .padding(horizontal = 20.dp, vertical = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val sliderLabelColor = if (isIntensitySliderEnabled) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                            }
                                            Text(
                                                text = stringResource(R.string.dev_haptic_intensity_label),
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = sliderLabelColor
                                            )
                                            Text(
                                                text = stringResource(R.string.dev_haptic_intensity_level, currentLevel),
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = sliderLabelColor,
                                                modifier = Modifier.testTag("text_haptic_intensity_level")
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Slider(
                                            value = sliderIntensity,
                                            onValueChange = { newValue ->
                                                val newLevel = newValue.roundToInt().coerceIn(1, 10)
                                                val previousLevel = sliderIntensity.roundToInt().coerceIn(1, 10)
                                                sliderIntensity = newLevel.toFloat()
                                                if (newLevel != previousLevel) {
                                                    onSetHapticIntensity(newLevel)
                                                    HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, newLevel)
                                                }
                                            },
                                            onValueChangeFinished = {
                                                onSetHapticIntensity(sliderIntensity.roundToInt().coerceIn(1, 10))
                                            },
                                            valueRange = 1f..10f,
                                            steps = 8,
                                            enabled = isIntensitySliderEnabled,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MaterialTheme.colorScheme.primary,
                                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                                                inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                                disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                                                disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                disabledActiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                                disabledInactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("slider_haptic_intensity")
                                        )
                                    }

                                    if (!hasVibratorMotor) {
                                        Text(
                                            text = stringResource(R.string.dev_haptic_unsupported_device),
                                            fontSize = 12.sp,
                                            color = colorScheme.error,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    } else if (effectiveApi < 26) {
                                        Text(
                                            text = stringResource(R.string.dev_haptic_unsupported_api),
                                            fontSize = 12.sp,
                                            color = colorScheme.error,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                        SettingsDevCard.AUTO_RESULT -> {
                            // Card 4: Auto Result Switch (Pill-Option)
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = RoundedCornerShape(percent = 50),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                    horizontal = 24.dp,
                                    vertical = 14.dp
                                ),
                                modifier = Modifier.defaultMinSize(minHeight = 56.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                                        Text(
                                            text = stringResource(R.string.dev_auto_result_title),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(R.string.dev_auto_result_desc),
                                            fontSize = 12.sp,
                                            color = colorScheme.primary
                                        )
                                    }
                                    Switch(
                                        checked = autoResultEnabled,
                                        onCheckedChange = {
                                            triggerHaptic()
                                            onToggleAutoResult(it)
                                        },
                                        modifier = Modifier.testTag("switch_auto_result")
                                    )
                                }
                            }
                        }
                        SettingsDevCard.LIFECYCLE -> {
                            // Card 5: Lifecycle & State
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.dev_lifecycle_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Text(
                                        text = "Status: $lifecycleStatus",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.primary
                                    )
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                triggerHaptic()
                                                onSimulateBackground()
                                            },
                                            shape = RoundedCornerShape(percent = 50),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_simulate_background")
                                        ) {
                                            Text(
                                                text = stringResource(R.string.dev_btn_background),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                triggerHaptic()
                                                onTestCrashProtection()
                                            },
                                            shape = RoundedCornerShape(percent = 50),
                                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_test_crash_protection")
                                        ) {
                                            Text(
                                                text = stringResource(R.string.dev_btn_crash_test),
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        SettingsDevCard.CLEAR_CACHE -> {
                            // Card 6: Cache leeren (Eigene Karte)
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.dev_clear_cache_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.dev_clear_cache_desc),
                                        fontSize = 12.sp,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            coroutineScope.launch {
                                                val success = withContext(Dispatchers.IO) {
                                                    runCatching {
                                                        context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                                                    }.isSuccess
                                                }
                                                val msgRes = if (success) R.string.toast_cache_cleared else R.string.toast_cache_error
                                                Toast.makeText(context, context.getString(msgRes), Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_clear_cache"),
                                        shape = RoundedCornerShape(50),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.primaryContainer,
                                            contentColor = colorScheme.primary
                                        )
                                    ) {
                                        Text(stringResource(R.string.dev_btn_clear_cache), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        SettingsDevCard.RESET_STORAGE -> {
                            // Card 7: App-Speicher zurücksetzen
                            SettingsGlassCard(
                                glassProps = glassProps,
                                shape = GlassDialogDefaults.NestedPanelShape
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = stringResource(R.string.dev_reset_storage_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.error
                                    )
                                    Text(
                                        text = stringResource(R.string.dev_reset_storage_desc),
                                        fontSize = 12.sp,
                                        color = colorScheme.primary
                                    )
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            onResetAppRequest()
                                        },
                                        shape = RoundedCornerShape(percent = 50),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .testTag("btn_reset_storage")
                                    ) {
                                        Text(stringResource(R.string.dev_btn_reset_storage), fontWeight = FontWeight.Bold)
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
private fun InformationSection(
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    apacheAccordionOpen: Boolean,
    mxparserAccordionOpen: Boolean,
    onToggleApache: () -> Unit,
    onToggleMxparser: () -> Unit,
    triggerHaptic: () -> Unit,
    context: android.content.Context,
    searchVisibility: SettingsSearchVisibility? = null
) {
    val orderedInfoCards = searchVisibility?.orderedInfoCards ?: SettingsInfoCard.entries

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        orderedInfoCards.forEach { infoCard ->
            when (infoCard) {
                SettingsInfoCard.VERSION -> {
        // App Version Card (Pill-Design)
        SettingsGlassCard(
            glassProps = glassProps,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            modifier = Modifier
                .defaultMinSize(minHeight = 56.dp)
                .testTag("card_app_version")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "GlassCalc v${BuildConfig.VERSION_NAME}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.app_version_subtitle),
                    fontSize = 12.sp,
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.copyright_notice),
                    fontSize = 11.sp,
                    color = colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
                }
                SettingsInfoCard.GITHUB -> {
        // GitHub Repository Card (Pill-Design)
        val githubCardInteraction = remember { MutableInteractionSource() }

        SettingsGlassCard(
            glassProps = glassProps,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            onClick = {
                triggerHaptic()
                try {
                    val intent = Intent(Intent.ACTION_VIEW, "https://github.com/LimoLP4/GlassCalc-Android/releases/".toUri())
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, context.getString(R.string.info_github_repo), Toast.LENGTH_SHORT).show()
                }
            },
            role = Role.Button,
            interactionSource = githubCardInteraction,
            modifier = Modifier
                .defaultMinSize(minHeight = 56.dp)
                .pressScale(githubCardInteraction)
                .testTag("card_github_repo")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.info_github_repo),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = "LimoLP4/GlassCalc-Android",
                        fontSize = 12.sp,
                        color = colorScheme.primary
                    )
                }
                GlassSurface(
                    shape = CircleShape,
                    customBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke,
                    modifier = Modifier.testTag("github_repo_icon_pill")
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = stringResource(R.string.info_github_repo),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
                }
                SettingsInfoCard.FRANKFURTER_API -> {
        // Frankfurter API Card (Pill-Design matching GitHub Repository)
        val frankfurterCardInteraction = remember { MutableInteractionSource() }

        SettingsGlassCard(
            glassProps = glassProps,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            onClick = {
                triggerHaptic()
                try {
                    val intent = Intent(Intent.ACTION_VIEW, "https://www.frankfurter.app".toUri())
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.info_oss_frankfurter_title),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            role = Role.Button,
            interactionSource = frankfurterCardInteraction,
            modifier = Modifier
                .defaultMinSize(minHeight = 56.dp)
                .pressScale(frankfurterCardInteraction)
                .testTag("card_frankfurter_api")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.info_oss_frankfurter_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.info_frankfurter_desc),
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        color = colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.info_oss_frankfurter_license),
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        color = colorScheme.primary
                    )
                }
                GlassSurface(
                    shape = CircleShape,
                    customBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke,
                    modifier = Modifier.testTag("frankfurter_api_icon_pill")
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = stringResource(R.string.info_oss_frankfurter_title),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
                }
                SettingsInfoCard.OSS -> {
        // Third-Party OSS Licenses Card (Compact Pill-Design)
        val ossCardInteraction = remember { MutableInteractionSource() }

        SettingsGlassCard(
            glassProps = glassProps,
            shape = RoundedCornerShape(percent = 50),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 14.dp),
            onClick = {
                triggerHaptic()
                try {
                    @Suppress("DEPRECATION")
                    val intent = Intent(context, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
                    context.startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(context, context.getString(R.string.error_calculation), Toast.LENGTH_SHORT).show()
                }
            },
            role = Role.Button,
            interactionSource = ossCardInteraction,
            modifier = Modifier
                .defaultMinSize(minHeight = 56.dp)
                .pressScale(ossCardInteraction)
                .testTag("card_oss_licenses")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.info_oss_licenses),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.info_oss_licenses_desc),
                        fontSize = 12.sp,
                        color = colorScheme.primary
                    )
                }
                GlassSurface(
                    shape = CircleShape,
                    customBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    borderStroke = glassProps.containerBorderStroke,
                    modifier = Modifier.testTag("oss_licenses_icon_pill")
                ) {
                    Box(
                        modifier = Modifier.padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = stringResource(R.string.info_oss_licenses),
                            tint = colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
                }
                SettingsInfoCard.APACHE -> {
        // Accordion 1: Apache
        val apacheChevronRotation by animateFloatAsState(
            targetValue = if (apacheAccordionOpen) 180f else 0f,
            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
            label = "apache_chevron_rotation"
        )
        val apacheCardInteraction = remember { MutableInteractionSource() }
        val apachePillInteraction = remember { MutableInteractionSource() }

        SettingsGlassCard(
            glassProps = glassProps,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            onClick = {
                triggerHaptic()
                onToggleApache()
            },
            role = Role.Button,
            interactionSource = apacheCardInteraction,
            modifier = Modifier.testTag("card_apache_license")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Apache License 2.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    )
                    GlassSurface(
                        shape = CircleShape,
                        customBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        borderStroke = glassProps.containerBorderStroke,
                        modifier = Modifier
                            .size(36.dp)
                            .pressScale(apachePillInteraction)
                    ) {
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                onToggleApache()
                            },
                            interactionSource = apachePillInteraction,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("btn_toggle_apache_license")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Apache License 2.0",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .graphicsLayer { rotationZ = apacheChevronRotation }
                            )
                        }
                    }
                }
                AnimatedVisibility(
                    visible = apacheAccordionOpen,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Text(
                        text = "Copyright 2026 limonatalp4\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at\n\n    http://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colorScheme.primary,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }
                }
            }
        }
    }
}

@Composable
private fun LanguageSection(
    currentLanguage: String,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    languageAccordionOpen: Boolean,
    onToggleLanguageAccordion: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
    triggerHaptic: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onSetDismissing: (Boolean) -> Unit,
    visibleLanguages: List<Language> = Language.entries
) {
    if (visibleLanguages.isEmpty()) return

    SettingsAccordionCard(
        title = stringResource(R.string.language_accordion_title),
        expanded = languageAccordionOpen,
        onToggle = onToggleLanguageAccordion,
        isTiefschwarz = isTiefschwarz,
        colorScheme = colorScheme,
        glassProps = glassProps,
        triggerHaptic = triggerHaptic,
        cardTestTag = "card_language_accordion",
        toggleButtonTestTag = "btn_toggle_language_accordion",
        chevronLabel = "language_chevron_rotation"
    ) {
        visibleLanguages.forEach { langOption ->
            val isSelected = when (langOption) {
                Language.SYSTEM -> currentLanguage.isBlank() || currentLanguage.equals("SYSTEM", ignoreCase = true)
                else -> currentLanguage.equals(langOption.localeTag, ignoreCase = true)
            }

            SettingsPillOption(
                selected = isSelected,
                onClick = {
                    triggerHaptic()
                    onSelectLanguage(langOption.localeTag)
                    onDismiss()
                },
                title = stringResource(langOption.stringResId),
                glassProps = glassProps,
                testTag = "radio_lang_${langOption.name}"
            )
        }
    }
}

@Composable
private fun SettingsPillOption(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    testTag: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    errorText: String? = null,
    enabled: Boolean = true,
    minHeight: Dp = GlassDialogDefaults.PillOptionExpandedMinHeight,
    descriptionTestTag: String? = null,
    errorTestTag: String? = null
) {
    val hasSecondaryText = description != null || errorText != null

    GlassPillOptionContainer(
        selected = selected,
        onClick = onClick,
        glassProps = glassProps,
        modifier = modifier.testTag(testTag),
        enabled = enabled,
        minHeight = minHeight,
        contentPadding = GlassDialogDefaults.PillOptionPadding,
        animationLabel = "pill_$testTag"
    ) { textColor ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary,
                    unselectedColor = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    },
                    disabledSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    disabledUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                enabled = enabled
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 4.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 14.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                    maxLines = if (hasSecondaryText) 1 else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (descriptionTestTag != null) Modifier.testTag(descriptionTestTag) else Modifier
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = errorText,
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (errorTestTag != null) Modifier.testTag(errorTestTag) else Modifier
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsGlassCard(
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = GlassDialogDefaults.SettingsCardShape,
    contentPadding: androidx.compose.foundation.layout.PaddingValues = GlassDialogDefaults.SettingsCardContentPadding,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    GlassCardContainer(
        glassProps = glassProps,
        modifier = modifier,
        shape = shape,
        contentPadding = contentPadding,
        onClick = onClick,
        enabled = enabled,
        role = role,
        interactionSource = interactionSource,
        content = content
    )
}

@Composable
private fun SettingsTabBar(
    selectedTab: Int,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    onSelectTab: (Int) -> Unit
) {
    val tabs = listOf(
        GlassPillTabItem(Icons.Default.Palette, stringResource(R.string.title_appearance), "tab_settings_0"),
        GlassPillTabItem(Icons.Default.Straighten, stringResource(R.string.title_units), "tab_settings_1"),
        GlassPillTabItem(Icons.Default.DeveloperMode, stringResource(R.string.title_dev_options), "tab_settings_2"),
        GlassPillTabItem(Icons.Default.Info, stringResource(R.string.title_info), "tab_settings_3"),
        GlassPillTabItem(Icons.Default.Language, stringResource(R.string.tab_language), "tab_settings_4")
    )

    GlassAutoCenterPillTabBar(
        items = tabs,
        selectedIndex = selectedTab,
        onSelectIndex = onSelectTab,
        isTiefschwarz = isTiefschwarz,
        glassProps = LocalGlassProperties.current,
        fillMaxWidth = false,
        minTabHeight = 36.dp,
        tabPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp, horizontal = 10.dp),
        labelFontSize = 12.sp,
        activeIndicatorColor = MaterialTheme.colorScheme.primaryContainer,
        activeContentColor = MaterialTheme.colorScheme.primary,
        inactiveContentColor = MaterialTheme.colorScheme.primary,
        animationLabelPrefix = "settings_tab"
    )
}
