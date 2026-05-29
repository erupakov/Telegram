package org.telegram.divo.screen.event_list.components

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.DivoTabSelector
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.TabConfig
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EventListTopBar(
    isModel: Boolean,
    isAgency: Boolean = false,
    selectedTabIndex: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 56.dp
    val bgColor = AppTheme.colors.backgroundLight

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
        ) {
        Box(
            modifier = Modifier
                .height(statusBarHeight + topBarHeight)
                .fillMaxWidth()
                .padding(top = statusBarHeight),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.divo_logo),
                    contentDescription = "DIVO",
                    modifier = Modifier.height(36.dp).offset(y = 6.dp),
                    colorFilter = ColorFilter.tint(AppTheme.colors.textPrimary)
                )

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppTheme.colors.onBackground)
                ) {
                    RoundedButton(
                        modifier = Modifier,
                        resId = R.drawable.ic_divo_search_24,
                        iconSize = 24.dp,
                        paddingEnd = 0.dp,
                        shadowEnabled = false,
                        onClick = onSearchClick
                    )
                    if (!isModel) {
                        RoundedButton(
                            modifier = Modifier,
                            resId = R.drawable.msg_add,
                            iconSize = 24.dp,
                            paddingEnd = 0.dp,
                            shadowEnabled = false,
                            onClick = onAddEventClick,
                        )
                    }
                }
            }
        }

        // Tabs — only for agency
        if (isAgency) {
            val tabs = remember {
                listOf(
                    TabConfig("MY_EVENTS", textResId = R.string.EventTabMyEvents),
                    TabConfig("ALL_EVENTS", textResId = R.string.EventTabAllEvents),
                )
            }
            DivoTabSelector(
                modifier = Modifier
                    .fillMaxWidth(),
                tabs = tabs,
                selectedIndex = selectedTabIndex,
                onTabSelected = onTabSelected,
                horizontalPadding = 16.dp,
            )
        }

        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            bgColor,
                            bgColor.copy(alpha = 0.8f),
                            bgColor.copy(alpha = 0.5f),
                            bgColor.copy(alpha = 0.2f),
                            bgColor.copy(alpha = 0f)
                        )
                    )
                )
        )
    }
}