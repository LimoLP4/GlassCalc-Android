package com.limonatalp4.glasscalcproject.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Opacity
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterDialog(
    currentTheme: AppTheme,
    onImportToCalculator: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val glassProps = LocalGlassProperties.current
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled)
    }

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    val categories = UnitCategory.entries

    val currentCategory = categories[selectedCategoryIndex]
    val conversionsInCategory = remember(currentCategory) {
        UnitConverter.CONVERSIONS.filter { it.category == currentCategory }
    }

    var selectedPairIndex by remember { mutableIntStateOf(0) }
    val currentPair = conversionsInCategory.getOrElse(selectedPairIndex) { conversionsInCategory.first() }

    var isForward by remember { mutableStateOf(true) }
    var inputValue by remember { mutableStateOf("1") }

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
    val isAmoled = glassProps.isAmoled
    val isTiefschwarz = glassProps.isTiefschwarz

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_unit_converter")
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
                else -> dialogBgColor
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
                        text = "Einheitenumrechnung",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAmoled || isTiefschwarz) Color.White else onSurfaceColor
                    )
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_close_converter")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Schließen",
                            tint = if (isAmoled || isTiefschwarz) Color.White else onSurfaceColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pill-Design Segmented Control for Categories (matching SettingsDialog design)
                val categoryTabs = listOf(
                    Triple(UnitCategory.LENGTH, Icons.Default.Straighten, "Länge"),
                    Triple(UnitCategory.MASS, Icons.Default.FitnessCenter, "Masse"),
                    Triple(UnitCategory.TEMPERATURE, Icons.Default.Thermostat, "Temp."),
                    Triple(UnitCategory.VOLUME, Icons.Default.Opacity, "Volumen")
                )

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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categoryTabs.forEachIndexed { index, (cat, icon, shortLabel) ->
                            val isSelected = selectedCategoryIndex == index
                            val tabBg by animateColorAsState(
                                targetValue = if (isSelected) primaryContainerColor else Color.Transparent,
                                animationSpec = tween(durationMillis = 220),
                                label = "cat_tab_bg_$index"
                            )
                            val tabTint by animateColorAsState(
                                targetValue = if (isSelected) {
                                    if (isAmoled || isTiefschwarz) Color.White else onPrimaryContainerColor
                                } else {
                                    if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.6f) else colorScheme.onSurfaceVariant
                                },
                                animationSpec = tween(durationMillis = 220),
                                label = "cat_tab_tint_$index"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(tabBg)
                                    .clickable {
                                        triggerHaptic()
                                        selectedCategoryIndex = index
                                        selectedPairIndex = 0
                                    }
                                    .padding(vertical = 9.dp, horizontal = 2.dp)
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
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = shortLabel,
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

                Spacer(modifier = Modifier.height(14.dp))

                // Conversion Pairs in this category
                Text(
                    text = "Umrechnung auswählen:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                conversionsInCategory.forEachIndexed { idx, pair ->
                    val isAmoled = glassProps.isAmoled
                    val isSelected = selectedPairIndex == idx
                    val pairBg = if (isSelected) {
                        if (isAmoled || isTiefschwarz) primaryContainerColor.copy(alpha = 0.7f) else primaryContainerColor
                    } else {
                        if (isAmoled) Color.Black else if (isTiefschwarz) colorScheme.surfaceVariant.copy(alpha = 0.7f) else colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }
                    val pairBorder = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else (if (isSelected) primaryColor else glassProps.buttonBorderColor)
                    val pairTextCol = if (isAmoled || isTiefschwarz) Color.White else (if (isSelected) onPrimaryContainerColor else onSurfaceColor)

                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                triggerHaptic()
                                selectedPairIndex = idx
                            }
                            .testTag("pair_${pair.unitFrom}_${pair.unitTo}"),
                        shape = RoundedCornerShape(50.dp),
                        borderWidth = if (isSelected) 1.5.dp else 1.dp,
                        customBorderColor = pairBorder,
                        customBgColor = pairBg
                    ) {
                        Text(
                            text = pair.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = pairTextCol,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Conversion Box
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    borderWidth = 1.2.dp,
                    customBgColor = when {
                        isAmoled -> Color.Black
                        isTiefschwarz -> colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        else -> null
                    },
                    customBorderColor = when {
                        isAmoled -> Color.White.copy(alpha = 0.15f)
                        isTiefschwarz -> Color.White.copy(alpha = 0.1f)
                        else -> null
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Eingabe ($fromUnit):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
                            )

                            IconButton(
                                onClick = {
                                    triggerHaptic()
                                    isForward = !isForward
                                },
                                modifier = Modifier.testTag("btn_swap_units")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Richtung umkehren",
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
                                focusedContainerColor = when {
                                    isAmoled -> Color.Black
                                    isTiefschwarz -> Color(0xFF1E1E1E)
                                    else -> colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                },
                                unfocusedContainerColor = when {
                                    isAmoled -> Color.Black
                                    isTiefschwarz -> Color(0xFF1A1A1A)
                                    else -> colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                focusedBorderColor = if (isAmoled || isTiefschwarz) Color.White else primaryColor,
                                unfocusedBorderColor = if (isAmoled) Color.White.copy(alpha = 0.15f) else if (isTiefschwarz) Color.White.copy(alpha = 0.1f) else glassProps.buttonBorderColor,
                                focusedTextColor = if (isAmoled || isTiefschwarz) Color.White else onSurfaceColor,
                                unfocusedTextColor = if (isAmoled || isTiefschwarz) Color.White else onSurfaceColor
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ergebnis ($toUnit):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAmoled || isTiefschwarz) Color.White.copy(alpha = 0.7f) else onSurfaceColor.copy(alpha = 0.7f)
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
                Button(
                    onClick = {
                        triggerHaptic()
                        val cleaned = convertedResult.replace(",", ".").replace(" ", "")
                        if (cleaned != "—") {
                            onImportToCalculator(cleaned)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_import_converter"),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = colorScheme.onPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("In Rechner übernehmen", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
