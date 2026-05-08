package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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