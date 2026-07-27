package org.telegram.divo.screen.edit_my_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.telegram.divo.screen.search.LocalCity
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import java.io.File

class EditMyProfileViewModel(
    private val isModel: Boolean
) : BaseViewModel<EventListViewState, EditMyProfileIntent, Effect>() {

    override fun createInitialState(): EventListViewState {
        return EventListViewState(isModel = isModel)
    }

    fun getData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val userResultDeferred = async { DivoApi.userRepository.getCurrentUserInfo() }
            val countriesDeferred = async { DivoApi.locationRepository.getCountries() }
            val citiesDeferred = async { DivoApi.locationRepository.getCities() }
            
            val result = userResultDeferred.await()
            val allCountries = countriesDeferred.await()
            val allCities = citiesDeferred.await()

            if (result is DivoResult.Success) {
                val user = result.value
                val matchedCountry = allCountries.find { it.shortName.equals(user.city?.countryCode, ignoreCase = true) }
                val localCity = user.city?.let { c ->
                    allCities.find { it.id == c.id.toLong() } ?: LocalCity(
                        id = c.id.toLong(),
                        name = c.name,
                        asciiName = "",
                        alternateNames = "",
                        countryCode = c.countryCode ?: "",
                        population = 0,
                        matchedName = ""
                    )
                }
                
                val telegramUser = UserConfig.getInstance(currentAccount).currentUser
                val firstName = telegramUser?.first_name ?: ""
                val lastName = telegramUser?.last_name ?: ""

                setState {
                    copy(
                        fName = firstName,
                        lName = lastName,
                        bio = if (isModel) user.model?.description.orEmpty() else user.agency?.description.orEmpty(),
                        userFull = user,
                        avatarUrl = if (isModel) user.avatarUrl else user.agency?.photo?.fullUrl ?: user.avatarUrl,
                        isLoading = false,
                        allCountries = allCountries,
                        allCities = allCities,
                        city = localCity,
                        country = matchedCountry?.name ?: "",
                        countryCode = matchedCountry?.shortName ?: ""
                    )
                }
            } else {
                setState { copy(isLoading = false) }
                sendEffect(Effect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun updateProfile(fNameRaw: String, lNameRaw: String, aboutRaw: String, file: Result<File>?) {
        viewModelScope.launch {
            val staleUserInfo = state.value.userFull
            val userInfo = DivoApi.userRepository.currentUserFlow.value ?: staleUserInfo
            if (userInfo != null) {
                setState { copy(isSaved = true) }

                val uploadedUuid = if (file != null) {
                    val uploadResult = file.fold(
                        onSuccess = { 
                            // Update TG avatar as well
                            org.telegram.divo.common.utils.TelegramProfileHelper.updateTelegramAvatar(currentAccount, it)
                            DivoApi.userRepository.uploadPhoto(it)
                        },
                        onFailure = { DivoResult.UnknownError(it) }
                    )
                    if (uploadResult !is DivoResult.Success) {
                        setState { copy(isSaved = false) }
                        sendEffect(Effect.ShowError(uploadResult.getErrorMessage()))
                        return@launch
                    }
                    uploadResult.value.uuid
                } else {
                    userInfo.avatarUuid
                }
                val isModel = state.value.isModel
                val fullNameStr = listOf(fNameRaw.trim(), lNameRaw.trim()).filter { it.isNotBlank() }.joinToString(" ")
                val result = if (isModel) {
                    DivoApi.userRepository.updateProfile(
                        userInfo = userInfo.copy(
                            fullName = fullNameStr,
                            model = userInfo.model?.copy(description = aboutRaw),
                            avatarUuid = uploadedUuid,
                            city = state.value.city?.let {
                                val isNewCity = it.id != userInfo.city?.id?.toLong()
                                org.telegram.divo.entity.City(id = if (isNewCity) 0 else it.id.toInt(), name = it.name, countryCode = it.countryCode)
                            } ?: userInfo.city
                        )
                    )
                } else {
                    val agency = userInfo.agency ?: org.telegram.divo.entity.Agency()
                    DivoApi.userRepository.updateAgency(
                        agency = agency.copy(
                            description = aboutRaw,
                            title = fullNameStr,
                            photo = if (uploadedUuid.isNotEmpty()) org.telegram.divo.entity.Photo(photoId = 0L, fileUuid = uploadedUuid) else agency.photo
                        ),
                        // For agency we might also want to update the user's city in userInfo
                    )
                    // The agency update doesn't take user city directly, it updates the agency. 
                    // However we should probably update user Profile to save the city.
                    DivoApi.userRepository.updateProfile(userInfo.copy(
                        city = state.value.city?.let {
                            val isNewCity = it.id != userInfo.city?.id?.toLong()
                            org.telegram.divo.entity.City(id = if (isNewCity) 0 else it.id.toInt(), name = it.name, countryCode = it.countryCode)
                        } ?: userInfo.city
                    ))
                }

                when (result) {
                    is DivoResult.Success -> {
                        // Update TG profile name as well
                        org.telegram.divo.common.utils.TelegramProfileHelper.updateTelegramName(
                            currentAccount = currentAccount,
                            firstName = fNameRaw.trim(),
                            lastName = lNameRaw.trim()
                        )

                        setState { copy(isSaved = false) }
                        sendEffect(Effect.SaveSuccess)
                    }
                    else -> {
                        setState { copy(isSaved = false) }
                        sendEffect(Effect.ShowError(result.getErrorMessage()))
                    }
                }
            }
        }
    }

    private fun refreshUserData() {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)
        val userFull = mc.getUserFull(uc.clientUserId)

        // Also get the updated user from MessagesController cache
        val updatedUser = mc.getUser(uc.clientUserId)
        if (userFull != null && updatedUser != null) {
            userFull.user = updatedUser
        }
    }


    private val currentAccount: Int = UserConfig.selectedAccount

    init {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)

        val me = uc.currentUser
        val userFull = mc.getUserFull(uc.clientUserId)
    }

    override fun handleIntent(intent: EditMyProfileIntent) {
        when (intent) {
            EditMyProfileIntent.OnLoad -> Unit
            is EditMyProfileIntent.OnSaveClicked -> {
                updateProfile(intent.fName, intent.lName, intent.bio, intent.file)
            }
            is EditMyProfileIntent.OnLocationChanged -> {
                setState {
                    copy(
                        country = intent.country,
                        countryCode = intent.countryCode,
                        city = intent.city
                    )
                }
            }

            is EditMyProfileIntent.OnAvatarUploaded -> {
                handleDidUploadPhoto(
                    intent.photo,
                    intent.video,
                    intent.videoStartTimestamp,
                    intent.videoPath,
                    intent.bigSize,
                    intent.isVideo,
                    intent.smallSize,
                    intent.emojiMarkup
                )
            }
        }
    }

    private fun handleDidUploadPhoto(
        photo: TLRPC.InputFile?,
        video: TLRPC.InputFile?,
        videoStartTimestamp: Double,
        videoPath: String?,
        bigSize: TLRPC.PhotoSize?,
        isVideo: Boolean,
        smallSize: TLRPC.PhotoSize?,
        emojiMarkup: TLRPC.VideoSize?
    ) {
        // 1) Ранний callback (превью) — пропускаем
        if (photo == null && video == null) {
            return
        }

        setState { copy(isLoading = true) }

        val req = TLRPC.TL_photos_uploadProfilePhoto()
        var flags = 0

        // --- PHOTO ---
        if (photo != null) {
            flags = flags or 1          // FLAG_FILE
            req.file = photo
        }

        // --- VIDEO AVATAR ---
        if (video != null) {
            flags = flags or 2          // FLAG_VIDEO
            req.video = video

            flags = flags or 4          // FLAG_VIDEO_START_TS
            req.video_start_ts = videoStartTimestamp

            if (emojiMarkup != null) {
                flags = flags or 8      // FLAG_VIDEO_EMOJI_MARKUP
                req.video_emoji_markup = emojiMarkup
            }
        }

        req.flags = flags

        ConnectionsManager.getInstance(currentAccount)
            .sendRequest(req) { response, error ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState {
                            copy(
                                isLoading = false,
                            )
                        }
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_photos_photo) {
                        MessagesController
                            .getInstance(currentAccount)
                            .putUsers(response.users, false)

                        // Update current user photo in UserConfig (like ProfileActivity does)
                        val uc = UserConfig.getInstance(currentAccount)
                        val currentUser = uc.currentUser
                        if (currentUser != null && response.photo != null) {
                            val bigSize = FileLoader.getClosestPhotoSizeWithSize(
                                response.photo.sizes, 800
                            )
                            val smallSize = FileLoader.getClosestPhotoSizeWithSize(
                                response.photo.sizes, 150
                            )
                            if (smallSize != null && bigSize != null) {
                                if (currentUser.photo == null) {
                                    currentUser.photo = TLRPC.TL_userProfilePhoto()
                                }
                                currentUser.photo.photo_id = response.photo.id
                                currentUser.photo.photo_small = smallSize.location
                                currentUser.photo.photo_big = bigSize.location
                                currentUser.photo.dc_id = response.photo.dc_id
                                uc.setCurrentUser(currentUser)
                                uc.saveConfig(true)
                            }
                        }
                    }

                    val nc = NotificationCenter.getInstance(currentAccount)
                    nc.postNotificationName(NotificationCenter.mainUserInfoChanged)
                    nc.postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_ALL)

                    // Force UI refresh with new timestamp
                    refreshUserData()
                }
            }
    }

    private var messageStorage: MessagesStorage? = null

    fun setMessageStorage(_messageStorage: MessagesStorage) {
        messageStorage = _messageStorage
    }

    private fun applyProfileLocally(fName: String, lName: String, about: String) {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)

        // 1) Update current user (имя/фамилия) + save config
        uc.currentUser?.let { me ->
            me.first_name = fName
            me.last_name = lName
            uc.setCurrentUser(me)
            uc.saveConfig(true)
        }

        // 2) Update UserFull and its user object + persist to storage
        val userFull = mc.getUserFull(uc.clientUserId)
        if (userFull != null) {
            userFull.about = about
            // Also update userFull.user with new name
            userFull.user?.let { user ->
                user.first_name = fName
                user.last_name = lName
            }
            messageStorage?.updateUserInfo(userFull, false)
        }

        // 3) Update user in MessagesController cache
        val cachedUser = mc.getUser(uc.clientUserId)
        if (cachedUser != null) {
            cachedUser.first_name = fName
            cachedUser.last_name = lName
            mc.putUser(cachedUser, false)
        }

        // 4) Notify UI (как делает UserInfoActivity)
        val nc = NotificationCenter.getInstance(currentAccount)
        nc.postNotificationName(NotificationCenter.mainUserInfoChanged)
        nc.postNotificationName(
            NotificationCenter.updateInterfaces,
            MessagesController.UPDATE_MASK_NAME or MessagesController.UPDATE_MASK_ALL
        )

        sendEffect(Effect.NavigateBack)
    }

    companion object {
        fun factory(isModel: Boolean) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return EditMyProfileViewModel(isModel) as T
            }
        }
    }
}
