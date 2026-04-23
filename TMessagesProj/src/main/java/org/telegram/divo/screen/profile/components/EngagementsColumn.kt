package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R.drawable

@Composable
fun EngagementsColumn(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onLikesClick: (Int, Boolean) -> Unit,
    onViewsClick: () -> Unit,
    onMarkClick: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
) {
    Column(modifier = modifier) {
        val user = uiState.statistic
        val backgroundColor = AppTheme.colors.onBackground.copy(alpha = 0.3f)//if (feed.isLiked) AppTheme.colors.onBackground else AppTheme.colors.onBackground.copy(alpha = 0.3f)
        val contentColor = AppTheme.colors.onBackground//if (feed.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground

        RoundedGlassContainer(
            modifier = Modifier
                .width(63.dp),
            height = 30.dp,
            background = Color.Red,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .clickableWithoutRipple {
                            if (uiState.isOwnProfile) onStatsClicked(StatsType.LIKES) else onLikesClick(uiState.userInfo.statistic.followersCount, uiState.userInfo.isFavorite)
                        },
                    painter = painterResource(drawable.ic_divo_favorite),//if (feed.isLiked) painterResource(drawable.ic_divo_favorite_selected) else painterResource(drawable.ic_divo_favorite),
                    contentDescription = null,
                    tint = contentColor,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .weight(1f)
                        .clickableWithoutRipple { onStatsClicked(StatsType.LIKES) },
                    text = "----",//feed.likesCount.toShortString(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = contentColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = Color.Red,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(drawable.ic_divo_visibility),
                contentDescription = null,
                tint = AppTheme.colors.onBackground
            )
            Spacer(Modifier.width(3.dp))
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = "----",
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.onBackground,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = Color.Red,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(drawable.ic_divo_bookmark_glass),
                contentDescription = null,
                tint = AppTheme.colors.onBackground
            )
            Spacer(Modifier.width(3.dp))
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = "----",
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.onBackground,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}