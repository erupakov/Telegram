package org.telegram.divo.common.controllers

import android.Manifest
import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MediaController
import org.telegram.ui.Components.Bulletin
import org.telegram.ui.Components.BulletinFactory
import org.telegram.ui.Components.ChatAttachAlert
import org.telegram.ui.LaunchActivity
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.io.File

private fun hasMediaInMediaStore(context: android.content.Context, isVideo: Boolean): Boolean {
    val uri = if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    return try {
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns._ID), null, null, null)?.use { cursor ->
            cursor.count > 0
        } ?: false
    } catch (e: Exception) {
        false
    }
}

private fun hasGalleryPermission(context: android.content.Context, isVideo: Boolean): Boolean {
    return when {
        Build.VERSION.SDK_INT >= 34 -> {
            val visualSelectedGranted = ContextCompat.checkSelfPermission(
                context,
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            ) == PackageManager.PERMISSION_GRANTED
            val imagesGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val videoGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED

            if (isVideo) {
                if (videoGranted) {
                    true
                } else if (visualSelectedGranted) {
                    val allVideos = MediaController.allVideosAlbumEntry
                    if (allVideos != null) allVideos.photos.isNotEmpty() else hasMediaInMediaStore(context, isVideo = true)
                } else {
                    false
                }
            } else {
                if (imagesGranted) {
                    true
                } else if (visualSelectedGranted) {
                    val allPhotos = MediaController.allPhotosAlbumEntry
                    if (allPhotos != null) allPhotos.photos.isNotEmpty() else hasMediaInMediaStore(context, isVideo = false)
                } else {
                    false
                }
            }
        }
        Build.VERSION.SDK_INT >= 33 -> {
            val imagesGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
            val videoGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
            if (isVideo) videoGranted else imagesGranted
        }
        else -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

private fun getGalleryPermissions(): Array<String> {
    return when {
        Build.VERSION.SDK_INT >= 34 -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            )
        }
        Build.VERSION.SDK_INT >= 33 -> {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        }
        else -> {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}

private fun openTelegramPhotoPicker(
    isVideo: Boolean,
    maxItems: Int,
    onPicked: (List<Uri>) -> Unit
) {
    val lastFragment = LaunchActivity.getLastFragment() ?: return
    val activity = lastFragment.parentActivity ?: return

    val chatAttachAlert = ChatAttachAlert(activity, lastFragment, false, false, false, null)
    chatAttachAlert.setMaxSelectedPhotos(maxItems, false)
    chatAttachAlert.allowAvatarConstructor = false
    chatAttachAlert.disableTypeButtons = true
    chatAttachAlert.documentsEnabled = false
    chatAttachAlert.videosEnabled = isVideo
    chatAttachAlert.photosEnabled = !isVideo
    if (maxItems == 1) {
        chatAttachAlert.setAvatarPicker(if (isVideo) 3 else 1, false, null)
    } else {
        chatAttachAlert.typeButtonsAvailable = false
        chatAttachAlert.selectedTextView?.setText(LocaleController.getString(if (isVideo) R.string.ChoosePhotoOrVideo else R.string.ChoosePhoto))
    }
    chatAttachAlert.photoLayout?.loadGalleryPhotos()
    chatAttachAlert.setDelegate(object : ChatAttachAlert.ChatAttachViewDelegate {
        override fun didPressedButton(
            button: Int,
            arg: Boolean,
            notify: Boolean,
            scheduleDate: Int,
            scheduleRepeatPeriod: Int,
            effectId: Long,
            invertMedia: Boolean,
            forceDocument: Boolean,
            payStars: Long
        ) {
            val photoLayout = chatAttachAlert.photoLayout ?: return
            val selectedPhotos = photoLayout.selectedPhotos
            val order = photoLayout.selectedPhotosOrder
            if (selectedPhotos.isNullOrEmpty()) return
            val uris = ArrayList<Uri>()
            if (!order.isNullOrEmpty()) {
                for (key in order) {
                    val value = selectedPhotos[key]
                    if (value is MediaController.PhotoEntry) {
                        val path = value.imagePath ?: value.path
                        if (path != null) {
                            uris.add(
                                if (path.startsWith("content://") || path.startsWith("file://")) {
                                    Uri.parse(path)
                                } else {
                                    Uri.fromFile(File(path))
                                }
                            )
                        }
                    }
                }
            } else {
                for (value in selectedPhotos.values) {
                    if (value is MediaController.PhotoEntry) {
                        val path = value.imagePath ?: value.path
                        if (path != null) {
                            uris.add(
                                if (path.startsWith("content://") || path.startsWith("file://")) {
                                    Uri.parse(path)
                                } else {
                                    Uri.fromFile(File(path))
                                }
                            )
                        }
                    }
                }
            }
            if (uris.isNotEmpty()) {
                onPicked(uris)
                chatAttachAlert.dismiss(true)
            }
        }

        override fun selectItemOnClicking(): Boolean {
            return maxItems == 1
        }
    })
    chatAttachAlert.init()
    chatAttachAlert.show()
}

private fun showPermissionToast(context: android.content.Context) {
    val message = LocaleController.getString(R.string.PermissionGalleryDeniedToast)
    try {
        val lastFragment = LaunchActivity.getLastFragment()
        val visibleFragment = LaunchActivity.getLastFragmentIncludeMainTabs()
        val containerLayout = lastFragment?.layoutContainer

        if (lastFragment != null) {
            val delegate = object : Bulletin.Delegate {
                override fun getBottomOffset(tag: Int): Int {
                    val navHeight = maxOf(
                        AndroidUtilities.navigationBarHeight,
                        containerLayout?.let { AndroidUtilities.getViewInset(it) } ?: 0
                    )
                    return navHeight + AndroidUtilities.dp(80f)
                }

                override fun onHide(bulletin: Bulletin?) {
                    Bulletin.removeDelegate(lastFragment)
                    if (visibleFragment != null && visibleFragment != lastFragment) {
                        Bulletin.removeDelegate(visibleFragment)
                    }
                    if (containerLayout != null) {
                        Bulletin.removeDelegate(containerLayout)
                    }
                }
            }

            Bulletin.addDelegate(lastFragment, delegate)
            if (visibleFragment != null && visibleFragment != lastFragment) {
                Bulletin.addDelegate(visibleFragment, delegate)
            }
            if (containerLayout != null) {
                Bulletin.addDelegate(containerLayout, delegate)
            }

            BulletinFactory.of(lastFragment).createErrorBulletin(message).show()
        } else {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun rememberGalleryLauncher(
    isVideo: Boolean = false,
    onPicked: (Uri) -> Unit
): () -> Unit {
    val context = LocalContext.current

    fun launchTelegramPicker() {
        openTelegramPhotoPicker(
            isVideo = isVideo,
            maxItems = 1,
            onPicked = { uris -> uris.firstOrNull()?.let(onPicked) }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasGalleryPermission(context, isVideo)) {
            MediaController.loadGalleryPhotosAlbums(0)
            launchTelegramPicker()
        } else {
            showPermissionToast(context)
        }
    }

    return {
        if (hasGalleryPermission(context, isVideo)) {
            launchTelegramPicker()
        } else {
            permissionLauncher.launch(getGalleryPermissions())
        }
    }
}

@Composable
fun rememberMultipleGalleryLauncher(
    maxItems: Int = 9,
    onPicked: (List<Uri>) -> Unit
): () -> Unit {
    val context = LocalContext.current

    fun launchTelegramPicker() {
        openTelegramPhotoPicker(
            isVideo = false,
            maxItems = maxItems,
            onPicked = onPicked
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasGalleryPermission(context, isVideo = false)) {
            MediaController.loadGalleryPhotosAlbums(0)
            launchTelegramPicker()
        } else {
            showPermissionToast(context)
        }
    }

    return {
        if (hasGalleryPermission(context, isVideo = false)) {
            launchTelegramPicker()
        } else {
            permissionLauncher.launch(getGalleryPermissions())
        }
    }
}

class CameraCapture(
    val launch: () -> Unit,
    val rationaleDialog: @Composable () -> Unit,
)

@Composable
fun rememberCameraCapture(
    onPhotoCaptured: (Uri) -> Unit,
): CameraCapture {
    val context = LocalContext.current
    var activity = context as? Activity
    if (activity == null) {
        var currentContext = context
        while (currentContext is ContextWrapper) {
            if (currentContext is Activity) {
                activity = currentContext
                break
            }
            currentContext = currentContext.baseContext
        }
    }
    
    var showRationale by remember { mutableStateOf(false) }
    var currentPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                onPhotoCaptured(Uri.parse(path))
            }
        }
    }

    fun createNewUri(): Uri {
        val file = File(context.cacheDir, "face_search_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        currentPhotoPath = uri.toString()
        return uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(createNewUri())
        else showRationale = true
    }

    fun launch() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> {
                cameraLauncher.launch(createNewUri())
            }
            activity != null && ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) -> {
                showRationale = true
            }
            else -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    val rationaleDialog: @Composable () -> Unit = {
        if (showRationale) {
            CameraPermissionRationaleDialog(
                onDismiss = { showRationale = false },
                onOpenSettings = {
                    showRationale = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        }
    }

    return remember(onPhotoCaptured) {
        CameraCapture(
            launch = ::launch,
            rationaleDialog = rationaleDialog,
        )
    }
}

@Composable
private fun CameraPermissionRationaleDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(270.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(AppTheme.colors.backgroundLight)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Icon(
                painter = painterResource(R.drawable.ic_divo_face_rec),
                contentDescription = null,
                tint = AppTheme.colors.accentOrange,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.CameraPermissionTitle),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.CameraPermissionDescription),
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(20.dp))
            UIButton(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                text = stringResource(R.string.OpenSettings),
                onClick = onOpenSettings
            )
            Spacer(Modifier.height(8.dp))
            UIButton(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                text = stringResource(R.string.ButtonCancel),
                background = Color.Transparent,
                textStyle = AppTheme.typography.manropeRegular.copy(
                    color = AppTheme.colors.textPrimary.copy(0.5f),
                    fontSize = 15.sp
                ),
                onClick = onDismiss
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}