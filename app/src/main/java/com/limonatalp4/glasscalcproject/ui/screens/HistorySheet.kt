package com.limonatalp4.glasscalcproject.ui.screens

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.limonatalp4.glasscalcproject.R
import com.limonatalp4.glasscalcproject.data.model.AppTheme
import com.limonatalp4.glasscalcproject.data.model.CalculationHistoryItem
import com.limonatalp4.glasscalcproject.ui.components.GlassSurface
import com.limonatalp4.glasscalcproject.ui.components.pressScale
import com.limonatalp4.glasscalcproject.ui.components.rememberDialogTransitionController
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import com.limonatalp4.glasscalcproject.ui.util.HapticUtil
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticFeedbackEnabled
import com.limonatalp4.glasscalcproject.ui.util.LocalHapticIntensity
import io.github.nadeemiqbal.liquidglass.LiquidGlassState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistorySheet(
    history: List<CalculationHistoryItem>,
    @Suppress("UNUSED_PARAMETER") currentTheme: AppTheme,
    glassState: LiquidGlassState? = null,
    onSelect: (CalculationHistoryItem) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val hapticFeedbackEnabled = LocalHapticFeedbackEnabled.current
    val hapticIntensity = LocalHapticIntensity.current
    val context = LocalContext.current
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    fun triggerHaptic() {
        HapticUtil.performAppHaptic(context, view, hapticFeedbackEnabled, hapticIntensity)
    }

    val glassProps = LocalGlassProperties.current
    val isAmoled = glassProps.isAmoled
    val pillShape = RoundedCornerShape(32.dp)
    val pillBackgroundColor = if (isAmoled) {
        Color(0xFF0A0A0A).copy(alpha = 0.85f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }
    val pillBorder = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.3f))
    val titleBadgeBackground = if (isAmoled) {
        Color(0xFF141414).copy(alpha = 0.8f)
    } else {
        colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val itemCardBackground = if (isAmoled) {
        Color(0xFF141414).copy(alpha = 0.8f)
    } else {
        colorScheme.surface.copy(alpha = 0.6f)
    }
    val itemCardBorder = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.25f))

    val transitionController = rememberDialogTransitionController(onDismiss)
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val animatedDragOffsetPx by animateFloatAsState(
        targetValue = dragOffsetPx,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "history_pill_drag_offset"
    )
    val dismissThresholdPx = with(density) { 72.dp.toPx() }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (transitionController.visibleState.targetState) {
            if (glassProps.isDark) 0.45f else 0.25f
        } else {
            0f
        },
        animationSpec = tween(durationMillis = 200),
        label = "history_scrim_alpha"
    )

    BackHandler(enabled = transitionController.visibleState.targetState) {
        transitionController.dismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(15f)
    ) {
        // Dimmed outside-tap scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    transitionController.dismiss()
                }
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                        .union(WindowInsets.navigationBars)
                        .union(WindowInsets.displayCutout)
                        .union(WindowInsets.ime)
                )
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE ||
                maxWidth > maxHeight
            val isCompactLandscape = isLandscape && maxHeight < 560.dp

            val panelHeightModifier = if (isCompactLandscape) {
                Modifier.heightIn(max = maxHeight * 0.80f)
            } else {
                Modifier.height(maxHeight * 0.55f)
            }

            AnimatedVisibility(
                visibleState = transitionController.visibleState,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200)
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { it / 3 },
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 150)
                ) + scaleOut(
                    targetScale = 0.95f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutLinearInEasing)
                )
            ) {
                GlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .then(panelHeightModifier)
                        .offset { IntOffset(0, animatedDragOffsetPx.roundToInt()) }
                        .animateContentSize(
                            animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
                        )
                        .pointerInput(Unit) {
                            // Block taps inside the floating pill from triggering the outside scrim
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                }
                            }
                        },
                    glassState = glassState,
                    shape = pillShape,
                    customBgColor = pillBackgroundColor,
                    borderStroke = pillBorder
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isCompactLandscape) Modifier else Modifier.fillMaxHeight()
                            )
                            .padding(horizontal = 20.dp)
                            .padding(bottom = if (isCompactLandscape) 16.dp else 20.dp)
                            .testTag("history_sheet")
                    ) {
                        // Top Drag Pill Handle (swipe down to dismiss)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onDragEnd = {
                                            if (dragOffsetPx >= dismissThresholdPx) {
                                                transitionController.dismiss()
                                            }
                                            dragOffsetPx = 0f
                                        },
                                        onDragCancel = {
                                            dragOffsetPx = 0f
                                        },
                                        onVerticalDrag = { _, dragAmount ->
                                            dragOffsetPx = (dragOffsetPx + dragAmount).coerceAtLeast(0f)
                                        }
                                    )
                                }
                                .padding(vertical = if (isCompactLandscape) 10.dp else 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(42.dp)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                            )
                        }

                        // Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pill Badge Title
                            GlassSurface(
                                shape = RoundedCornerShape(50),
                                customBgColor = titleBadgeBackground,
                                borderStroke = pillBorder
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.title_history),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colorScheme.primary
                                    )
                                }
                            }

                            if (history.isNotEmpty()) {
                                val clearHistoryInteraction = remember { MutableInteractionSource() }
                                OutlinedButton(
                                    onClick = {
                                        triggerHaptic()
                                        onClear()
                                    },
                                    interactionSource = clearHistoryInteraction,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colorScheme.error
                                    ),
                                    shape = RoundedCornerShape(50.dp),
                                    border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .pressScale(clearHistoryInteraction)
                                        .testTag("btn_clear_history")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(R.string.btn_history_clear_desc),
                                            tint = colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = stringResource(R.string.btn_history_clear),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactLandscape) 12.dp else 16.dp))

                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (isCompactLandscape) {
                                            Modifier
                                                .verticalScroll(rememberScrollState())
                                                .padding(vertical = 24.dp)
                                        } else {
                                            Modifier
                                                .weight(1f, fill = true)
                                                .verticalScroll(rememberScrollState())
                                                .padding(vertical = 32.dp)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.history_empty),
                                        fontSize = 15.sp,
                                        color = colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                            ) {
                                items(history, key = { it.id }) { item ->
                                    val itemInteraction = remember { MutableInteractionSource() }
                                    GlassSurface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .pressScale(itemInteraction)
                                            .clickable(
                                                interactionSource = itemInteraction,
                                                indication = ripple()
                                            ) {
                                                triggerHaptic()
                                                onSelect(item)
                                                transitionController.dismiss()
                                            }
                                            .testTag("history_item_${item.id}"),
                                        shape = RoundedCornerShape(22.dp),
                                        borderStroke = itemCardBorder,
                                        customBgColor = itemCardBackground
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 18.dp, vertical = 14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = item.expression,
                                                    fontSize = 16.sp,
                                                    color = colorScheme.onSurface.copy(alpha = 0.85f),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = dateFormat.format(Date(item.timestamp)),
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "= ${item.result}",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colorScheme.primary,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.End
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
