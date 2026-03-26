package org.telegram.divo.screen.models.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont.HelveticaNeue

@OptIn(ExperimentalTextApi::class)
@Composable
fun TabsRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF000000),
    inactiveColor: Color = Color(0xFFBEBEBE)
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val style = AppTheme.typography.textButtonSmall.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )

    val tabWidths = remember(tabs) {
        tabs.map {
            with(density) {
                textMeasurer.measure(it, style).size.width.toDp()
            }
        }
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = AppTheme.colors.backgroundNew,
        contentColor = activeColor,
        indicator = { tabPositions ->
            val targetIndicatorWidth = tabWidths[selectedIndex]
            val animatedIndicatorWidth by animateDpAsState(
                targetValue = targetIndicatorWidth,
                label = "indicatorWidth"
            )

            val currentTabPosition = tabPositions[selectedIndex]
            val targetIndicatorOffset =
                currentTabPosition.left + (currentTabPosition.width - targetIndicatorWidth) / 2
            val animatedIndicatorOffset by animateDpAsState(
                targetValue = targetIndicatorOffset,
                label = "indicatorOffset"
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.BottomStart)
                    .offset(x = animatedIndicatorOffset)
                    .width(animatedIndicatorWidth)
                    .height(2.dp)
                    .background(color = activeColor)
            )
        },
        divider = {
            TabRowDefaults.Divider(
                thickness = 1.dp,
                color = Color(0x11000000)
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontFamily = HelveticaNeue,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                selectedContentColor = activeColor,
                unselectedContentColor = inactiveColor
            )
        }
    }
}