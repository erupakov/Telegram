package org.telegram.divo.screen.face_search

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.telegram.divo.common.BaseViewModel
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.common.utils.ImageCacheHelper
import org.telegram.divo.dal.dto.face.SimilarFaceDto
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.face_search.Effect.*
import java.io.File

class FaceSearchViewModel(
    private val uri: String,
    private val appContext: Context,
) : BaseViewModel<State, Intent, Effect>() {

    private var searchJob: Job? = null
    private var cachedFilePath: String? = null

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
            Intent.OnFindClicked -> performSearch()
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
            setState {
                copy(
                    imageUri = uri,
                    detectionResult = FaceDetectionResult.Loading,
                    selectedFaceIndex = null
                )
            }

            try {
                val path = ImageCacheHelper.cacheUri(appContext, uri)
                if (path == null) {
                    setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                    return@launch
                }
                cachedFilePath = path
                val file = File(path)

                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, options)
                var imageWidth = options.outWidth
                var imageHeight = options.outHeight

                try {
                    val exif = androidx.exifinterface.media.ExifInterface(path)
                    val orientation = exif.getAttributeInt(
                        androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                        androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    if (orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 ||
                        orientation == androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270
                    ) {
                        imageWidth = options.outHeight
                        imageHeight = options.outWidth
                    }
                } catch (e: Exception) { }

                if (imageWidth == 0 || imageHeight == 0) {
                    setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                    return@launch
                }

                when (val result = DivoApi.faceRecognitionRepository.detectFaces(file)) {
                    is DivoResult.Success -> {
                        val faces = result.value.faces?.map { face ->
                            val bbox = face.bbox
                            ServerFace(
                                index = face.index,
                                x1 = bbox?.x1 ?: 0f,
                                y1 = bbox?.y1 ?: 0f,
                                x2 = bbox?.x2 ?: 0f,
                                y2 = bbox?.y2 ?: 0f,
                                area = face.area ?: 0.0
                            )
                        } ?: emptyList()

                        setState {
                            copy(
                                detectionResult = if (faces.isEmpty()) {
                                    FaceDetectionResult.NoFace
                                } else {
                                    FaceDetectionResult.Success(faces, imageWidth, imageHeight)
                                },
                                facesCount = faces.size,
                                selectedFaceIndex = if (faces.size == 1) 0 else null
                            )
                        }
                    }
                    is DivoResult.HttpError -> {
                        if (result.code == 400) {
                            setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                        } else {
                            setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                            sendEffect(ShowError(result.getErrorMessage()))
                        }
                    }
                    else -> {
                        setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                        sendEffect(ShowError(result.getErrorMessage()))
                    }
                }
            } catch (e: Exception) {
                setState { copy(detectionResult = FaceDetectionResult.NoFace) }
                sendEffect(ShowError(e.message ?: "Detection error"))
            }
        }
    }

    private fun performSearch() {
        viewModelScope.launch {
            setState { copy(isSearching = true) }

            try {
                val path = cachedFilePath
                if (path == null) {
                    sendEffect(ShowError("Image not cached"))
                    setState { copy(isSearching = false) }
                    return@launch
                }
                val file = File(path)
                val currentState = state.value
                val faceIndex = currentState.selectedFaceIndex ?: 0

                val result = DivoApi.faceRecognitionRepository.search(
                    file = file,
                    kRatio = 0.35,
                    topK = 30,
                    faceIndex = faceIndex
                )

                when (result) {
                    is DivoResult.Success -> {
                        val response = result.value

                        var fx: Float? = null
                        var fy: Float? = null
                        if (currentState.detectionResult is FaceDetectionResult.Success) {
                            val face = currentState.detectionResult.faces.getOrNull(faceIndex)
                            if (face != null) {
                                val imgW = currentState.detectionResult.imageWidth.toFloat()
                                val imgH = currentState.detectionResult.imageHeight.toFloat()
                                fx = (face.centerX / imgW).coerceIn(0f, 1f)
                                fy = (face.centerY / imgH).coerceIn(0f, 1f)
                            }
                        }

                        val persistentPath = ImageCacheHelper.savePersistent(appContext, currentState.imageUri)
                        val uriToPass = persistentPath ?: currentState.imageUri.toString()
                        val resultsJson = Gson().toJson(response.results ?: emptyList<SimilarFaceDto>())

                        sendEffect(NavigateToSimilarProfiles(uriToPass, fx, fy, resultsJson))
                        delay(100)
                        setState { copy(isSearching = false) }
                    }
                    else -> {
                        setState { copy(isSearching = false) }
                        sendEffect(ShowError(result.getErrorMessage()))
                    }
                }
            } catch (e: Exception) {
                setState { copy(isSearching = false) }
                sendEffect(ShowError(e.message ?: "Search error"))
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
                pState.error?.let { sendEffect(ShowError(it)) }
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