package com.limonatalp4.glasscalcproject.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limonatalp4.glasscalcproject.ui.theme.GlassThemeProperties
import com.limonatalp4.glasscalcproject.ui.theme.LocalGlassProperties
import kotlin.math.roundToInt

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
    val PillOptionMinHeight: Dp = 56.dp
    val PillOptionExpandedMinHeight: Dp = 74.dp
    val ActionButtonMinHeight: Dp = 48.dp
    val PillHorizontalPadding: Dp = 18.dp
    val PillVerticalPadding: Dp = 10.dp

    val PillOptionPadding = PaddingValues(
        horizontal = PillHorizontalPadding,
        vertical = PillVerticalPadding
    )
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
    fun pillOptionTextColor(
        selected: Boolean,
        enabled: Boolean = true
    ): Color = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
    contentPadding: PaddingValues = GlassDialogDefaults.PillOptionPadding,
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
        targetValue = GlassDialogDefaults.pillOptionTextColor(selected, enabled),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "${animationLabel}_text"
    )

    GlassSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
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
                .padding(contentPadding),
            contentAlignment = Alignment.CenterStart
        ) {
            content(textColor)
        }
    }
}

@Immutable
data class GlassPillTabItem(
    val icon: ImageVector,
    val label: String,
    val testTag: String
)

/**
 * Gemeinsame, horizontal scrollbare Liquid-Glass Pill-Tab-Leiste mit animierter
 * aktiver Auswahl-Pille, geclipptem Ripple und automatischer Zentrierung auf den
 * real verfügbaren Scroll-Grenzen [0 .. maxValue].
 */
@Composable
fun GlassAutoCenterPillTabBar(
    items: List<GlassPillTabItem>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    isTiefschwarz: Boolean,
    modifier: Modifier = Modifier,
    glassProps: GlassThemeProperties = LocalGlassProperties.current,
    fillMaxWidth: Boolean = false,
    minTabHeight: Dp = 36.dp,
    tabPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
    labelFontSize: TextUnit = 12.sp,
    tabSpacing: Dp = 4.dp,
    activeIndicatorColor: Color = MaterialTheme.colorScheme.primaryContainer,
    activeContentColor: Color = MaterialTheme.colorScheme.primary,
    inactiveContentColor: Color = MaterialTheme.colorScheme.primary,
    animationLabelPrefix: String = "pill_tab"
) {
    if (items.isEmpty()) return

    val tabScrollState = rememberScrollState()
    val density = LocalDensity.current
    val tabWidths = remember { mutableStateMapOf<Int, Dp>() }
    val tabHeights = remember { mutableStateMapOf<Int, Dp>() }
    var viewportWidthPx by remember { mutableIntStateOf(0) }

    val safeSelectedIndex = selectedIndex.coerceIn(0, items.lastIndex)
    val resolvedTabHeight = (tabHeights.values.maxOrNull() ?: minTabHeight).coerceAtLeast(minTabHeight)

    val targetPosition = remember(safeSelectedIndex, tabWidths.toMap(), tabSpacing) {
        var pos = 0.dp
        for (i in 0 until safeSelectedIndex) {
            pos += (tabWidths[i] ?: 0.dp) + tabSpacing
        }
        pos
    }
    val targetWidth = tabWidths[safeSelectedIndex] ?: 0.dp

    val indicatorOffset by animateDpAsState(
        targetValue = targetPosition,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "${animationLabelPrefix}_indicator_offset"
    )
    val indicatorWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "${animationLabelPrefix}_indicator_width"
    )
    val activePillBgColor by animateColorAsState(
        targetValue = activeIndicatorColor,
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "${animationLabelPrefix}_active_pill_bg"
    )

    LaunchedEffect(items.size) {
        tabWidths.keys.filter { it >= items.size }.forEach { tabWidths.remove(it) }
        tabHeights.keys.filter { it >= items.size }.forEach { tabHeights.remove(it) }
    }

    LaunchedEffect(
        safeSelectedIndex,
        targetPosition,
        targetWidth,
        viewportWidthPx,
        tabScrollState.maxValue
    ) {
        val maxScrollPx = tabScrollState.maxValue
        val effectiveViewportPx = if (viewportWidthPx > 0) viewportWidthPx else tabScrollState.viewportSize
        if (targetWidth <= 0.dp || effectiveViewportPx <= 0 || maxScrollPx == Int.MAX_VALUE) {
            return@LaunchedEffect
        }
        val tabCenterPx = with(density) { targetPosition.toPx() + targetWidth.toPx() / 2f }
        val centeredScrollPx = (tabCenterPx - effectiveViewportPx / 2f)
            .roundToInt()
            .coerceIn(0, maxScrollPx)
        if (tabScrollState.value != centeredScrollPx) {
            tabScrollState.animateScrollTo(centeredScrollPx)
        }
    }

    val widthModifier = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.wrapContentWidth()
    val currentOnSelectIndex by rememberUpdatedState(onSelectIndex)

    Box(
        modifier = modifier
            .then(widthModifier)
            .clip(GlassDialogDefaults.PillShape)
            .background(GlassDialogDefaults.pillBarBackgroundColor(isTiefschwarz))
            .border(
                border = glassProps.containerBorderStroke,
                shape = GlassDialogDefaults.PillShape
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = widthModifier
                .onSizeChanged { size ->
                    viewportWidthPx = size.width
                }
                .horizontalScroll(tabScrollState)
        ) {
            if (targetWidth > 0.dp) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(indicatorOffset.roundToPx(), 0) }
                        .width(indicatorWidth)
                        .height(resolvedTabHeight)
                        .clip(GlassDialogDefaults.PillShape)
                        .background(activePillBgColor)
                )
            }

            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(tabSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = safeSelectedIndex == index
                    val tabInteractionSource = remember(index, item.testTag) { MutableInteractionSource() }
                    val tabTint by animateColorAsState(
                        targetValue = if (isSelected) activeContentColor else inactiveContentColor,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "${animationLabelPrefix}_${index}_tint"
                    )

                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = resolvedTabHeight)
                            .onSizeChanged { size ->
                                tabWidths[index] = with(density) { size.width.toDp() }
                            }
                            .pressScale(tabInteractionSource)
                            .clip(GlassDialogDefaults.PillShape)
                            .clickable(
                                interactionSource = tabInteractionSource,
                                indication = ripple(bounded = true),
                                role = Role.Tab,
                                onClick = { currentOnSelectIndex(index) }
                            )
                            .padding(tabPadding)
                            .testTag(item.testTag),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.onSizeChanged { contentSize ->
                                val contentHeight = with(density) { contentSize.height.toDp() }
                                val verticalPad = tabPadding.calculateTopPadding() + tabPadding.calculateBottomPadding()
                                tabHeights[index] = (contentHeight + verticalPad).coerceAtLeast(minTabHeight)
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = tabTint,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.label,
                                fontSize = labelFontSize,
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
    }
}
