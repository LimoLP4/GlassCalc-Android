package com.limonatalp4.glasscalcproject.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Policy
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.Language
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
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
    autoResultEnabled: Boolean,
    lifecycleStatus: String,
    onSelectTheme: (AppTheme) -> Unit,
    onSelectAngleMode: (AngleMode) -> Unit,
    onSetSimulatedApi: (Boolean, Int) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onToggleAutoResult: (Boolean) -> Unit,
    onSimulateBackground: () -> Unit,
    onTestCrashProtection: () -> Unit,
    onResetApp: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val config = LocalConfiguration.current
    val context = LocalContext.current
    val view = LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
    }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    val realDeviceApi = Build.VERSION.SDK_INT
    val sliderMax = realDeviceApi.toFloat().coerceIn(21f, 37f)
    val sliderMin = 21f
    val sliderSteps = if (sliderMax > sliderMin) (sliderMax - sliderMin).toInt() - 1 else 0

    var currentSliderApi by remember(simulatedApiLevel, sliderMax) { 
        mutableFloatStateOf(simulatedApiLevel.toFloat().coerceIn(sliderMin, sliderMax)) 
    }
    var lastHapticStep by remember(simulatedApiLevel) { mutableIntStateOf(simulatedApiLevel) }

    // Accordion expansion states for Info tab
    var apacheAccordionOpen by remember { mutableStateOf(false) }
    var mxparserAccordionOpen by remember { mutableStateOf(false) }

    val effectiveApi = if (simulatedApiEnabled) simulatedApiLevel else Build.VERSION.SDK_INT
    val isAmoled = glassProps.isAmoled
    val isTiefschwarz = glassProps.isTiefschwarz

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_settings")
    ) {
        GlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(28.dp),
            borderWidth = 1.2.dp,
            customBgColor = when {
                isAmoled -> Color.Black
                isTiefschwarz -> Color(0xFF121212)
                else -> colorScheme.surfaceContainerHigh
            },
            customBorderColor = when {
                isAmoled -> Color.White.copy(alpha = 0.15f)
                isTiefschwarz -> Color.White.copy(alpha = 0.1f)
                else -> null
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                        color = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_close_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.nav_close),
                            tint = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5-Tab Segmented Control (Pill-Design)
                val tabs = listOf(
                    Triple(0, Icons.Default.Palette, androidx.compose.ui.res.stringResource(id = R.string.title_appearance)),
                    Triple(1, Icons.Default.Straighten, androidx.compose.ui.res.stringResource(id = R.string.title_converter)),
                    Triple(2, Icons.Default.DeveloperMode, androidx.compose.ui.res.stringResource(id = R.string.title_dev_options)),
                    Triple(3, Icons.Default.Info, androidx.compose.ui.res.stringResource(id = R.string.title_info)),
                    Triple(4, Icons.Default.Language, androidx.compose.ui.res.stringResource(id = R.string.tab_language))
                )

                val tabScrollState = androidx.compose.foundation.rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            when {
                                isAmoled -> Color.Black
                                isTiefschwarz -> colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                else -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = when {
                                isAmoled -> Color.White.copy(alpha = 0.15f)
                                isTiefschwarz -> Color.White.copy(alpha = 0.1f)
                                else -> colorScheme.outline.copy(alpha = 0.3f)
                            },
                            shape = RoundedCornerShape(50.dp)
                        )
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(tabScrollState),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEach { (index, icon, label) ->
                            val isSelected = selectedTab == index
                            val tabBg by animateColorAsState(
                                targetValue = if (isSelected) colorScheme.primaryContainer else Color.Transparent,
                                animationSpec = tween(durationMillis = 220),
                                label = "tab_${index}_bg"
                            )
                            val tabTint by animateColorAsState(
                                targetValue = if (isSelected) {
                                    if (isAmoled || isTiefschwarz) Color.White else colorScheme.onPrimaryContainer
                                } else {
                                    if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant
                                },
                                animationSpec = tween(durationMillis = 220),
                                label = "tab_${index}_tint"
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(tabBg)
                                    .clickable {
                                        triggerHaptic()
                                        selectedTab = index
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

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // Appearance Section (Individual Pill Cards with Haptic Feedback)
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = R.string.theme_select_heading),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AppTheme.entries.forEach { themeOption ->
                                val isAmoled = glassProps.isAmoled
                                val isMaterialYou = themeOption == AppTheme.MATERIAL_YOU
                                val isSupported = !isMaterialYou || effectiveApiLevel >= 31
                                val isSelected = currentTheme == themeOption && isSupported
                                
                                val cardBg = if (isSelected) {
                                    if (isAmoled || isTiefschwarz) colorScheme.primaryContainer.copy(alpha = 0.7f) else colorScheme.primaryContainer
                                } else {
                                    if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                                
                                val cardBorder = if (isAmoled) {
                                    Color.White.copy(alpha = 0.15f)
                                } else if (isTiefschwarz) {
                                    Color.White.copy(alpha = 0.1f)
                                } else {
                                    if (isSelected) colorScheme.primary else glassProps.buttonBorderColor
                                }
                                
                                val textColor = if (isAmoled || isTiefschwarz) Color.White else (if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface)
                                val radioIconColor = if (isAmoled || isTiefschwarz) Color.White else colorScheme.primary

                                GlassSurface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isSupported) 1.0f else 0.45f)
                                        .clickable(enabled = isSupported) {
                                            triggerHaptic()
                                            onSelectTheme(themeOption)
                                        }
                                        .testTag("radio_theme_${themeOption.name}"),
                                    shape = RoundedCornerShape(24.dp),
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp,
                                    customBorderColor = cardBorder,
                                    customBgColor = cardBg
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = if (isSupported) {
                                                {
                                                    triggerHaptic()
                                                    onSelectTheme(themeOption)
                                                }
                                            } else null,
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = radioIconColor,
                                                unselectedColor = if (isAmoled) Color.White.copy(alpha = 0.5f) else colorScheme.onSurfaceVariant
                                            ),
                                            enabled = isSupported
                                        )

                                        Column(modifier = Modifier.padding(start = 12.dp)) {
                                            val themeLabel = when (themeOption) {
                                                AppTheme.MATERIAL_YOU -> androidx.compose.ui.res.stringResource(id = R.string.theme_material_you)
                                                AppTheme.AUTOMATIC -> androidx.compose.ui.res.stringResource(id = R.string.theme_automatic)
                                                AppTheme.WHITE_MODE -> androidx.compose.ui.res.stringResource(id = R.string.theme_white_mode)
                                                AppTheme.STANDARD_DARK -> androidx.compose.ui.res.stringResource(id = R.string.theme_standard_dark)
                                                AppTheme.DARK_MODE -> androidx.compose.ui.res.stringResource(id = R.string.theme_dark_mode)
                                                AppTheme.AMOLED_DARK -> androidx.compose.ui.res.stringResource(id = R.string.theme_amoled_dark)
                                            }
                                            Text(
                                                text = themeLabel,
                                                fontSize = 15.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = textColor
                                            )

                                            if (!isSupported) {
                                                Text(
                                                    text = androidx.compose.ui.res.stringResource(id = R.string.theme_material_you_unsupported, effectiveApiLevel),
                                                    fontSize = 12.sp,
                                                    color = colorScheme.error,
                                                    modifier = Modifier.testTag("text_material_you_unsupported")
                                                )
                                            } else if (themeOption == AppTheme.AMOLED_DARK) {
                                                Text(
                                                    text = androidx.compose.ui.res.stringResource(id = R.string.theme_amoled_desc),
                                                    fontSize = 12.sp,
                                                    color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else (if (isSelected) colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else colorScheme.onSurface.copy(alpha = 0.55f)),
                                                    softWrap = true
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Units Section (Individual Pill Cards with Haptic Feedback)
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = R.string.units_angle_heading),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val isAmoled = glassProps.isAmoled
                            // Option 1: DEG
                            val isDeg = currentAngleMode == AngleMode.DEG
                            val degBg = if (isDeg) {
                                if (isAmoled || isTiefschwarz) colorScheme.primaryContainer.copy(alpha = 0.7f) else colorScheme.primaryContainer
                            } else {
                                if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            
                            val degBorder = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else (if (isDeg) colorScheme.primary else glassProps.buttonBorderColor)
                            val degTextColor = if (isAmoled || isTiefschwarz) Color.White else (if (isDeg) colorScheme.onPrimaryContainer else colorScheme.onSurface)

                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        triggerHaptic()
                                        onSelectAngleMode(AngleMode.DEG)
                                    }
                                    .testTag("radio_angle_deg"),
                                shape = RoundedCornerShape(24.dp),
                                borderWidth = if (isDeg) 1.5.dp else 1.dp,
                                customBorderColor = degBorder,
                                customBgColor = degBg
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isDeg,
                                        onClick = {
                                            triggerHaptic()
                                            onSelectAngleMode(AngleMode.DEG)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = colorScheme.primary,
                                            unselectedColor = colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.angle_deg_label),
                                            fontSize = 15.sp,
                                            fontWeight = if (isDeg) FontWeight.Bold else FontWeight.Medium,
                                            color = degTextColor
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.angle_deg_desc),
                                            fontSize = 12.sp,
                                            color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else (if (isDeg) colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else colorScheme.onSurface.copy(alpha = 0.55f))
                                        )
                                    }
                                }
                            }

                            // Option 2: RAD
                            val isRad = currentAngleMode == AngleMode.RAD
                            val radBg = if (isRad) {
                                if (isAmoled || isTiefschwarz) colorScheme.primaryContainer.copy(alpha = 0.7f) else colorScheme.primaryContainer
                            } else {
                                if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                            val radBorder = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else (if (isRad) colorScheme.primary else glassProps.buttonBorderColor)
                            val radTextColor = if (isAmoled || isTiefschwarz) Color.White else (if (isRad) colorScheme.onPrimaryContainer else colorScheme.onSurface)

                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        triggerHaptic()
                                        onSelectAngleMode(AngleMode.RAD)
                                    }
                                    .testTag("radio_angle_rad"),
                                shape = RoundedCornerShape(24.dp),
                                borderWidth = if (isRad) 1.5.dp else 1.dp,
                                customBorderColor = radBorder,
                                customBgColor = radBg
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isRad,
                                        onClick = {
                                            triggerHaptic()
                                            onSelectAngleMode(AngleMode.RAD)
                                        },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = colorScheme.primary,
                                            unselectedColor = colorScheme.onSurfaceVariant
                                        )
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.angle_rad_label),
                                            fontSize = 15.sp,
                                            fontWeight = if (isRad) FontWeight.Bold else FontWeight.Medium,
                                            color = radTextColor
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.angle_rad_desc),
                                            fontSize = 12.sp,
                                        color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else (if (isRad) colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else colorScheme.onSurface.copy(alpha = 0.55f))
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Supported Systems Card (Pill Shape)
                        GlassSurface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            borderWidth = 1.dp,
                            customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = R.string.units_supported_systems),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = androidx.compose.ui.res.stringResource(id = R.string.units_systems_detail),
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurface.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                    2 -> {
                        // Developer Options Section
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Card 1: Screen & WindowSizeClass
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = stringResource(R.string.dev_screen_scaling),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "WindowSizeClass: ${windowSizeClass?.windowWidthSizeClass ?: "Compact"}",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.dev_resolution, config.screenWidthDp, config.screenHeightDp),
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Card 2: Simulated API Level
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
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
                                            color = if (isAmoled || isTiefschwarz) Color.White else colorScheme.onSurface
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
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = stringResource(R.string.dev_api_level_slider, currentSliderApi.toInt()),
                                            fontSize = 12.sp,
                                            color = colorScheme.primary
                                        )
                                        Slider(
                                            value = currentSliderApi,
                                            onValueChange = { newVal ->
                                                currentSliderApi = newVal
                                                val rounded = newVal.roundToInt()
                                                if (rounded != lastHapticStep) {
                                                    lastHapticStep = rounded
                                                    triggerHaptic()
                                                }
                                            },
                                            onValueChangeFinished = {
                                                onSetSimulatedApi(true, currentSliderApi.toInt())
                                            },
                                            valueRange = sliderMin..sliderMax,
                                            steps = sliderSteps,
                                            colors = SliderDefaults.colors(
                                                thumbColor = colorScheme.primary,
                                                activeTrackColor = colorScheme.primary
                                            ),
                                            modifier = Modifier.testTag("slider_simulated_api")
                                        )
                                    }

                                    // Dynamic API Feature Legend (Pill Design)
                                    val activeApiForLegend = if (simulatedApiEnabled) currentSliderApi.toInt() else effectiveApi

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = stringResource(R.string.dev_api_feature_compatibility, realDeviceApi),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val apiFeatures = listOf(
                                        Triple("VibrationEffect", 26, "API 26+ / Android 8.0+"),
                                        Triple("Edge-to-Edge", 29, "API 29+ / Android 10+"),
                                        Triple("Material You", 31, "API 31+ / Android 12+"),
                                        Triple("Voller Blur-Effekt", 31, "API 31+ / Android 12+"),
                                        Triple("Swipe-Gesten (Predictive Back)", 33, "API 33+ / Android 13+")
                                    )

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        apiFeatures.forEach { (name, minApi, suffix) ->
                                            val isAvailable = activeApiForLegend >= minApi
                                            val pillBg = if (isAvailable) {
                                                colorScheme.primaryContainer.copy(alpha = 0.85f)
                                            } else {
                                                colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            }
                                            val textColor = if (isAvailable) {
                                                colorScheme.onPrimaryContainer
                                            } else {
                                                colorScheme.onSurface.copy(alpha = 0.35f)
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .background(pillBg)
                                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isAvailable) Icons.Default.Check else Icons.Default.Close,
                                                        contentDescription = if (isAvailable) stringResource(R.string.dev_feature_supported) else stringResource(R.string.dev_feature_not_supported),
                                                        tint = if (isAvailable) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.35f),
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = "$name ($suffix)",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isAvailable) FontWeight.Bold else FontWeight.Medium,
                                                        color = textColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Card 3: Haptic Feedback with hardware motor detection (LRA, ERM, None) and simulated API check
                            val hasVibratorMotor = remember(context) { HapticUtil.hasVibrator(context) }
                            val hapticMotorType = remember(context) { HapticUtil.getHapticMotorType(context) }
                            val isSupportedBySimulatedApi = effectiveApi >= 26
                            val isHapticUsable = hasVibratorMotor && isSupportedBySimulatedApi

                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp)
                                ) {
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
                                                text = androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_title),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary
                                            )
                                            Text(
                                                text = androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_desc),
                                                fontSize = 12.sp,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Switch(
                                            checked = if (isHapticUsable) hapticFeedbackEnabled else false,
                                            onCheckedChange = if (isHapticUsable) {
                                                {
                                                    HapticUtil.performAppHaptic(context, view, true)
                                                    onToggleHapticFeedback(it)
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

                                    when {
                                        !hasVibratorMotor -> {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_unsupported_device),
                                                fontSize = 12.sp,
                                                color = colorScheme.error,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.testTag("text_haptic_unsupported")
                                            )
                                        }
                                        simulatedApiEnabled && !isSupportedBySimulatedApi -> {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_unsupported_api),
                                                fontSize = 12.sp,
                                                color = colorScheme.error,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.testTag("text_haptic_simulated_api_unsupported")
                                            )
                                        }
                                        else -> {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = if (hapticMotorType == com.limonatalp4.glasscalcproject.ui.util.HapticMotorType.LRA) {
                                                    androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_lra_detected)
                                                } else {
                                                    androidx.compose.ui.res.stringResource(id = R.string.dev_haptic_erm_detected)
                                                },
                                                fontSize = 11.sp,
                                                color = colorScheme.primary.copy(alpha = 0.8f),
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.testTag("text_haptic_motor_type")
                                            )
                                        }
                                    }
                                }
                            }

                            // Auto Result Switch
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.dev_auto_result_title),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.dev_auto_result_desc),
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = autoResultEnabled,
                                        onCheckedChange = {
                                            triggerHaptic()
                                            onToggleAutoResult(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colorScheme.onPrimary,
                                            checkedTrackColor = colorScheme.primary,
                                            uncheckedThumbColor = colorScheme.outline,
                                            uncheckedTrackColor = colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.testTag("switch_auto_result")
                                    )
                                }
                            }

                            // Card 4: Lifecycle & State
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.dev_lifecycle_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Status: $lifecycleStatus",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (lifecycleStatus.contains("STOP")) colorScheme.error else colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                triggerHaptic()
                                                onSimulateBackground()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_simulate_background"),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Text(androidx.compose.ui.res.stringResource(id = R.string.dev_btn_background), fontSize = 11.sp, maxLines = 1)
                                        }
                                        Button(
                                            onClick = {
                                                triggerHaptic()
                                                onTestCrashProtection()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("btn_test_crash_protection"),
                                            shape = RoundedCornerShape(24.dp)
                                        ) {
                                            Text(androidx.compose.ui.res.stringResource(id = R.string.dev_btn_crash_test), fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            // Card 5: Separate Card 1 - App-Speicher zurücksetzen (DataStore / History)
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.dev_reset_storage_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.dev_reset_storage_desc),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            showResetConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.error,
                                            contentColor = colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_reset_storage")
                                    ) {
                                        Text(androidx.compose.ui.res.stringResource(id = R.string.dev_btn_reset_storage), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Card 6: Separate Card 2 - Cache leeren (Temporary files)
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.dev_clear_cache_title),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.dev_clear_cache_desc),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    val cacheSuccessMsg = androidx.compose.ui.res.stringResource(id = R.string.toast_cache_cleared)
                                    val cacheErrorMsg = androidx.compose.ui.res.stringResource(id = R.string.toast_cache_error)
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            try {
                                                context.cacheDir.deleteRecursively()
                                                Toast.makeText(context, cacheSuccessMsg, Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Toast.makeText(context, cacheErrorMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.primary,
                                            contentColor = colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(50),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_clear_cache")
                                    ) {
                                        Text(androidx.compose.ui.res.stringResource(id = R.string.dev_btn_clear_cache), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Information Tab (Task 5: Complete Legal, GitHub & OSS Licenses)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // App Version Card
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else glassProps.buttonBorderColor
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "GlassCalc v1.2.2",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.app_version_subtitle),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.copyright_notice),
                                        fontSize = 11.sp,
                                        color = colorScheme.onSurface.copy(alpha = 0.6f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // GitHub Repository Card
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else glassProps.buttonBorderColor
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            triggerHaptic()
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LimoLP4/GlassCalc-Android/releases/"))
                                            context.startActivity(intent)
                                        }
                                        .padding(18.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.info_github_repo),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "LimoLP4/GlassCalc-Android",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = androidx.compose.ui.res.stringResource(id = R.string.info_github_open),
                                        tint = colorScheme.primary
                                    )
                                }
                            }

                            // Third-Party OSS Licenses Button (play-services-oss-licenses)
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else glassProps.buttonBorderColor
                            ) {
                                Column(modifier = Modifier.padding(18.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Policy,
                                            contentDescription = null,
                                            tint = colorScheme.primary
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = R.string.info_oss_licenses),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.info_oss_licenses_desc),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            try {
                                                val intent = Intent(context, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Lizenzen können nicht geöffnet werden: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colorScheme.primary,
                                            contentColor = colorScheme.onPrimary
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_oss_licenses")
                                    ) {
                                        Text(androidx.compose.ui.res.stringResource(id = R.string.info_btn_oss_licenses), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Accordion 1: Apache License 2.0 (App License)
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            triggerHaptic()
                                            apacheAccordionOpen = !apacheAccordionOpen
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Apache License 2.0",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = if (apacheAccordionOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = colorScheme.onSurface
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = apacheAccordionOpen,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            Text(
                                                text = "Copyright 2026 limonatalp4\n\n" +
                                                        "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                                                        "you may not use this file except in compliance with the License.\n" +
                                                        "You may obtain a copy of the License at\n\n" +
                                                        "    http://www.apache.org/licenses/LICENSE-2.0\n\n" +
                                                        "Unless required by applicable law or agreed to in writing, software\n" +
                                                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                                                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                                                        "See the License for the specific language governing permissions and\n" +
                                                        "limitations under the License.",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Accordion 2: mXparser License Notice
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            triggerHaptic()
                                            mxparserAccordionOpen = !mxparserAccordionOpen
                                        }
                                        .padding(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "mXparser Lizenz (Non-Commercial)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = if (mxparserAccordionOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = colorScheme.onSurface
                                        )
                                    }

                                    AnimatedVisibility(
                                        visible = mxparserAccordionOpen,
                                        enter = expandVertically() + fadeIn(),
                                        exit = shrinkVertically() + fadeOut()
                                    ) {
                                        Column(modifier = Modifier.padding(top = 10.dp)) {
                                            Text(
                                                text = "mXparser - Math Parser for JAVA, Android and C# .NET\n" +
                                                        "Copyright (c) 2010 - 2026 Mariusz Gromada - MathParser.org\n\n" +
                                                        "GlassCalc nutzt mXparser unter der \"mXparser Non-Commercial License Agreement\".\n" +
                                                        "Bestätigung erfolgte via:\n" +
                                                        "License.iConfirmNonCommercialUse(\"limonatalp4\")\n\n" +
                                                        "Diese Software ist ein nicht-kommerzielles Open-Source-Projekt und respektiert sämtliche Bedingungen des Urhebers.",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            // Library Usages & Architecture Card
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant,
                                customBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else null
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.info_used_libraries),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = androidx.compose.ui.res.stringResource(id = R.string.info_libraries_detail),
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    4 -> {
                        // Language Selection Section
                        Text(
                            text = androidx.compose.ui.res.stringResource(id = R.string.language_select_heading),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Language.entries.forEach { langOption ->
                                val isSelected = currentLanguage == langOption.localeTag
                                val cardBg = if (isSelected) {
                                    if (isAmoled || isTiefschwarz) colorScheme.primaryContainer.copy(alpha = 0.7f) else colorScheme.primaryContainer
                                } else {
                                    if (isAmoled) Color.Black else if (isTiefschwarz) Color(0xFF121212) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                }
                                val cardBorder = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else (if (isSelected) colorScheme.primary else glassProps.buttonBorderColor)
                                val textColor = if (isAmoled || isTiefschwarz) Color.White else (if (isSelected) colorScheme.onPrimaryContainer else colorScheme.onSurface)

                                GlassSurface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            triggerHaptic()
                                            onSelectLanguage(langOption.localeTag)
                                        }
                                        .testTag("radio_lang_${langOption.name}"),
                                    shape = RoundedCornerShape(24.dp),
                                    borderWidth = if (isSelected) 1.5.dp else 1.dp,
                                    customBorderColor = cardBorder,
                                    customBgColor = cardBg
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                triggerHaptic()
                                                onSelectLanguage(langOption.localeTag)
                                            },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = textColor,
                                                unselectedColor = if (isAmoled) Color.White.copy(alpha = 0.5f) else colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(id = langOption.stringResId),
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor,
                                            modifier = Modifier.padding(start = 12.dp)
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

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colorScheme.error) },
            title = { Text("App wirklich zurücksetzen?") },
            text = { Text("Alle DataStore-Einstellungen, gespeicherten Berechnungen und der gesamte Verlauf werden unwiderruflich gelöscht.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        triggerHaptic()
                        showResetConfirmDialog = false
                        onResetApp()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_confirm_reset")
                ) {
                    Text("Zurücksetzen", color = colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    triggerHaptic()
                    showResetConfirmDialog = false
                }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
