package org.telegram.divo.screen.event_create

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error

class CreateEventViewModel(
) : BaseViewModel<CreateEventViewModel.State,
        CreateEventViewModel.Intent,
        CreateEventViewModel.Effect>() {

    data class State(
        val isLoading: Boolean = false,

        val title: String = "",
        val description: String = "",

        val errorMessage: String? = null,
        val query: String = "",

        val eventTypes: List<TLRPC.TL_event_eventType> = emptyList(),
        val selectedEventType: TLRPC.TL_event_eventType? = null,

        val availableParameters: List<TLRPC.TL_event_availableParameter> = emptyList(),
        val selectedAvailableParameters: HashSet<TLRPC.TL_event_availableParameter> = HashSet(),
        val startDate: String? = null,
        val endDate: String? = null,

        val countries: List<TLRPC.TL_event_country> = emptyList(),
        val selectedCountry: TLRPC.TL_event_country? = null,
        val cities: List<TLRPC.TL_event_city> = emptyList(),
        val selectedCity: TLRPC.TL_event_city? = null,

        // Cover Photo
        val coverPhotoId: Long? = null,
        val coverPhotoPath: String? = null,
        val coverPhotoUploading: Boolean = false

    ) : ViewState {

        val filtered: List<TLRPC.TL_event_city>
            get() {
                val q = query.trim()
                if (q.isEmpty()) return cities
                return cities.filter { it.city?.contains(q, ignoreCase = true) == true }
            }
    }

    sealed interface Intent : ViewIntent {
        data object Load : Intent
        data class OnQueryChanged(val value: String) : Intent

        data class OnCountrySelected(val item: TLRPC.TL_event_country) : Intent
        data class OnCitySelected(val item: TLRPC.TL_event_city) : Intent

        data object OnBackClicked : Intent
        data class OnCreateClicked(
            val title: String,
            val description: String,
            val date: String,
            val time: String,
        ) : Intent

        // Cover Photo
        data class OnCoverPhotoSelected(
            val photo: TLRPC.InputFile,
            val localPath: String?
        ) : Intent

        data object OnClearCoverPhoto : Intent
    }

    sealed interface Effect : ViewEffect {
        data object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
    }

    init {
        loadCountries()
        getEventTypes()
    }

    override fun createInitialState(): State = State()

    private fun loadCountries() {
        val req = TLRPC.TL_event_getCountries().apply {
            offset = 0
            limit = 300
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState { copy(isLoading = false, errorMessage = error.text ?: "Error") }
                        return@runOnUIThread
                    }
                    val list = (response as? TLRPC.TL_event_countries)
                        ?.countries
                        .orEmpty()
                        .sortedBy { (it.country ?: "").lowercase() }

                    setState {
                        copy(
                            isLoading = false,
                            countries = list,
                            errorMessage = if (list.isEmpty()) "Empty list" else null
                        )
                    }
                }
            }
        )
    }

    private fun loadCities(countryId: Int) {
        val req = TLRPC.TL_event_getCities().apply {
            country_id = countryId
            offset = 0
            limit = 300
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = error.text ?: "Error"
                            )
                        }
                        return@runOnUIThread
                    }
                    val list = (response as? TLRPC.TL_event_cities)
                        ?.cities
                        .orEmpty()
                        .sortedBy { (it.city ?: "").lowercase() }

                    setState {
                        copy(
                            isLoading = false,
                            cities = list,
                        )
                    }
                }
            }
        )
    }

    private fun uploadPhotoToServer(
        inputFile: TLRPC.InputFile,
        onSuccess: (photoId: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        val req = TLRPC.TL_messages_uploadMedia().apply {
            peer = TLRPC.TL_inputPeerSelf()
            media = TLRPC.TL_inputMediaUploadedPhoto().apply {
                file = inputFile
            }
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        onError(error.text ?: "Upload failed")
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_messageMediaPhoto) {
                        val photoId = response.photo?.id ?: 0L
                        if (photoId != 0L) {
                            onSuccess(photoId)
                        } else {
                            onError("Failed to get photo ID")
                        }
                    } else {
                        onError("Unexpected response type")
                    }
                }
            }
        )
    }

     fun getEventTypes() {
        val req = TLRPC.TL_event_getEventTypes().apply {
            offset = 0
            limit = 100
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("getEventTypes error: ${error.text} code=${error.code}")
                        setState { copy(errorMessage = error.text ?: "Error") }
                        return@runOnUIThread
                    }

                    val list = (response as? TLRPC.TL_event_eventTypes)?.data.orEmpty()
                    setState {
                        copy(
                            eventTypes = list,
                            selectedEventType = list.getOrNull(0),
                            errorMessage = null
                        )
                    }
                }
            }
        )
    }

    fun getAvailableParameters() {
        val req = TLRPC.TL_event_getAvailableParameters()
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_event_availableParameters) {
                    response.data

                } else {
                    response
                }
                error
            },
        )

    }

    private val currentAccount = UserConfig.selectedAccount

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> {}
            is Intent.OnQueryChanged -> setState { copy(query = intent.value) }

            is Intent.OnCountrySelected -> {
                loadCities(intent.item.country_id)
                setState {
                    copy(
                        selectedCountry = intent.item
                    )
                }
            }

            is Intent.OnCitySelected -> {
                setState {
                    copy(
                        selectedCity = intent.item
                    )
                }
            }

            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            is Intent.OnCreateClicked -> {
                createEvent(
                    title = intent.title,
                    description = intent.description,
                    date = intent.date,
                    time = intent.time
                )
            }

            is Intent.OnCoverPhotoSelected -> {
                setState {
                    copy(
                        coverPhotoUploading = true,
                        coverPhotoPath = intent.localPath
                    )
                }
                uploadPhotoToServer(
                    inputFile = intent.photo,
                    onSuccess = { photoId ->
                        setState {
                            copy(
                                coverPhotoId = photoId,
                                coverPhotoUploading = false
                            )
                        }
                    },
                    onError = { errorMsg ->
                        setState {
                            copy(
                                coverPhotoUploading = false,
                                coverPhotoPath = null
                            )
                        }
                        sendEffect(Effect.ShowError(errorMsg))
                    }
                )
            }

            Intent.OnClearCoverPhoto -> {
                setState {
                    copy(
                        coverPhotoId = null,
                        coverPhotoPath = null
                    )
                }
            }
        }
    }

    // event.getEventTypes //
    //event.getAvailableParameters#79aac9ef user_id:long = Vector<event.AvailableParameter>;

    private fun createEvent(
        title: String,
        description: String,
        date: String,
        time: String
    ) {
        val currentState = state.value
        val req = TLRPC.TL_event_createEvent()

        req.title = title
        req.description = description

        req.event_date = "2026-03-03"
        req.event_time = "10:00:00+03:00[Europe/Kyiv]"

        req.event_type = state.value.selectedEventType

        currentState.coverPhotoId?.let { photoId ->
            req.cover_photo_id = photoId
        }

        // Set location if selected
        currentState.selectedCity?.let { city ->
            val location = TLRPC.TL_event_location()
            location.city = TLRPC.TL_event_city().apply {
                city_id = city.city_id
                this.city = city.city
            }
            currentState.selectedCountry?.let { country ->
                location.country = TLRPC.TL_event_country().apply {
                    country_id = country.country_id
                    this.country = country.country
                }
            }
            req.location = location
        }
        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                response
                error
                if (response is TLRPC.TL_event_event) {
                    sendEffect(Effect.NavigateBack)
                } else {
                    response
                }
                error
            },
        )
    }
}
