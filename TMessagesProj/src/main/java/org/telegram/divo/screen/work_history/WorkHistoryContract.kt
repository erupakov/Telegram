package org.telegram.divo.screen.work_history

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.WorkExperience

data class State(
    val isLoading: Boolean = false,
    val deletingId: Int? = null,
    val experiences: List<WorkExperience> = emptyList(),
) : ViewState


sealed interface Intent : ViewIntent {
    data object Load : Intent
    data object OnBackClicked : Intent
    data object OnCreateClicked : Intent
    data class OnEditClicked(val id: Int) : Intent
    data class OnDeleteClicked(val id: Int) : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class NavigateToCreate(val id: Int?) : Effect
    data class ShowError(val message: String) : Effect
}