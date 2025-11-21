package org.telegram.divo.screen.event_list

import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState

data class EventListViewState(
    val events: List<EventListItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : ViewState

data class EventListItem(
    val id: String,
    val title: String,
    val bannerUrl: String,
    val hostHandle: String,
    val hostAvatarUrl: String,
    val dateLocationText: String,
    val durationText: String,
    val ctaType: EventCtaType = EventCtaType.Apply,
) {
    val ctaLabel: String
        get() = when (ctaType) {
            EventCtaType.Apply -> "Apply"
            EventCtaType.MyEvent -> "my event"
        }
}

enum class EventCtaType {
    Apply,
    MyEvent,
}

sealed class EventListIntent : ViewIntent {
    data object OnSearchClicked : EventListIntent()
    data object OnAddEventClicked : EventListIntent()
    data class OnEventCardClicked(val eventId: String) : EventListIntent()
    data class OnEventCtaClicked(val eventId: String) : EventListIntent()
}

sealed class EventListAction : ViewAction {
    data object NavigateToSearch : EventListAction()
    data object NavigateToCreateEvent : EventListAction()
    data class NavigateToEventDetails(val eventId: String) : EventListAction()
}

