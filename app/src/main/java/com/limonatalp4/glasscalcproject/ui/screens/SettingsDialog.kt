package com.limonatalp4.glasscalcproject.ui.screens

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperMode
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentTheme: AppTheme,
    effectiveApiLevel: Int,
    currentAngleMode: AngleMode,
    windowSizeClass: WindowSizeClass?,
    simulatedApiEnabled: Boolean,
    simulatedApiLevel: Int,
    hapticFeedbackEnabled: Boolean,
    lifecycleStatus: String,
    onSelectTheme: (AppTheme) -> Unit,
    onSelectAngleMode: (AngleMode) -> Unit,
    onSetSimulatedApi: (Boolean, Int) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onSimulateBackground: () -> Unit,
    onTestCrashProtection: () -> Unit,
    onResetApp: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val config = LocalConfiguration.current
    val density = LocalDensity.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
    }

    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var currentSliderApi by remember(simulatedApiLevel) { mutableFloatStateOf(simulatedApiLevel.toFloat()) }

    val effectiveApi = if (simulatedApiEnabled) simulatedApiLevel else Build.VERSION.SDK_INT
    val isBlurActive = effectiveApi in 31..35 && com.limonatalp4.glasscalcproject.ui.components.isBlurHardwareSupported()

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
            customBgColor = colorScheme.surfaceContainerHigh
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
                        text = "Einstellungen",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
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
                            contentDescription = "Schließen",
                            tint = colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Tab Segmented Control (Pill-Design)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Tab 0: Erscheinungsbild
                    val tab0Selected = selectedTab == 0
                    val tab0Bg by animateColorAsState(
                        targetValue = if (tab0Selected) colorScheme.primary else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab0_bg"
                    )
                    val tab0Tint = if (tab0Selected) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.75f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(tab0Bg)
                            .clickable {
                                triggerHaptic()
                                selectedTab = 0
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("tab_appearance"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = tab0Tint, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Design",
                                fontSize = 12.sp,
                                fontWeight = if (tab0Selected) FontWeight.Bold else FontWeight.Medium,
                                color = tab0Tint,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Tab 1: Einheiten
                    val tab1Selected = selectedTab == 1
                    val tab1Bg by animateColorAsState(
                        targetValue = if (tab1Selected) colorScheme.primary else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab1_bg"
                    )
                    val tab1Tint = if (tab1Selected) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.75f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(tab1Bg)
                            .clickable {
                                triggerHaptic()
                                selectedTab = 1
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("tab_units"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Straighten, contentDescription = null, tint = tab1Tint, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Einheiten",
                                fontSize = 12.sp,
                                fontWeight = if (tab1Selected) FontWeight.Bold else FontWeight.Medium,
                                color = tab1Tint,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    // Tab 2: Entwickleroptionen
                    val tab2Selected = selectedTab == 2
                    val tab2Bg by animateColorAsState(
                        targetValue = if (tab2Selected) colorScheme.primary else Color.Transparent,
                        animationSpec = tween(durationMillis = 200),
                        label = "tab2_bg"
                    )
                    val tab2Tint = if (tab2Selected) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.75f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50.dp))
                            .background(tab2Bg)
                            .clickable {
                                triggerHaptic()
                                selectedTab = 2
                            }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .testTag("tab_dev_options"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.DeveloperMode, contentDescription = null, tint = tab2Tint, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Entwickler",
                                fontSize = 12.sp,
                                fontWeight = if (tab2Selected) FontWeight.Bold else FontWeight.Medium,
                                color = tab2Tint,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    0 -> {
                        // Appearance Section
                        Text(
                            text = "Wähle das Design:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AppTheme.entries.forEach { themeOption ->
                            val isMaterialYou = themeOption == AppTheme.MATERIAL_YOU
                            val isSupported = !isMaterialYou || effectiveApiLevel >= 31
                            val isSelected = currentTheme == themeOption && isSupported

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .alpha(if (isSupported) 1.0f else 0.45f)
                                    .clickable(enabled = isSupported) {
                                        triggerHaptic()
                                        onSelectTheme(themeOption)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                                    .testTag("radio_theme_${themeOption.name}"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = if (isSupported) { { onSelectTheme(themeOption) } } else null,
                                    colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary),
                                    enabled = isSupported
                                )

                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = themeOption.label,
                                        fontSize = 16.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = colorScheme.onSurface
                                    )

                                    if (!isSupported) {
                                        Text(
                                            text = "Nicht mit dieser Android-Version kompatibel (erfordert API 33+, aktiv: API $effectiveApiLevel)",
                                            fontSize = 12.sp,
                                            color = colorScheme.error,
                                            modifier = Modifier.testTag("text_material_you_unsupported")
                                        )
                                    } else if (themeOption == AppTheme.AMOLED_DARK) {
                                        Text(
                                            text = "Reines Schwarz #000000 mit 10-15% Weiß-Glas für maximale OLED-Energieersparnis",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Units Section
                        Text(
                            text = "Standard-Winkelmodus für Trigonometrie:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    triggerHaptic()
                                    onSelectAngleMode(AngleMode.DEG)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAngleMode == AngleMode.DEG,
                                onClick = {
                                    triggerHaptic()
                                    onSelectAngleMode(AngleMode.DEG)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text("Grad (DEG)", fontSize = 16.sp, color = colorScheme.onSurface)
                                Text("Standardkreis = 360°", fontSize = 12.sp, color = colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    triggerHaptic()
                                    onSelectAngleMode(AngleMode.RAD)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentAngleMode == AngleMode.RAD,
                                onClick = {
                                    triggerHaptic()
                                    onSelectAngleMode(AngleMode.RAD)
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colorScheme.primary)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text("Bogenmaß (RAD)", fontSize = 16.sp, color = colorScheme.onSurface)
                                Text("Vollkreis = 2π", fontSize = 12.sp, color = colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Unterstützte Umrechnungssysteme:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "• Metrisch ↔ US/Imperial (m ↔ ft, cm ↔ in, km ↔ mi, kg ↔ lb)\n• Temperatur (°C ↔ °F)\n• Volumen (l ↔ gal)",
                            fontSize = 13.sp,
                            color = colorScheme.onSurface.copy(alpha = 0.7f)
                        )
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
                                customBgColor = colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Bildschirmgröße & Skalierung",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "WindowSizeClass: ${windowSizeClass?.windowWidthSizeClass ?: "Compact"}",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Auflösung: ${config.screenWidthDp}dp x ${config.screenHeightDp}dp",
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
                                customBgColor = colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Simulierte Android-Version",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Effektive UI-API: $effectiveApi",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (effectiveApi < 33) colorScheme.error else colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "API-Stufe simulieren",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = colorScheme.onSurface
                                        )
                                        Switch(
                                            checked = simulatedApiEnabled,
                                            onCheckedChange = { checked ->
                                                triggerHaptic()
                                                onSetSimulatedApi(checked, currentSliderApi.toInt())
                                            },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = colorScheme.primary,
                                                checkedTrackColor = colorScheme.primaryContainer
                                            ),
                                            modifier = Modifier.testTag("switch_simulate_api")
                                        )
                                    }

                                    if (simulatedApiEnabled) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Stufe: API ${currentSliderApi.toInt()}",
                                            fontSize = 12.sp,
                                            color = colorScheme.primary
                                        )
                                        Slider(
                                            value = currentSliderApi,
                                            onValueChange = { currentSliderApi = it },
                                            onValueChangeFinished = {
                                                onSetSimulatedApi(true, currentSliderApi.toInt())
                                            },
                                            valueRange = 28f..35f,
                                            steps = 6,
                                            colors = SliderDefaults.colors(thumbColor = colorScheme.primary, activeTrackColor = colorScheme.primaryContainer),
                                            modifier = Modifier.testTag("slider_simulated_api")
                                        )
                                    }
                                }
                            }

                            // Card 3: Haptic Feedback
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = colorScheme.surfaceVariant
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
                                            text = "Haptisches Feedback",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.primary
                                        )
                                        Text(
                                            text = "Vibration bei Tastendruck",
                                            fontSize = 12.sp,
                                            color = colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = hapticFeedbackEnabled,
                                        onCheckedChange = {
                                            triggerHaptic()
                                            onToggleHapticFeedback(it)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = colorScheme.primary,
                                            checkedTrackColor = colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag("switch_haptic_feedback")
                                    )
                                }
                            }

                            // Card 4: Lifecycle & State
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Zustandserhalt & Lifecycle",
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
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Hintergrund", fontSize = 11.sp, maxLines = 1)
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
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text("Crash-Test", fontSize = 11.sp, maxLines = 1)
                                        }
                                    }
                                }
                            }

                            // Card 5: Cache Clear
                            GlassSurface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                customBgColor = colorScheme.surfaceVariant
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "App-Speicher & Cache",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Setzt DataStore und Verlauf auf Standardwerte zurück.",
                                        fontSize = 12.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            triggerHaptic()
                                            showResetConfirmDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("btn_clear_cache")
                                    ) {
                                        Text("Cache leeren", fontWeight = FontWeight.Bold)
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
            text = { Text("Alle DataStore-Einstellungen, Berechnungen und der aktuelle Zustand werden gelöscht.") },
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
