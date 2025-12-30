package org.telegram.divo.screen.profile

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC

class ProfileViewModel :
    BaseViewModel<ProfileViewModel.ProfileViewState, ProfileViewModel.EventListIntent, ProfileViewModel.EventListEffect>() {

    data class ProfileViewState(
        val userFull: TLRPC.UserFull,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    sealed class EventListIntent : ViewIntent {
        data object OnSearchClicked : EventListIntent()
        data object OnAddEventClicked : EventListIntent()
        data class OnEventCardClicked(val eventId: Long) : EventListIntent()
        data class OnEventCtaClicked(val eventId: Long) : EventListIntent()
        data object OnLoad : EventListIntent()

    }

    sealed class EventListEffect : ViewEffect {
        data object NavigateToSearch : EventListEffect()
        data object NavigateToCreateEvent : EventListEffect()
        data class NavigateToEventDetails(val eventId: Long) : EventListEffect()
    }

    override fun createInitialState(): ProfileViewState {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        return ProfileViewState(
            userFull = userFull
        )
    }

    private var currentAccount = UserConfig.selectedAccount

    override fun handleIntent(intent: EventListIntent) {
        when (intent) {
            EventListIntent.OnLoad -> {
            }

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

