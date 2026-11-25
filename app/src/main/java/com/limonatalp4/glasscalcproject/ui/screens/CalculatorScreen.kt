@file:Suppress("DEPRECATION")

package com.limonatalp4.glasscalcproject.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.stringResource
import com.limonatalp4.glasscalcproject.BuildConfig
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.window.core.layout.WindowWidthSizeClass
import com.limonatalp4.glasscalcproject.ui.components.ButtonType
import com.limonatalp4.glasscalcproject.ui.components.DrawerFormulaSearch
import com.limonatalp4.glasscalcproject.ui.components.GlassCalculatorButton
import com.limonatalp4.glasscalcproject.ui.components.GlassDisplay
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberAdaptiveGlassState
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorDisplayState
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import io.github.nadeemiqbal.liquidglass.liquidGlassSource

@Immutable
private data class CalculatorKeypadActions(
    val onCharacterInput: (String) -> Unit,
    val onScientificFunction: (String) -> Unit,
    val onConstant: (String) -> Unit,
    val onPower: (String) -> Unit,
    val onFractionSlash: () -> Unit,
    val onNthRoot: () -> Unit,
    val onToggleSign: () -> Unit,
    val onDelete: () -> Unit,
    val onClearAll: () -> Unit,
    val onEquals: () -> Unit,
    val onToggleAngleMode: () -> Unit,
    val onToggleScientificPanel: () -> Unit,
    val onOpenConstants: () -> Unit,
    val onCursorPositionChanged: (Int) -> Unit,
    val onExpressionChange: (String, Int) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    @Suppress("DEPRECATION")
    val isTablet = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val isDualPane = isTablet || isLandscape

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentAppIcon by viewModel.currentAppIcon.collectAsState()
    val appIconBitmap = remember(context, currentAppIcon) {
        runCatching {
            val drawable = ContextCompat.getDrawable(context, currentAppIcon.iconResId)
                ?: context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(128, 128).asImageBitmap()
        }.getOrNull()
    }
    val coroutineScope = rememberCoroutineScope()
    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    val menuVisibleState = remember { androidx.compose.animation.core.MutableTransitionState(isDrawerOpen) }

    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()

    androidx.compose.runtime.LaunchedEffect(hapticIntensity) {
        HapticUtil.currentIntensityLevel = hapticIntensity
    }

    var isHistoryOpen by rememberSaveable { mutableStateOf(false) }
    var isSettingsOpen by rememberSaveable { mutableStateOf(false) }
    var isUnitConverterOpen by rememberSaveable { mutableStateOf(false) }
    var showConstantsDialog by rememberSaveable { mutableStateOf(false) }
    var isGraphOpen by rememberSaveable { mutableStateOf(false) }
    var formulaSearchQuery by rememberSaveable { mutableStateOf("") }

    val keypadActions = remember(viewModel) {
        CalculatorKeypadActions(
            onCharacterInput = viewModel::onCharacterInput,
            onScientificFunction = viewModel::onScientificFunction,
            onConstant = viewModel::onConstant,
            onPower = viewModel::onPower,
            onFractionSlash = viewModel::onFractionSlash,
            onNthRoot = viewModel::onNthRoot,
            onToggleSign = viewModel::onToggleSign,
            onDelete = viewModel::onDelete,
            onClearAll = viewModel::onClearAll,
            onEquals = viewModel::onEquals,
            onToggleAngleMode = viewModel::toggleAngleMode,
            onToggleScientificPanel = viewModel::toggleScientificPanel,
            onOpenConstants = { showConstantsDialog = true },
            onCursorPositionChanged = viewModel::setCursorPosition,
            onExpressionChange = viewModel::onDirectExpressionChange
        )
    }

    androidx.compose.runtime.LaunchedEffect(isDrawerOpen) {
        menuVisibleState.targetState = isDrawerOpen
        focusManager.clearFocus()
        keyboardController?.hide()
        if (!isDrawerOpen) {
            formulaSearchQuery = ""
        }
    }

    val glassState = rememberAdaptiveGlassState(forceFallback = !glassProps.supportsBlur)

    CompositionLocalProvider(
        LocalHapticFeedbackEnabled provides hapticFeedbackEnabled,
        LocalHapticIntensity provides hapticIntensity
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(glassProps.backgroundBrush)
                    .then(
                        if (glassState.quality != io.github.nadeemiqbal.liquidglass.LiquidGlassQuality.Fallback) {
                            Modifier.liquidGlassSource(glassState)
                        } else {
                            Modifier
                        }
                    )
            )
            // Main Content
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    val density = LocalDensity.current
                    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
                    val topBarVerticalPadding by animateDpAsState(
                        targetValue = if (isLandscape && isImeVisible) 4.dp else 12.dp,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "topbar_vertical_padding"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.statusBars
                                    .union(WindowInsets.navigationBars)
                                    .union(WindowInsets.displayCutout)
                                    .only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                            )
                            .padding(horizontal = 16.dp, vertical = topBarVerticalPadding),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Floating Pill (Menu + App Name)
                        val menuBtnInteraction = remember { MutableInteractionSource() }
                        val view = LocalView.current
                        val toggleDrawer: () -> Unit = {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                            isDrawerOpen = !isDrawerOpen
                        }
                        GlassSurface(
                            modifier = Modifier
                                .pressScale(menuBtnInteraction)
                                .testTag("header_pill"),
                            shape = RoundedCornerShape(50),
                            customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            borderStroke = glassProps.containerBorderStroke,
                            glassState = glassState,
                            onClick = toggleDrawer,
                            role = Role.Button,
                            interactionSource = menuBtnInteraction
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable(
                                            interactionSource = menuBtnInteraction,
                                            indication = null,
                                            onClick = toggleDrawer
                                        )
                                        .testTag("btn_hamburger_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = stringResource(R.string.nav_open_menu),
                                        tint = colorScheme.onSurface,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Text(
                                    text = "GlassCalc",
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.onSurface,
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 6.dp, end = 14.dp)
                                )
                            }
                        }

                        // History Pill (Right)
                        val historyBtnInteraction = remember { MutableInteractionSource() }
                        GlassSurface(
                            modifier = Modifier
                                .pressScale(historyBtnInteraction)
                                .testTag("history_pill"),
                            shape = RoundedCornerShape(50),
                            customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            borderStroke = glassProps.containerBorderStroke,
                            glassState = glassState
                        ) {
                            val view = LocalView.current
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                                    isHistoryOpen = true
                                },
                                interactionSource = historyBtnInteraction,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(4.dp)
                                    .testTag("btn_top_history")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = stringResource(R.string.nav_history_show),
                                    tint = colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .windowInsetsPadding(
                            WindowInsets.navigationBars
                                .union(WindowInsets.ime)
                                .union(WindowInsets.displayCutout)
                                .only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        ),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (isDualPane) {
                        LandscapeCalculatorLayout(
                            displayStateFlow = viewModel.displayState,
                            actions = keypadActions,
                            angleMode = angleMode,
                            glassState = glassState
                        )
                    } else {
                        PortraitCalculatorLayout(
                            displayStateFlow = viewModel.displayState,
                            actions = keypadActions,
                            angleMode = angleMode,
                            isScientificExpanded = isScientificExpanded,
                            glassState = glassState
                        )
                    }
                }
            }
            
            // In-window Floating Navigation Menu (avoids Android PopupWindow BLASTBufferQueue dtor leaks)
            if (isDrawerOpen || menuVisibleState.targetState || menuVisibleState.currentState) {
                BackHandler(enabled = isDrawerOpen) {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isDrawerOpen = false
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .zIndex(10f)
                ) {
                    // Transparent outside-tap scrim to dismiss drawer
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                isDrawerOpen = false
                            }
                    )

                    val density = LocalDensity.current
                    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
                    val drawerTopPadding = if (isLandscape && isImeVisible) 8.dp else 66.dp
                    val drawerBottomPadding = if (isLandscape && isImeVisible) 8.dp else 16.dp
                    val drawerScrollState = rememberScrollState()

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .windowInsetsPadding(
                                WindowInsets.statusBars
                                    .union(WindowInsets.ime)
                                    .union(WindowInsets.navigationBars)
                                    .union(WindowInsets.displayCutout)
                            )
                            .padding(
                                start = 16.dp,
                                end = 16.dp,
                                top = drawerTopPadding,
                                bottom = drawerBottomPadding
                            )
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visibleState = menuVisibleState,
                            enter = androidx.compose.animation.slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                            ) + androidx.compose.animation.fadeIn(
                                animationSpec = tween(durationMillis = 200)
                            ),
                        exit = androidx.compose.animation.slideOutHorizontally(
                            targetOffsetX = { -it },
                            animationSpec = tween(durationMillis = 220, easing = FastOutLinearInEasing)
                        ) + androidx.compose.animation.fadeOut(
                            animationSpec = tween(durationMillis = 200)
                        )
                    ) {
                        val isPureBlack = glassProps.isTiefschwarz
                        val drawerBgColor = colorScheme.surfaceContainerHigh
                        val primaryTint = colorScheme.primary
                        val onSurfaceColor = colorScheme.onSurface
                        val view = LocalView.current
                        
                        val drawerBorder = glassProps.containerBorderStroke

                        GlassSurface(
                            modifier = Modifier
                                .widthIn(max = 300.dp)
                                .fillMaxWidth()
                                .testTag("floating_nav_menu"),
                            shape = RoundedCornerShape(32.dp),
                            borderStroke = drawerBorder,
                            customBgColor = if (isPureBlack) Color.Black else drawerBgColor.copy(alpha = 0.9f),
                            glassState = glassState
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(drawerScrollState)
                        ) {
                            // Header
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                            ) {
                                if (appIconBitmap != null) {
                                    Image(
                                        bitmap = appIconBitmap,
                                        contentDescription = "App-Icon",
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "GlassCalc",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPureBlack) Color.White else onSurfaceColor
                                )
                                Text(
                                    text = stringResource(R.string.app_version_tag, BuildConfig.VERSION_NAME),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryTint,
                                    maxLines = 1
                                )
                            }
                            HorizontalDivider(color = (if (isPureBlack) Color.White else onSurfaceColor).copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(12.dp))

                            DrawerFormulaSearch(
                                query = formulaSearchQuery,
                                onQueryChange = { newValue ->
                                    formulaSearchQuery = newValue
                                },
                                onClearClick = {
                                    HapticUtil.performAppHaptic(
                                        context = context,
                                        view = view,
                                        enabled = hapticFeedbackEnabled,
                                        intensityLevel = hapticIntensity
                                    )
                                    formulaSearchQuery = ""
                                },
                                onFormulaSelected = { formula ->
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.insertFormula(formula.insertExpression)
                                    formulaSearchQuery = ""
                                    isDrawerOpen = false
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val menuItems = remember {
                                listOf(
                                    Triple(Icons.Default.Calculate, R.string.nav_calculator, "nav_item_calculator"),
                                    Triple(Icons.Default.History, R.string.nav_history, "nav_item_history"),
                                    Triple(Icons.Default.Straighten, R.string.nav_converter, "nav_item_converter"),
                                    Triple(Icons.AutoMirrored.Filled.ShowChart, R.string.nav_graph, "nav_item_graph"),
                                    Triple(Icons.Default.Settings, R.string.nav_settings, "nav_item_settings")
                                )
                            }

                            // Outer Segmented Control Container (große schwebende Pille)
                            var selectedNavTag by remember { mutableStateOf("nav_item_calculator") }
                            LaunchedEffect(
                                isDrawerOpen,
                                isHistoryOpen,
                                isUnitConverterOpen,
                                isGraphOpen,
                                isSettingsOpen
                            ) {
                                selectedNavTag = when {
                                    isHistoryOpen -> "nav_item_history"
                                    isUnitConverterOpen -> "nav_item_converter"
                                    isGraphOpen -> "nav_item_graph"
                                    isSettingsOpen -> "nav_item_settings"
                                    else -> "nav_item_calculator"
                                }
                            }

                            val selectedIndex = remember(selectedNavTag, menuItems) {
                                menuItems.indexOfFirst { it.third == selectedNavTag }.coerceAtLeast(0)
                            }
                            val itemHeights = remember { mutableStateMapOf<Int, Dp>() }
                            val itemSpacing = 6.dp
                            val defaultItemHeight = 48.dp
                            val targetOffsetY = remember(selectedIndex, itemHeights.toMap()) {
                                var pos = 0.dp
                                for (i in 0 until selectedIndex) {
                                    pos += (itemHeights[i] ?: defaultItemHeight) + itemSpacing
                                }
                                pos
                            }
                            val targetHeight = itemHeights[selectedIndex] ?: defaultItemHeight

                            val indicatorOffsetY by animateDpAsState(
                                targetValue = targetOffsetY,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "drawer_pill_offset_y"
                            )
                            val indicatorHeight by animateDpAsState(
                                targetValue = targetHeight,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ),
                                label = "drawer_pill_height"
                            )
                            val activePillBgColor by animateColorAsState(
                                targetValue = colorScheme.primaryContainer,
                                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                                label = "drawer_active_pill_bg"
                            )

                            val outerPillShape = RoundedCornerShape(32.dp)
                            val innerPillShape = RoundedCornerShape(percent = 50)
                            val outerPillBg = colorScheme.surfaceVariant.copy(
                                alpha = if (isPureBlack) 0.45f else 0.3f
                            )
                            val outerPillBorder = glassProps.containerBorderStroke

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(outerPillShape)
                                    .background(outerPillBg)
                                    .border(
                                        border = outerPillBorder,
                                        shape = outerPillShape
                                    )
                                    .padding(8.dp)
                            ) {
                                // Gleitende aktive Pille (konzentrisch zum 32.dp-Container)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset { IntOffset(0, indicatorOffsetY.roundToPx()) }
                                        .height(indicatorHeight)
                                        .clip(innerPillShape)
                                        .background(activePillBgColor)
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(itemSpacing)
                                ) {
                                    menuItems.forEachIndexed { index, (icon, labelRes, tag) ->
                                        val isSelected = selectedNavTag == tag
                                        val menuItemInteraction = remember { MutableInteractionSource() }
                                        val iconTint by animateColorAsState(
                                            targetValue = if (isSelected) {
                                                colorScheme.onPrimaryContainer
                                            } else {
                                                if (isPureBlack) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                                            },
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                            label = "nav_icon_tint_$tag"
                                        )
                                        val labelColor by animateColorAsState(
                                            targetValue = if (isSelected) {
                                                colorScheme.onPrimaryContainer
                                            } else {
                                                if (isPureBlack) Color.White.copy(alpha = 0.85f) else onSurfaceColor.copy(alpha = 0.85f)
                                            },
                                            animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                                            label = "nav_label_color_$tag"
                                        )

                                        Surface(
                                            selected = isSelected,
                                            onClick = {
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                                HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                                                selectedNavTag = tag
                                                isDrawerOpen = false
                                                menuVisibleState.targetState = false
                                                coroutineScope.launch {
                                                    snapshotFlow { menuVisibleState.isIdle && !menuVisibleState.currentState }
                                                        .first { it }
                                                    when (tag) {
                                                        "nav_item_history" -> isHistoryOpen = true
                                                        "nav_item_converter" -> isUnitConverterOpen = true
                                                        "nav_item_graph" -> isGraphOpen = true
                                                        "nav_item_settings" -> isSettingsOpen = true
                                                    }
                                                }
                                            },
                                            shape = innerPillShape,
                                            color = Color.Transparent,
                                            contentColor = labelColor,
                                            interactionSource = menuItemInteraction,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = defaultItemHeight)
                                                .onSizeChanged { size ->
                                                    itemHeights[index] = with(density) { size.height.toDp() }
                                                }
                                                .pressScale(menuItemInteraction)
                                                .testTag(tag)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = iconTint,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = stringResource(labelRes),
                                                        fontSize = 15.sp,
                                                        lineHeight = 19.sp,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = labelColor
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
        }


        // Dialogs & Sheets (collect modal-only flows only when open)
        if (isHistoryOpen) {
            val history by viewModel.history.collectAsState()
            val currentTheme by viewModel.currentTheme.collectAsState()
            HistorySheet(
                history = history,
                currentTheme = currentTheme,
                glassState = glassState,
                onSelect = { item -> viewModel.restoreHistoryItem(item) },
                onClear = { viewModel.clearHistory() },
                onDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isHistoryOpen = false
                }
            )
        }

        if (isSettingsOpen) {
            val currentTheme by viewModel.currentTheme.collectAsState()
            val currentAccentColor by viewModel.customAccentColor.collectAsState()
            val accentHexInput by viewModel.accentHexInput.collectAsState()
            val isAccentHexInvalid by viewModel.isAccentHexInvalid.collectAsState()
            val simulatedApiEnabled by viewModel.simulatedApiEnabled.collectAsState()
            val simulatedApiLevel by viewModel.simulatedApiLevel.collectAsState()
            val autoResultEnabled by viewModel.isAutoResultEnabled.collectAsState()
            val lifecycleStatus by viewModel.lifecycleStatus.collectAsState()
            val currentLanguage by viewModel.language.collectAsState()
            SettingsDialog(
                currentTheme = currentTheme,
                currentAccentColor = currentAccentColor,
                accentHexInput = accentHexInput,
                isAccentHexInvalid = isAccentHexInvalid,
                currentAppIcon = currentAppIcon,
                currentLanguage = currentLanguage,
                effectiveApiLevel = viewModel.getEffectiveApiLevel(),
                currentAngleMode = angleMode,
                onSelectLanguage = { tag -> viewModel.setLanguage(tag) },
                windowSizeClass = adaptiveInfo.windowSizeClass,
                simulatedApiEnabled = simulatedApiEnabled,
                simulatedApiLevel = simulatedApiLevel,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                hapticIntensity = hapticIntensity,
                lifecycleStatus = lifecycleStatus,
                onSelectTheme = { theme -> viewModel.setTheme(theme) },
                onSelectAccentColor = { colorArgb, defaultDisplayHexArgb ->
                    viewModel.setCustomAccentColor(colorArgb, defaultDisplayHexArgb)
                },
                onAccentHexChanged = { rawHex -> viewModel.onAccentHexInput(rawHex) },
                onSelectAppIcon = { icon -> viewModel.setAppIcon(icon) },
                onSelectAngleMode = { mode ->
                    if (mode != angleMode) viewModel.toggleAngleMode()
                },
                onSetSimulatedApi = { enabled, level -> viewModel.setSimulatedApi(enabled, level) },
                onToggleHapticFeedback = { enabled -> viewModel.setHapticFeedbackEnabled(enabled) },
                onSetHapticIntensity = { intensity -> viewModel.setHapticIntensity(intensity) },
                onToggleAutoResult = { enabled -> viewModel.setAutoResultEnabled(enabled) },
                onSimulateBackground = { viewModel.simulateBackgroundSave() },
                onTestCrashProtection = { viewModel.testCrashProtection() },
                onResetApp = { viewModel.resetApp() },
                onDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isSettingsOpen = false
                },
                autoResultEnabled = autoResultEnabled
            )
        }

        if (isUnitConverterOpen) {
            UnitConverterDialog(
                onImportToCalculator = { value ->
                    viewModel.importConvertedValue(value)
                },
                onDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isUnitConverterOpen = false
                }
            )
        }

        if (showConstantsDialog) {
            ConstantsDialog(
                onSelectConstant = { constantValue ->
                    viewModel.onConstant(constantValue)
                },
                onDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    showConstantsDialog = false
                }
            )
        }

        if (isGraphOpen) {
            GraphDialog(
                viewModel = viewModel,
                angleMode = angleMode,
                onDismiss = {
                    focusManager.clearFocus()
                    keyboardController?.hide()
                    isGraphOpen = false
                }
            )
        }
    }
}

@Composable
private fun PortraitCalculatorLayout(
    displayStateFlow: StateFlow<CalculatorDisplayState>,
    actions: CalculatorKeypadActions,
    angleMode: AngleMode,
    isScientificExpanded: Boolean,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val localContext = LocalContext.current
    val localView = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val scrollState = rememberScrollState()

    val scrollbarColor = colorScheme.primary.copy(alpha = 0.45f)

    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val windowInfo = LocalWindowInfo.current
    val isCompactPortrait = isImeVisible && with(density) { windowInfo.containerSize.height.toDp() } < 640.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 440.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fixed Top Section: Display & Mode bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassDisplay(
                displayStateFlow = displayStateFlow,
                angleMode = angleMode,
                isScientificExpanded = isScientificExpanded,
                onCursorPositionChanged = actions.onCursorPositionChanged,
                onExpressionChange = actions.onExpressionChange,
                onEvaluate = actions.onEquals,
                isCompact = isCompactPortrait,
                glassState = glassState
            )

            // Bar with DEG/RAD Toggle & Scientific Switch
            val angleButtonBorder = glassProps.containerBorderStroke
            val scientificButtonBorder = if (isScientificExpanded) {
                BorderStroke(width = 1.dp, color = colorScheme.tertiary)
            } else {
                glassProps.containerBorderStroke
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DEG/RAD Toggle
                val angleBtnInteraction = remember { MutableInteractionSource() }
                GlassSurface(
                    modifier = Modifier
                        .size(width = 76.dp, height = 36.dp)
                        .pressScale(angleBtnInteraction)
                        .testTag("btn_toggle_angle_mode"),
                    shape = CircleShape,
                    borderStroke = angleButtonBorder,
                    onClick = {
                        HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                        actions.onToggleAngleMode()
                    },
                    role = Role.Button,
                    interactionSource = angleBtnInteraction
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (angleMode == AngleMode.DEG) "DEG" else "RAD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }

                // Scientific Expand / Collapse Button
                val sciBtnInteraction = remember { MutableInteractionSource() }
                GlassSurface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .widthIn(min = 108.dp)
                        .height(36.dp)
                        .pressScale(sciBtnInteraction)
                        .testTag("btn_toggle_scientific"),
                    shape = CircleShape,
                    borderStroke = scientificButtonBorder,
                    onClick = {
                        HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                        actions.onToggleScientificPanel()
                    },
                    role = Role.Button,
                    interactionSource = sciBtnInteraction
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = if (isScientificExpanded) colorScheme.tertiary else colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isScientificExpanded) stringResource(R.string.mode_standard) else stringResource(R.string.mode_scientific),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isScientificExpanded) colorScheme.tertiary else colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scrollable Lower Container: Keys ONLY
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val availableWidth = maxWidth
            val mainButtonSize = ((availableWidth - 32.dp) / 4f).coerceIn(48.dp, 72.dp)
            val sciButtonSize = ((availableWidth - 25.dp) / 5f).coerceIn(36.dp, 60.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (isScientificExpanded && scrollState.maxValue > 0) {
                            val totalHeight = size.height
                            val scrollFraction = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
                            val indicatorHeight = (totalHeight * 0.22f).coerceAtLeast(32.dp.toPx())
                            val indicatorY = scrollFraction * (totalHeight - indicatorHeight)
                            drawRoundRect(
                                color = scrollbarColor,
                                topLeft = Offset(size.width - 2.dp.toPx(), indicatorY),
                                size = Size(3.dp.toPx(), indicatorHeight),
                                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                            )
                        }
                    }
                    .verticalScroll(scrollState, enabled = true),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isScientificExpanded) {
                    ScientificKeyboardGrid(
                        actions = actions,
                        glassState = glassState,
                        buttonSize = sciButtonSize,
                        spacing = 5.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Main 4-column rigid Keypad
                MainKeyboardGrid(
                    actions = actions,
                    glassState = glassState,
                    buttonSize = mainButtonSize,
                    spacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun LandscapeCalculatorLayout(
    displayStateFlow: StateFlow<CalculatorDisplayState>,
    actions: CalculatorKeypadActions,
    angleMode: AngleMode,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassProps = LocalGlassProperties.current
    val localContext = LocalContext.current
    val localView = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    val animatedOuterVerticalPadding by animateDpAsState(
        targetValue = if (isImeVisible) 2.dp else 6.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "landscape_outer_v_pad"
    )
    val animatedDisplaySpacing by animateDpAsState(
        targetValue = if (isImeVisible) 4.dp else 8.dp,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "landscape_display_spacing"
    )
    var leftContentHeightPx by remember { mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            leftScrollState.animateScrollTo(0)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = animatedOuterVerticalPadding)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Stabile Basis-Höhe für Tasten, damit die Tastenfelder während der IME-Animation nicht springen
        var stableKeypadHeight by remember(availableWidth) { mutableStateOf(availableHeight) }
        if (!isImeVisible && availableHeight > 0.dp) {
            stableKeypadHeight = availableHeight
        }

        val rightHalfWidth = (availableWidth - 14.dp) / 2f
        val interKeypadSpacing = 8.dp

        val maxButtonWidth = ((rightHalfWidth - interKeypadSpacing - (3.6.dp * 9)) / 9.2f)
        val maxButtonHeight = ((stableKeypadHeight - 20.dp) / 6.3f)

        val buttonSize = minOf(maxButtonWidth, maxButtonHeight).coerceIn(28.dp, 62.dp)
        val spacing = (buttonSize * 0.08f).coerceIn(2.5.dp, 6.dp)

        val leftContentHeightDp = with(density) { leftContentHeightPx.toDp() }
        val targetLeftTopPadding = if (isImeVisible || leftContentHeightDp <= 0.dp) {
            0.dp
        } else {
            ((availableHeight - leftContentHeightDp) / 2f).coerceAtLeast(0.dp)
        }
        val animatedLeftTopPadding by animateDpAsState(
            targetValue = targetLeftTopPadding,
            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
            label = "landscape_left_top_padding"
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // LINKS (ca. 50% der Breite): Display + DEG/RAD-Button
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(leftScrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = animatedLeftTopPadding)
                        .onSizeChanged { leftContentHeightPx = it.height }
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                        ),
                    verticalArrangement = Arrangement.spacedBy(animatedDisplaySpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassDisplay(
                        displayStateFlow = displayStateFlow,
                        angleMode = angleMode,
                        isScientificExpanded = true,
                        onCursorPositionChanged = actions.onCursorPositionChanged,
                        onExpressionChange = actions.onExpressionChange,
                        onEvaluate = actions.onEquals,
                        isCompact = isImeVisible || availableHeight < 250.dp,
                        glassState = glassState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    val landscapeAngleBorder = glassProps.containerBorderStroke
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val landscapeAngleInteraction = remember { MutableInteractionSource() }
                        GlassSurface(
                            shape = CircleShape,
                            borderStroke = landscapeAngleBorder,
                            onClick = {
                                HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                                actions.onToggleAngleMode()
                            },
                            role = Role.Button,
                            interactionSource = landscapeAngleInteraction,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(
                                    width = (buttonSize * 1.8f).coerceIn(68.dp, 96.dp),
                                    height = (buttonSize * 0.75f).coerceIn(28.dp, 38.dp)
                                )
                                .pressScale(landscapeAngleInteraction)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (angleMode == AngleMode.DEG) "DEG" else "RAD",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // RECHTS (ca. 50% der Breite): Wissenschaftliche Tasten & Ziffernblock NEBENEINANDER
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rightScrollState),
                verticalArrangement = if (isImeVisible) Arrangement.Top else Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScientificKeyboardGrid(
                        actions = actions,
                        glassState = glassState,
                        buttonSize = buttonSize,
                        spacing = spacing,
                        modifier = Modifier.width((buttonSize + spacing) * 5)
                    )

                    Spacer(modifier = Modifier.width(interKeypadSpacing))

                    MainKeyboardGrid(
                        actions = actions,
                        glassState = glassState,
                        buttonSize = buttonSize,
                        spacing = spacing,
                        modifier = Modifier.width((buttonSize + spacing) * 4)
                    )
                }
            }
        }
    }
}

/**
 * 6-row x 5-column Scientific Grid:
 * Row 1: sin, cos, tan, π, e
 * Row 2: ln, log, x², x³, xʸ
 * Row 3: √, ³√, ⁿ√, n!, /
 * Row 4: asin, acos, atan, (, )
 * Row 5: sinh, cosh, tanh, |x|, mod
 * Row 6: d/dx, ∫, x, ;, Konst.
 */
@Composable
private fun ScientificKeyboardGrid(
    actions: CalculatorKeypadActions,
    modifier: Modifier = Modifier,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    buttonSize: Dp? = null,
    spacing: Dp = 6.dp
) {
    val touchPadding = if (buttonSize != null) spacing / 2 else 0.dp
    val cellSize = if (buttonSize != null) buttonSize + spacing else null
    val gridSpacing = if (buttonSize != null) 0.dp else spacing
    val sizedButtonModifier = remember(cellSize) {
        if (cellSize != null) Modifier.size(cellSize) else null
    }
    fun RowScope.buttonModifier(): Modifier = sizedButtonModifier ?: Modifier.weight(1f)
    val rowModifier = if (cellSize != null) Modifier else Modifier.fillMaxWidth()

    // Stabilized button callbacks
    val onSin = remember(actions) { { actions.onScientificFunction("sin") } }
    val onCos = remember(actions) { { actions.onScientificFunction("cos") } }
    val onTan = remember(actions) { { actions.onScientificFunction("tan") } }
    val onPi = remember(actions) { { actions.onConstant("π") } }
    val onEuler = remember(actions) { { actions.onConstant("e") } }

    val onLn = remember(actions) { { actions.onScientificFunction("ln") } }
    val onLog = remember(actions) { { actions.onScientificFunction("log") } }
    val onPow2 = remember(actions) { { actions.onPower("2") } }
    val onPow3 = remember(actions) { { actions.onPower("3") } }
    val onPowY = remember(actions) { { actions.onPower("y") } }

    val onSqrt = remember(actions) { { actions.onScientificFunction("√") } }
    val onCbrt = remember(actions) { { actions.onScientificFunction("³√") } }
    val onNthRoot = actions.onNthRoot
    val onFact = remember(actions) { { actions.onCharacterInput("!") } }
    val onFrac = actions.onFractionSlash

    val onAsin = remember(actions) { { actions.onScientificFunction("arcsin") } }
    val onAcos = remember(actions) { { actions.onScientificFunction("arccos") } }
    val onAtan = remember(actions) { { actions.onScientificFunction("arctan") } }
    val onOpenParen = remember(actions) { { actions.onCharacterInput("(") } }
    val onCloseParen = remember(actions) { { actions.onCharacterInput(")") } }

    val onSinh = remember(actions) { { actions.onScientificFunction("sinh") } }
    val onCosh = remember(actions) { { actions.onScientificFunction("cosh") } }
    val onTanh = remember(actions) { { actions.onScientificFunction("tanh") } }
    val onAbs = remember(actions) { { actions.onScientificFunction("abs") } }
    val onMod = remember(actions) { { actions.onCharacterInput(" mod ") } }

    val onDeriv = remember(actions) { { actions.onScientificFunction("d/dx") } }
    val onIntegral = remember(actions) { { actions.onScientificFunction("∫") } }
    val onVarX = remember(actions) { { actions.onCharacterInput("x") } }
    val onSemicolon = remember(actions) { { actions.onCharacterInput("; ") } }
    val onOpenConstants = actions.onOpenConstants

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
        horizontalAlignment = if (cellSize != null) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // Row 1: sin, cos, tan, π, e
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "sin", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onSin)
            GlassCalculatorButton(text = "cos", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onCos)
            GlassCalculatorButton(text = "tan", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onTan)
            GlassCalculatorButton(text = "π", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onPi)
            GlassCalculatorButton(
                text = "e",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_euler",
                contentDesc = stringResource(R.string.btn_euler_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onEuler
            )
        }

        // Row 2: ln, log, x², x³, xʸ
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "ln", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onLn)
            GlassCalculatorButton(text = "log", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onLog)
            GlassCalculatorButton(text = "x²", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onPow2)
            GlassCalculatorButton(
                text = "x³",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_power_cube",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onPow3
            )
            GlassCalculatorButton(
                text = "xʸ",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_power_xy",
                contentDesc = stringResource(R.string.btn_power_xy_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onPowY
            )
        }

        // Row 3: √, ³√, ⁿ√, n!, /
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "√", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onSqrt)
            GlassCalculatorButton(text = "³√", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onCbrt)
            GlassCalculatorButton(
                text = "ⁿ√",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_nth_root",
                contentDesc = stringResource(R.string.btn_nth_root_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onNthRoot
            )
            GlassCalculatorButton(text = "n!", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onFact)
            GlassCalculatorButton(
                text = "/",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_fraction_slash",
                contentDesc = stringResource(R.string.btn_fraction_slash_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onFrac
            )
        }

        // Row 4: asin, acos, atan, (, )
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "asin", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onAsin)
            GlassCalculatorButton(text = "acos", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onAcos)
            GlassCalculatorButton(text = "atan", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onAtan)
            GlassCalculatorButton(text = "(", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onOpenParen)
            GlassCalculatorButton(text = ")", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onCloseParen)
        }

        // Row 5: sinh, cosh, tanh, |x|, mod
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "sinh", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onSinh)
            GlassCalculatorButton(text = "cosh", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onCosh)
            GlassCalculatorButton(text = "tanh", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onTanh)
            GlassCalculatorButton(text = "|x|", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onAbs)
            GlassCalculatorButton(text = "mod", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onMod)
        }

        // Row 6: d/dx, ∫, x, ;, Konst.
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "d/dx", type = ButtonType.FUNCTION, glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = onDeriv)
            GlassCalculatorButton(
                text = "∫",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_integral",
                contentDesc = stringResource(R.string.btn_integral_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onIntegral
            )
            GlassCalculatorButton(
                text = "x",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_var_x",
                contentDesc = stringResource(R.string.btn_var_x_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onVarX
            )
            GlassCalculatorButton(
                text = ";",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_semicolon",
                contentDesc = stringResource(R.string.btn_semicolon_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onSemicolon
            )
            GlassCalculatorButton(
                text = stringResource(R.string.btn_constants),
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_constants",
                contentDesc = stringResource(R.string.btn_constants_desc),
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onOpenConstants
            )
        }
    }
}

/**
 * Rigid 4-column x 5-row Keypad.
 */
@Composable
private fun MainKeyboardGrid(
    actions: CalculatorKeypadActions,
    modifier: Modifier = Modifier,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    buttonSize: Dp? = null,
    spacing: Dp = 10.dp
) {
    val touchPadding = if (buttonSize != null) spacing / 2 else 0.dp
    val cellSize = if (buttonSize != null) buttonSize + spacing else null
    val gridSpacing = if (buttonSize != null) 0.dp else spacing
    val sizedButtonModifier = remember(cellSize) {
        if (cellSize != null) Modifier.size(cellSize) else null
    }
    fun RowScope.buttonModifier(): Modifier = sizedButtonModifier ?: Modifier.weight(1f)
    val rowModifier = if (cellSize != null) Modifier else Modifier.fillMaxWidth()

    // Stabilized button callbacks
    val onClearAll = actions.onClearAll
    val onDelete = actions.onDelete
    val onPercent = remember(actions) { { actions.onCharacterInput("%") } }
    val onDiv = remember(actions) { { actions.onCharacterInput("÷") } }
    val on7 = remember(actions) { { actions.onCharacterInput("7") } }
    val on8 = remember(actions) { { actions.onCharacterInput("8") } }
    val on9 = remember(actions) { { actions.onCharacterInput("9") } }
    val onMul = remember(actions) { { actions.onCharacterInput("×") } }
    val on4 = remember(actions) { { actions.onCharacterInput("4") } }
    val on5 = remember(actions) { { actions.onCharacterInput("5") } }
    val on6 = remember(actions) { { actions.onCharacterInput("6") } }
    val onSub = remember(actions) { { actions.onCharacterInput("−") } }
    val on1 = remember(actions) { { actions.onCharacterInput("1") } }
    val on2 = remember(actions) { { actions.onCharacterInput("2") } }
    val on3 = remember(actions) { { actions.onCharacterInput("3") } }
    val onAdd = remember(actions) { { actions.onCharacterInput("+") } }
    val onSign = actions.onToggleSign
    val on0 = remember(actions) { { actions.onCharacterInput("0") } }
    val onComma = remember(actions) { { actions.onCharacterInput(",") } }
    val onEquals = actions.onEquals

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(gridSpacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: AC, DEL (with Long-Press), %, ÷
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(
                text = "AC",
                type = ButtonType.ACTION_CLEAR,
                glassState = glassState,
                testTag = "btn_clear",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onClearAll
            )
            GlassCalculatorButton(
                text = "DEL",
                type = ButtonType.ACTION_CLEAR,
                glassState = glassState,
                testTag = "btn_delete",
                contentDesc = stringResource(R.string.btn_del_desc),
                isRepeatable = true,
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onDelete
            )
            GlassCalculatorButton(
                text = "%",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_percent",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onPercent
            )
            GlassCalculatorButton(
                text = "÷",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_div",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onDiv
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "7", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on7)
            GlassCalculatorButton(text = "8", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on8)
            GlassCalculatorButton(text = "9", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on9)
            GlassCalculatorButton(
                text = "×",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_mul",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onMul
            )
        }

        // Row 3: 4, 5, 6, −
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "4", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on4)
            GlassCalculatorButton(text = "5", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on5)
            GlassCalculatorButton(text = "6", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on6)
            GlassCalculatorButton(
                text = "−",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_sub",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onSub
            )
        }

        // Row 4: 1, 2, 3, +
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(text = "1", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on1)
            GlassCalculatorButton(text = "2", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on2)
            GlassCalculatorButton(text = "3", glassState = glassState, visualPadding = touchPadding, modifier = buttonModifier(), onClick = on3)
            GlassCalculatorButton(
                text = "+",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_add",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onAdd
            )
        }

        // Row 5: ±, 0, ,, = (All perfectly equal size!)
        Row(modifier = rowModifier, horizontalArrangement = Arrangement.spacedBy(gridSpacing)) {
            GlassCalculatorButton(
                text = "±",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_sign",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onSign
            )
            GlassCalculatorButton(
                text = "0",
                glassState = glassState,
                testTag = "btn_zero",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = on0
            )
            GlassCalculatorButton(
                text = ",",
                glassState = glassState,
                testTag = "btn_comma",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onComma
            )
            GlassCalculatorButton(
                text = "=",
                type = ButtonType.EQUALS,
                glassState = glassState,
                testTag = "btn_equals",
                visualPadding = touchPadding,
                modifier = buttonModifier(),
                onClick = onEquals
            )
        }
    }
}
