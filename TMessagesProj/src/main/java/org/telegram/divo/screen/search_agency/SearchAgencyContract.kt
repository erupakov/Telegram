package org.telegram.divo.screen.search_agency

import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState
import org.telegram.divo.entity.Agency
import org.telegram.divo.dal.repository.AgencySelection

data class State(
    val items: List<Agency> = emptyList(),
    val agencyName: String = "",
    val isLoadingAgencies: Boolean = false,
    val isLoadingMoreAgencies: Boolean = false,
    val hasMoreAgencies: Boolean = false,
    val query: String = "",
) : ViewState

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data object OnLoadMore : Intent
    data class OnQueryChanged(val value: String) : Intent
    data class OnSearchSelected(val selection: AgencySelection) : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data class ShowError(val message: String) : Effect
}
