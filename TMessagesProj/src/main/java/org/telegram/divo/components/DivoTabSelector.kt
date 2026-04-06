package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme

data class TabConfig(
    val id: String,
    @field:StringRes val textResId: Int? = null,
    @field:DrawableRes val iconResId: Int? = null,
    val contentDescription: String? = null,
    val enabled: Boolean = true
)

@Composable
fun DivoTabSelector(
    modifier: Modifier = Modifier,
    tabs: List<TabConfig>,
    selectedIndex: Int,
    containerColor: Color = AppTheme.colors.onBackground,
    activeTabColor: Color = AppTheme.colors.backgroundLight,
    selectedContentColor: Color = AppTheme.colors.textPrimary,
    unselectedContentColor: Color = AppTheme.colors.textPrimary,
    height: Dp = 32.dp,
    cornerRadius: Dp = 99.dp,
    innerPadding: Dp = 2.dp,
    horizontalPadding: Dp = 16.dp,
    animationSpec: AnimationSpec<Dp> = spring(
        stiffness = Spring.StiffnessMediumLow,
    ),
    onTabSelected: (Int) -> Unit,
) {
    val density = LocalDensity.current
    val tabPositions = remember { mutableStateListOf<Rect>() }

    LaunchedEffect(tabs.size) {
        tabPositions.clear()
        repeat(tabs.size) { tabPositions.add(Rect.Zero) }
    }

    val targetOffset = if (selectedIndex in tabPositions.indices) {
        with(density) { tabPositions[selectedIndex].left.toDp() }
    } else 0.dp

    val targetWidth = if (selectedIndex in tabPositions.indices) {
        with(density) { tabPositions[selectedIndex].width.toDp() }
    } else 0.dp

    val animatedOffset by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = animationSpec,
        label = "tab_offset"
    )

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = animationSpec,
        label = "tab_width",
    )

    Box(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .padding(innerPadding)
    ) {
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(animatedWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(cornerRadius - innerPadding))
                .background(activeTabColor)
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(cornerRadius - innerPadding),
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    spotColor = Color.Black.copy(alpha = 0.1f)
                )
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = selectedIndex == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            enabled = tab.enabled,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onTabSelected(index)
                        }
                        .onGloballyPositioned { coordinates ->
                            if (index < tabPositions.size) {
                                tabPositions[index] = coordinates.boundsInParent()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        tab.iconResId?.let { resId ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = tab.contentDescription,
                                modifier = Modifier.size(16.dp),
                                colorFilter = ColorFilter.tint(
                                    if (isSelected) selectedContentColor else unselectedContentColor
                                )
                            )
                        }

                        tab.textResId?.let { resId ->
                            Text(
                                modifier = Modifier.padding(top = 2.dp),
                                text = stringResource(id = resId),
                                color = if (isSelected) selectedContentColor else unselectedContentColor,
                                fontSize = 10.sp,
                                style = AppTheme.typography.helveticaNeueLtCom,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}