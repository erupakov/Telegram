package org.telegram.divo.screen.event_list

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.UserInfo

data class EventListViewState(
    val userInfo: UserInfo = UserInfo(),
    val events: List<Event> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val errorMessage: String? = null,
    val filterCount: Int? = null
) : ViewState {
    val isModel: Boolean
        get() = userInfo.role.isModel()
}

enum class EventCtaType {
    Apply,
    MyEvent,
}

sealed class EventListIntent : ViewIntent {
    data object OnSearchClicked : EventListIntent()
    data object OnAddEventClicked : EventListIntent()
    data class OnEventCardClicked(val eventId: Long) : EventListIntent()
    data class OnEventCtaClicked(val eventId: Long) : EventListIntent()
    data object OnLoad : EventListIntent()
    data object OnLoadMore : EventListIntent()
}

sealed class EventListEffect : ViewEffect {
    data object NavigateToSearch : EventListEffect()
    data object NavigateToCreateEvent : EventListEffect()
    data class NavigateToEventDetails(val eventId: Long) : EventListEffect()
}