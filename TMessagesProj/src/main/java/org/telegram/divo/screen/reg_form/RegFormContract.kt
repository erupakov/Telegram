package org.telegram.divo.screen.reg_form

import android.net.Uri
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.screen.search.LocalCity

data class RegFormsState(
    val steps: List<RegFormStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val formData: RegistrationFormData? = null,
    val isLoading: Boolean = false,
    val allCountries: List<LocalCountry> = emptyList(),
    val allCities: List<LocalCity> = emptyList(),
    val currentAccount: Int = 0,
    val phoneHash: String = "",
    val phoneNumber: String = "",
    val firebaseUid: String? = null,
    val googleEmail: String? = null,
) : ViewState {
    val currentStep: RegFormStep? get() = steps.getOrNull(currentStepIndex)
    val totalSteps: Int get() = steps.size
    val isLastStep: Boolean get() = currentStepIndex == steps.lastIndex

    val canContinue: Boolean get() = formData?.isStepValid(currentStep) ?: false

    private fun RegistrationFormData.isStepValid(step: RegFormStep?): Boolean = when (step) {
        RegFormStep.IDENTITY,
        RegFormStep.CREATIVE_IDENTITY -> firstName.isNotBlank() && lastName.isNotBlank()

        RegFormStep.COMPANY_INFO,
        RegFormStep.STUDIO_DETAILS -> companyName.isNotBlank() && (country.isBlank() || city != null)

        RegFormStep.TALENT_IDENTITY,
        RegFormStep.FAN_IDENTITY -> firstName.isNotBlank() && lastName.isNotBlank() && (country.isBlank() || city != null)

        // Location steps: city is required only when country is selected
        RegFormStep.COMPANY_LOCATION,
        RegFormStep.TALENT_LOCATION,
        RegFormStep.PERSONAL_DETAILS -> country.isBlank() || city != null

        else -> true
    }

    val isCurrentStepEmpty: Boolean get() = formData?.isStepEmpty(currentStep) ?: true

    private fun RegistrationFormData.isStepEmpty(step: RegFormStep?): Boolean = when (step) {
        // Step 2
        RegFormStep.COMPANY_LOCATION,
        RegFormStep.PERSONAL_DETAILS,
        RegFormStep.TALENT_LOCATION -> country.isBlank() && city == null && dateOfBirth.isNullOrEmpty() && gender.isNullOrEmpty() && specialisation.isNullOrEmpty()
        
        RegFormStep.STUDIO_CONTACT -> websiteUrl.isBlank() && contactName.isBlank() && contactPhone.isBlank()

        // Step 3
        RegFormStep.COMPANY_CONTACT -> websiteUrl.isBlank() && contactRole.isBlank() && contactName.isBlank() && contactPhone.isBlank()
        
        RegFormStep.PROFESSIONAL_LINKS,
        RegFormStep.TALENT_LINKS,
        RegFormStep.PORTFOLIO -> instagramUrl.isBlank() && portfolioUrl.isBlank() && agencyName.isBlank() && castingProfileUrl.isBlank() && showreelUrl.isBlank()

        // Step 4
        RegFormStep.COMPANY_PHOTO,
        RegFormStep.PROFILE_PHOTO,
        RegFormStep.STUDIO_PHOTO,
        RegFormStep.TALENT_PHOTO,
        RegFormStep.FAN_PHOTO -> photoUri == null

        else -> false
    }
}

sealed class RegFormsIntent : ViewIntent {
    data class Init(
        val subRole: SubRole,
        val currentAccount: Int,
        val phoneHash: String,
        val phoneNumber: String,
        val firebaseUid: String? = null,
        val googleEmail: String? = null,
    ) : RegFormsIntent()
    data class OnFieldChanged(val update: RegistrationFormData.() -> RegistrationFormData) : RegFormsIntent()
    data object OnContinue : RegFormsIntent()
    data object OnBack : RegFormsIntent()
}

sealed class RegFormsEffect : ViewEffect {
    data object NavigateBack : RegFormsEffect()
    data object NavigateBackToPhone : RegFormsEffect()
    data class FinishRegistration(val authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization) : RegFormsEffect()
    data class ShowError(val message: String) : RegFormsEffect()
}

// Накопитель данных всего флоу регистрации
data class RegistrationFormData(
    val subRole: SubRole,
    // Common
    val firstName: String = "",
    val lastName: String = "",
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val country: String = "",
    val countryCode: String = "",
    val city: LocalCity? = null,
    // Company
    val companyName: String = "",
    val websiteUrl: String = "",
    val contactRole: String = "",
    // Creative / Professional
    val specialisation: String? = null,
    val instagramUrl: String = "",
    val portfolioUrl: String = "",
    val agencyName: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val castingProfileUrl: String = "",
    // Showreel (Actor/Dancer/Singer)
    val showreelUrl: String = "",
    // Photo
    val photoUri: Uri? = null,
)

// Шаги — определяются по subRole
enum class RegFormStep {
    // Companies
    COMPANY_INFO,
    COMPANY_LOCATION,
    COMPANY_CONTACT,
    COMPANY_PHOTO,
    // Industry Pros
    IDENTITY,
    PERSONAL_DETAILS,
    PROFESSIONAL_LINKS,
    PROFILE_PHOTO,
    // Creative Individual
    CREATIVE_IDENTITY,
    PORTFOLIO,
    // Studio
    STUDIO_DETAILS,
    STUDIO_CONTACT,
    STUDIO_PHOTO,
    // Talent
    TALENT_IDENTITY,
    TALENT_LOCATION,
    TALENT_LINKS,
    TALENT_PHOTO,
    // Fan
    FAN_IDENTITY,
    FAN_PHOTO,
}

fun SubRole.formSteps(): List<RegFormStep> = when (this) {
    // Companies (A)
    SubRole.MODELING_AGENCY,
    SubRole.FASHION_BRAND,
    SubRole.BEAUTY_BRAND,
    SubRole.BRAND_OR_BUSINESS,
    SubRole.EVENT_AGENCY,
    SubRole.MAGAZINE -> listOf(
        RegFormStep.COMPANY_INFO,
        RegFormStep.COMPANY_LOCATION,
        RegFormStep.COMPANY_CONTACT,
        RegFormStep.COMPANY_PHOTO,
    )
    // Industry Pros (B)
    SubRole.SCOUT,
    SubRole.BOOKER,
    SubRole.CASTING_DIRECTOR,
    SubRole.TALENT_MANAGER -> listOf(
        RegFormStep.IDENTITY,
        RegFormStep.PERSONAL_DETAILS,
        RegFormStep.PROFESSIONAL_LINKS,
        RegFormStep.PROFILE_PHOTO,
    )
    // Creative Individual (C1)
    SubRole.PHOTOGRAPHER,
    SubRole.STYLIST,
    SubRole.MUA,
    SubRole.HAIR_STYLIST,
    SubRole.VIDEOGRAPHER,
    SubRole.CREATIVE_DIRECTOR,
    SubRole.FASHION_DESIGNER -> listOf(
        RegFormStep.CREATIVE_IDENTITY,
        RegFormStep.PERSONAL_DETAILS,
        RegFormStep.PORTFOLIO,
        RegFormStep.PROFILE_PHOTO,
    )
    // Creative Individual (C2)
    SubRole.STUDIO -> listOf(
        RegFormStep.STUDIO_DETAILS,
        RegFormStep.STUDIO_CONTACT,
        RegFormStep.STUDIO_PHOTO,
    )
    // Talent: Model (D1)
    SubRole.MODEL -> listOf(
        RegFormStep.TALENT_IDENTITY,
        RegFormStep.TALENT_LOCATION,
        RegFormStep.TALENT_LINKS,
        RegFormStep.TALENT_PHOTO,
    )
    // Talent: New Talent (D2)
    SubRole.NEW_TALENT -> listOf(
        RegFormStep.TALENT_IDENTITY,
        RegFormStep.TALENT_LOCATION,
        RegFormStep.PROFESSIONAL_LINKS,
        RegFormStep.TALENT_PHOTO,
    )
    // Talent: Actor / Dancer / Singer (D3/D4/D5)
    SubRole.ACTOR,
    SubRole.DANCER,
    SubRole.SINGER -> listOf(
        RegFormStep.TALENT_IDENTITY,
        RegFormStep.TALENT_LOCATION,
        RegFormStep.PROFESSIONAL_LINKS,
        RegFormStep.PROFILE_PHOTO,
    )
    // Fan (E)
    SubRole.FAN -> listOf(
        RegFormStep.FAN_IDENTITY,
        RegFormStep.FAN_PHOTO,
    )
}