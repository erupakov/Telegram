package org.telegram.divo.screen.similar_profiles

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.SearchedProfile

const val HIGH_PERCENT = 85

data class State(
    val imageUrl: String,
    val profiles: List<SearchedProfile> = emptyList(),
    val isLoading: Boolean = false,
) : ViewState {

    val topMatches: Int
        get() = profiles.count { (it.similarity ?: 0) >= HIGH_PERCENT }
}

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnLikeChanged(val id: Int) : Intent
    data class OnMarkChanged(val id: Int) : Intent
    data class OnProfileClicked(val id: Int) : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
    data class NavigateToProfile(val id: Int) : Effect
}
