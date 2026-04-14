package org.telegram.divo.screen.face_search

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.telegram.divo.common.BaseViewModel
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.utils.FaceDetectionHelper

class FaceSearchViewModel(
    private val uri: String,
    private val appContext: Context,
)  : BaseViewModel<State, Intent, Effect>() {

    override fun createInitialState(): State = State(imageUri = uri.toUri())

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> {
                if (state.value.isSearching) {
                    setState { copy(isSearching = false) }
                } else {
                    sendEffect(Effect.NavigateBack)
                }
            }
            is Intent.OnChangePhoto -> analyzeImage(intent.uri)
            Intent.OnFindClicked -> {
                viewModelScope.launch {
                    setState { copy(isSearching = true) }
                    delay(3000)

                    val currentState = state.value
                    var fx: Float? = null
                    var fy: Float? = null

                    if (currentState.detectionResult is FaceDetectionResult.Success) {
                        val index = currentState.selectedFaceIndex ?: 0
                        if (index < currentState.detectionResult.faces.size) {
                            val rect = currentState.detectionResult.faces[index].boundingBox
                            val imgW = currentState.detectionResult.imageWidth.toFloat()
                            val imgH = currentState.detectionResult.imageHeight.toFloat()

                            fx = (rect.centerX() / imgW).coerceIn(0f, 1f)
                            fy = (rect.centerY() / imgH).coerceIn(0f, 1f)
                        }
                    }

                    sendEffect(Effect.NavigateToSimilarProfiles(currentState.imageUri.toString(), fx, fy))
                    delay(100)
                    setState { copy(isSearching = false) }
                }
            }
            Intent.OnFindProfilesClicked -> sendEffect(Effect.NavigateToSearch)
            is Intent.OnFaceSelected -> setState { copy(selectedFaceIndex = intent.index) }
        }
    }

    init {
        analyzeImage(uri.toUri())
    }

    private fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            setState { copy(imageUri = uri, detectionResult = FaceDetectionResult.Loading, selectedFaceIndex = null) }

            val result = FaceDetectionHelper.detect(appContext, uri)

            setState {
                val facesCount = result?.faces?.size ?: 0
                copy(
                    detectionResult = if (result == null || facesCount == 0) {
                        FaceDetectionResult.NoFace
                    } else {
                        FaceDetectionResult.Success(result.faces, result.imageWidth, result.imageHeight)
                    },
                    facesCount = facesCount,
                    selectedFaceIndex = if (facesCount == 1) 0 else null
                )
            }
        }
    }

    companion object {
        fun factory(uri: String, context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FaceSearchViewModel(uri, context.applicationContext) as T
            }
        }
    }
}