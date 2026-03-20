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

class SearchViewModel : BaseViewModel<State, Intent, Effect>() {

    private var searchJob: Job? = null

    private val agencyPaginator = OffsetPaginator(limit = 10) { offset, limit ->
        when (val result = DivoApi.workHistory.searchAgencies(
            offset = offset,
            limit = limit,
            query = state.value.query
        )) {
            is DivoResult.Success -> PaginatedResult(
                items = result.value.first,
                totalCount = result.value.second
            )
            else -> throw Exception(result.getErrorMessage())
        }
    }

    init {
        observeAgencies()
    }

    override fun createInitialState(): State = State(
        query = DivoApi.workHistory.selectedAgency.value
    )

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnQueryChanged -> onQueryChanged(intent.value)
            is Intent.OnSearchSelected -> {
                DivoApi.workHistory.selectAgency(intent.value)
                sendEffect(Effect.NavigateBack)
            }
            is Intent.OnLoadMore -> viewModelScope.launch { agencyPaginator.loadMore() }
        }
    }

    private fun observeAgencies() {
        viewModelScope.launch {
            agencyPaginator.state.collect { pState ->
                setState {
                    copy(
                        items = pState.items,
                        isLoadingAgencies = pState.isLoading,
                        isLoadingMoreAgencies = pState.isLoadingMore,
                        hasMoreAgencies = pState.hasMore,
                    )
                }
                pState.error?.let { sendEffect(Effect.ShowError(it)) }
            }
        }
    }

    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(query = query) }

        if (query.isBlank()) {
            agencyPaginator.reset()
            setState { copy(items = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            agencyPaginator.reset()
            agencyPaginator.loadInitial()
        }
    }
}