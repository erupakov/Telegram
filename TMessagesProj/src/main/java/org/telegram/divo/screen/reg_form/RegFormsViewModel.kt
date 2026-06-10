package org.telegram.divo.screen.reg_form

import android.os.Build
import android.net.Uri
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
import org.telegram.divo.dal.network.DivoAuthHelper
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.tgnet.tl.TL_account
import java.io.BufferedReader
import java.security.MessageDigest

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
                formData = RegistrationFormData(
                    subRole = intent.subRole,
                    firstName = intent.googleFirstName ?: "",
                    lastName = intent.googleLastName ?: "",
                    photoUri = intent.googlePhotoUrl?.let { Uri.parse(it) }
                ),
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
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                val context = ApplicationLoader.applicationContext
                                val inputStream = if (uri.scheme == "http" || uri.scheme == "https") {
                                    val request = okhttp3.Request.Builder().url(uri.toString()).build()
                                    val response = okhttp3.OkHttpClient().newCall(request).execute()
                                    response.body?.byteStream()
                                } else {
                                    context.contentResolver.openInputStream(uri)
                                }
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
                    additionalInfo[AdditionalInfoKeys.MEASURING_SYSTEM] = org.telegram.divo.common.DivoSettings.measuringSystem
                    additionalInfo[AdditionalInfoKeys.SUB_ROLE] = data.subRole.name.lowercase()
                    if (mappedSubrole != null) additionalInfo[AdditionalInfoKeys.SUBROLE_MAPPED] = mappedSubrole

                    if (tgUser != null) {
                        additionalInfo[AdditionalInfoKeys.TELEGRAM_ID] = tgUser.id
                        if (!tgUser.username.isNullOrBlank()) {
                            additionalInfo[AdditionalInfoKeys.TELEGRAM_USERNAME] = tgUser.username
                        }
                        additionalInfo[AdditionalInfoKeys.TELEGRAM_ACCESS_HASH] = tgUser.access_hash
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
                            password = DivoAuthHelper.generatePassword(phone),
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
                    val profileUpdateResult = org.telegram.divo.common.utils.TelegramProfileHelper.updateTelegramName(
                        currentAccount = state.value.currentAccount,
                        firstName = firstName,
                        lastName = lastName
                    )

                    // 2.5 TG Profile Photo Update
                    if (telegramPhotoFile != null) {
                        org.telegram.divo.common.utils.TelegramProfileHelper.updateTelegramAvatar(state.value.currentAccount, telegramPhotoFile)
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
                        gender = org.telegram.divo.entity.mapGenderToEnglish(data.gender) ?: "female",
                        birthday = data.dateOfBirth ?: "",
                        geoCityId = resolvedCityId?.takeIf { it > 0 },
                        measuringSystem = org.telegram.divo.common.DivoSettings.measuringSystem,
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