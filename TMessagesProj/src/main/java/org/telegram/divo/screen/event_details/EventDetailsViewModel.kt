package org.telegram.divo.screen.event_details

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.divo.screen.event_list.EventIntentData
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error
import kotlin.collections.emptyList

class EventDetailsViewModel :
    BaseViewModel<EventDetailsViewModel.EventListViewState, EventDetailsViewModel.EventListIntent, EventDetailsViewModel.EventListAction>() {

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

    sealed class EventListAction : ViewAction {
        data object NavigateToSearch : EventListAction()
        data object NavigateToCreateEvent : EventListAction()
        data class NavigateToEventDetails(val eventId: Long) : EventListAction()
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
            EventListIntent.OnAddEventClicked -> sendAction(EventListAction.NavigateToCreateEvent)
            is EventListIntent.OnEventCardClicked -> sendAction(
                EventListAction.NavigateToEventDetails(intent.eventId)
            )
            is EventListIntent.OnEventCtaClicked -> sendAction(
                EventListAction.NavigateToEventDetails(intent.eventId)
            )
            EventListIntent.OnSearchClicked -> sendAction(EventListAction.NavigateToSearch)
        }
    }
}

