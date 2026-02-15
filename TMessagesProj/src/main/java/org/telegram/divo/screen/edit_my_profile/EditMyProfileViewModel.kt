package org.telegram.divo.screen.edit_my_profile

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLoader
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesController.DialogPhotos
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class EditMyProfileViewModel :
    BaseViewModel<
            EditMyProfileViewModel.EventListViewState,
            EditMyProfileViewModel.EditMyProfileIntent,
            EditMyProfileViewModel.Effect
            >() {

    data class EventListViewState(
        val fName: String = "",
        val lName: String = "",
        val bio: String = "",
        val userFull: TLRPC.UserFull,
        val avatarUpdateTimestamp: Long = 0L, // Force recomposition when avatar changes
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    sealed class EditMyProfileIntent : ViewIntent {
        data class OnSaveClicked(val fName: String, val lName: String, val bio: String) :
            EditMyProfileIntent()

        data object OnLoad : EditMyProfileIntent()
        data class OnAvatarUploaded(
            val photo: TLRPC.InputFile?,
            val video: TLRPC.InputFile?,
            val videoStartTimestamp: Double,
            val videoPath: String?,
            val bigSize: TLRPC.PhotoSize?,
            val smallSize: TLRPC.PhotoSize?,
            val isVideo: Boolean,
            val emojiMarkup: TLRPC.VideoSize?
        ) : EditMyProfileIntent()
    }

    sealed class Effect : ViewEffect {
        data object NavigateBack : Effect()
    }

    override fun createInitialState(): EventListViewState {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())


        return EventListViewState(
            userFull = userFull
        )
    }

    fun getData() {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)
        val userFull = mc.getUserFull(uc.clientUserId)
        val me = uc.currentUser

        setState {
            copy(
                fName = me?.first_name ?: userFull?.user?.first_name ?: "",
                lName = me?.last_name ?: userFull?.user?.last_name ?: "",
                bio = userFull?.about ?: "",
                userFull = userFull ?: state.value.userFull,
                avatarUpdateTimestamp = System.currentTimeMillis()
            )
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

        setState {
            copy(
                userFull = userFull ?: state.value.userFull,
                avatarUpdateTimestamp = System.currentTimeMillis(),
                isLoading = false
            )
        }
    }


    private val currentAccount: Int = UserConfig.selectedAccount

    init {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)

        val me = uc.currentUser
        val userFull = mc.getUserFull(uc.clientUserId)

        setState {
            copy(
                fName = me?.first_name ?: userFull?.user?.first_name ?: "",
                lName = me?.last_name ?: userFull?.user?.last_name ?: "",
                bio = userFull?.about ?: "",
                errorMessage = null
            )
        }
    }

    override fun handleIntent(intent: EditMyProfileIntent) {
        when (intent) {
            EditMyProfileIntent.OnLoad -> Unit
            is EditMyProfileIntent.OnSaveClicked -> {
                updateProfile(intent.fName, intent.lName, intent.bio)
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

    fun updateProfile(fNameRaw: String, lNameRaw: String, aboutRaw: String) {
        // first name в Telegram лучше не отправлять пустым
        val fName = fNameRaw.trim().ifEmpty { " " }
        val lName = lNameRaw.trim()          // можно пустую строку
        val about = aboutRaw.trim()          // можно пустую строку

        setState { copy(isLoading = true, errorMessage = null) }

        // Use TL_profile_updateProfile API
        val req = TLRPC.TL_profile_updateProfile()

        // Flags for TL_profile_updateProfile: 1=first_name, 2=last_name, 8=about
        var flags = 0

        if (fName.isNotEmpty()) {
            flags = flags or 1
            req.first_name = fName
        }

        if (lName.isNotEmpty()) {
            flags = flags or 2
            req.last_name = lName
        }

        if (about.isNotEmpty()) {
            flags = flags or 8
            req.about = about
        }

        req.flags = flags

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = "${error.text ?: "Unknown error"} (code=${error.code})"
                            )
                        }
                        FileLog.e("updateProfile error: ${error.text} code=${error.code}")
                        return@runOnUIThread
                    }

                    FileLog.d("TL_profile_updateProfile success")

                    // Apply changes locally
                    applyProfileLocally(fName = fName, lName = lName, about = about)

                    setState {
                        copy(
                            fName = fName,
                            lName = lName,
                            bio = about,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            },
        )
    }
}
