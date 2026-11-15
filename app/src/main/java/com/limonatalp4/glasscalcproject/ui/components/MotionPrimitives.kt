package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * GPU-effizienter Press-Effekt (1.0f -> 0.95f) über graphicsLayer,
 * ohne Layout-Passes auf älteren Geräten auszulösen.
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.95f,
    enabled: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) pressedScale else 1.0f,
        animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing),
        label = "press_scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Stable
class DialogTransitionController(
    val visibleState: MutableTransitionState<Boolean>,
    val dismiss: () -> Unit
)

@Composable
fun rememberDialogTransitionController(
    onDismiss: () -> Unit
): DialogTransitionController {
    val visibleState = remember { MutableTransitionState(false) }
    var isDismissing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val currentOnDismiss by rememberUpdatedState(onDismiss)

    LaunchedEffect(Unit) {
        visibleState.targetState = true
    }

    val dismiss: () -> Unit = remember {
        {
            if (!isDismissing) {
                isDismissing = true
                visibleState.targetState = false
                coroutineScope.launch {
                    snapshotFlow { visibleState.isIdle && !visibleState.currentState }
                        .first { it }
                    currentOnDismiss()
                }
            }
        }
    }

    return remember(visibleState, dismiss) {
        DialogTransitionController(visibleState = visibleState, dismiss = dismiss)
    }
}

@Composable
fun DialogAnimatedContent(
    visibleState: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = fadeIn(animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)) +
            scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            ),
        exit = fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)) +
            scaleOut(
                targetScale = 0.92f,
                animationSpec = tween(durationMillis = 150, easing = FastOutLinearInEasing)
            )
    ) {
        content()
    }
}
