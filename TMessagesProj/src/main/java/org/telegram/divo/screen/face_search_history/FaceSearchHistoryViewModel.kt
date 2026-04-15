package org.telegram.divo.screen.face_search_history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult

class FaceSearchHistoryViewModel : BaseViewModel<State, Intent, Effect>() {
    override fun createInitialState(): State = State()

    private var observeHistoryJob: Job? = null
    private var currentUserId: Int? = null
    private var allItems = emptyList<FaceRecognitionEntity>()
    private var loadedCount = PAGE_SIZE

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            Intent.OnClearAllClicked -> {
                viewModelScope.launch {
                    val userId = currentUserId ?: return@launch
                    DivoApi.faceRecognitionRepository.clearHistory(userId)
                    sendEffect(Effect.NavigateBack)
                }
            }
            Intent.OnLoadMore -> {
                loadedCount += PAGE_SIZE
                publishVisibleItems()
            }
            is Intent.OnSimilarityProfileClicked -> {
                sendEffect(Effect.NavigateToSimilarity(intent.item.imageUri, intent.item.filtersJson.ifBlank { null }))
            }
        }
    }

    init {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val userInfoResult = DivoApi.userRepository.getCurrentUserInfo()
            val userId = (userInfoResult as? DivoResult.Success)?.value?.id
            currentUserId = userId

            if (userId == null) {
                setState { copy(isLoading = false) }
                sendEffect(Effect.ShowError("Unauthorized"))
                return@launch
            }

            observeHistoryJob?.cancel()
            observeHistoryJob = launch {
                DivoApi.faceRecognitionRepository.observeByUser(userId).collectLatest { items ->
                    allItems = items
                    publishVisibleItems()
                    setState { copy(isLoading = false) }
                }
            }
        }
    }

    private fun publishVisibleItems() {
        val visible = allItems.take(loadedCount)
        setState {
            copy(
                frSearchHistory = visible,
                hasMore = allItems.size > visible.size,
                isLoadingMore = false
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}