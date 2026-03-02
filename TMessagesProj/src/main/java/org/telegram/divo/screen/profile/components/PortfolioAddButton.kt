package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.PortfolioUploadPreview
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun PortfolioAddButton(
    modifier: Modifier = Modifier,
    isUploading: Boolean,
    onImageSelected: (Uri) -> Unit,
) {
    val openGallery = rememberGalleryLauncher { uri ->
        onImageSelected(uri)
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
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Uploading Photos...",
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = Color.White
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_divo_add_a_photo),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.UploadPhotos),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
//        when {
//            isUploading && uploadLocalPath != null -> {
//                Box(modifier = Modifier.fillMaxSize()) {
//                    PortfolioUploadPreview(
//                        filePath = uploadLocalPath,
//                        modifier = Modifier.fillMaxSize(),
//                        cornerRadiusDp = 8
//                    )
//                    CircularProgressIndicator(
//                        modifier = Modifier
//                            .size(32.dp)
//                            .align(Alignment.Center),
//                        color = AppTheme.colors.accentColor,
//                        strokeWidth = 3.dp
//                    )
//                }
//            }
//            isUploading -> {
//                CircularProgressIndicator(
//                    modifier = Modifier.size(32.dp),
//                    color = AppTheme.colors.accentColor,
//                    strokeWidth = 3.dp
//                )
//            }
//            else -> {
//                Icon(
//                    imageVector = Icons.Default.Add,
//                    contentDescription = "Add photo",
//                    modifier = Modifier.size(48.dp),
//                    tint = Color.White
//                )
//            }
//        }
    }
}