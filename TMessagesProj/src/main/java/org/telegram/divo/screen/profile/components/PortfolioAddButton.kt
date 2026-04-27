package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
fun PortfolioAddButton(
    modifier: Modifier = Modifier,
    isUploading: Boolean,
    isVideo: Boolean = false,
    onMediaSelected: (Uri) -> Unit,
) {
    val openGallery = rememberGalleryLauncher(isVideo) { uri ->
        onMediaSelected(uri)
    }

    Box(
        modifier = modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(40.dp)
                .shadow(elevation = 8.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(AppTheme.colors.accentOrange)
                .padding(horizontal = 16.dp)
                .clickableWithoutRipple { openGallery() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isUploading) {
                LottieProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = AppTheme.colors.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier.offset(y = 1.dp),
                    text = if (isVideo) stringResource(R.string.UploadingVideos) else stringResource(R.string.UploadingPhotos),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onBackground
                )
            } else {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_divo_add_a_photo),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    modifier = Modifier.offset(y = 1.dp),
                    text = if (isVideo) stringResource(R.string.UploadVideos) else stringResource(R.string.UploadPhotos),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 16.sp,
                    color = AppTheme.colors.onBackground
                )
            }
        }
    }
}