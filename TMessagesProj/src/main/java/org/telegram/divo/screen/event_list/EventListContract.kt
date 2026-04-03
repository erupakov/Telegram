package org.telegram.divo.screen.event_list

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.Event

data class EventListViewState(
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,

    val isModel: Boolean = false,
    val isRoleLoading: Boolean = false,
    val filterCount: Int? = null
) : ViewState

enum class EventCtaType {
    Apply,
    MyEvent,
}

sealed class EventListIntent : ViewIntent {
    data object OnSearchClicked : EventListIntent()
    data object OnAddEventClicked : EventListIntent()
    data class OnEventCardClicked(val eventId: Int) : EventListIntent()
    data class OnEventCtaClicked(val eventId: Int) : EventListIntent()
    data object OnLoad : EventListIntent()
    data object OnLoadMore : EventListIntent()
}

sealed class EventListEffect : ViewEffect {
    data object NavigateToSearch : EventListEffect()
    data object NavigateToCreateEvent : EventListEffect()
    data class NavigateToEventDetails(val eventId: Int) : EventListEffect()
    data class ShowError(val message: String) : EventListEffect()
}