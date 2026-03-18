package org.telegram.divo.screen.edit_my_profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

class EditMyProfileViewModel : BaseViewModel<EventListViewState, EditMyProfileIntent, Effect>() {

    override fun createInitialState(): EventListViewState {
        return EventListViewState()
    }

    fun getData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                val user = result.value
                val parts = user.fullName.split(" ") //TODO временно
                setState {
                    copy(
                        fName = parts.getOrNull(0).orEmpty(),
                        lName = parts.getOrNull(1).orEmpty(),
                        bio = user.model?.description.orEmpty(),
                        userFull = user,
                        avatarUrl = user.avatarUrl,
                        isLoading = false
                    )
                }
            } else {
                val errorMsg = result.getErrorMessage()
                setState { copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    private fun updateProfile(fNameRaw: String, lNameRaw: String, aboutRaw: String, file: Result<File>?) {
        viewModelScope.launch {
            val userInfo = state.value.userFull
            if (userInfo != null) {
                setState { copy(isSaved = true) }

                val uploadedUuid = if (file != null) {
                    val uploadResult = file.fold(
                        onSuccess = { DivoApi.userRepository.uploadPhoto(it) },
                        onFailure = { DivoResult.UnknownError(it) }
                    )
                    if (uploadResult !is DivoResult.Success) {
                        setState { copy(isSaved = false, errorMessage = uploadResult.getErrorMessage()) }
                        return@launch
                    }
                    uploadResult.value.uuid
                } else {
                    userInfo.avatarUuid
                }

                val result = DivoApi.userRepository.updateProfile(
                    userInfo = userInfo.copy(
                        fullName = "$fNameRaw $lNameRaw",
                        model = userInfo.model?.copy(description = aboutRaw),
                        avatarUuid = uploadedUuid
                    )
                )

                when (result) {
                    is DivoResult.Success -> {
                        setState { copy(isSaved = false) }
                        sendEffect(Effect.SaveSuccess)
                    }
                    else -> {
                        setState { copy(isSaved = false, errorMessage = result.getErrorMessage()) }
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

        setState { copy(isLoading = true, errorMessage = null) }

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
                                errorMessage = error.text ?: "Upload failed"
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

//    fun updateProfile(fNameRaw: String, lNameRaw: String, aboutRaw: String) {
//        // first name в Telegram лучше не отправлять пустым
//        val fName = fNameRaw.trim().ifEmpty { " " }
//        val lName = lNameRaw.trim()          // можно пустую строку
//        val about = aboutRaw.trim()          // можно пустую строку
//
//        setState { copy(isLoading = true, errorMessage = null) }
//
//        // Use TL_profile_updateProfile API
//        val req = TLRPC.TL_profile_updateProfile()
//
//        // Flags for TL_profile_updateProfile: 1=first_name, 2=last_name, 8=about
//        var flags = 0
//
//        if (fName.isNotEmpty()) {
//            flags = flags or 1
//            req.first_name = fName
//        }
//
//        if (lName.isNotEmpty()) {
//            flags = flags or 2
//            req.last_name = lName
//        }
//
//        if (about.isNotEmpty()) {
//            flags = flags or 8
//            req.about = about
//        }
//
//        req.flags = flags
//
//        ConnectionsManager.getInstance(currentAccount).sendRequest(
//            req,
//            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
//                AndroidUtilities.runOnUIThread {
//                    if (error != null) {
//                        setState {
//                            copy(
//                                isLoading = false,
//                                errorMessage = "${error.text ?: "Unknown error"} (code=${error.code})"
//                            )
//                        }
//                        FileLog.e("updateProfile error: ${error.text} code=${error.code}")
//                        return@runOnUIThread
//                    }
//
//                    FileLog.d("TL_profile_updateProfile success")
//
//                    // Apply changes locally
//                    applyProfileLocally(fName = fName, lName = lName, about = about)
//
//                    setState {
//                        copy(
//                            fName = fName,
//                            lName = lName,
//                            bio = about,
//                            isLoading = false,
//                            errorMessage = null
//                        )
//                    }
//                }
//            },
//        )
//    }
}
