@file:Suppress("DEPRECATION")

package com.limonatalp4.glasscalcproject.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.runtime.snapshotFlow
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
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
import androidx.core.graphics.drawable.toBitmap
import androidx.window.core.layout.WindowWidthSizeClass
import com.limonatalp4.glasscalcproject.ui.components.ButtonType
import com.limonatalp4.glasscalcproject.ui.components.DrawerFormulaSearch
import com.limonatalp4.glasscalcproject.ui.components.GlassCalculatorButton
import com.limonatalp4.glasscalcproject.ui.components.GlassDisplay
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.rememberAdaptiveGlassState
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import io.github.nadeemiqbal.liquidglass.liquidGlassSource

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
    val appIconBitmap = remember(context) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(128, 128).asImageBitmap()
        }.getOrNull()
    }
    val coroutineScope = rememberCoroutineScope()
    val menuVisibleState = remember { androidx.compose.animation.core.MutableTransitionState(false) }

    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val history by viewModel.history.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val simulatedApiEnabled by viewModel.simulatedApiEnabled.collectAsState()
    val simulatedApiLevel by viewModel.simulatedApiLevel.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsState()
    val hapticIntensity by viewModel.hapticIntensity.collectAsState()
    val autoResultEnabled by viewModel.isAutoResultEnabled.collectAsState()
    val lifecycleStatus by viewModel.lifecycleStatus.collectAsState()

    androidx.compose.runtime.LaunchedEffect(hapticIntensity) {
        HapticUtil.currentIntensityLevel = hapticIntensity
    }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showConverterDialog by remember { mutableStateOf(false) }
    var showConstantsDialog by remember { mutableStateOf(false) }
    var showGraphDialog by remember { mutableStateOf(false) }
    var formulaSearchQuery by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(menuVisibleState.targetState) {
        if (!menuVisibleState.targetState) {
            formulaSearchQuery = ""
        }
    }

    val glassState = rememberAdaptiveGlassState(forceFallback = !glassProps.supportsBlur)

    CompositionLocalProvider(
        LocalHapticFeedbackEnabled provides hapticFeedbackEnabled,
        LocalHapticIntensity provides hapticIntensity
    ) {
        Box(
            modifier = modifier.fillMaxSize()
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Floating Pill (Menu + App Name)
                        GlassSurface(
                            modifier = Modifier.testTag("header_pill"),
                            shape = RoundedCornerShape(50),
                            customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            borderWidth = 1.2.dp,
                            glassState = glassState
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                val view = LocalView.current
                                IconButton(
                                    onClick = {
                                        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                                        menuVisibleState.targetState = !menuVisibleState.targetState
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
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
                        GlassSurface(
                            modifier = Modifier.testTag("history_pill"),
                            shape = RoundedCornerShape(50),
                            customBgColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            borderWidth = 1.2.dp,
                            glassState = glassState
                        ) {
                            val view = LocalView.current
                            IconButton(
                                onClick = {
                                    HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                                    showHistorySheet = true
                                },
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
                        .navigationBarsPadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    if (isDualPane) {
                        LandscapeCalculatorLayout(
                            viewModel = viewModel,
                            angleMode = angleMode,
                            glassState = glassState,
                            onOpenConstants = { showConstantsDialog = true }
                        )
                    } else {
                        PortraitCalculatorLayout(
                            viewModel = viewModel,
                            angleMode = angleMode,
                            isScientificExpanded = isScientificExpanded,
                            glassState = glassState,
                            onOpenConstants = { showConstantsDialog = true }
                        )
                    }
                }
            }
            
            // In-window Floating Navigation Menu (avoids Android PopupWindow BLASTBufferQueue dtor leaks)
            if (menuVisibleState.targetState || menuVisibleState.currentState) {
                BackHandler(enabled = menuVisibleState.targetState) {
                    menuVisibleState.targetState = false
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
                                menuVisibleState.targetState = false
                            }
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .imePadding()
                            .padding(start = 16.dp, top = 66.dp, bottom = 16.dp)
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
                        val isAmoled = currentTheme == com.limonatalp4.glasscalcproject.data.model.AppTheme.AMOLED_DARK
                        val drawerBgColor = colorScheme.surfaceContainerHigh
                        val primaryTint = colorScheme.primary
                        val onSurfaceColor = colorScheme.onSurface
                        val view = LocalView.current
                        
                        val drawerBorder = BorderStroke(
                            width = 1.2.dp,
                            color = when {
                                isAmoled || glassProps.isAmoled -> Color.White.copy(alpha = 0.28f)
                                glassProps.isDark -> colorScheme.outline.copy(alpha = 0.45f)
                                else -> colorScheme.onSurface.copy(alpha = 0.25f)
                            }
                        )

                        GlassSurface(
                            modifier = Modifier
                                .width(300.dp)
                                .testTag("floating_nav_menu"),
                            shape = RoundedCornerShape(32.dp),
                            borderStroke = drawerBorder,
                            customBgColor = if (isAmoled) Color.Black else drawerBgColor.copy(alpha = 0.9f),
                            glassState = glassState
                        ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
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
                                    color = if (isAmoled) Color.White else onSurfaceColor
                                )
                                Text(
                                    text = stringResource(R.string.app_version_tag, BuildConfig.VERSION_NAME),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryTint,
                                    maxLines = 1
                                )
                            }
                            HorizontalDivider(color = (if (isAmoled) Color.White else onSurfaceColor).copy(alpha = 0.08f))
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
                                    HapticUtil.performAppHaptic(
                                        context = context,
                                        view = view,
                                        enabled = hapticFeedbackEnabled,
                                        intensityLevel = hapticIntensity
                                    )
                                    viewModel.insertFormula(formula.insertExpression)
                                    formulaSearchQuery = ""
                                    menuVisibleState.targetState = false
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            val menuItems = listOf(
                                Triple(Icons.Default.Calculate, R.string.nav_calculator, "nav_item_calculator"),
                                Triple(Icons.Default.History, R.string.nav_history, "nav_item_history"),
                                Triple(Icons.Default.Straighten, R.string.nav_converter, "nav_item_converter"),
                                Triple(Icons.AutoMirrored.Filled.ShowChart, R.string.nav_graph, "nav_item_graph"),
                                Triple(Icons.Default.Settings, R.string.nav_settings, "nav_item_settings")
                            )

                            // Outer Segmented Control Container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(if (isAmoled) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .border(
                                        width = 1.dp,
                                        color = if (isAmoled) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(32.dp)
                                    )
                                    .padding(horizontal = 2.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                menuItems.forEach { (icon, labelRes, tag) ->
                                    val isSelected = tag == "nav_item_calculator"
                                    
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 48.dp)
                                            .clip(RoundedCornerShape(percent = 50))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .clickable {
                                                HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
                                                coroutineScope.launch {
                                                    menuVisibleState.targetState = false
                                                    snapshotFlow { menuVisibleState.isIdle && !menuVisibleState.currentState }
                                                        .first { it }
                                                    when (tag) {
                                                        "nav_item_history" -> showHistorySheet = true
                                                        "nav_item_converter" -> showConverterDialog = true
                                                        "nav_item_graph" -> showGraphDialog = true
                                                        "nav_item_settings" -> showSettingsDialog = true
                                                    }
                                                }
                                            }
                                            .testTag(tag),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            val iconTextTint = if (isAmoled) {
                                                Color.White
                                            } else {
                                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else onSurfaceColor.copy(alpha = 0.7f)
                                            }
                                            
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = iconTextTint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = stringResource(labelRes),
                                                fontSize = 15.sp,
                                                lineHeight = 19.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isAmoled) Color.White else (if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else onSurfaceColor)
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


        // Dialogs & Sheets
        if (showHistorySheet) {
            HistorySheet(
                history = history,
                currentTheme = currentTheme,
                onSelect = { item -> viewModel.restoreHistoryItem(item) },
                onClear = { viewModel.clearHistory() },
                onDismiss = { showHistorySheet = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentTheme = currentTheme,
                currentLanguage = viewModel.language.collectAsState().value,
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
                onDismiss = { showSettingsDialog = false },
                autoResultEnabled = autoResultEnabled
            )
        }

        if (showConverterDialog) {
            UnitConverterDialog(
                onImportToCalculator = { value ->
                    viewModel.importConvertedValue(value)
                },
                onDismiss = { showConverterDialog = false }
            )
        }

        if (showConstantsDialog) {
            ConstantsDialog(
                onSelectConstant = { constantValue ->
                    viewModel.onConstant(constantValue)
                },
                onDismiss = { showConstantsDialog = false }
            )
        }

        if (showGraphDialog) {
            GraphDialog(
                viewModel = viewModel,
                angleMode = angleMode,
                onDismiss = { showGraphDialog = false }
            )
        }
    }
}

@Composable
private fun PortraitCalculatorLayout(
    viewModel: CalculatorViewModel,
    angleMode: com.limonatalp4.glasscalcproject.data.model.AngleMode,
    isScientificExpanded: Boolean,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    onOpenConstants: () -> Unit = {}
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val localContext = LocalContext.current
    val localView = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val expression by viewModel.expression.collectAsState()
    var scientificSubTab by remember { mutableIntStateOf(0) }
    val isGestureLocked by viewModel.isGraphLocked.collectAsState()
    val scrollState = rememberScrollState()

    val scrollbarColor = colorScheme.primary.copy(alpha = 0.45f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 440.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fixed Top Section: Display, Mode bar & Sub-Tab Switcher
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassDisplay(
                expressionFlow = viewModel.expression,
                resultFlow = viewModel.result,
                cursorPositionFlow = viewModel.cursorPosition,
                errorMessageFlow = viewModel.errorMessage,
                angleMode = angleMode,
                isScientificExpanded = isScientificExpanded,
                onCursorPositionChanged = { viewModel.setCursorPosition(it) },
                isCompact = false,
                glassState = glassState
            )

            // Bar with DEG/RAD Toggle & Scientific Switch
            val angleButtonBorder = BorderStroke(
                width = 1.dp,
                color = if (glassProps.isDark) {
                    glassProps.buttonBorderColor
                } else {
                    colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )
            val scientificButtonBorder = BorderStroke(
                width = 1.dp,
                color = when {
                    isScientificExpanded -> colorScheme.tertiary
                    glassProps.isDark -> glassProps.buttonBorderColor
                    else -> colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // DEG/RAD Toggle
                GlassSurface(
                    modifier = Modifier.testTag("btn_toggle_angle_mode"),
                    shape = CircleShape,
                    borderStroke = angleButtonBorder
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                            viewModel.toggleAngleMode()
                        },
                        modifier = Modifier.size(width = 76.dp, height = 36.dp)
                    ) {
                        Text(
                            text = if (angleMode == com.limonatalp4.glasscalcproject.data.model.AngleMode.DEG) "DEG" else "RAD",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                    }
                }

                // Scientific Expand / Collapse Button
                GlassSurface(
                    modifier = Modifier.testTag("btn_toggle_scientific"),
                    shape = CircleShape,
                    borderStroke = scientificButtonBorder
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                            viewModel.toggleScientificPanel()
                        },
                        modifier = Modifier
                            .wrapContentWidth()
                            .widthIn(min = 108.dp)
                            .height(36.dp)
                            .padding(horizontal = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 2.dp)
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

            // No sub-tab switcher
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scrollable Lower Container: Keys & Graph Content ONLY
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val availableWidth = maxWidth
            val mainButtonSize = ((availableWidth - 30.dp) / 4f).coerceIn(48.dp, 72.dp)
            val sciButtonSize = ((availableWidth - 24.dp) / 5f).coerceIn(36.dp, 60.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        if (isScientificExpanded && scientificSubTab == 0 && scrollState.maxValue > 0) {
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
                        viewModel = viewModel,
                        glassState = glassState,
                        buttonSize = sciButtonSize,
                        spacing = 5.dp,
                        onOpenConstants = onOpenConstants,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Main 4-column rigid Keypad
                MainKeyboardGrid(
                    viewModel = viewModel,
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
    viewModel: CalculatorViewModel,
    angleMode: com.limonatalp4.glasscalcproject.data.model.AngleMode,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    onOpenConstants: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassProps = LocalGlassProperties.current
    val localContext = LocalContext.current
    val localView = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val leftScrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // In the right half (~50% width), we have:
        // Scientific Keyboard (5 columns) + Main Keyboard (4 columns) side-by-side = 9 columns total.
        // There is spacing between columns in each grid, plus spacing between the two grids (~10.dp).
        val rightHalfWidth = (availableWidth - 14.dp) / 2f
        val interKeypadSpacing = 8.dp

        // Total columns = 9. Total column gaps = 4 (in sci) + 3 (in main) = 7 gaps.
        // We calculate maxButtonWidth so all 9 buttons fit horizontally within the right half:
        val maxButtonWidth = ((rightHalfWidth - interKeypadSpacing - (3.dp * 7)) / 9.2f)

        // For vertical height: 6 rows in scientific grid and 5 rows in main grid.
        // 6 * buttonSize + 5 * spacing <= availableHeight
        val maxButtonHeight = ((availableHeight - 14.dp) / 6.3f)

        // Button size fits both dimensions perfectly
        val buttonSize = minOf(maxButtonWidth, maxButtonHeight).coerceIn(28.dp, 62.dp)
        val spacing = (buttonSize * 0.08f).coerceIn(2.5.dp, 6.dp)

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LINKS (ca. 50% der Breite): Display + DEG/RAD-Button rechtsbündig darunter
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(leftScrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassDisplay(
                        expressionFlow = viewModel.expression,
                        resultFlow = viewModel.result,
                        cursorPositionFlow = viewModel.cursorPosition,
                        errorMessageFlow = viewModel.errorMessage,
                        angleMode = angleMode,
                        isScientificExpanded = true,
                        onCursorPositionChanged = { viewModel.setCursorPosition(it) },
                        isCompact = false,
                        glassState = glassState,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // DEG/RAD Button rechtsbündig unter dem Display
                    val landscapeAngleBorder = BorderStroke(
                        width = 1.dp,
                        color = if (glassProps.isDark) {
                            glassProps.buttonBorderColor
                        } else {
                            colorScheme.onSurface.copy(alpha = 0.3f)
                        }
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        GlassSurface(
                            shape = CircleShape,
                            borderStroke = landscapeAngleBorder,
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            IconButton(
                                onClick = {
                                    HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled, hapticIntensity)
                                    viewModel.toggleAngleMode()
                                },
                                modifier = Modifier.size(
                                    width = (buttonSize * 1.8f).coerceIn(68.dp, 96.dp),
                                    height = (buttonSize * 0.75f).coerceIn(28.dp, 38.dp)
                                )
                            ) {
                                Text(
                                    text = if (angleMode == com.limonatalp4.glasscalcproject.data.model.AngleMode.DEG) "DEG" else "RAD",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // RECHTS (ca. 50% der Breite): Wissenschaftliche Tasten & Ziffernblock NEBENEINANDER (in einer inneren Row)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rightScrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wissenschaftliche Tasten (sin, cos, etc.) in einer Column links
                    ScientificKeyboardGrid(
                        viewModel = viewModel,
                        glassState = glassState,
                        buttonSize = buttonSize,
                        spacing = spacing,
                        onOpenConstants = onOpenConstants,
                        modifier = Modifier.width(buttonSize * 5 + spacing * 4)
                    )

                    Spacer(modifier = Modifier.width(interKeypadSpacing))

                    // Der Ziffernblock (Zahlen, AC, DEL, =) in einer Column rechts daneben
                    MainKeyboardGrid(
                        viewModel = viewModel,
                        glassState = glassState,
                        buttonSize = buttonSize,
                        spacing = spacing,
                        modifier = Modifier.width(buttonSize * 4 + spacing * 3)
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
    viewModel: CalculatorViewModel,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    buttonSize: Dp? = null,
    spacing: Dp = 6.dp,
    onOpenConstants: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    fun RowScope.buttonModifier(): Modifier =
        if (buttonSize != null) Modifier.size(buttonSize) else Modifier.weight(1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = if (buttonSize != null) Alignment.CenterHorizontally else Alignment.Start
    ) {
        // Row 1: sin, cos, tan, π, e
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "sin", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("sin")
            }
            GlassCalculatorButton(text = "cos", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("cos")
            }
            GlassCalculatorButton(text = "tan", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("tan")
            }
            GlassCalculatorButton(text = "π", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onConstant("π")
            }
            GlassCalculatorButton(
                text = "e",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_euler",
                contentDesc = stringResource(R.string.btn_euler_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onConstant("e")
            }
        }

        // Row 2: ln, log, x², x³, xʸ
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "ln", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("ln")
            }
            GlassCalculatorButton(text = "log", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("log")
            }
            GlassCalculatorButton(text = "x²", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onPower("2")
            }
            GlassCalculatorButton(
                text = "x³",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_power_cube",
                modifier = buttonModifier()
            ) {
                viewModel.onPower("3")
            }
            GlassCalculatorButton(
                text = "xʸ",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_power_xy",
                contentDesc = stringResource(R.string.btn_power_xy_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onPower("y")
            }
        }

        // Row 3: √, ³√, ⁿ√, n!, /
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "√", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("√")
            }
            GlassCalculatorButton(text = "³√", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("³√")
            }
            GlassCalculatorButton(
                text = "ⁿ√",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_nth_root",
                contentDesc = stringResource(R.string.btn_nth_root_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onNthRoot()
            }
            GlassCalculatorButton(text = "n!", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("!")
            }
            GlassCalculatorButton(
                text = "/",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_fraction_slash",
                contentDesc = stringResource(R.string.btn_fraction_slash_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onFractionSlash()
            }
        }

        // Row 4: asin, acos, atan, (, )
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "asin", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("arcsin")
            }
            GlassCalculatorButton(text = "acos", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("arccos")
            }
            GlassCalculatorButton(text = "atan", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("arctan")
            }
            GlassCalculatorButton(text = "(", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("(")
            }
            GlassCalculatorButton(text = ")", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput(")")
            }
        }

        // Row 5: sinh, cosh, tanh, |x|, mod
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "sinh", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("sinh")
            }
            GlassCalculatorButton(text = "cosh", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("cosh")
            }
            GlassCalculatorButton(text = "tanh", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("tanh")
            }
            GlassCalculatorButton(text = "|x|", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("abs")
            }
            GlassCalculatorButton(text = "mod", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput(" mod ")
            }
        }

        // Row 6: d/dx, ∫, x, ;, Konst.
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "d/dx", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("d/dx")
            }
            GlassCalculatorButton(
                text = "∫",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_integral",
                contentDesc = stringResource(R.string.btn_integral_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onScientificFunction("∫")
            }
            GlassCalculatorButton(
                text = "x",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_var_x",
                contentDesc = stringResource(R.string.btn_var_x_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("x")
            }
            GlassCalculatorButton(
                text = ";",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_semicolon",
                contentDesc = stringResource(R.string.btn_semicolon_desc),
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("; ")
            }
            GlassCalculatorButton(
                text = stringResource(R.string.btn_constants),
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_constants",
                contentDesc = stringResource(R.string.btn_constants_desc),
                modifier = buttonModifier()
            ) {
                onOpenConstants()
            }
        }
    }
}

/**
 * Rigid 4-column x 5-row Keypad.
 */
@Composable
private fun MainKeyboardGrid(
    viewModel: CalculatorViewModel,
    glassState: io.github.nadeemiqbal.liquidglass.LiquidGlassState? = null,
    buttonSize: Dp? = null,
    spacing: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    fun RowScope.buttonModifier(): Modifier = if (buttonSize != null) Modifier.size(buttonSize) else Modifier.weight(1f)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Row 1: AC, DEL (with Long-Press), %, ÷
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(
                text = "AC",
                type = ButtonType.ACTION_CLEAR,
                glassState = glassState,
                testTag = "btn_clear",
                modifier = buttonModifier()
            ) {
                viewModel.onClearAll()
            }
            GlassCalculatorButton(
                text = "DEL",
                type = ButtonType.ACTION_CLEAR,
                glassState = glassState,
                testTag = "btn_delete",
                contentDesc = stringResource(R.string.btn_del_desc),
                isRepeatable = true,
                modifier = buttonModifier()
            ) {
                viewModel.onDelete()
            }
            GlassCalculatorButton(
                text = "%",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_percent",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("%")
            }
            GlassCalculatorButton(
                text = "÷",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_div",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("÷")
            }
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "7", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("7")
            }
            GlassCalculatorButton(text = "8", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("8")
            }
            GlassCalculatorButton(text = "9", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("9")
            }
            GlassCalculatorButton(
                text = "×",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_mul",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("×")
            }
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "4", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("4")
            }
            GlassCalculatorButton(text = "5", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("5")
            }
            GlassCalculatorButton(text = "6", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("6")
            }
            GlassCalculatorButton(
                text = "−",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_sub",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("−")
            }
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(text = "1", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("1")
            }
            GlassCalculatorButton(text = "2", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("2")
            }
            GlassCalculatorButton(text = "3", glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("3")
            }
            GlassCalculatorButton(
                text = "+",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_add",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("+")
            }
        }

        // Row 5: ±, 0, ,, = (All perfectly equal size!)
        Row(
            modifier = if (buttonSize != null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            GlassCalculatorButton(
                text = "±",
                type = ButtonType.OPERATOR,
                glassState = glassState,
                testTag = "btn_sign",
                modifier = buttonModifier()
            ) {
                viewModel.onToggleSign()
            }
            GlassCalculatorButton(
                text = "0",
                glassState = glassState,
                testTag = "btn_zero",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput("0")
            }
            GlassCalculatorButton(
                text = ",",
                glassState = glassState,
                testTag = "btn_comma",
                modifier = buttonModifier()
            ) {
                viewModel.onCharacterInput(",")
            }
            GlassCalculatorButton(
                text = "=",
                type = ButtonType.EQUALS,
                glassState = glassState,
                testTag = "btn_equals",
                modifier = buttonModifier()
            ) {
                viewModel.onEquals()
            }
        }
    }
}
