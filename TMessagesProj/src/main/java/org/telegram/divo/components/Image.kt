package org.telegram.divo.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
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
    user: TLRPC.User?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56
) {
    if (user == null) {
        return
    }

    // Use photo_id as key to force view recreation when photo changes
    val photoId = user.photo?.photo_id ?: 0L

    androidx.compose.runtime.key(photoId) {
        AndroidView(
            modifier = modifier.size(sizeDp.dp),
            factory = { context ->
                BackupImageView(context).apply {
                    setRoundRadius(AndroidUtilities.dp(sizeDp / 2f)) // круг
                }
            },
            update = { view ->
                val location = ImageLocation.getForUser(user, ImageLocation.TYPE_BIG)
                val placeholder = AvatarDrawable(user)
                view.setImage(location, "50_50", placeholder, user)
            }
        )
    }
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
fun TelegramUserAvatarEditable(
    user: TLRPC.User?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56,
    onEditClick: () -> Unit
) {
    Box(modifier = modifier.padding(), contentAlignment = Alignment.BottomEnd) {
        TelegramUserAvatar(
            user = user,
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
 * Portfolio item image component - displays a photo from portfolio
 */
@Composable
fun PortfolioItemImage(
    portfolioItem: TLRPC.TL_profile_portfolioItem?,
    modifier: Modifier = Modifier,
    cornerRadiusDp: Int = 8
) {
    if (portfolioItem?.file == null) return

    val photo = portfolioItem.file
    if (photo is TLRPC.TL_photoEmpty || photo.sizes == null) return

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

            if (photo.dc_id != 0) {
                fullSize.location.dc_id = photo.dc_id
                fullSize.location.file_reference = photo.file_reference
            }

            val fullLoc = ImageLocation.getForPhoto(fullSize, photo) ?: return@AndroidView
            val thumbLoc = thumbSize?.let { ImageLocation.getForPhoto(it, photo) }

            val parentKey = "portfolio_${portfolioItem.id}"
            val thumbFilter = if (thumbSize is TLRPC.TL_photoStrippedSize) "b" else null

            view.setImageMedia(
                null,
                null,
                null,
                fullLoc,
                null,
                thumbLoc,
                thumbFilter,
                null,
                fullSize.size,
                1,
                parentKey
            )
        }
    )
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