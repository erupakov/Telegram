package org.telegram.divo.screen.search

import android.net.Uri
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.SearchedProfile

data class State(
    val query: String = "",
    val searchResults: List<SearchedProfile> = emptyList(),
    val totalProfiles: Int = 0,
    val isSearchConfirmed: Boolean = false,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasSearched: Boolean = false,
    val hasMore: Boolean = false,
) : ViewState

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnQueryChanged(val value: String) : Intent
    data class OnPhotoSelected(val uri: Uri) : Intent
    data object OnLoadMore : Intent
    data class OnItemClicked(val user: SearchedProfile) : Intent
    data object OnSearchConfirmed : Intent
    data object OnDivoProfilesClicked : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
    data class NavigateToFaceSearch(val uri: String) : Effect
    data class NavigateToProfile(val user: SearchedProfile) : Effect
    data object NavigateToSearchSimilarity : Effect
}
