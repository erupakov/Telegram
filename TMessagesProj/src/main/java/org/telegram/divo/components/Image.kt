package org.telegram.divo.components

import android.media.Image
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.dal.dto.user.GalleryItem
import org.telegram.divo.dal.dto.user.UserGalleryListData

import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLoader
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.BackupImageView

@Composable
fun TelegramUserAvatar(
    modifier: Modifier = Modifier,
    photoUrl: String?,
    sizeDp: Int = 56
) {
    DivoAsyncImage(
        modifier = modifier.size(sizeDp.dp),
        url = photoUrl,

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
fun EventTelegramPhoto(
    photo: TLRPC.Photo?,
    modifier: Modifier = Modifier,
) {
    TelegramPhoto(
        photo = photo,
        modifier = modifier,
    )
}


@Composable
fun TelegramPhotoBackground(
    photo: String?,
    modifier: Modifier = Modifier,
) {

    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {

        DivoAsyncImage(
            url = photo,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .hazeSource(state = hazeState)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.1f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startIntensity = 0f,
                        endIntensity = 1f,
                        easing = FastOutSlowInEasing
                    )
                }
        )
    }
}

@Composable
fun TelegramUserAvatarEditable(
    user: TLRPC.User?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56,
    onEditClick: () -> Unit
) {
    Box(modifier = modifier.padding(), contentAlignment = Alignment.BottomEnd) {
        TelegramUserAvatar(
            photoUrl = "uiState.userInfo?.avatarUrl",
            modifier = Modifier
                .padding(8.dp)
                .clickable(onClick = { onEditClick() }),
            sizeDp = sizeDp
        )
        Card(
            border = BorderStroke(width = 2.dp, color = AppTheme.colors.accentColor),
            shape = CircleShape,
            modifier = Modifier.clickable(onClick = { onEditClick() })
        ) {
            Icon(
                painter = painterResource(R.drawable.msg_edit),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.accentColor
            )
        }
    }
}

/**
 * Local image preview for uploading - shows local file path while uploading
 */
@Composable
fun PortfolioUploadPreview(
    filePath: String?,
    modifier: Modifier = Modifier,
    cornerRadiusDp: Int = 8
) {
    if (filePath == null) return

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BackupImageView(context).apply {
                if (cornerRadiusDp > 0) {
                    setRoundRadius(AndroidUtilities.dp(cornerRadiusDp.toFloat()))
                }
            }
        },
        update = { view ->
            val location = ImageLocation.getForPath(filePath)
            view.setImage(location, "400_400", null as android.graphics.drawable.Drawable?, null)
        }
    )
}