package org.telegram.divo.screen.face_search

import android.net.Uri
import com.google.mlkit.vision.face.Face
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState

data class State(
    val imageUri: Uri,
    val isSearching: Boolean = false,
    val detectionResult: FaceDetectionResult = FaceDetectionResult.Loading,
) : ViewState

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnChangePhoto(val uri: Uri) : Intent
    data object OnFindClicked : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data object NavigateToSimilarProfiles : Effect
    data class ShowError(val message: String) : Effect
}

sealed class FaceDetectionResult {
    object Loading : FaceDetectionResult()
    object NoFace : FaceDetectionResult()
    data class Success(
        val faces: List<Face>,
        val imageWidth: Int,
        val imageHeight: Int
    ) : FaceDetectionResult()
}