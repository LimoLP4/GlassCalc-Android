package com.limonatalp4.glasscalcproject.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.limonatalp4.glasscalcproject.domain.converter.UnitCategory
import com.limonatalp4.glasscalcproject.domain.converter.UnitConverter
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterDialog(
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
            customBgColor = dialogBgColor
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
                        color = onSurfaceColor
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
                            tint = onSurfaceColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pill-Design Segmented Control for Categories
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50.dp))
                        .background(colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.forEachIndexed { index, cat ->
                        val isSelected = selectedCategoryIndex == index
                        val tabBg by animateColorAsState(
                            targetValue = if (isSelected) primaryContainerColor else Color.Transparent,
                            animationSpec = tween(durationMillis = 200),
                            label = "cat_tab_bg_$index"
                        )
                        val tabTint = if (isSelected) onPrimaryContainerColor else onSurfaceColor.copy(alpha = 0.75f)

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
                                .padding(vertical = 10.dp, horizontal = 4.dp)
                                .testTag("tab_cat_${cat.name}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat.displayName,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tabTint,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Conversion Pairs in this category
                Text(
                    text = "Umrechnung auswählen:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                conversionsInCategory.forEachIndexed { idx, pair ->
                    val isSelected = selectedPairIndex == idx
                    val pairBg = if (isSelected) primaryColor.copy(alpha = 0.15f) else null
                    val pairBorder = if (isSelected) primaryColor else glassProps.buttonBorderColor
                    val pairTextCol = if (isSelected) primaryColor else onSurfaceColor

                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                triggerHaptic()
                                selectedPairIndex = idx
                            }
                            .testTag("pair_${pair.unitFrom}_${pair.unitTo}"),
                        shape = RoundedCornerShape(14.dp),
                        customBorderColor = pairBorder,
                        customBgColor = pairBg
                    ) {
                        Text(
                            text = pair.label,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = pairTextCol,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Live Conversion Box
                GlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    borderWidth = 1.2.dp
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
                                color = onSurfaceColor.copy(alpha = 0.7f)
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
                                focusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = glassProps.buttonBorderColor,
                                focusedTextColor = onSurfaceColor,
                                unfocusedTextColor = onSurfaceColor
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ergebnis ($toUnit):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = onSurfaceColor.copy(alpha = 0.7f)
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
