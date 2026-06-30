package org.telegram.divo.screen.event_create

import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.common.utils.MeasuringUnits
import org.telegram.divo.common.utils.uriToFile
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.dal.dto.event.toCreateEventRequest
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.LocalCountry
import org.telegram.divo.entity.UploadedFile
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.BufferedReader
import java.io.InputStreamReader

class CreateEventViewModel : BaseViewModel<State, Intent, Effect>() {

    init {
        loadCountries()
        getEventTypes()
        loadAppearances()
        loadPaymentTypes()
        loadCurrentUser()
    }

    override fun createInitialState(): State = State()

    private fun loadAppearances() {
        viewModelScope.launch {
            val result = DivoApi.userRepository.getAppearances()
            if (result is DivoResult.Success) {
                val dict = result.value
                setState {
                    copy(
                        hairLengthOptions = dict.hairLength.orEmpty(),
                        hairColorOptions  = dict.hairColor.orEmpty(),
                        eyeColorOptions   = dict.eyeColor.orEmpty(),
                        skinColorOptions  = dict.skinColor.orEmpty()
                    )
                }
            }
        }
    }

    private fun getEventTypes() {
        viewModelScope.launch {
            val res = DivoApi.eventRepository.getEventTypes()
            if (res is DivoResult.Success) {
                setState { copy(eventTypes = res.value) }
            } else {
                sendEffect(Effect.ShowError(res.getErrorMessage()))
            }
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> {}
            is Intent.OnInitEdit -> initEditMode(intent.eventId)
            Intent.OnBackClicked -> sendEffect(Effect.NavigateBack)
            Intent.OnPreviewClicked -> {
                DivoAnalytics.logEvent(AnalyticsEvent.EventCreatePreviewOpened())
                sendEffect(Effect.NavigateToPreview)
            }
            Intent.OnEditFromPreviewClicked -> setState { copy(resetPagerToFirstPage = true) }
            Intent.OnFirstPageReached -> setState { copy(resetPagerToFirstPage = false) }
            Intent.OnPublishClicked -> publishEvent()

            is Intent.OnEventTypeSelected -> setState { copy(selectedEventType = intent.eventType) }
            is Intent.OnEventNameChanged -> setState { copy(eventName = intent.value) }
            is Intent.OnEventDescriptionChanged -> setState { copy(eventDescription = intent.value) }
            is Intent.OnAvatarSelected -> setState {
                val rest = galleryUris.drop(1)
                copy(galleryUris = listOf(intent.uri) + rest)
            }
            is Intent.OnEventDateChanged -> setState { copy(eventDate = intent.value) }
            is Intent.OnEventTimeChanged -> setState { copy(eventTime = intent.value) }
            is Intent.OnCountriesChanged -> setState { copy(selectedCountries = intent.countries) }

            // Second page
            is Intent.OnRoleChanged -> setState { copy(role = intent.param) }
            is Intent.OnGenderChanged -> setState { copy(gender = intent.param) }
            is Intent.OnHairLengthChanged -> setState { copy(hairLength = intent.param) }
            is Intent.OnHairColorChanged -> setState { copy(hairColor = intent.param) }
            is Intent.OnEyeColorChanged -> setState { copy(eyeColor = intent.param) }
            is Intent.OnSkinColorChanged -> setState { copy(skinColor = intent.param) }
            is Intent.OnBlockParamChanged -> setState {
                val base = blockParams.ifEmpty { getDefaultBlockParams() }
                copy(blockParams = base.map {
                    if (it.type == intent.param.type) intent.param else it
                })
            }
            is Intent.OnRequirementsChanged -> setState { copy(eventRequirements = intent.value) }
            is Intent.OnNdaToggled -> setState { copy(isNdaRequired = intent.value) }
            is Intent.OnMaxParticipantsChanged -> setState { copy(maxParticipants = intent.value) }

            // Third page
            is Intent.OnDeadlineDateChanged -> setState { copy(deadlineDate = intent.value) }
            is Intent.OnDeadlineTimeChanged -> setState { copy(deadlineTime = intent.value) }
            is Intent.OnIsPaidToggled -> setState { copy(isPaid = intent.value) }
            is Intent.OnEventRateChanged -> setState { copy(eventRate = intent.value) }
            is Intent.OnPaymentTypeSelected -> setState { copy(selectedPaymentType = intent.paymentType) }
            is Intent.OnPaymentFrequencySelected -> setState { copy(selectedPaymentFrequency = intent.paymentFrequency) }
            is Intent.OnIsPublicToggled -> setState { copy(isPublicEvent = intent.value) }
            is Intent.OnGalleryPhotosAdded -> setState { copy(galleryUris = galleryUris + intent.uris) }
            is Intent.OnGalleryPhotoRemoved -> setState {
                copy(
                    galleryUris = galleryUris - intent.uri,
                    existingGalleryFiles = existingGalleryFiles.filterNot { it.fullUrl == intent.uri.toString() }
                )
            }
        }
    }

    private fun initEditMode(eventId: Int?) {
        if (eventId == null) return
        if (state.value.isEditDataLoaded && state.value.editingEventId == eventId) return
        loadEventForEdit(eventId)
    }

    private fun loadEventForEdit(eventId: Int) {
        viewModelScope.launch {
            when (val result = DivoApi.eventRepository.getEvent(eventId)) {
                is DivoResult.Success -> setState {
                    copyFromEvent(eventId, result.value)
                }
                else -> sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun State.copyFromEvent(eventId: Int, event: EventDetails): State {
        val eventDate = event.date.orEmpty()
        val deadlineDate = event.applicationDeadline.orEmpty()
        fun formatDateFromApi(apiDateStr: String): Pair<String, String> {
            if (apiDateStr.isBlank()) return "" to ""
            try {
                val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                val date = sdfIn.parse(apiDateStr) ?: return "" to ""
                val sdfOutDate = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.US)
                val sdfOutTime = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                return sdfOutDate.format(date) to sdfOutTime.format(date).uppercase(java.util.Locale.US)
            } catch (e: Exception) {
                return apiDateStr.substringBefore(" ", "") to apiDateStr.substringAfter(" ", "")
            }
        }

        val (datePart, timePart) = formatDateFromApi(eventDate)
        val (deadlineDatePart, deadlineTimePart) = formatDateFromApi(deadlineDate)

        val sortedFiles = event.files.sortedBy { it.order }
        val gallery = sortedFiles.map { Uri.parse(it.fullUrl) }

        val attrs = event.modelAttributes
        val storedSystem = MeasuringUnits.resolveStoredSystem(attrs?.measuringSystem)
        fun rangeToStr(type: ParametersType, from: Int?, to: Int?): String {
            if (from == null && to == null) return ""
            return MeasuringUnits.formatStoredRange(type, from, to, storedSystem)
        }

        val roleValue = attrs?.roles?.joinToString(", ") ?: ""
        val genderValue = attrs?.genders?.joinToString(", ") ?: ""
        val hairLengthValue = attrs?.hairLengths?.joinToString(", ") ?: ""
        val hairColorValue = attrs?.hairColors?.joinToString(", ") ?: ""
        val eyeColorValue = attrs?.eyeColors?.joinToString(", ") ?: ""
        val skinColorValue = attrs?.skinColors?.joinToString(", ") ?: ""

        return copy(
            editingEventId = eventId,
            isEditDataLoaded = true,
            eventName = event.title.orEmpty(),
            eventDescription = event.description.orEmpty(),
            selectedEventType = eventTypes.find { it.title == event.type } ?: selectedEventType,
            eventDate = datePart,
            eventTime = timePart,
            selectedCountries = allCountries.filter { it.name == event.address?.countryName }.ifEmpty { selectedCountries },
            deadlineDate = deadlineDatePart,
            deadlineTime = deadlineTimePart,
            isPaid = event.cost?.isNotBlank() == true,
            eventRate = event.cost.orEmpty(),
            role = role.copy(value = roleValue),
            gender = gender.copy(value = genderValue),
            hairLength = hairLength.copy(value = hairLengthValue),
            hairColor = hairColor.copy(value = hairColorValue),
            eyeColor = eyeColor.copy(value = eyeColorValue),
            skinColor = skinColor.copy(value = skinColorValue),
            blockParams = listOf(
                ProfileParameter(ParametersType.AGE, rangeToStr(ParametersType.AGE, attrs?.ageFrom, attrs?.ageTo)),
                ProfileParameter(ParametersType.HEIGHT, rangeToStr(ParametersType.HEIGHT, attrs?.heightFrom, attrs?.heightTo)),
                ProfileParameter(ParametersType.WEIGHT, rangeToStr(ParametersType.WEIGHT, attrs?.weightFrom, attrs?.weightTo)),
                ProfileParameter(ParametersType.WAIST, rangeToStr(ParametersType.WAIST, attrs?.waistFrom, attrs?.waistTo)),
                ProfileParameter(ParametersType.HIPS, rangeToStr(ParametersType.HIPS, attrs?.hipsFrom, attrs?.hipsTo)),
                ProfileParameter(ParametersType.SHOE_SIZE, rangeToStr(ParametersType.SHOE_SIZE, attrs?.shoesSizeFrom, attrs?.shoesSizeTo)),
                ProfileParameter(ParametersType.BREAST_SIZE, rangeToStr(ParametersType.BREAST_SIZE, attrs?.breastSizeFrom, attrs?.breastSizeTo))
            ),
            galleryUris = gallery,
            existingGalleryFiles = sortedFiles,
            eventRequirements = event.description.orEmpty()
        )
    }

    private fun publishEvent() {
        viewModelScope.launch {
            setState { copy(isUploading = true) }
            try {
                val uploadedFiles = mutableListOf<UploadedFile>()
                val uris = state.value.galleryUris
                val existingByUrl = state.value.existingGalleryFiles.associateBy { it.fullUrl }
                
                if (uris.isNotEmpty()) {
                    for (uri in uris) {
                        val existing = existingByUrl[uri.toString()]
                        if (existing != null) {
                            uploadedFiles.add(UploadedFile(existing.fileUuid, existing.fullUrl))
                            continue
                        }
                        val fileResult = ApplicationLoader.applicationContext.uriToFile(uri)
                        fileResult.getOrNull()?.let { file ->
                            val uploadResult = DivoApi.userRepository.uploadPhoto(file)
                            if (uploadResult is DivoResult.Success) {
                                uploadedFiles.add(uploadResult.value)
                            }
                        }
                    }
                }
                
                val request = state.value.toCreateEventRequest(uploadedFiles)
                val result = state.value.editingEventId?.let { eventId ->
                    DivoApi.eventRepository.updateEvent(eventId, request)
                } ?: DivoApi.eventRepository.createEvent(request)
                if (result is DivoResult.Success) {
                    if (state.value.editingEventId != null) {
                        DivoAnalytics.logEvent(AnalyticsEvent.EventEdited(state.value.editingEventId!!.toLong()))
                    } else {
                        DivoAnalytics.logEvent(AnalyticsEvent.EventCreateSuccess(result.value.id.toLong()))
                    }
                    sendEffect(Effect.EventPublished)
                    setState { copy(isUploading = false) }
                } else {
                    sendEffect(Effect.ShowError(result.getErrorMessage()))
                    setState { copy(isUploading = false) }
                }
            } catch (e: Exception) {
                sendEffect(Effect.ShowError(e.message ?: "Unknown error"))
                setState { copy(isUploading = false) }
            }
        }
    }

    private fun loadPaymentTypes() {
        viewModelScope.launch {
            val result = DivoApi.paymentRepository.getPayments()
            if (result is DivoResult.Success) {
                val payments = result.value.data
                setState {
                    copy(
                        paymentTypes = payments.paymentType,
                        paymentFrequencies = payments.paymentFrequency,
                        selectedPaymentType = payments.paymentType.find { it.title == "Free" },
                        selectedPaymentFrequency = payments.paymentFrequency.firstOrNull()
                    )
                }
            } else {
                sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun loadCountries() {
        viewModelScope.launch(Dispatchers.IO) {
            val list = mutableListOf<LocalCountry>()
            try {
                val stream = ApplicationLoader.applicationContext.assets.open("countries.txt")
                val reader = BufferedReader(InputStreamReader(stream))
                reader.forEachLine { line ->
                    val args = line.split(";")
                    if (args.size >= 3) {
                        val code = args[0]
                        val shortname = args[1]
                        val defaultName = args[2]
                        val locName = LocaleController.getCountryName(shortname)
                        val name = if (!locName.isNullOrEmpty()) locName else defaultName
                        val flag = LocaleController.getLanguageFlag(shortname)
                        list.add(
                            LocalCountry(
                                code = code,
                                shortName = shortname,
                                name = name,
                                flag = flag
                            )
                        )
                    }
                }
                reader.close()
                stream.close()

                list.sortBy { it.name }

                setState { copy(allCountries = list) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            when (val result = DivoApi.userRepository.getCurrentUserInfo()) {
                is DivoResult.Success -> setState { copy(currentUser = result.value) }
                else -> sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }
}
