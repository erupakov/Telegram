package org.telegram.divo.common.controllers

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MediaController
import org.telegram.messenger.VideoEditedInfo
import org.telegram.ui.LaunchActivity
import org.telegram.ui.PhotoViewer
import java.io.File

/**
 * Gallery picker for a profile photo: after a photo is picked, Telegram's avatar editor
 * (circle crop) opens so the user can choose the avatar area.
 *
 * [onPicked] receives the untouched original (main profile photo: header, cards) and the
 * square circle-crop result (avatar). If the crop editor can't be opened, both are the original.
 */
@Composable
fun rememberAvatarCropLauncher(
    onPicked: (original: Uri, cropped: Uri) -> Unit
): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)
    return rememberGalleryLauncher { original ->
        val path = original.toFilePath()
        if (path == null) {
            currentOnPicked(original, original)
        } else {
            openAvatarCropWhenIdle(path, onFailed = { currentOnPicked(original, original) }) { croppedPath ->
                currentOnPicked(original, Uri.fromFile(File(croppedPath)))
            }
        }
    }
}

private const val IDLE_CHECK_DELAY_MS = 100L
private const val IDLE_CHECK_ATTEMPTS = 10

/**
 * PhotoViewer is a singleton: reopening it while it is still closing (e.g. after the picker's own
 * viewer) shows the photo without any controls. Waits until it is idle, then opens the crop.
 */
private fun openAvatarCropWhenIdle(
    path: String,
    onFailed: () -> Unit,
    attempt: Int = 0,
    onCropped: (String) -> Unit
) {
    val busy = PhotoViewer.hasInstance() && PhotoViewer.getInstance().isVisibleOrAnimating
    if (busy && attempt < IDLE_CHECK_ATTEMPTS) {
        AndroidUtilities.runOnUIThread({ openAvatarCropWhenIdle(path, onFailed, attempt + 1, onCropped) }, IDLE_CHECK_DELAY_MS)
        return
    }
    if (!openAvatarCrop(path, onCropped)) onFailed()
}

private fun Uri.toFilePath(): String? = when (scheme) {
    null, "file" -> path
    else -> null
}

/** Opens the avatar crop editor for [path]; returns false if it couldn't be shown. */
private fun openAvatarCrop(path: String, onCropped: (String) -> Unit): Boolean {
    val fragment = LaunchActivity.getLastFragment() ?: return false
    return try {
        val orientation = AndroidUtilities.getImageOrientation(path)
        val entry = MediaController.PhotoEntry(0, 0, 0, path, orientation.first, false, 0, 0, 0)
            .setOrientation(orientation)
        val photos = arrayListOf<Any>(entry)
        val viewer = PhotoViewer.getInstance()
        viewer.setParentActivity(fragment)
        // Left over from the picker's own viewer; the crop has no attach alert behind it
        viewer.setParentAlert(null)
        val opened = viewer.openPhotoForSelect(photos, 0, PhotoViewer.SELECT_TYPE_AVATAR, false, object : PhotoViewer.EmptyPhotoViewerProvider() {
            override fun sendButtonPressed(
                index: Int,
                videoEditedInfo: VideoEditedInfo?,
                notify: Boolean,
                scheduleDate: Int,
                scheduleRepeatPeriod: Int,
                forceDocument: Boolean
            ) {
                val edited = photos[0] as MediaController.PhotoEntry
                // The editor saves the crop to imagePath; without it, fall back to the original
                onCropped(edited.imagePath ?: path)
            }

            override fun allowCaption(): Boolean = false

            override fun canScrollAway(): Boolean = false
        }, null)
        viewer.closePhotoAfterSelectWithAnimation = true
        opened
    } catch (e: Exception) {
        FileLog.e(e)
        false
    }
}
