package org.telegram.divo.screen.face_search

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.telegram.divo.common.BaseViewModel
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.common.utils.FaceDetectionHelper
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.face_search.Effect.*
import org.telegram.divo.screen.search.Effect.ShowError

class FaceSearchViewModel(
    private val uri: String,
    private val appContext: Context,
)  : BaseViewModel<State, Intent, Effect>() {

    private var searchJob: Job? = null

    private val searchPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = state.value.query,
        )) {
            is DivoResult.Success -> {
                PaginatedResult(
                    items = result.value.items,
                    totalCount = result.value.pagination?.totalCount ?: result.value.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    override fun createInitialState(): State = State(imageUri = uri.toUri())

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> {
                if (state.value.isSearching) {
                    setState { copy(isSearching = false) }
                } else {
                    sendEffect(NavigateBack)
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

                    sendEffect(NavigateToSimilarProfiles(currentState.imageUri.toString(), fx, fy))
                    delay(100)
                    setState { copy(isSearching = false) }
                }
            }
            Intent.OnFindProfilesClicked -> sendEffect(NavigateToSearch)
            is Intent.OnFaceSelected -> setState { copy(selectedFaceIndex = intent.index) }
            Intent.OnLoadMore -> loadMore()
            is Intent.OnQueryChanged -> onQueryChanged(intent.value)
        }
    }

    init {
        analyzeImage(uri.toUri())
        observePaginator()
    }

    private fun analyzeImage(uri: Uri?) {
        if (uri == null) return
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

    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(query = query) }

        if (query.isBlank()) {
            searchPaginator.reset()
            setState { copy(isLoading = false, searchResults = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            setState { copy(isLoading = true) }
            searchPaginator.reset()
            searchPaginator.loadInitial()
            setState { copy(isLoading = false) }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            searchPaginator.loadMore()
        }
    }

    private fun observePaginator() {
        viewModelScope.launch {
            searchPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResults = pState.items.map { it.toSearchedProfile() },
                        isLoadingMore = pState.isLoadingMore,
                        hasMore = pState.hasMore,
                    )
                }
                pState.error?.let { sendEffect(Effect.ShowError(it)) }
            }
        }
    }

    private fun FeedlineItem.toSearchedProfile() = SearchedProfile(
        id = this.id,
        name = this.user?.fullName.orEmpty(),
        age = this.user?.age,
        country = this.user?.city?.countryName,
        countryCode = this.user?.city?.countryCode,
        isMarked = this.isFavoriteByUser,
        likes = this.likesCount,
        isLiked = this.isLikedByUser,
        photo = this.searchImageUrl.orEmpty(),
        roleLabel = this.user?.roleLabel.orEmpty(),
        similarity = null
    )

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L

        fun factory(uri: String, context: Context) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return FaceSearchViewModel(uri, context.applicationContext) as T
            }
        }
    }
}