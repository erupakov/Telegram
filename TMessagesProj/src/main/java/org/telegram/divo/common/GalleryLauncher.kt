package org.telegram.divo.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

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