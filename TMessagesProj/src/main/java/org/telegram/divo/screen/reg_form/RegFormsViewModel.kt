package org.telegram.divo.screen.reg_form

import android.os.Build
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.search.LocalCity
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.LocaleController
import java.io.InputStreamReader
import kotlinx.coroutines.suspendCancellableCoroutine
import org.telegram.divo.common.AdditionalInfoKeys
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.dto.auth.RegistrationRequest
import org.telegram.divo.dal.dto.auth.TelegramLinkRequest
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.tgnet.tl.TL_account
import java.io.BufferedReader

class RegFormsViewModel : BaseViewModel<RegFormsState, RegFormsIntent, RegFormsEffect>() {

    override fun createInitialState() = RegFormsState()

    override fun handleIntent(intent: RegFormsIntent) {
        when (intent) {
            is RegFormsIntent.Init -> init(intent)
            is RegFormsIntent.OnFieldChanged -> onFieldChanged(intent.update)
            is RegFormsIntent.OnContinue -> onContinue()
            is RegFormsIntent.OnBack -> onBack()
        }
    }

    private fun init(intent: RegFormsIntent.Init) {
        setState {
            copy(
                steps = intent.subRole.formSteps(),
                formData = RegistrationFormData(subRole = intent.subRole),
                currentAccount = intent.currentAccount,
                phoneHash = intent.phoneHash,
                phoneNumber = intent.phoneNumber,
                firebaseUid = intent.firebaseUid,
                googleEmail = intent.googleEmail,
            )
        }
        loadCountries()
        loadCities()
    }

    private fun onFieldChanged(update: RegistrationFormData.() -> RegistrationFormData) {
        val current = state.value.formData ?: return
        setState { copy(formData = current.update()) }
    }

    private fun onContinue() {
        logFormData()
        if (state.value.isLastStep) {
            setState { copy(isLoading = true) }
            val data = state.value.formData ?: return

            viewModelScope.launch {
                var telegramPhotoFile: java.io.File? = null
                try {
                    // 0. Upload Photo (can be done without auth)
                    var uploadedPhotoUuid: String? = null
                    var uploadedPhotoUrl: String? = null
                    data.photoUri?.let { uri ->
                        try {
                            val context = ApplicationLoader.applicationContext
                            val inputStream = context.contentResolver.openInputStream(uri)
                            if (inputStream != null) {
                                val tempFile = java.io.File(context.cacheDir, "upload_avatar_${System.currentTimeMillis()}.jpg")
                                val outputStream = java.io.FileOutputStream(tempFile)
                                inputStream.copyTo(outputStream)
                                inputStream.close()
                                outputStream.close()
                                
                                val uploadResult = DivoApi.userRepository.uploadPhoto(tempFile)
                                if (uploadResult is DivoResult.Success) {
                                    uploadedPhotoUuid = uploadResult.value.uuid
                                    uploadedPhotoUrl = uploadResult.value.fullUrl
                                }
                                telegramPhotoFile = tempFile
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 1. Divo Registration (REST)
                    val tgUser = org.telegram.messenger.UserConfig.getInstance(state.value.currentAccount).currentUser

                    val rawPhone = tgUser?.phone?.takeIf { it.isNotBlank() } ?: state.value.phoneNumber
                    val phone = rawPhone.replace("+", "").trim()

                    val firebaseUid = state.value.firebaseUid
                    val email = if (firebaseUid != null) {
                        state.value.googleEmail ?: "$phone@divo.global"
                    } else {
                        "$phone@divo.global"
                    }
                    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
                    val model = Build.MODEL ?: "Android Device"
                    val deviceId = "$manufacturer $model"
                    val deviceType = "android"

                    val (mappedRole, mappedSubrole) = data.subRole.toDivoRoleAndSubrole()
                    
                    val additionalInfo = mutableMapOf<String, Any>()
                    // Raw form fields
                    if (data.firstName.isNotBlank()) additionalInfo[AdditionalInfoKeys.FIRST_NAME] = data.firstName
                    if (data.lastName.isNotBlank()) additionalInfo[AdditionalInfoKeys.LAST_NAME] = data.lastName
                    if (!data.dateOfBirth.isNullOrBlank()) additionalInfo[AdditionalInfoKeys.DATE_OF_BIRTH] = data.dateOfBirth
                    if (!data.gender.isNullOrBlank()) additionalInfo[AdditionalInfoKeys.GENDER] = data.gender.lowercase(java.util.Locale.US)
                    if (data.country.isNotBlank()) additionalInfo[AdditionalInfoKeys.COUNTRY] = data.country
                    if (data.countryCode.isNotBlank()) additionalInfo[AdditionalInfoKeys.COUNTRY_CODE] = data.countryCode
                    if (data.city != null) additionalInfo[AdditionalInfoKeys.CITY] = data.city.name
                    
                    if (data.companyName.isNotBlank()) additionalInfo[AdditionalInfoKeys.COMPANY_NAME] = data.companyName
                    if (data.websiteUrl.isNotBlank()) additionalInfo[AdditionalInfoKeys.WEBSITE_URL] = data.websiteUrl
                    if (data.contactRole.isNotBlank()) additionalInfo[AdditionalInfoKeys.CONTACT_ROLE] = data.contactRole
                    if (data.contactName.isNotBlank()) additionalInfo[AdditionalInfoKeys.CONTACT_NAME] = data.contactName
                    if (data.contactPhone.isNotBlank()) additionalInfo[AdditionalInfoKeys.CONTACT_PHONE] = data.contactPhone
                    
                    if (!data.specialisation.isNullOrBlank()) additionalInfo[AdditionalInfoKeys.SPECIALISATION] = data.specialisation
                    if (data.instagramUrl.isNotBlank()) additionalInfo[AdditionalInfoKeys.INSTAGRAM_URL] = data.instagramUrl
                    if (data.portfolioUrl.isNotBlank()) additionalInfo[AdditionalInfoKeys.PORTFOLIO_URL] = data.portfolioUrl
                    if (data.agencyName.isNotBlank()) additionalInfo[AdditionalInfoKeys.AGENCY_NAME] = data.agencyName
                    
                    if (data.showreelUrl.isNotBlank()) additionalInfo[AdditionalInfoKeys.SHOWREEL_URL] = data.showreelUrl
                    if (data.castingProfileUrl.isNotBlank()) additionalInfo[AdditionalInfoKeys.CASTING_PROFILE_URL] = data.castingProfileUrl
                    if (uploadedPhotoUuid != null) additionalInfo[AdditionalInfoKeys.PHOTO_UUID] = uploadedPhotoUuid
                    if (uploadedPhotoUrl != null) additionalInfo[AdditionalInfoKeys.PHOTO_URL] = uploadedPhotoUrl

                    // Computed / derived values
                    val fullNameForInfo = listOf(data.firstName, data.lastName).filter { it.isNotBlank() }.joinToString(" ")
                        .ifBlank { data.companyName }
                    if (fullNameForInfo.isNotBlank()) additionalInfo[AdditionalInfoKeys.FULL_NAME] = fullNameForInfo
                    if (rawPhone.isNotBlank()) additionalInfo[AdditionalInfoKeys.PHONE] = rawPhone
                    additionalInfo[AdditionalInfoKeys.EMAIL] = email
                    additionalInfo[AdditionalInfoKeys.TIMEZONE] = java.util.TimeZone.getDefault().id
                    additionalInfo[AdditionalInfoKeys.MEASURING_SYSTEM] = "metric"
                    additionalInfo[AdditionalInfoKeys.SUB_ROLE] = data.subRole.name.lowercase()
                    if (mappedSubrole != null) additionalInfo[AdditionalInfoKeys.SUBROLE_MAPPED] = mappedSubrole

                    if (tgUser != null) {
                        additionalInfo[AdditionalInfoKeys.TELEGRAM_ID] = tgUser.id
                        if (!tgUser.username.isNullOrBlank()) {
                            additionalInfo[AdditionalInfoKeys.TELEGRAM_USERNAME] = tgUser.username
                        }
                    }

                    // Branch: social registration vs regular registration
                    val divoUserId: Long?
                    if (firebaseUid != null) {
                        // Google Sign-In flow → registration-social
                        val socialRequest = org.telegram.divo.dal.dto.auth.SocialRegistrationRequest(
                            email = email,
                            role = mappedRole,
                            subrole = mappedSubrole,
                            providerId = "google.com",
                            uid = firebaseUid,
                            timezone = java.util.TimeZone.getDefault().id,
                            deviceId = deviceId,
                            deviceType = deviceType,
                            additionalInfo = additionalInfo
                        )
                        val divoResponse = DivoApi.authRepository.registrationSocial(socialRequest)
                        if (divoResponse !is DivoResult.Success) {
                            val errorMessage = divoResponse.getErrorMessage()
                            sendEffect(RegFormsEffect.ShowError("Registration failed: $errorMessage"))
                            setState { copy(isLoading = false) }
                            return@launch
                        }
                        DivoApi.accessTokenProvider.setGoogleLogin(true)
                        divoUserId = divoResponse.value.data?.user?.id
                    } else {
                        // Phone flow → regular registration
                        val regRequest = RegistrationRequest(
                            email = email,
                            role = mappedRole,
                            password = "divo_${phone}",
                            subrole = mappedSubrole,
                            deviceId = deviceId,
                            deviceType = deviceType,
                            additionalInfo = additionalInfo
                        )
                        val divoResponse = DivoApi.authRepository.register(regRequest)
                        if (divoResponse !is DivoResult.Success) {
                            val errorMessage = divoResponse.getErrorMessage()
                            sendEffect(RegFormsEffect.ShowError("Registration failed: $errorMessage"))
                            setState { copy(isLoading = false) }
                            return@launch
                        }
                        DivoApi.accessTokenProvider.setGoogleLogin(false)
                        divoUserId = divoResponse.value.data?.user?.id
                    }

                    // 2. TG Profile Update
                    var firstName = data.firstName
                    var lastName = data.lastName
                    if (firstName.isBlank() && data.companyName.isNotBlank()) {
                        firstName = data.companyName
                        lastName = ""
                    }
                    if (firstName.isBlank()) {
                        firstName = "User"
                    }

                    val req = TL_account.updateProfile().apply {
                        flags = 1 or 2 // 1 = first_name, 2 = last_name
                        first_name = firstName
                        last_name = lastName
                    }

                    val profileUpdateResult = kotlinx.coroutines.withTimeoutOrNull(5000) {
                        suspendCancellableCoroutine<TLRPC.User> { continuation ->
                            val reqId = ConnectionsManager.getInstance(state.value.currentAccount).sendRequest(req) { response, error ->
                                if (error != null) {
                                    continuation.resumeWithException(RuntimeException(error.text))
                                } else if (response is TLRPC.User) {
                                    continuation.resume(response)
                                } else {
                                    continuation.resumeWithException(RuntimeException("Invalid response type"))
                                }
                            }
                            continuation.invokeOnCancellation {
                                ConnectionsManager.getInstance(state.value.currentAccount).cancelRequest(reqId, true)
                            }
                        }
                    }

                    // 2.5 TG Profile Photo Update
                    if (telegramPhotoFile != null) {
                        try {
                            val inputFile = suspendCancellableCoroutine<org.telegram.tgnet.TLRPC.InputFile?> { continuation ->
                                org.telegram.messenger.FileLoader.getInstance(state.value.currentAccount).uploadFile(telegramPhotoFile!!.absolutePath) { result ->
                                    continuation.resume(result)
                                }
                            }
                            if (inputFile != null) {
                                val photoReq = TLRPC.TL_photos_uploadProfilePhoto().apply {
                                    file = inputFile
                                    flags = flags or 1
                                }
                                val photoResult = kotlinx.coroutines.withTimeoutOrNull(10000) {
                                    suspendCancellableCoroutine<TLRPC.TL_photos_photo?> { continuation ->
                                        val reqId = ConnectionsManager.getInstance(state.value.currentAccount).sendRequest(photoReq) { response, error ->
                                            if (error == null && response is TLRPC.TL_photos_photo) {
                                                continuation.resume(response)
                                            } else {
                                                continuation.resume(null)
                                            }
                                        }
                                        continuation.invokeOnCancellation {
                                            ConnectionsManager.getInstance(state.value.currentAccount).cancelRequest(reqId, true)
                                        }
                                    }
                                }
                                if (photoResult != null) {
                                    val uc = org.telegram.messenger.UserConfig.getInstance(state.value.currentAccount)
                                    val currentUser = uc.currentUser
                                    if (currentUser != null && photoResult.photo != null) {
                                        val bigSize = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(photoResult.photo.sizes, 800)
                                        val smallSize = org.telegram.messenger.FileLoader.getClosestPhotoSizeWithSize(photoResult.photo.sizes, 150)
                                        if (smallSize != null && bigSize != null) {
                                            if (currentUser.photo == null) {
                                                currentUser.photo = TLRPC.TL_userProfilePhoto()
                                            }
                                            currentUser.photo.photo_id = photoResult.photo.id
                                            currentUser.photo.photo_small = smallSize.location
                                            currentUser.photo.photo_big = bigSize.location
                                            currentUser.photo.dc_id = photoResult.photo.dc_id
                                            uc.setCurrentUser(currentUser)
                                            uc.saveConfig(true)
                                        }
                                    }
                                    org.telegram.messenger.MessagesController.getInstance(state.value.currentAccount).putUsers(photoResult.users, false)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // 3. Divo Link (REST)
                    val tgUserId = profileUpdateResult?.id ?: tgUser?.id ?: 0L

                    val linkRequest = TelegramLinkRequest(
                        divoUserId = divoUserId,
                        telegramUserId = tgUserId,
                        phone = rawPhone,
                        deviceId = deviceId,
                        deviceType = deviceType
                    )
                    
                    val linkResponse = DivoApi.authRepository.linkTelegramAccount(linkRequest)
                    if (linkResponse !is DivoResult.Success) {
                        val errorMessage = linkResponse.getErrorMessage()

                        DivoApi.accessTokenProvider.setAccessToken(null)
                        sendEffect(RegFormsEffect.ShowError("Linking failed: $errorMessage"))
                        setState { copy(isLoading = false) }
                        return@launch
                    }

                    // 4. Divo Update Profile (REST)
                    val fullName = listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
                    val photoContainer = uploadedPhotoUuid?.let { org.telegram.divo.dal.dto.common.UuidContainerDto(it) }

                    // Resolve geoCityId from city name via geo API
                    var resolvedCityId: Int? = null
                    if (data.city != null) {
                        try {
                            val geoResponse = DivoApi.geoService.searchByAddressName(data.city.name)
                            resolvedCityId = geoResponse.data?.firstOrNull()?.city?.id
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    // Build role-specific DTOs from form data
                    val modelDto: org.telegram.divo.dal.dto.user.UpdateProfileModelDto? =
                        if (mappedRole == RoleType.MODEL.value || mappedRole == RoleType.NEW_FACE.value || mappedRole == RoleType.FAN.value) {
                            org.telegram.divo.dal.dto.user.UpdateProfileModelDto(
                                agencyId = null,
                                profileUrl = data.castingProfileUrl.takeIf { it.isNotBlank() },
                                education = null,
                                workExperience = null,
                                description = null,
                                languages = null,
                                hasInternationalPassport = false,
                                hasTattoo = false,
                                hasPiercing = false,
                                hasActingEducation = false,
                                appearance = null,
                                tiktokUrl = null,
                                youtubeUrl = data.showreelUrl.takeIf { it.isNotBlank() },
                                instagramUrl = data.instagramUrl.takeIf { it.isNotBlank() },
                                websiteUrl = data.portfolioUrl.takeIf { it.isNotBlank() },
                            )
                        } else null

                    val customerDto: org.telegram.divo.dal.dto.common.CustomerDto? =
                        if (mappedRole == RoleType.CUSTOMER.value) {
                            org.telegram.divo.dal.dto.common.CustomerDto(
                                site = data.websiteUrl.takeIf { it.isNotBlank() }
                                    ?: data.portfolioUrl.takeIf { it.isNotBlank() },
                                description = data.specialisation?.takeIf { it.isNotBlank() },
                            )
                        } else null

                    val agencyDto: org.telegram.divo.dal.dto.user.UpdateProfileAgencyRequest? =
                        if (mappedRole == RoleType.AGENCY.value) {
                            org.telegram.divo.dal.dto.user.UpdateProfileAgencyRequest(
                                agencyId = null,
                                title = data.companyName.takeIf { it.isNotBlank() },
                                description = data.websiteUrl.takeIf { it.isNotBlank() },
                                address = null,
                                background = null,
                                photo = photoContainer,
                            )
                        } else null
                    
                    val updateProfileRequest = org.telegram.divo.dal.dto.user.UpdateProfileRequest(
                        fullName = fullName,
                        phone = rawPhone,
                        timezone = java.util.TimeZone.getDefault().id,
                        gender = data.gender?.lowercase()?.takeIf { it.isNotBlank() },
                        birthday = data.dateOfBirth ?: "",
                        geoCityId = resolvedCityId?.takeIf { it > 0 },
                        measuringSystem = "metric",
                        subrole = mappedSubrole,
                        pushNotifications = true,
                        isRegistrationFinished = true,
                        photo = photoContainer,
                        avatar = photoContainer,
                        model = modelDto,
                        agency = agencyDto,
                        customer = customerDto
                    )
                    
                    val updateResponse = DivoApi.userRepository.updateProfile(updateProfileRequest)
                    if (updateResponse !is DivoResult.Success) {
                        val errorMessage = updateResponse.getErrorMessage()
                        sendEffect(RegFormsEffect.ShowError(errorMessage))
                    }

                    val dummyAuth = TLRPC.TL_auth_authorization().apply {
                        user = profileUpdateResult ?: tgUser
                    }
                    sendEffect(RegFormsEffect.FinishRegistration(dummyAuth))
                } catch (e: Exception) {
                    e.printStackTrace()
                    setState { copy(isLoading = false) }
                    if (e.message?.contains("PHONE_CODE_EXPIRED") == true) {
                        sendEffect(RegFormsEffect.NavigateBackToPhone)
                    } else {
                        sendEffect(RegFormsEffect.ShowError(e.message ?: "Unknown error occurred"))
                    }
                } finally {
                    telegramPhotoFile?.delete()
                }
            }
        } else {
            setState { copy(currentStepIndex = currentStepIndex + 1) }
        }
    }

    private fun onBack() {
        if (state.value.currentStepIndex == 0) {
            sendEffect(RegFormsEffect.NavigateBack)
        } else {
            setState { copy(currentStepIndex = currentStepIndex - 1) }
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

    private fun loadCities() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cities = mutableListOf<LocalCity>()
                val stream = ApplicationLoader.applicationContext.assets.open("cities.txt")
                stream.bufferedReader().forEachLine { line ->
                    val cols = line.split("\t")
                    if (cols.size > 14) {
                        cities.add(
                            LocalCity(
                                id = cols[0].toLongOrNull() ?: return@forEachLine,
                                name = cols[1],
                                asciiName = cols[2],
                                alternateNames = cols[3],
                                countryCode = cols[8],
                                population = cols[14].toIntOrNull() ?: 0
                            )
                        )
                    }
                }
                stream.close()
                setState { copy(allCities = cities) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun logFormData() {
        val data = state.value.formData ?: return
        android.util.Log.d("RegForm", buildString {
            appendLine("=== REGISTRATION FORM DATA ===")
            appendLine("SubRole: ${data.subRole}")
            appendLine("--- Common ---")
            appendLine("First name: ${data.firstName}")
            appendLine("Last name: ${data.lastName}")
            appendLine("Date of birth: ${data.dateOfBirth}")
            appendLine("Gender: ${data.gender}")
            appendLine("Country: ${data.country}")
            appendLine("City: ${data.city?.name}")
            appendLine("--- Company ---")
            appendLine("Company name: ${data.companyName}")
            appendLine("Website URL: ${data.websiteUrl}")
            appendLine("Contact role: ${data.contactRole}")
            appendLine("Contact name: ${data.contactName}")
            appendLine("Contact phone: ${data.contactPhone}")
            appendLine("--- Creative / Professional ---")
            appendLine("Specialisation: ${data.specialisation}")
            appendLine("Instagram URL: ${data.instagramUrl}")
            appendLine("Portfolio URL: ${data.portfolioUrl}")
            appendLine("Agency name: ${data.agencyName}")
            appendLine("--- Talent ---")
            appendLine("Showreel URL: ${data.showreelUrl}")
            appendLine("Casting profile URL: ${data.castingProfileUrl}")
            appendLine("--- Photo ---")
            appendLine("Photo URI: ${data.photoUri}")
            appendLine("==============================")
        })
    }

    private fun SubRole.toDivoRoleAndSubrole(): Pair<String, String?> {
        return when (this) {
            SubRole.MODELING_AGENCY -> RoleType.AGENCY.value to null //"owner"
            SubRole.FASHION_BRAND, SubRole.BEAUTY_BRAND, SubRole.BRAND_OR_BUSINESS -> RoleType.AGENCY.value to null //"brand"
            SubRole.EVENT_AGENCY -> RoleType.AGENCY.value to null //"owner"
            SubRole.MAGAZINE -> RoleType.AGENCY.value to null //"media"
            SubRole.SCOUT -> RoleType.AGENCY.value to null //"scout"
            SubRole.BOOKER -> RoleType.AGENCY.value to null //"booker"
            SubRole.CASTING_DIRECTOR, SubRole.TALENT_MANAGER -> RoleType.AGENCY.value to null //"agent"
            SubRole.PHOTOGRAPHER -> RoleType.NEW_FACE.value to null //"photographer"
            SubRole.STYLIST, SubRole.MUA, SubRole.HAIR_STYLIST, SubRole.FASHION_DESIGNER -> RoleType.NEW_FACE.value to null //"stylist"
            SubRole.VIDEOGRAPHER, SubRole.CREATIVE_DIRECTOR -> RoleType.NEW_FACE.value to null //"media"
            SubRole.STUDIO -> RoleType.NEW_FACE.value to null //"place"
            SubRole.MODEL -> RoleType.MODEL.value to null
            SubRole.NEW_TALENT, SubRole.ACTOR, SubRole.DANCER, SubRole.SINGER -> RoleType.NEW_FACE.value to null
            SubRole.FAN -> RoleType.FAN.value to null //"fan"
        }
    }
}