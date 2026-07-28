package org.telegram.divo.common.controllers

import android.Manifest
import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import java.io.File

@Composable
fun rememberGalleryLauncher(
    isVideo: Boolean = false,
    onPicked: (Uri) -> Unit
): () -> Unit {

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(onPicked)
    }

    return {
        val request = PickVisualMediaRequest(
            if (isVideo) ActivityResultContracts.PickVisualMedia.VideoOnly
            else ActivityResultContracts.PickVisualMedia.ImageOnly
        )
        launcher.launch(request)
    }
}

@Composable
fun rememberMultipleGalleryLauncher(
    maxItems: Int = 9,
    onPicked: (List<Uri>) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems)
    ) { uris ->
        if (uris.isNotEmpty()) onPicked(uris)
    }

    return {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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