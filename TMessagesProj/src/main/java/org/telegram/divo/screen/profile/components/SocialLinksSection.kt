package org.telegram.divo.screen.profile.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SocialLinksSection(
    instagram: String,
    tiktok: String,
    youtube: String,
    website: String,
    isOwnProfile: Boolean,
    onEditLinksClicked: () -> Unit,
    onSocialLinkClicked: (SocialNetworkType) -> Unit
) {
    val filledLinksCount = listOf(instagram, tiktok, youtube, website).count { it.isNotBlank() }

    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (isOwnProfile && filledLinksCount != 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.MyLinks), color = AppTheme.colors.textPrimary.copy(0.6f), fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                Text(
                    modifier = Modifier.clickableWithoutRipple { onEditLinksClicked() },
                    text = stringResource(R.string.EditLinks).uppercase(),
                    color = AppTheme.colors.textPrimary,
                    fontSize = 12.sp
                )
            }
        }

        if (filledLinksCount != 0) {
            if (isOwnProfile) Spacer(modifier = Modifier.height(12.dp)) else Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (instagram.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_instagram,
                        username = instagram,
                        filledLinksCount = filledLinksCount,
                        onClick = { onSocialLinkClicked(SocialNetworkType.INSTAGRAM) }
                    )
                }
                if (tiktok.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_tiktok,
                        username = tiktok,
                        filledLinksCount = filledLinksCount,
                        onClick = { onSocialLinkClicked(SocialNetworkType.TIKTOK) }
                    )
                }
                if (youtube.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_youtube,
                        username = youtube,
                        filledLinksCount = filledLinksCount,
                        onClick = { onSocialLinkClicked(SocialNetworkType.YOUTUBE) }
                    )
                }
                if (website.isNotBlank()) {
                    SocialLinkItem(
                        iconResId = R.drawable.icon_divo_web,
                        username = website,
                        filledLinksCount = filledLinksCount,
                        onClick = { onSocialLinkClicked(SocialNetworkType.WEBSITE) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.SocialLinkItem(
    @DrawableRes iconResId: Int,
    username: String,
    filledLinksCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (filledLinksCount == 1) {
        Box(
            modifier = modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.onBackground)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = username,
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.onBackground)
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = AppTheme.colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = username,
                    style = AppTheme.typography.helveticaNeueRegular,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}