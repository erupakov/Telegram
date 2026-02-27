package org.telegram.divo.screen.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.profile.UserStatistic
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R.*

@Composable
fun EngagementStatsRow(
    modifier: Modifier = Modifier,
    stats: UserStatistic,
    onClicked: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .height(36.dp)
                .clickableWithoutRipple(onClicked)
                .clip(RoundedCornerShape(6.dp))
                .background(AppTheme.colors.blackAlpha12)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(drawable.ic_divo_send),
                tint = Color.White,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = "Send DM",
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 13.sp,
                color = Color.White
            )
        }

        EngagementStatsItem(
            count = stats.likes,
            text = "Like",
            iconResId = drawable.ic_divo_favorite,
            onStatsClicked = {
                onStatsClicked(StatsType.LIKES)
            }
        )
        EngagementStatsItem(
            count = stats.views,
            text = "Viewed",
            iconResId = drawable.ic_divo_visibility,
            onStatsClicked = {
                onStatsClicked(StatsType.VIEWS)
            }
        )
        EngagementStatsItem(
            count = stats.saves,
            text = "Save",
            iconResId = drawable.ic_divo_bookmark,
            innerPadding = 0.dp,
            onStatsClicked = {
                onStatsClicked(StatsType.SAVES)
            }
        )
    }
}

@Composable
private fun EngagementStatsItem(
    modifier: Modifier = Modifier,
    count: Int,
    text: String,
    @DrawableRes iconResId: Int,
    innerPadding: Dp = 2.dp,
    onStatsClicked: () -> Unit
) {
    Row(
        modifier = modifier.clickableWithoutRipple(onStatsClicked),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(iconResId),
            tint = Color.White,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(innerPadding))
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = count.toString(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            modifier = Modifier.padding(top = 2.dp),
            text = text,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 10.sp,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun EngagementStatsRowPreview() {
    AppTheme {
        EngagementStatsRow(
            stats = UserStatistic(
                11, 200, 150, 12, 2,
            ),
            onClicked = {},
            onStatsClicked = {},
        )
    }
}