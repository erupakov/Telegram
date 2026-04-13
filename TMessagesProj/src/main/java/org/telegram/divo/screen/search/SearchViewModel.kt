package org.telegram.divo.screen.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.screen.search.Effect.*

class SearchViewModel : BaseViewModel<State, Intent, Effect>() {

    private var searchJob: Job? = null

    private val searchPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = state.value.query
        )) {
            is DivoResult.Success -> {
                val data = result.value
                setState { copy(totalProfiles = data.pagination?.totalCount ?: data.items.size) }
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }

            else -> throw Exception(result.getErrorMessage())
        }
    }

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(NavigateBack)
            is Intent.OnQueryChanged -> onQueryChanged(intent.value)
            is Intent.OnPhotoSelected -> sendEffect(NavigateToFaceSearch(intent.uri.toString()))
            Intent.OnLoadMore -> loadMore()
            is Intent.OnItemClicked -> sendEffect(NavigateToProfile(intent.user))
            is Intent.OnSearchConfirmed -> setState { copy(isSearchConfirmed = true) }
            Intent.OnDivoProfilesClicked -> sendEffect(NavigateToSearchSimilarity)
        }
    }

    init {
        observePaginator()
    }

    private fun observePaginator() {
        viewModelScope.launch {
            searchPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResults = pState.items.map {
                            SearchedProfile(
                                id = it.id,
                                name = it.user?.fullName.orEmpty(),
                                age = it.user?.age,
                                country = it.user?.city?.countryName,
                                countryCode = it.user?.city?.countryCode,
                                isMarked = it.isFavoriteByUser,
                                likes = it.likesCount,
                                isLiked = it.isLikedByUser,
                                photo = it.searchImageUrl.orEmpty(),
                                roleLabel = it.user?.roleLabel.orEmpty(),
                                similarity = null
                            )
                        },
                        isLoadingMore = pState.isLoadingMore,
                        hasMore = pState.hasMore,
                    )
                }

                pState.error?.let {
                    sendEffect(ShowError(it))
                }
            }
        }
    }

    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(query = query, isSearchConfirmed = false, hasSearched = false) }

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
            setState { copy(isLoading = false, hasSearched = true) }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            searchPaginator.loadMore()
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L
    }
}