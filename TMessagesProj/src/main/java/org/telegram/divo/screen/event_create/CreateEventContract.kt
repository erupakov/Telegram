package org.telegram.divo.screen.event_create

import android.net.Uri
import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.EventFile
import org.telegram.divo.entity.EventType
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.dal.dto.payment.PaymentTypeDto
import org.telegram.divo.dal.dto.payment.PaymentFrequencyDto
import org.telegram.divo.entity.LocalCountry

data class State(
    val eventName: String = "",
    val eventDescription: String = "",
    val eventTypes: List<EventType> = emptyList(),
    val selectedEventType: EventType? = null,

    val allCountries: List<LocalCountry> = emptyList(),
    val selectedCountries: List<LocalCountry> = emptyList(),
    val eventDate: String = "",
    val eventTime: String = "",

    // Second page
    val role: ProfileParameter = ProfileParameter(ParametersType.ROLE, ""),
    val gender: ProfileParameter = ProfileParameter(ParametersType.GENDER, ""),
    val hairLength: ProfileParameter = ProfileParameter(ParametersType.HAIR_LENGTH, ""),
    val hairColor: ProfileParameter = ProfileParameter(ParametersType.HAIR_COLOR, ""),
    val eyeColor: ProfileParameter = ProfileParameter(ParametersType.EYE_COLOR, ""),
    val skinColor: ProfileParameter = ProfileParameter(ParametersType.SKIN_COLOR, ""),
    val blockParams: List<ProfileParameter> = listOf(),
    val hairLengthOptions: List<AppearanceItem> = emptyList(),
    val hairColorOptions: List<AppearanceItem> = emptyList(),
    val eyeColorOptions: List<AppearanceItem> = emptyList(),
    val skinColorOptions: List<AppearanceItem> = emptyList(),

    val eventRequirements: String = "",
    val isNdaRequired: Boolean = false,
    val maxParticipants: Int = 100,

    // Third page
    val deadlineDate: String = "",
    val deadlineTime: String = "",
    val isPaid: Boolean = false,
    val eventRate: String = "",
    val paymentTypes: List<PaymentTypeDto> = emptyList(),
    val paymentFrequencies: List<PaymentFrequencyDto> = emptyList(),
    val selectedPaymentType: PaymentTypeDto? = null,
    val selectedPaymentFrequency: PaymentFrequencyDto? = null,
    val isPublicEvent: Boolean = true,
    val galleryUris: List<Uri> = emptyList(),
    val existingGalleryFiles: List<EventFile> = emptyList(),
    val editingEventId: Int? = null,
    val isEditDataLoaded: Boolean = false,
    val currentUser: UserInfo = UserInfo(),
    val resetPagerToFirstPage: Boolean = false,

    val isUploading: Boolean = false,
) : ViewState {

    val isFirstPageValid: Boolean
        get() = galleryUris.firstOrNull() != null &&
                eventName.trim().isNotEmpty() &&
                eventDescription.trim().isNotEmpty() &&
                eventDate.isNotBlank() &&
                eventTime.isNotBlank()
                //&& selectedCountries.isNotEmpty()

    val isThirdPageValid: Boolean
        get() = deadlineDate.isNotBlank() &&
                deadlineTime.isNotBlank() &&
                (!isPaid || eventRate.trim().isNotEmpty())

    fun getDefaultBlockParams(): List<ProfileParameter> =
        listOf(
            ProfileParameter(ParametersType.AGE, ""),
            ProfileParameter(ParametersType.HEIGHT, ""),
            ProfileParameter(ParametersType.WEIGHT, ""),
            ProfileParameter(ParametersType.WAIST, ""),
            ProfileParameter(ParametersType.HIPS, ""),
            ProfileParameter(ParametersType.SHOE_SIZE, ""),
            ProfileParameter(ParametersType.BREAST_SIZE, ""),
        )
}

sealed interface Intent : ViewIntent {
    data object Load : Intent
    data class OnInitEdit(val eventId: Int?) : Intent
    data object OnBackClicked : Intent

    data class OnEventTypeSelected(val eventType: EventType) : Intent
    data class OnEventNameChanged(val value: String) : Intent
    data class OnEventDescriptionChanged(val value: String) : Intent
    data class OnAvatarSelected(val uri: Uri) : Intent
    data class OnEventDateChanged(val value: String) : Intent
    data class OnEventTimeChanged(val value: String) : Intent
    data class OnCountriesChanged(val countries: List<LocalCountry>) : Intent

    // Second page
    data class OnRoleChanged(val param: ProfileParameter) : Intent
    data class OnGenderChanged(val param: ProfileParameter) : Intent
    data class OnHairLengthChanged(val param: ProfileParameter) : Intent
    data class OnHairColorChanged(val param: ProfileParameter) : Intent
    data class OnEyeColorChanged(val param: ProfileParameter) : Intent
    data class OnSkinColorChanged(val param: ProfileParameter) : Intent
    data class OnBlockParamChanged(val param: ProfileParameter) : Intent
    data class OnRequirementsChanged(val value: String) : Intent
    data class OnNdaToggled(val value: Boolean) : Intent
    data class OnMaxParticipantsChanged(val value: Int) : Intent

    // Third page
    data class OnDeadlineDateChanged(val value: String) : Intent
    data class OnDeadlineTimeChanged(val value: String) : Intent
    data class OnIsPaidToggled(val value: Boolean) : Intent
    data class OnEventRateChanged(val value: String) : Intent
    data class OnPaymentTypeSelected(val paymentType: PaymentTypeDto) : Intent
    data class OnPaymentFrequencySelected(val paymentFrequency: PaymentFrequencyDto) : Intent
    data class OnIsPublicToggled(val value: Boolean) : Intent
    data class OnGalleryPhotosAdded(val uris: List<Uri>) : Intent
    data class OnGalleryPhotoRemoved(val uri: Uri) : Intent

    data object OnPreviewClicked : Intent
    data object OnEditFromPreviewClicked : Intent
    data object OnFirstPageReached : Intent
    data object OnPublishClicked : Intent
}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data object NavigateToPreview : Effect
    data object EventPublished : Effect
    data class ShowError(val message: String) : Effect
}