package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AnimatedPortfolioAddButton(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    showAddButton: Boolean,
    isUploading: Boolean,
    onMediaSelected: (Uri) -> Unit,
    onEventCreate: () -> Unit,
) {
    var frozenPage by remember { mutableIntStateOf(pagerState.currentPage) }

    LaunchedEffect(showAddButton, pagerState.currentPage) {
        if (showAddButton) frozenPage = pagerState.currentPage
    }

    Box(
        modifier = modifier
            .zIndex(4f)
            .padding(
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 12.dp
            )
    ) {
        AnimatedVisibility(
            visible = showAddButton,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            PortfolioAddButton(
                currentPage = frozenPage,
                isUploading = isUploading,
                onMediaSelected = onMediaSelected,
                onEventCreate = onEventCreate,
            )
        }
    }
}

@Composable
private fun PortfolioAddButton(
    modifier: Modifier = Modifier,
    currentPage: Int,
    isUploading: Boolean,
    onMediaSelected: (Uri) -> Unit,
    onEventCreate: () -> Unit,
) {
    val isEventPage = currentPage == 4
    val isVideo = currentPage == 1

    val text = when (currentPage) {
        0 -> stringResource(R.string.UploadPhotos)
        1 -> stringResource(R.string.UploadVideos)
        else -> stringResource(R.string.AddEvent)
    }

    val iconRes = if (isEventPage) R.drawable.ic_divo_add else R.drawable.ic_divo_add_a_photo

    val iconSize = if (isEventPage) 16.dp else 24.dp

    val openGallery = rememberGalleryLauncher(isVideo) { uri -> onMediaSelected(uri) }

    val handleClick: () -> Unit = if (isEventPage) onEventCreate else openGallery

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.accentOrange)
                .padding(horizontal = 16.dp)
                .clickableWithoutRipple { handleClick() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isUploading) {
                LottieProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppTheme.colors.onBackground
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    modifier = Modifier,
                    text = if (isVideo) stringResource(R.string.UploadingVideos) else stringResource(R.string.UploadingPhotos),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onBackground
                )
            } else {
                Image(
                    modifier = Modifier.size(iconSize),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    modifier = Modifier,
                    text = text,
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onBackground
                )
            }
        }
    }
}