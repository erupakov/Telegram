package org.telegram.divo.screen.event_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun EventListTopBar(
    isModel: Boolean,
    onSearchClick: () -> Unit,
    onAddEventClick: () -> Unit,
) {
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 56.dp

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
            Text(
                modifier = Modifier.weight(1f).offset(y = 4.dp),
                text = stringResource(R.string.EventList).uppercase(),
                color = AppTheme.colors.textPrimary,
                style = AppTheme.typography.displayLarge
            )

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
}