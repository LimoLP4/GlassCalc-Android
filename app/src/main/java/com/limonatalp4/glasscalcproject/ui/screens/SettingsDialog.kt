package com.limonatalp4.glasscalcproject.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.core.view.WindowCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.window.core.layout.WindowSizeClass
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.Language
import com.limonatalp4.glasscalcproject.BuildConfig
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.GlassCardContainer
import com.limonatalp4.glasscalcproject.ui.components.GlassDialogDefaults
import com.limonatalp4.glasscalcproject.ui.components.GlassPillOptionContainer
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
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticMotorType
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentTheme: AppTheme,
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

    // Accordion expansion states for Info tab
    var apacheAccordionOpen by rememberSaveable { mutableStateOf(false) }
    var mxparserAccordionOpen by rememberSaveable { mutableStateOf(false) }

    val effectiveApi = if (simulatedApiEnabled) simulatedApiLevel else Build.VERSION.SDK_INT
    val isTiefschwarz = glassProps.isTiefschwarz
    val searchVisibility = rememberSettingsSearchVisibility(
        query = searchQuery,
        effectiveApiLevel = effectiveApi
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
                                color = if (isTiefschwarz) Color.White else colorScheme.onSurface
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
                                        tint = if (isTiefschwarz) Color.White else colorScheme.onSurface,
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
                                    effectiveApiLevel = effectiveApiLevel,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    onSelectTheme = onSelectTheme,
                                    triggerHaptic = ::triggerHaptic,
                                    visibleThemes = searchVisibility.matchingThemes,
                                    showHeading = !isSearchActive
                                )
                                1 -> UnitsSection(
                                    currentAngleMode = currentAngleMode,
                                    isTiefschwarz = isTiefschwarz,
                                    colorScheme = colorScheme,
                                    glassProps = glassProps,
                                    onSelectAngleMode = onSelectAngleMode,
                                    triggerHaptic = ::triggerHaptic,
                                    orderedCards = searchVisibility.orderedUnitCards,
                                    showHeading = !isSearchActive
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
                                    onSelectLanguage = onSelectLanguage,
                                    onDismiss = dialogTransition.dismiss,
                                    triggerHaptic = ::triggerHaptic,
                                    coroutineScope = coroutineScope,
                                    onSetDismissing = { if (it) dialogTransition.dismiss() },
                                    visibleLanguages = searchVisibility.matchingLanguages,
                                    showHeading = !isSearchActive
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
            title = { Text(stringResource(R.string.dialog_reset_title)) },
            text = { Text(stringResource(R.string.dialog_reset_text)) },
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
                    Text(stringResource(R.string.dialog_reset_cancel))
                }
            }
        )
    }
}


@Composable
private fun AppearanceSection(
    currentTheme: AppTheme,
    effectiveApiLevel: Int,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    onSelectTheme: (AppTheme) -> Unit,
    triggerHaptic: () -> Unit,
    visibleThemes: List<AppTheme> = AppTheme.entries,
    showHeading: Boolean = true
) {
    if (showHeading) {
        Text(
            text = androidx.compose.ui.res.stringResource(id = R.string.theme_select_heading),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isTiefschwarz) Color.White.copy(alpha = 0.7f) else colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
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

@Composable
private fun UnitsSection(
    currentAngleMode: AngleMode,
    isTiefschwarz: Boolean,
    colorScheme: androidx.compose.material3.ColorScheme,
    glassProps: com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties,
    onSelectAngleMode: (AngleMode) -> Unit,
    triggerHaptic: () -> Unit,
    orderedCards: List<SettingsUnitCard> = SettingsUnitCard.entries,
    showHeading: Boolean = true
) {
    if (showHeading) {
        Text(
            text = stringResource(id = R.string.units_angle_heading),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        orderedCards.forEach { card ->
            when (card) {
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
                SettingsUnitCard.SYSTEMS -> {
                    SettingsGlassCard(glassProps = glassProps) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = stringResource(id = R.string.units_supported_systems),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(id = R.string.units_systems_detail),
                                fontSize = 13.sp,
                                color = colorScheme.onSurface.copy(alpha = 0.75f)
                            )
                        }
                    }
                }
            }
        }
    }
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

    val orderedDevCards = searchVisibility?.orderedDevCards ?: SettingsDevCard.entries

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        orderedDevCards.forEach { devCard ->
            when (devCard) {
                SettingsDevCard.SCREEN -> {
        // Card 1: Screen & WindowSizeClass
        SettingsGlassCard(glassProps = glassProps) {
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
                    color = if (isTiefschwarz) Color.White else colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.dev_resolution, screenWidthDp, screenHeightDp),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
                }
                SettingsDevCard.API -> {
        // Card 2: Simulated API Level
        SettingsGlassCard(glassProps = glassProps) {
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
                        color = if (isTiefschwarz) Color.White else colorScheme.onSurface
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
                    color = colorScheme.onSurfaceVariant
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
                        val textColor = if (isAvailable) colorScheme.onPrimaryContainer else colorScheme.onSurface.copy(alpha = 0.35f)
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
                                    tint = if (isAvailable) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.35f),
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
        val hasVibratorMotor = remember(context) { HapticUtil.hasVibrator(context) }
        val hapticMotorType = remember(context) { HapticUtil.getHapticMotorType(context) }
        val isSupportedBySimulatedApi = effectiveApi >= 26
        val isHapticUsable = hasVibratorMotor && isSupportedBySimulatedApi
        val isIntensitySliderEnabled = hapticMotorType != HapticMotorType.NONE && isSupportedBySimulatedApi && hapticFeedbackEnabled
        var sliderIntensity by remember(hapticIntensity) {
            mutableFloatStateOf(hapticIntensity.toFloat().coerceIn(1f, 10f))
        }

        SettingsGlassCard(glassProps = glassProps) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .alpha(if (isHapticUsable) 1.0f else 0.5f)
                    ) {
                        Text(
                            text = stringResource(R.string.dev_haptic_title),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.dev_haptic_desc),
                            fontSize = 12.sp,
                            color = colorScheme.onSurfaceVariant
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
                    color = colorScheme.primary.copy(alpha = 0.8f)
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isIntensitySliderEnabled) 1.0f else 0.45f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dev_haptic_intensity_label),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.dev_haptic_intensity_level, currentLevel),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isIntensitySliderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Text(text = stringResource(R.string.dev_auto_result_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    Text(text = stringResource(R.string.dev_auto_result_desc), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                }
                Switch(checked = autoResultEnabled, onCheckedChange = { triggerHaptic(); onToggleAutoResult(it) }, modifier = Modifier.testTag("switch_auto_result"))
            }
        }
                }
                SettingsDevCard.LIFECYCLE -> {
        // Card 5: Lifecycle & State
        SettingsGlassCard(glassProps = glassProps) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.dev_lifecycle_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                Text(text = "Status: $lifecycleStatus", fontSize = 13.sp, fontFamily = FontFamily.Monospace, color = colorScheme.primary)
                Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { triggerHaptic(); onSimulateBackground() },
                        shape = RoundedCornerShape(percent = 50),
                        modifier = Modifier.weight(1f).testTag("btn_simulate_background")
                    ) {
                        Text(stringResource(R.string.dev_btn_background), fontSize = 11.sp, maxLines = 1)
                    }
                    Button(
                        onClick = { triggerHaptic(); onTestCrashProtection() },
                        shape = RoundedCornerShape(percent = 50),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                        modifier = Modifier.weight(1f).testTag("btn_test_crash_protection")
                    ) {
                        Text(stringResource(R.string.dev_btn_crash_test), fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
                }
                SettingsDevCard.CLEAR_CACHE -> {
        // Card 6: Cache leeren (Eigene Karte)
        SettingsGlassCard(glassProps = glassProps) {
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
                    color = colorScheme.onSurfaceVariant
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
                    modifier = Modifier.fillMaxWidth().testTag("btn_clear_cache"),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text(stringResource(R.string.dev_btn_clear_cache), fontWeight = FontWeight.Bold)
                }
            }
        }
                }
                SettingsDevCard.RESET_STORAGE -> {
        // Card 7: App-Speicher zurücksetzen
        SettingsGlassCard(glassProps = glassProps) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.dev_reset_storage_title), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.error)
                Text(text = stringResource(R.string.dev_reset_storage_desc), fontSize = 12.sp, color = colorScheme.onSurfaceVariant)
                Button(
                    onClick = { triggerHaptic(); onResetAppRequest() },
                    shape = RoundedCornerShape(percent = 50),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp).testTag("btn_reset_storage")
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
        // App Version Card
        SettingsGlassCard(glassProps = glassProps) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "GlassCalc v${BuildConfig.VERSION_NAME}", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary, textAlign = TextAlign.Center)
                Text(text = stringResource(R.string.app_version_subtitle), fontSize = 12.sp, color = colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Text(text = stringResource(R.string.copyright_notice), fontSize = 11.sp, color = colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
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
                        color = colorScheme.onSurfaceVariant
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
                SettingsInfoCard.OSS -> {
        // Third-Party OSS Licenses Button
        SettingsGlassCard(glassProps = glassProps) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(R.string.info_oss_licenses), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colorScheme.primary)
                Text(text = stringResource(R.string.info_oss_licenses_desc), fontSize = 12.sp, color = colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                Button(
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
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp).testTag("btn_oss_licenses")
                ) {
                    Text(stringResource(R.string.info_btn_oss_licenses), fontWeight = FontWeight.Bold)
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
                                tint = if (isTiefschwarz) Color.White else colorScheme.onSurface,
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
                        color = colorScheme.onSurfaceVariant,
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
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
    triggerHaptic: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onSetDismissing: (Boolean) -> Unit,
    visibleLanguages: List<Language> = Language.entries,
    showHeading: Boolean = true
) {
    if (showHeading) {
        Text(
            text = stringResource(R.string.language_select_heading),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isTiefschwarz) Color.White.copy(alpha = 0.7f) else colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
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
                testTag = "radio_lang_${langOption.name}",
                minHeight = GlassDialogDefaults.PillOptionMinHeight
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
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    lineHeight = if (hasSecondaryText) 18.sp else 17.sp,
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
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
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
        Triple(0, Icons.Default.Palette, stringResource(R.string.title_appearance)),
        Triple(1, Icons.Default.Straighten, stringResource(R.string.title_units)),
        Triple(2, Icons.Default.DeveloperMode, stringResource(R.string.title_dev_options)),
        Triple(3, Icons.Default.Info, stringResource(R.string.title_info)),
        Triple(4, Icons.Default.Language, stringResource(R.string.tab_language))
    )

    val tabScrollState = rememberScrollState()
    val density = LocalDensity.current
    val tabWidths = remember { mutableStateMapOf<Int, androidx.compose.ui.unit.Dp>() }
    var tabHeight by remember { mutableStateOf(0.dp) }

    val spacing = 4.dp
    val targetPosition = remember(selectedTab, tabWidths.toMap()) {
        var pos = 0.dp
        for (i in 0 until selectedTab) {
            pos += (tabWidths[i] ?: 0.dp) + spacing
        }
        pos
    }
    val targetWidth = tabWidths[selectedTab] ?: 0.dp

    val indicatorOffset by animateDpAsState(
        targetValue = targetPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_indicator_offset"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_indicator_width"
    )
    val activePillBgColor by animateColorAsState(
        targetValue = colorScheme.primaryContainer,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "tab_active_pill_bg"
    )

    LaunchedEffect(selectedTab, targetPosition) {
        if (targetWidth > 0.dp) {
            val targetPx = with(density) { targetPosition.roundToPx() }
            val offsetPaddingPx = with(density) { 28.dp.roundToPx() }
            tabScrollState.animateScrollTo((targetPx - offsetPaddingPx).coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .clip(GlassDialogDefaults.PillShape)
            .background(
                GlassDialogDefaults.pillBarBackgroundColor(isTiefschwarz)
            )
            .border(
                border = LocalGlassProperties.current.containerBorderStroke,
                shape = GlassDialogDefaults.PillShape
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .horizontalScroll(tabScrollState)
        ) {
            if (targetWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(indicatorWidth)
                        .height(if (tabHeight > 0.dp) tabHeight else 36.dp)
                        .clip(GlassDialogDefaults.PillShape)
                        .background(activePillBgColor)
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (index, icon, label) ->
                    val isSelected = selectedTab == index
                    var isPressed by remember { mutableStateOf(false) }
                    val scale by animateFloatAsState(
                        targetValue = if (isPressed) 0.95f else 1.0f,
                        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
                        label = "tab_scale"
                    )
                    val tabTint by animateColorAsState(
                        targetValue = if (isSelected) {
                            if (isTiefschwarz) Color.White else colorScheme.onPrimaryContainer
                        } else {
                            if (isTiefschwarz) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant
                        },
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "tab_${index}_tint"
                    )

                            val currentOnSelectTab by rememberUpdatedState(onSelectTab)
                            Box(
                                modifier = Modifier
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
                                    .clip(GlassDialogDefaults.PillShape)
                                    .pointerInput(index) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                try { awaitRelease() } finally { isPressed = false }
                                            },
                                            onTap = {
                                                currentOnSelectTab(index)
                                            }
                                        )
                                    }
                            .padding(vertical = 8.dp, horizontal = 10.dp)
                            .testTag("tab_settings_$index"),
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
                                text = label,
                                fontSize = 12.sp,
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
}
