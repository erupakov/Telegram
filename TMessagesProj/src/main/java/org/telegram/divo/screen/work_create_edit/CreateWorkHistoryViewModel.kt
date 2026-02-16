package org.telegram.divo.screen.work_create_edit

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class CreateWorkHistoryViewModel(
) : BaseViewModel<CreateWorkHistoryViewModel.State,
        CreateWorkHistoryViewModel.Intent,
        CreateWorkHistoryViewModel.Effect>() {

    data class State(
        val isLoading: Boolean = false,
        val agencies: ArrayList<TLRPC.TL_profile_agency> = ArrayList(),
        val selectedAgency: TLRPC.TL_profile_agency? = null,
        val query: String = "",
        val startDate: String? = null,
        val startDateMil: Long? = null,
        val endDate: String? = null,
        val endDateMil: Long? = null,
        val isCurrent: Boolean = false,
        val createEnabled: Boolean = false,
        val isEditMode: Boolean = false
    ) : ViewState

    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data object OnBackClicked : Intent
        data class OnAgencySelected(val agency: TLRPC.TL_profile_agency) : Intent
        data class OnQueryChanged(val value: String) : Intent
        data class OnStartDateSelected(val date: String, val mil:Long) : Intent
        data class OnEndDateSelected(val date: String, val mil:Long) : Intent
        data class OnCurrentChanged(val isCurrent: Boolean): Intent
        data object OnCreateExperience: Intent

    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
    }

    private val currentAccount = UserConfig.selectedAccount

    override fun createInitialState(): State = State()

    fun searchAgency(query: String) {
//        if (query.length < 2) {
//            setState { copy(agencies = ArrayList()) }
//            return
//        }
        val req = TLRPC.TL_profile_searchAgencies()
        req.offset = 0
        req.limit = 30
        req.query = query
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_profile_foundAgencies) {

                    setState { copy(
                        agencies = ArrayList(response.agencies)
                    ) }
                } else {
                    response
                }
                error
            },
        )
    }

    fun checkData() {
        val value = state.value
        val startDateValid = value.startDate != null
        val agencyValid = value.selectedAgency != null

        val endDateValid = if (value.isCurrent) {
            true
        } else {
            value.endDate != null
        }

        val createEnabled =
            startDateValid &&
                    agencyValid &&
                    endDateValid

        setState { copy(createEnabled = createEnabled) }
    }



    override fun handleIntent(intent: Intent) {
        when (intent) {

            Intent.Load -> {}
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)

            is Intent.OnQueryChanged -> {
                setState { copy(query = query, ) }
                searchAgency(intent.value)
            }

            is Intent.OnStartDateSelected -> {
                setState {
                    copy(
                        startDate = intent.date,
                        startDateMil = intent.mil
                    )
                }
                checkData()
            }
            is Intent.OnEndDateSelected -> {
                setState {
                    copy(
                        endDate = intent.date,
                        endDateMil = intent.mil
                    )
                }
                checkData()
            }
            is Intent.OnCurrentChanged -> {
                setState {
                    copy(
                        isCurrent = intent.isCurrent
                    )
                }
                checkData()
            }
            is Intent.OnCreateExperience ->{
                createWorkExperience()
            }
            is Intent.OnAgencySelected -> {
                setState { copy(selectedAgency = intent.agency, query = (intent.agency.name ?: "")) }
                checkData()
            }
        }
    }

    private fun createWorkExperience() {

        val isCurrent = state.value.isCurrent
        val selectedAgency = state.value.selectedAgency

        val req = TLRPC.TL_profile_createWorkExperience()
        req.is_current = isCurrent
        req.agency = selectedAgency
        req.start_date = 1111
        req.end_date = 1111

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_profile_workExperience) {
                    sendEffect(Effect.NavigateBack)
                } else {
                    response
                }
                error
            },
        )
    }
}
