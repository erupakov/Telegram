package org.telegram.divo.components

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLoader
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.BackupImageView

@Composable
fun TelegramUserAvatar(
    modifier: Modifier = Modifier,
    photoUrl: String?,
    sizeDp: Int = 56
) {
    DivoAsyncImage(
        modifier = modifier.size(sizeDp.dp),
        model = photoUrl,
    )
}

@Composable
fun TelegramPhoto(
    photo: TLRPC.Photo?,
    dialogId: Long = 0L,
    modifier: Modifier = Modifier,
) {
    if (photo == null || photo is TLRPC.TL_photoEmpty || photo.sizes == null) return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BackupImageView(context)
        },
        update = { view ->
            // thumb ~50px, full ~640px (как в ProfileGalleryView)
            var thumbSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 50)
            for (i in 0 until photo.sizes.size) {
                val ps = photo.sizes[i]
                if (ps is TLRPC.TL_photoStrippedSize) {
                    thumbSize = ps
                    break
                }
            }
            val fullSize =
                FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 640) ?: return@AndroidView

            // важно: прокинуть dc_id и file_reference (как в ProfileGalleryView)
            if (photo.dc_id != 0) {
                fullSize.location.dc_id = photo.dc_id
                fullSize.location.file_reference = photo.file_reference
            }

            val fullLoc = ImageLocation.getForPhoto(fullSize, photo) ?: return@AndroidView
            val thumbLoc = thumbSize?.let { ImageLocation.getForPhoto(it, photo) }

            val parentKey = "avatar_$dialogId"
            val thumbFilter = if (thumbSize is TLRPC.TL_photoStrippedSize) "b" else null

            // setImageMedia = более “родной” путь (см. ProfileGalleryView) :contentReference[oaicite:1]{index=1}
            view.setImageMedia(
                null,           // vector avatar
                null,           // video location
                null,           // filter
                fullLoc,        // full
                null,           // ext
                thumbLoc,       // thumb
                thumbFilter,    // thumb filter
                null,           // cache
                fullSize.size,  // size
                1,              // priority
                parentKey
            )
        }
    )
}


@Composable
fun LocalImageView(
    filePath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Int = 0
) {
    if (filePath == null) return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BackupImageView(context).apply {
                if (cornerRadius > 0) {
                    setRoundRadius(AndroidUtilities.dp(cornerRadius.toFloat()))
                }
            }
        },
        update = { view ->
            val location = ImageLocation.getForPath(filePath)
            view.setImage(location, "800_800", null as android.graphics.drawable.Drawable?, null)
        }
    )
}

@Composable
fun TelegramPhotoBackground(
    photo: String?,
    modifier: Modifier = Modifier,
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val startYPx = with(LocalDensity.current) { (screenHeight * 0.23f).toPx() } // 20% без блюра
    val endYPx = with(LocalDensity.current) { (screenHeight * 0.5f).toPx() }   // переход 10%

    val hazeState = remember { HazeState() }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {

        DivoAsyncImage(
            model = photo,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
            modifier = Modifier
                .matchParentSize()
                .hazeSource(state = hazeState),
            loadingContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                )
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.White,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.1f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = startYPx,
                        startIntensity = 0f,
                        endY = endYPx,
                        endIntensity = 1f,
                        easing = LinearEasing
                    )
                }
        )
    }
}

@Composable
fun TelegramUserAvatarEditable(
    modifier: Modifier = Modifier,
    avatarUrl: String = "",
    localUri: Uri? = null,
    background: Color = AppTheme.colors.backgroundDark,
    borderColor: Color = Color.White.copy(alpha = 0.4f),
    isVisibleSmallIcon: Boolean = true,
    placeholderSymbols: String = "",
    placeholderIconSize: Dp = 32.dp,
    usePlaceholder: Boolean = false,
    @DrawableRes smallIconResId: Int = R.drawable.ic_camera_add,
    onEditClick: () -> Unit
) {
    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(background)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .clickableWithoutRipple(onClick = onEditClick)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(CircleShape)
            ) {
                if (localUri != null || avatarUrl.isNotEmpty()) {
                    DivoAsyncImage(
                        modifier = Modifier
                            .size(100.dp),
                        model = localUri ?: avatarUrl,
                        placeholderColor = Color.Transparent,
                        loadingContent = {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                LottieProgressIndicator(
                                    modifier = Modifier.size(placeholderIconSize),
                                )
                            }
                        }
                    )
                } else {
                    if (usePlaceholder) {
                        PlaceholderAvatar(
                            modifier = Modifier.fillMaxSize(),
                            name = placeholderSymbols,
                        )
                    } else {
                        Icon(
                            modifier = Modifier.align(Alignment.Center).size(placeholderIconSize),
                            painter = painterResource(R.drawable.ic_divo_add_photo),
                            contentDescription = null,
                            tint = borderColor
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White.copy(alpha = 0.05f),
                        shape = CircleShape
                    )
            )
        }

        if (isVisibleSmallIcon) {
            Image(
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                painter = painterResource(smallIconResId),
                contentDescription = null,
            )
        }
    }
}
