package com.limonatalp4.glasscalcproject.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke

@Immutable
data class MathConstant(
    val symbol: String,
    val nameResId: Int,
    val value: String,
    val unit: String
)

val MATH_CONSTANTS = listOf(
    MathConstant("π", R.string.const_pi, "3.141592653589793", ""),
    MathConstant("e", R.string.const_e, "2.718281828459045", ""),
    MathConstant("c", R.string.const_c, "299792458", "m/s"),
    MathConstant("g", R.string.const_g, "9.80665", "m/s²"),
    MathConstant("h", R.string.const_h, "6.62607015E-34", "J·s"),
    MathConstant("G", R.string.const_G, "6.67430E-11", "m³/(kg·s²)"),
    MathConstant("Na", R.string.const_na, "6.02214076E23", "mol⁻¹"),
    MathConstant("k", R.string.const_k, "1.380649E-23", "J/K")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstantsDialog(
    onSelectConstant: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassProps = LocalGlassProperties.current
    val isTiefschwarz = glassProps.isTiefschwarz
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val context = LocalContext.current
    val view = LocalView.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    val dialogTransition = rememberDialogTransitionController(onDismiss)

    BasicAlertDialog(
        onDismissRequest = dialogTransition.dismiss,
        modifier = Modifier.testTag("dialog_constants")
    ) {
        DialogAnimatedContent(visibleState = dialogTransition.visibleState) {
            GlassSurface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(28.dp),
                customBgColor = if (isTiefschwarz) Color.Black else colorScheme.surfaceContainerHigh,
                borderStroke = glassProps.containerBorderStroke
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Functions,
                                contentDescription = null,
                                tint = colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = stringResource(R.string.title_constants),
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTiefschwarz) Color.White else colorScheme.onSurface
                            )
                        }

                        val closeBtnInteraction = remember { MutableInteractionSource() }
                        IconButton(
                            onClick = {
                                triggerHaptic()
                                dialogTransition.dismiss()
                            },
                            interactionSource = closeBtnInteraction,
                            modifier = Modifier
                                .pressScale(closeBtnInteraction)
                                .testTag("btn_close_constants")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.nav_close),
                                tint = if (isTiefschwarz) Color.White else colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.constants_select_heading),
                        fontSize = 13.sp,
                        color = if (isTiefschwarz) Color.White.copy(alpha = 0.7f) else colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                    ) {
                        items(MATH_CONSTANTS, key = { it.symbol }) { constant ->
                            val cardInteraction = remember { MutableInteractionSource() }
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pressScale(cardInteraction)
                                    .clickable(
                                        interactionSource = cardInteraction,
                                        indication = ripple()
                                    ) {
                                        triggerHaptic()
                                        onSelectConstant(constant.value)
                                        dialogTransition.dismiss()
                                    }
                                    .testTag("constant_${constant.symbol}"),
                                shape = RoundedCornerShape(20.dp),
                                customBgColor = if (isTiefschwarz) Color(0xFF141414).copy(alpha = 0.85f) else colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                borderStroke = glassProps.containerBorderStroke
                            ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Symbol Pill Badge
                                    GlassSurface(
                                        shape = RoundedCornerShape(12.dp),
                                        borderWidth = 1.dp,
                                        customBgColor = colorScheme.primaryContainer,
                                        customBorderColor = colorScheme.primary
                                    ) {
                                        Text(
                                            text = constant.symbol,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = stringResource(constant.nameResId),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = colorScheme.onSurface
                                        )
                                        if (constant.unit.isNotEmpty()) {
                                            Text(
                                                text = constant.unit,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                    Text(
                                        text = constant.value,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = colorScheme.primary,
                                        fontWeight = FontWeight.Medium
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

