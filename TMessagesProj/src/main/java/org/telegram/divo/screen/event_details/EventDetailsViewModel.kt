package org.telegram.divo.screen.event_details

import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.screen.event_list.EventIntentData
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class EventDetailsViewModel :
    BaseViewModel<EventDetailsViewModel.EventListViewState, EventDetailsViewModel.EventListIntent, EventDetailsViewModel.EventListEffect>() {

    data class EventListViewState(
        val event: TLRPC.TL_event_event? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    enum class EventCtaType {
        Apply,
        MyEvent,
    }

    sealed class EventListIntent : ViewIntent {
        data object OnSearchClicked : EventListIntent()
        data object OnAddEventClicked : EventListIntent()
        data class OnEventCardClicked(val eventId: Long) : EventListIntent()
        data class OnEventCtaClicked(val eventId: Long) : EventListIntent()
    }

    sealed class EventListEffect : ViewEffect {
        data object NavigateToSearch : EventListEffect()
        data object NavigateToCreateEvent : EventListEffect()
        data class NavigateToEventDetails(val eventId: Long) : EventListEffect()
    }

    override fun createInitialState(): EventListViewState = EventListViewState(
    )

    private var currentAccount = UserConfig.selectedAccount

    init {
        getEvent(0)
    }

    fun getEvent(_eventId:Long){
        val eventId = EventIntentData.eventId
        val req = TLRPC.TL_event_getEvent()
        req.event_id = eventId
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if(response is TLRPC.TL_event_event){
                    response.creator
                }else{
                    response
                }
                error
            },
        )
    }

    override fun handleIntent(intent: EventListIntent) {
        when (intent) {
            EventListIntent.OnAddEventClicked -> sendEffect(EventListEffect.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> sendEffect(
                EventListEffect.NavigateToEventDetails(intent.eventId)
            )
            EventListIntent.OnSearchClicked -> sendEffect(EventListEffect.NavigateToSearch)
        }
    }
}

