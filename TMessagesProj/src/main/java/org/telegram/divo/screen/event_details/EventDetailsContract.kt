package org.telegram.divo.screen.event_details

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.screen.gallery.GalleryItem

data class EventDetailsViewState(
    val eventId: Int,
    val isOwnProfile: Boolean,
    val isModel: Boolean = false,
    val isRoleLoading: Boolean = false,
    val eventDetails: EventDetails? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val events: List<Event> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
) : ViewState

sealed class EventDetailsIntent : ViewIntent {
    data object OnSearchClicked : EventDetailsIntent()
    data object OnAddEventClicked : EventDetailsIntent()
    data object OnBackClicked : EventDetailsIntent()
    data object OnParamsClick : EventDetailsIntent()
    data class OnEventCardClicked(val eventId: Long) : EventDetailsIntent()
    data class OnEventCtaClicked(val eventId: Long) : EventDetailsIntent()
    data class OnPrevEventClicked(val eventId: Int) : EventDetailsIntent()
    data class OnPhotoClick(val items: List<GalleryItem>, val id: Int) : EventDetailsIntent()
}

sealed class EventDetailsEffect : ViewEffect {
    data object Back : EventDetailsEffect()
    data object NavigateToParams : EventDetailsEffect()
    data class NavigateToGallery(val items: List<GalleryItem>, val id: Int) : EventDetailsEffect()
    data class NavigateToPrevEvent(val id: Int) : EventDetailsEffect()
    data class ShowError(val message: String) : EventDetailsEffect()
}