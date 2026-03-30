package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
            .fillMaxWidth()
            .padding(vertical = 65.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(140.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppTheme.colors.blackAlpha12)
                .clickableWithoutRipple { openGallery() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUploading) {
                LottieProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isVideo) stringResource(R.string.UploadingVideos) else stringResource(R.string.UploadingPhotos),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = Color.White
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_divo_add_a_photo),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isVideo) stringResource(R.string.UploadVideos) else stringResource(R.string.UploadPhotos),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}