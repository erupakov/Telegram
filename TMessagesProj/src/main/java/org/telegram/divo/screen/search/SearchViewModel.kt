package org.telegram.divo.screen.search

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel

class SearchViewModel : BaseViewModel<State, Intent, Effect>() {

    private var searchJob: Job? = null

    override fun createInitialState(): State = State()

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnQueryChanged -> onQueryChanged(intent.value)
            is Intent.OnPhotoSelected -> sendEffect(Effect.NavigateToFaceSearch(intent.uri.toString()))
        }
    }

    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(query = query) }

        if (query.isBlank()) {
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)

        }
    }
}