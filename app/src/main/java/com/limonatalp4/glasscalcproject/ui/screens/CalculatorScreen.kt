package com.limonatalp4.glasscalcproject.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.window.core.layout.WindowWidthSizeClass
import com.limonatalp4.glasscalcproject.ui.components.ButtonType
import com.limonatalp4.glasscalcproject.ui.components.FunctionGraphView
import com.limonatalp4.glasscalcproject.ui.components.GlassCalculatorButton
import com.limonatalp4.glasscalcproject.ui.components.GlassDisplay
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.rememberAdaptiveGlassState
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel
import io.github.nadeemiqbal.liquidglass.liquidGlassSource
import kotlinx.coroutines.launch

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
    val isTablet = adaptiveInfo.windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    val isDualPane = isTablet || isLandscape

    val context = LocalContext.current
    val appIconBitmap = remember(context) {
        runCatching {
            val drawable = context.packageManager.getApplicationIcon(context.packageName)
            drawable.toBitmap(128, 128).asImageBitmap()
        }.getOrNull()
    }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isScientificExpanded by viewModel.isScientificExpanded.collectAsState()
    val angleMode by viewModel.angleMode.collectAsState()
    val history by viewModel.history.collectAsState()
    val currentTheme by viewModel.currentTheme.collectAsState()
    val simulatedApiEnabled by viewModel.simulatedApiEnabled.collectAsState()
    val simulatedApiLevel by viewModel.simulatedApiLevel.collectAsState()
    val hapticFeedbackEnabled by viewModel.hapticFeedbackEnabled.collectAsState()
    val lifecycleStatus by viewModel.lifecycleStatus.collectAsState()

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showConverterDialog by remember { mutableStateOf(false) }
    var showConstantsDialog by remember { mutableStateOf(false) }

    val glassState = rememberAdaptiveGlassState(forceFallback = !glassProps.supportsBlur)

    CompositionLocalProvider(LocalHapticFeedbackEnabled provides hapticFeedbackEnabled) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(glassProps.backgroundBrush)
                .then(
                    if (glassState != null && glassState.quality != io.github.nadeemiqbal.liquidglass.LiquidGlassQuality.Fallback) {
                        Modifier.liquidGlassSource(glassState)
                    } else {
                        Modifier
                    }
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "GlassCalc",
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurface
                            )
                        },
                        navigationIcon = {
                            val view = LocalView.current
                            IconButton(
                                onClick = {
                                    HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
                                    scope.launch { 
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                modifier = Modifier.testTag("btn_hamburger_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü öffnen",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            val view = LocalView.current
                            IconButton(
                                onClick = {
                                    HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
                                    showHistorySheet = true
                                },
                                modifier = Modifier.testTag("btn_top_history")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Verlauf anzeigen",
                                    tint = colorScheme.onSurface
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
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

            // --- Floating Vertical Pill Menu (Custom Floating Drawer) ---
            val isMenuOpen = drawerState.isOpen
            
            // Scrim (Background overlay to close menu)
            AnimatedVisibility(
                visible = isMenuOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable(interactionSource = null, indication = null) {
                            scope.launch { drawerState.close() }
                        }
                )
            }

            // The Floating Menu Panel
            AnimatedVisibility(
                visible = isMenuOpen,
                enter = fadeIn() + androidx.compose.animation.slideInHorizontally(initialOffsetX = { -it / 2 }),
                exit = fadeOut() + androidx.compose.animation.slideOutHorizontally(targetOffsetX = { -it / 2 })
            ) {
                val drawerBgColor = colorScheme.surfaceContainerHigh
                val primaryTint = colorScheme.primary
                val onSurfaceColor = colorScheme.onSurface
                val selectedContainerCol = colorScheme.primary
                val selectedTextCol = colorScheme.onPrimary
                val view = LocalView.current

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 64.dp, bottom = 16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    GlassSurface(
                        modifier = Modifier
                            .width(280.dp)
                            .testTag("floating_nav_menu"),
                        shape = RoundedCornerShape(28.dp),
                        borderWidth = 1.2.dp,
                        customBgColor = drawerBgColor.copy(alpha = 0.85f),
                        glassState = glassState
                    ) {
                        Column(
                            modifier = Modifier
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
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "GlassCalc",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "v1.1 • Android 8-17 Ready",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = primaryTint,
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(16.dp))

                            // Navigation Items (Vertical Pills) - Renamed "Einheiten" to "Einheitenumrechnung"
                            val menuItems = listOf(
                                Triple(Icons.Default.Calculate, "Rechner", "nav_item_calculator"),
                                Triple(Icons.Default.History, "Verlauf", "nav_item_history"),
                                Triple(Icons.Default.Straighten, "Einheitenumrechnung", "nav_item_converter"),
                                Triple(Icons.Default.Settings, "Einstellungen", "nav_item_settings")
                            )

                            menuItems.forEach { (icon, label, tag) ->
                                val isSelected = label == "Rechner"
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) selectedContainerCol else Color.Transparent)
                                        .clickable {
                                            HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
                                            scope.launch { drawerState.close() }
                                            when (label) {
                                                "Verlauf" -> showHistorySheet = true
                                                "Einheitenumrechnung" -> showConverterDialog = true
                                                "Einstellungen" -> showSettingsDialog = true
                                            }
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .testTag(tag),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) selectedTextCol else onSurfaceColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = label,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) selectedTextCol else onSurfaceColor
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // Dialogs & Sheets
        if (showHistorySheet) {
            HistorySheet(
                history = history,
                onSelect = { item -> viewModel.restoreHistoryItem(item) },
                onClear = { viewModel.clearHistory() },
                onDismiss = { showHistorySheet = false }
            )
        }

        if (showSettingsDialog) {
            SettingsDialog(
                currentTheme = currentTheme,
                effectiveApiLevel = viewModel.getEffectiveApiLevel(),
                currentAngleMode = angleMode,
                windowSizeClass = adaptiveInfo.windowSizeClass,
                simulatedApiEnabled = simulatedApiEnabled,
                simulatedApiLevel = simulatedApiLevel,
                hapticFeedbackEnabled = hapticFeedbackEnabled,
                lifecycleStatus = lifecycleStatus,
                onSelectTheme = { theme -> viewModel.setTheme(theme) },
                onSelectAngleMode = { mode ->
                    if (mode != angleMode) viewModel.toggleAngleMode()
                },
                onSetSimulatedApi = { enabled, level -> viewModel.setSimulatedApi(enabled, level) },
                onToggleHapticFeedback = { enabled -> viewModel.setHapticFeedbackEnabled(enabled) },
                onSimulateBackground = { viewModel.simulateBackgroundSave() },
                onTestCrashProtection = { viewModel.testCrashProtection() },
                onResetApp = { viewModel.resetApp() },
                onDismiss = { showSettingsDialog = false }
            )
        }

        if (showConverterDialog) {
            UnitConverterDialog(
                onImportToCalculator = { value ->
                    viewModel.onClearAll()
                    for (ch in value) {
                        viewModel.onCharacterInput(ch.toString())
                    }
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
    val expression by viewModel.expression.collectAsState()
    var scientificSubTab by remember { mutableIntStateOf(0) }
    var isGestureLocked by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    val scrollbarColor = colorScheme.primary.copy(alpha = 0.45f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 440.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
            .verticalScroll(scrollState, enabled = isScientificExpanded),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Section: Display & Mode bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassDisplay(
                expressionFlow = viewModel.expression,
                resultFlow = viewModel.result,
                errorMessageFlow = viewModel.errorMessage,
                angleMode = angleMode,
                isScientificExpanded = isScientificExpanded,
                isCompact = false,
                glassState = glassState
            )

            // Bar with DEG/RAD Toggle & Scientific Switch
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
                    borderWidth = 1.dp
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
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
                    borderWidth = 1.dp,
                    customBorderColor = if (isScientificExpanded) colorScheme.tertiary else glassProps.buttonBorderColor
                ) {
                    IconButton(
                        onClick = {
                            HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
                            viewModel.toggleScientificPanel()
                        },
                        modifier = Modifier.size(width = 120.dp, height = 36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = if (isScientificExpanded) colorScheme.tertiary else colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isScientificExpanded) "Standard" else "Wissensch.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isScientificExpanded) colorScheme.tertiary else colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Middle/Bottom Section: Button Container
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val availableWidth = maxWidth
            val mainButtonSize = ((availableWidth - 30.dp) / 4f).coerceIn(48.dp, 72.dp)
            val sciButtonSize = ((availableWidth - 24.dp) / 5f).coerceIn(36.dp, 60.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Scientific functions grid or Graph view
                AnimatedVisibility(
                    visible = isScientificExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Sub-Tab Switcher (Tastatur vs Graph) with Haptic Feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GlassSurface(
                                shape = CircleShape,
                                borderWidth = 1.dp
                            ) {
                                Row(modifier = Modifier.padding(2.dp)) {
                                    // Keyboard Tab
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (scientificSubTab == 0) colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
                                                scientificSubTab = 0
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .testTag("tab_sci_keyboard"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.GridOn,
                                                contentDescription = null,
                                                tint = if (scientificSubTab == 0) colorScheme.onPrimary else colorScheme.onSurface,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Tastatur",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (scientificSubTab == 0) colorScheme.onPrimary else colorScheme.onSurface
                                            )
                                        }
                                    }

                                    // Graph Tab
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (scientificSubTab == 1) colorScheme.primary else Color.Transparent)
                                            .clickable {
                                                HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
                                                scientificSubTab = 1
                                            }
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .testTag("tab_sci_graph"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShowChart,
                                                contentDescription = null,
                                                tint = if (scientificSubTab == 1) colorScheme.onPrimary else colorScheme.onSurface,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Graph",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (scientificSubTab == 1) colorScheme.onPrimary else colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (scientificSubTab == 0) {
                            ScientificKeyboardGrid(
                                viewModel = viewModel,
                                glassState = glassState,
                                buttonSize = sciButtonSize,
                                spacing = 5.dp,
                                onOpenConstants = onOpenConstants,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            FunctionGraphView(
                                expression = expression,
                                angleMode = angleMode,
                                isGestureLocked = isGestureLocked,
                                onGestureLockChange = { isGestureLocked = it },
                                glassState = glassState,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Main 4-column rigid Keypad
                MainKeyboardGrid(
                    viewModel = viewModel,
                    glassState = glassState,
                    buttonSize = mainButtonSize,
                    spacing = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                // Extra safety spacer so scrolling leaves comfortable margin at bottom
                if (isScientificExpanded) {
                    Spacer(modifier = Modifier.height(24.dp))
                }
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
    val localContext = LocalContext.current
    val localView = LocalView.current
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val expression by viewModel.expression.collectAsState()
    var scientificSubTab by remember { mutableIntStateOf(0) }
    var isGestureLocked by remember { mutableStateOf(true) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        val availableHeight = maxHeight
        val availableWidth = maxWidth

        val maxButtonSizeFromHeight = ((availableHeight - 24.dp) / 5.2f).coerceIn(36.dp, 60.dp)
        val mainKeypadWidth = maxButtonSizeFromHeight * 4 + 20.dp
        val leftColumnAvailableWidth = availableWidth - mainKeypadWidth - 24.dp
        val maxSciButtonSizeFromWidth = ((leftColumnAvailableWidth - 16.dp) / 5f).coerceAtLeast(30.dp)

        val uniformButtonSize = minOf(maxButtonSizeFromHeight, maxSciButtonSizeFromWidth)
        val finalKeypadWidth = uniformButtonSize * 4 + 20.dp

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Compact Display + scrollable Scientific keys / Graph
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(end = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GlassDisplay(
                    expressionFlow = viewModel.expression,
                    resultFlow = viewModel.result,
                    errorMessageFlow = viewModel.errorMessage,
                    angleMode = angleMode,
                    isScientificExpanded = true,
                    isCompact = true,
                    glassState = glassState
                )

                // DEG/RAD Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    val localViewInternal = LocalView.current
                    GlassSurface(
                        modifier = Modifier.testTag("btn_toggle_angle_mode_tablet"),
                        shape = CircleShape,
                        borderWidth = 1.dp
                    ) {
                        IconButton(
                            onClick = {
                                HapticUtil.performAppHaptic(localContext, localViewInternal, hapticFeedbackEnabled)
                                viewModel.toggleAngleMode()
                            },
                            modifier = Modifier.size(width = 88.dp, height = 30.dp)
                        ) {
                            Text(
                                text = if (angleMode == com.limonatalp4.glasscalcproject.data.model.AngleMode.DEG) "DEG (Grad)" else "RAD (Bogen)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.primary
                            )
                        }
                    }
                }

                // Sub-Tab Switcher for Landscape Mode (Tastatur vs Graph) with Haptic Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassSurface(
                        shape = CircleShape,
                        borderWidth = 1.dp
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            // Keyboard Tab
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (scientificSubTab == 0) colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
                                        scientificSubTab = 0
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = null,
                                        tint = if (scientificSubTab == 0) colorScheme.onPrimary else colorScheme.onSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Tastatur",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (scientificSubTab == 0) colorScheme.onPrimary else colorScheme.onSurface
                                    )
                                }
                            }

                            // Graph Tab
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (scientificSubTab == 1) colorScheme.primary else Color.Transparent)
                                    .clickable {
                                        HapticUtil.performAppHaptic(localContext, localView, hapticFeedbackEnabled)
                                        scientificSubTab = 1
                                    }
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShowChart,
                                        contentDescription = null,
                                        tint = if (scientificSubTab == 1) colorScheme.onPrimary else colorScheme.onSurface,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Graph",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (scientificSubTab == 1) colorScheme.onPrimary else colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                if (scientificSubTab == 0) {
                    ScientificKeyboardGrid(
                        viewModel = viewModel,
                        glassState = glassState,
                        buttonSize = uniformButtonSize,
                        spacing = 4.dp,
                        onOpenConstants = onOpenConstants
                    )
                } else {
                    FunctionGraphView(
                        expression = expression,
                        angleMode = angleMode,
                        isGestureLocked = isGestureLocked,
                        onGestureLockChange = { isGestureLocked = it },
                        glassState = glassState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Right Column: Fixed Main 4x5 Keypad
            Box(
                modifier = Modifier
                    .width(finalKeypadWidth)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                MainKeyboardGrid(
                    viewModel = viewModel,
                    glassState = glassState,
                    buttonSize = uniformButtonSize,
                    spacing = 5.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 5-row Scientific Grid including:
 * Row 1: sin, cos, tan, π, e
 * Row 2: ln, log, x², x³, x^y
 * Row 3: √, ³√, n!, (, )
 * Row 4: asin, acos, atan, |x|, mod
 * Row 5: sinh, cosh, tanh, d/dx, ∫
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
        // Row 1: sin, cos, tan, π, Konst.
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
                text = "Konst.",
                type = ButtonType.FUNCTION,
                glassState = glassState,
                testTag = "btn_constants",
                contentDesc = "Physikalische und mathematische Konstanten",
                modifier = buttonModifier()
            ) {
                onOpenConstants()
            }
        }

        // Row 2: ln, log, x², x³, x^y
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
            GlassCalculatorButton(text = "x³", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onPower("3")
            }
            GlassCalculatorButton(text = "xʸ", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onPower("y")
            }
        }

        // Row 3: √, ³√, n!, (, )
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
            GlassCalculatorButton(text = "n!", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("!")
            }
            GlassCalculatorButton(text = "(", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput("(")
            }
            GlassCalculatorButton(text = ")", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput(")")
            }
        }

        // Row 4: asin, acos, atan, |x|, mod
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
            GlassCalculatorButton(text = "|x|", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("abs")
            }
            GlassCalculatorButton(text = "mod", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onCharacterInput(" mod ")
            }
        }

        // Row 5: sinh, cosh, tanh, d/dx, ∫
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
            GlassCalculatorButton(text = "d/dx", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("der")
            }
            GlassCalculatorButton(text = "∫", type = ButtonType.FUNCTION, glassState = glassState, modifier = buttonModifier()) {
                viewModel.onScientificFunction("int")
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
                contentDesc = "Löschen (Gedrückt halten zum Zurücksetzen)",
                onLongClick = { viewModel.onClearAll() },
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
                type = ButtonType.FUNCTION,
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
