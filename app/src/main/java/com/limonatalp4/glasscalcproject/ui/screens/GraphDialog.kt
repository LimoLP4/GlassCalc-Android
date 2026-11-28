package com.limonatalp4.glasscalcproject.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.components.DialogAnimatedContent
import com.limonatalp4.glasscalcproject.ui.components.FunctionGraphView
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import com.limonatalp4.glasscalcproject.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphDialog(
    viewModel: CalculatorViewModel,
    angleMode: AngleMode,
    onDismiss: () -> Unit
) {
    val glassProps = LocalGlassProperties.current
    val isTiefschwarz = glassProps.isTiefschwarz
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val expression by viewModel.expression.collectAsState()
    val isGestureLocked by viewModel.isGraphLocked.collectAsState()

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    val dialogTransition = rememberDialogTransitionController(onDismiss)

    BasicAlertDialog(
        onDismissRequest = dialogTransition.dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.testTag("dialog_graph")
    ) {
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

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val isCompactVertical = maxHeight < 500.dp || maxWidth > maxHeight
                val adaptiveCanvasHeight = if (isCompactVertical) {
                    (maxHeight * 0.36f).coerceIn(130.dp, 190.dp)
                } else {
                    230.dp
                }
                val dialogMaxWidth = if (maxWidth > maxHeight) 560.dp else 420.dp

                DialogAnimatedContent(visibleState = dialogTransition.visibleState) {
                    GlassSurface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = dialogMaxWidth)
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent()
                                    }
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        customBgColor = if (isTiefschwarz) Color.Black else colorScheme.surfaceContainerHigh,
                        borderStroke = glassProps.containerBorderStroke
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(
                                    horizontal = 20.dp,
                                    vertical = if (isCompactVertical) 14.dp else 20.dp
                                )
                        ) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.title_graph),
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colorScheme.primary
                                )
                                val closeBtnInteraction = remember { MutableInteractionSource() }
                                GlassSurface(
                                    shape = RoundedCornerShape(50),
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
                                            .testTag("btn_close_graph_dialog")
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

                            Spacer(modifier = Modifier.height(if (isCompactVertical) 8.dp else 14.dp))

                            FunctionGraphView(
                                expression = expression,
                                angleMode = angleMode,
                                isGestureLocked = isGestureLocked,
                                onGestureLockChange = { viewModel.setGraphLocked(it) },
                                canvasHeight = adaptiveCanvasHeight,
                                modifier = Modifier.fillMaxWidth(),
                                onApplyPointToCalculator = { x, y ->
                                    viewModel.applyGraphPointToCalculator(x, y)
                                    dialogTransition.dismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
