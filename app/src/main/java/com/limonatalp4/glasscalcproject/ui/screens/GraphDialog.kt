package com.limonatalp4.glasscalcproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AngleMode
import com.limonatalp4.glasscalcproject.ui.components.FunctionGraphView
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
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

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_graph")
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
                        color = colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            triggerHaptic()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("btn_close_graph_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.nav_close),
                            tint = colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FunctionGraphView(
                    expression = expression,
                    angleMode = angleMode,
                    isGestureLocked = isGestureLocked,
                    onGestureLockChange = { viewModel.setGraphLocked(it) },
                    modifier = Modifier.fillMaxWidth(),
                    onApplyPointToCalculator = { x, y ->
                        viewModel.applyGraphPointToCalculator(x, y)
                        onDismiss()
                    }
                )
            }
        }
    }
}
