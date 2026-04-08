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
                    sendEffect(Effect.NavigateToSimilarProfiles)
                    setState { copy(isSearching = false) }
                }

            }
        }
    }

    init {
        analyzeImage(uri.toUri())
    }

    private fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            setState { copy(imageUri = uri, detectionResult = FaceDetectionResult.Loading) }

            val result = FaceDetectionHelper.detect(appContext, uri)

            setState {
                copy(
                    detectionResult = if (result == null || result.faces.isEmpty()) {
                        FaceDetectionResult.NoFace
                    } else {
                        FaceDetectionResult.Success(result.faces, result.imageWidth, result.imageHeight)
                    }
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