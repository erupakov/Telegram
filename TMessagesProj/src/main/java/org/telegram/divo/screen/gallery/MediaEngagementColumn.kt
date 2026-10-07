package org.telegram.divo.screen.gallery

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
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.inputs.RoundedGlassContainer
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R.drawable

/**
 * Likes and views of one photo / video, styled like the profile's EngagementsColumn.
 * The like toggles only on someone else's media; views are shown only when the backend reports them.
 */
@Composable
fun MediaEngagementColumn(
    item: GalleryItem,
    canLike: Boolean,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        val backgroundColor = AppTheme.colors.backgroundDark.copy(alpha = 0.4f)
        val likeContentColor = if (item.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground

        // LIKES
        RoundedGlassContainer(
            modifier = Modifier.width(63.dp),
            height = 30.dp,
            background = if (item.isLiked) AppTheme.colors.onBackground else backgroundColor,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (canLike) Modifier.clickableWithoutRipple { onLikeClick() } else Modifier
            ) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(if (item.isLiked) drawable.ic_divo_favorite_selected else drawable.ic_divo_favorite),
                    contentDescription = null,
                    tint = likeContentColor,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    modifier = Modifier
                        .offset(y = 1.dp)
                        .weight(1f),
                    text = item.likesCount.toShortString(),
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = likeContentColor,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // VIEWS
        val views = item.viewsCount
        if (views != null) {
            Spacer(Modifier.height(10.dp))
            RoundedGlassContainer(
                modifier = Modifier.width(63.dp),
                height = 30.dp,
                background = backgroundColor,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        text = views.toShortString(),
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = AppTheme.colors.onBackground,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
