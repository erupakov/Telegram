package org.telegram.divo.screen.face_search

import android.net.Uri
import com.google.mlkit.vision.face.Face
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.SearchedProfile

data class State(
    val imageUri: Uri,
    val isSearching: Boolean = false,
    val facesCount: Int = 0,
    val detectionResult: FaceDetectionResult = FaceDetectionResult.Loading,
    val selectedFaceIndex: Int? = null,

    val query: String = "",
    val searchResults: List<SearchedProfile> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
) : ViewState

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnChangePhoto(val uri: Uri?) : Intent
    data object OnFindClicked : Intent
    data object OnFindProfilesClicked : Intent
    data class OnFaceSelected(val index: Int) : Intent

    data class OnQueryChanged(val value: String) : Intent
    data object OnLoadMore : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class NavigateToSimilarProfiles(val uri: String, val fx: Float?, val fy: Float?) : Effect
    data class ShowError(val message: String) : Effect
    data object NavigateToSearch : Effect
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