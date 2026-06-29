package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun PortfolioEmptyAddButton(
    modifier: Modifier = Modifier,
    isUploading: Boolean,
    isVideo: Boolean = false,
    onMediaSelected: (Uri) -> Unit,
) {
    val openGallery = rememberGalleryLauncher(isVideo) { uri ->
        onMediaSelected(uri)
    }

    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clickableWithoutRipple { openGallery() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isUploading) {
            LottieProgressIndicator(
                modifier = Modifier.size(34.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier.offset(y = 1.dp),
                text = if (isVideo) stringResource(R.string.UploadingVideos) else stringResource(R.string.UploadingPhotos),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 12.sp,
                color = AppTheme.colors.textPrimary
            )
        } else {
            androidx.compose.material3.Icon(
                modifier = Modifier.size(34.dp),
                painter = painterResource(R.drawable.ic_divo_add_photo),
                tint = AppTheme.colors.accentOrange,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                modifier = Modifier,
                text = if (isVideo) stringResource(R.string.UploadVideos) else stringResource(R.string.UploadPhotos),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 12.sp,
                color = AppTheme.colors.textPrimary
            )
        }
    }
}

@Composable
fun EmptyMediaPlaceholder(text: String, isVideo: Boolean, transitionProgress: Float, topPadding: androidx.compose.ui.unit.Dp) {
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val startOffset = topPadding + 16.dp
        val endOffset = maxHeight / 2 - 100.dp
        val currentOffset = startOffset + (endOffset - startOffset) * transitionProgress

        Column(
            modifier = Modifier
                .offset(y = currentOffset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.textPrimary.copy(0.1f))
            ) {
                androidx.compose.material3.Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    painter = painterResource(if (isVideo) R.drawable.ic_action_play else R.drawable.ic_divo_add_photo),
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = text,
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
        }
    }
}