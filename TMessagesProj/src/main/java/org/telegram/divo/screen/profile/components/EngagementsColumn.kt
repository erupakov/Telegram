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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R.drawable

@Composable
fun EngagementsColumn(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    onLikeClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onStatsClicked: (StatsType) -> Unit,
) {
    Column(modifier = modifier) {
        val backgroundColor = AppTheme.colors.onBackground.copy(alpha = 0.3f)
        val contentColor = if (uiState.userInfo.isLikedByUser) AppTheme.colors.textPrimary else AppTheme.colors.onBackground
        val isFollowed = uiState.userInfo.isFollowed

        // LIKES
        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = if (uiState.userInfo.isLikedByUser) AppTheme.colors.onBackground else backgroundColor,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (uiState.isOwnProfile) {
                    Modifier.clickableWithoutRipple { onStatsClicked(StatsType.LIKES) }
                } else Modifier
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .then(
                            if (!uiState.isOwnProfile) Modifier.clickableWithoutRipple { onLikeClick() }
                            else Modifier
                        ),
                    painter = if (uiState.userInfo.isLikedByUser) painterResource(drawable.ic_divo_favorite_selected) else painterResource(drawable.ic_divo_favorite),
                    contentDescription = null,
                    tint = contentColor,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .weight(1f)
                        .then(
                            if (!uiState.isOwnProfile) Modifier.clickableWithoutRipple { onStatsClicked(StatsType.LIKES) }
                            else Modifier
                        ),
                    text = uiState.userInfo.statistic.likesCount.toShortString(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = contentColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // VIEWS — всегда открывает stats
        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = backgroundColor,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickableWithoutRipple { onStatsClicked(StatsType.VIEWS) }
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(drawable.ic_divo_visibility),
                    contentDescription = null,
                    tint = AppTheme.colors.onBackground
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .weight(1f),
                    text = uiState.userInfo.statistic.viewsCount.toShortString(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = AppTheme.colors.onBackground,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // SAVES
        val bookmarkIconRes = if (isFollowed) drawable.ic_divo_bookmark_glass_selected else drawable.ic_divo_bookmark_glass
        val bookmarkColor = if (isFollowed) AppTheme.colors.textPrimary else AppTheme.colors.onBackground

        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = if (isFollowed) AppTheme.colors.onBackground else backgroundColor,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (uiState.isOwnProfile) {
                    Modifier.clickableWithoutRipple { onStatsClicked(StatsType.SAVES) }
                } else Modifier
            ) {
                Icon(
                    modifier = Modifier
                        .size(16.dp)
                        .then(
                            if (!uiState.isOwnProfile) Modifier.clickableWithoutRipple { onBookmarkClick() }
                            else Modifier
                        ),
                    painter = painterResource(bookmarkIconRes),
                    contentDescription = null,
                    tint = bookmarkColor
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .weight(1f)
                        .then(
                            if (!uiState.isOwnProfile) Modifier.clickableWithoutRipple { onStatsClicked(StatsType.SAVES) }
                            else Modifier
                        ),
                    text = uiState.userInfo.statistic.followersCount.toShortString(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = bookmarkColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
