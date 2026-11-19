package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties

/**
 * Gemeinsame Liquid-Glass Design-Konstanten und Bausteine für Dialoge
 * (u. a. Einstellungen und Einheitenumrechnung), um Pill-Shapes, Ränder,
 * Kontraste und Innenabstände app-weit konsistent zu halten.
 */
object GlassDialogDefaults {
    val DialogShellShape: Shape = RoundedCornerShape(32.dp)
    val LargeCardShape: Shape = RoundedCornerShape(32.dp)
    val SettingsCardShape: Shape = RoundedCornerShape(36.dp)
    val NestedPanelShape: Shape = RoundedCornerShape(28.dp)
    val SearchEmptyStateShape: Shape = RoundedCornerShape(24.dp)
    val PillShape: Shape = CircleShape

    val SearchBarHeight: Dp = 48.dp
    val PillOptionMinHeight: Dp = 52.dp
    val ActionButtonMinHeight: Dp = 48.dp
    val PillHorizontalPadding: Dp = 20.dp

    val CardContentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    val SettingsCardContentPadding = PaddingValues(horizontal = 24.dp, vertical = 22.dp)

    @Composable
    fun cardBackgroundColor(): Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    @Composable
    fun pillBarBackgroundColor(isTiefschwarz: Boolean): Color =
        if (isTiefschwarz) Color.Black else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    @Composable
    fun searchBarBackgroundColor(isTiefschwarz: Boolean): Color =
        if (isTiefschwarz) Color(0xFF0A0A0A) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)

    @Composable
    fun pillOptionBackgroundColor(selected: Boolean): Color = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    @Composable
    fun pillOptionBorderColor(
        selected: Boolean,
        glassProps: GlassThemeProperties
    ): Color = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        glassProps.buttonBorderColor
    }

    @Composable
    fun pillOptionTextColor(selected: Boolean): Color = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}

@Composable
fun GlassCardContainer(
    glassProps: GlassThemeProperties,
    modifier: Modifier = Modifier,
    shape: Shape = GlassDialogDefaults.LargeCardShape,
    contentPadding: PaddingValues = GlassDialogDefaults.CardContentPadding,
    backgroundColor: Color = GlassDialogDefaults.cardBackgroundColor(),
    borderStroke: BorderStroke = glassProps.containerBorderStroke,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    role: Role? = null,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        customBgColor = backgroundColor,
        borderStroke = borderStroke,
        onClick = onClick,
        enabled = enabled,
        role = role,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun GlassPillOptionContainer(
    selected: Boolean,
    onClick: () -> Unit,
    glassProps: GlassThemeProperties,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    minHeight: Dp = GlassDialogDefaults.PillOptionMinHeight,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = GlassDialogDefaults.PillHorizontalPadding,
        vertical = 12.dp
    ),
    animationLabel: String = "pill_option",
    content: @Composable (textColor: Color) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val cardBg by animateColorAsState(
        targetValue = GlassDialogDefaults.pillOptionBackgroundColor(selected),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "${animationLabel}_bg"
    )

    val cardBorder by animateColorAsState(
        targetValue = GlassDialogDefaults.pillOptionBorderColor(selected, glassProps),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "${animationLabel}_border"
    )

    val textColor by animateColorAsState(
        targetValue = GlassDialogDefaults.pillOptionTextColor(selected),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "${animationLabel}_text"
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight)
            .alpha(if (enabled) 1.0f else 0.45f)
            .pressScale(interactionSource, enabled = enabled),
        shape = GlassDialogDefaults.PillShape,
        borderWidth = if (selected) 1.5.dp else 1.dp,
        customBorderColor = cardBorder,
        customBgColor = cardBg,
        onClick = onClick,
        selected = selected,
        enabled = enabled,
        role = Role.RadioButton,
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding)
        ) {
            content(textColor)
        }
    }
}
