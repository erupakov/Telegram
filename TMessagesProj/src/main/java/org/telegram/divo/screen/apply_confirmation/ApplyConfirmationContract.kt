package org.telegram.divo.screen.apply_confirmation

import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.UserInfo

data class ApplyConfirmationViewState(
    val eventId: Int,
    val eventDetails: EventDetails? = null,
    val userInfo: UserInfo? = null,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
) : ViewState

sealed class ApplyConfirmationIntent : ViewIntent {
    data object OnLoad : ApplyConfirmationIntent()
    data object OnSubmitClick : ApplyConfirmationIntent()
    data object OnBackClick : ApplyConfirmationIntent()
    data object OnSuccessDismiss : ApplyConfirmationIntent()
}

sealed class ApplyConfirmationEffect : ViewEffect {
    data object Back : ApplyConfirmationEffect()
    data object FinishWithSuccess : ApplyConfirmationEffect()
    data class ShowError(val message: String) : ApplyConfirmationEffect()
}
