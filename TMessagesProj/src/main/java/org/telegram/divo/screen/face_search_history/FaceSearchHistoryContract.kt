package org.telegram.divo.screen.face_search_history

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.db.entity.FaceRecognitionEntity

data class State(
    val frSearchHistory: List<FaceRecognitionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
) : ViewState

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data object OnClearAllClicked : Intent
    data object OnLoadMore : Intent
    data class OnSimilarityProfileClicked(val item: FaceRecognitionEntity) : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
    data class NavigateToSimilarity(val url: String, val filtersJson: String?) : Effect
}