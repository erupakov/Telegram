package org.telegram.divo.screen.work_history

import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class WorkHistoryViewModel(
) : BaseViewModel<WorkHistoryViewModel.State,
        WorkHistoryViewModel.Intent,
        WorkHistoryViewModel.Effect>() {


    private val currentAccount = UserConfig.selectedAccount

    data class State(
        val isLoading: Boolean = false,
        val experiences: ArrayList<TLRPC.TL_profile_workExperience> = ArrayList()
    ) : ViewState


    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data object OnBackClicked : Intent
        data object OnCreateClicked : Intent
        data class OnEditClicked(
            val id: Long
        ) : Intent
        data class OnDeleteClicked(val id: Long) : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
    }

    init {
        loadWorkExperience()
    }

    override fun createInitialState(): State = State()

    private fun loadWorkExperience() {
        currentAccount

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        val inputUser = TLRPC.TL_inputUser()
        inputUser.user_id = userFull.user.id
        inputUser.access_hash = userFull.user.access_hash


        val req = TLRPC.TL_profile_getWorkHistory().apply {
            user_id = inputUser
            offset = 0
            limit = 300
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState { copy(isLoading = false) }
                        return@runOnUIThread
                    }
                    val list = (response as? TLRPC.TL_profile_workHistory)
                        ?.experiences
                        .orEmpty()

                    setState { copy(experiences = ArrayList(list)) }
                }

                setState {
                    copy(
                        isLoading = false,
                    )
                }
            }
        )
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> {}
            // is Intent.OnQueryChanged -> setState { copy(query = intent.value) }
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)

            is Intent.OnCreateClicked -> {

            }

            else -> {

            }
        }
    }
}
