package org.telegram.divo.screen.profile

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class ProfileViewModel :
    BaseViewModel<ProfileViewModel.ProfileViewState, ProfileViewModel.ProfileIntent, ProfileViewModel.ProfileEffect>() {

    data class SocialLinks(
        val instagram: String = "",
        val tiktok: String = "",
        val youtube: String = "",
        val website: String = ""
    )

    data class PhysicalParams(
        val gender: String = "",
       val age: Long = 0,
        val height: Long = 0,
        val waist: Long = 0,
        val hips: Long = 0,
        val shoeSize: Long = 0,
        val hairLength: Long = 0,
        val hairColor: String = "",
        val eyeColor: String = "",
        val skinColor: String = "",
        val breastSize: String = ""
    )

    data class ProfileViewState(
        val userFull: TLRPC.UserFull,
        val userProfile: TLRPC.TL_userProfile? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val userPhotos: List<TLRPC.Photo> = emptyList(),
        val portfolioItems: List<TLRPC.TL_profile_portfolioItem> = emptyList(),
        val portfolioLoading: Boolean = false,
        val portfolioUploading: Boolean = false,
        val portfolioUploadLocalPath: String? = null,
        val socialLinks: SocialLinks = SocialLinks(),
        val physicalParams: PhysicalParams = PhysicalParams(),
        val backgroundPhoto: TLRPC.Photo? = null
    ) : ViewState

    sealed class ProfileIntent : ViewIntent {
        data object OnLoad : ProfileIntent()
        data object LoadPortfolio : ProfileIntent()
        data class OnPortfolioPhotoSelected(
            val photo: TLRPC.InputFile,
            val localPath: String?
        ) : ProfileIntent()
        data class OnBackgroundPhotoSelected(
            val photo: TLRPC.InputFile,
            val localPath: String?
        ) : ProfileIntent()
        data object OnClearPortfolioUpload : ProfileIntent()
    }

    sealed class ProfileEffect : ViewEffect {
        data object NavigateToSearch : ProfileEffect()
        data object NavigateToCreateEvent : ProfileEffect()
        data class NavigateToEventDetails(val eventId: Long) : ProfileEffect()
        data class OpenUrl(val url: String) : ProfileEffect()
    }

    override fun createInitialState(): ProfileViewState {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        return ProfileViewState(
            userFull = userFull
        )
    }

    fun getData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            val userFull = MessagesController.getInstance(currentAccount)
                .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

            val userPhotos = MessagesController
                .getInstance(currentAccount)
                .getDialogPhotos(userFull.id)

            userPhotos.loadCache()

            if (userPhotos.loaded) {
                setState {
                    copy(
                        userFull = userFull,
                        userPhotos = ArrayList(userPhotos.photos)
                    )
                }
            } else {
                delay(3000)
                setState {
                    copy(
                        userFull = userFull,
                        userPhotos = ArrayList(userPhotos.photos)
                    )
                }
            }

            getUserProfile()
            loadPortfolio()
        }
    }

    private fun getUserProfile() {
        val req = TLRPC.TL_profile_getUserProfile()
        val user = TLRPC.TL_inputUser()

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        if (userFull?.user == null) {
            setState { copy(isLoading = false) }
            return
        }

        user.user_id = userFull.user.id
        user.access_hash = userFull.user.access_hash
        req.user_id = user

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (response is TLRPC.TL_userProfile) {
                        FileLog.d("TL_profile_getUserProfile response -> $response")
                        applyUserProfileToState(response)
                    } else {
                        FileLog.e("TL_profile_getUserProfile failed: ${error?.text}")
                        setState { copy(isLoading = false) }
                    }
                }
            },
        )
    }

    private fun applyUserProfileToState(profile: TLRPC.TL_userProfile) {
        // Parse social links
        val socialLinks = parseSocialLinks(profile.social_links)

        // Parse physical params
        val physicalParams = parsePhysicalParams(profile)

        // Get background photo
        val backgroundPhoto = profile.background

        setState {
            copy(
                isLoading = false,
                userProfile = profile,
                socialLinks = socialLinks,
                physicalParams = physicalParams,
                backgroundPhoto = backgroundPhoto,
            )
        }
    }

    private fun parseSocialLinks(socialLinks: TLRPC.TL_profile_socialLinks?): SocialLinks {
        var instagram = ""
        var tiktok = ""
        var youtube = ""
        var website = ""

        socialLinks?.links?.forEach { keyVal ->
            when (keyVal.key.lowercase()) {
                "instagram" -> instagram = keyVal.`val`
                "tiktok" -> tiktok = keyVal.`val`
                "youtube" -> youtube = keyVal.`val`
                "website" -> website = keyVal.`val`
            }
        }

        return SocialLinks(
            instagram = instagram,
            tiktok = tiktok,
            youtube = youtube,
            website = website
        )
    }

    private fun parsePhysicalParams(profile: TLRPC.TL_userProfile): PhysicalParams {
        val params = profile.physical_params
        val gender = when (profile.gender) {
            is TLRPC.TL_genderMale -> "Male"
            is TLRPC.TL_genderFemale -> "Female"
            else -> ""
        }

        return PhysicalParams(
            gender = gender,
            age = params?.age ?: 0L,
            height = params?.height ?: 0L,
            waist = params?.waist ?: 0L,
            hips = params?.hips?: 0L,
            shoeSize = params?.shoe_size ?: 0L,
            hairLength = params?.hair_length ?: 0L,
            hairColor = params?.hair_color?.capitalize() ?: "",
            eyeColor = params?.eye_color?.capitalize() ?: "",
            skinColor = params?.skin_color?.capitalize() ?: "",
            breastSize = params?.breast_size?.uppercase() ?: ""
        )
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun openSocialLink(url: String) {
        if (url.isNotBlank()) {
            sendEffect(ProfileEffect.OpenUrl(url))
        }
    }

    private var currentAccount = UserConfig.selectedAccount
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.OnLoad -> {
                getData()
            }
            ProfileIntent.LoadPortfolio -> {
                loadPortfolio()
            }
            is ProfileIntent.OnPortfolioPhotoSelected -> {
                uploadPortfolioItem(intent.photo, intent.localPath)
            }
            is ProfileIntent.OnBackgroundPhotoSelected -> {
                uploadBackgroundImage(intent.photo, intent.localPath)
            }
            ProfileIntent.OnClearPortfolioUpload -> {
                setState { copy(portfolioUploadLocalPath = null, portfolioUploading = false) }
            }
        }
    }

    private fun loadPortfolio() {
        setState { copy(portfolioLoading = true) }

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        if (userFull?.user == null) {
            setState { copy(portfolioLoading = false) }
            return
        }

        val req = TLRPC.TL_profile_getPortfolio().apply {
            user_id = TLRPC.TL_inputUser().apply {
                user_id = userFull.user.id
                access_hash = userFull.user.access_hash
            }
            tab = ""
            offset = 0
            limit = 100
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("loadPortfolio error: ${error.text}")
                        setState { copy(portfolioLoading = false) }
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_profile_portfolio) {
                        setState {
                            copy(
                                portfolioItems = response.items,
                                portfolioLoading = false
                            )
                        }
                    } else {
                        setState { copy(portfolioLoading = false) }
                    }
                }
            }
        )
    }

    private fun uploadBackgroundImage(inputFile: TLRPC.InputFile, localPath: String?) {
        setState {
            copy(
                portfolioUploading = true,
                portfolioUploadLocalPath = localPath
            )
        }

        // Step 1: Upload media to get photo ID
        uploadMediaToServer(
            inputFile = inputFile,
            onSuccess = { photoId ->
                // Step 2: Set background using photo ID
                setBackgroundPhoto(photoId)
            },
            onError = { errorMessage ->
                FileLog.e("uploadBackgroundImage error: $errorMessage")
                setState {
                    copy(
                        portfolioUploading = false,
                        portfolioUploadLocalPath = null
                    )
                }
            }
        )
    }

    private fun uploadPortfolioItem(inputFile: TLRPC.InputFile, localPath: String?) {
        setState {
            copy(
                portfolioUploading = true,
                portfolioUploadLocalPath = localPath
            )
        }

        // Step 1: Upload media to get photo ID
        uploadMediaToServer(
            inputFile = inputFile,
            onSuccess = { photoId ->
                // Step 2: Create portfolio item with photo ID
                savePortfolioItem(photoId)
            },
            onError = { errorMessage ->
                FileLog.e("uploadPortfolioItem error: $errorMessage")
                setState {
                    copy(
                        portfolioUploading = false,
                        portfolioUploadLocalPath = null
                    )
                }
            }
        )
    }

    private fun savePortfolioItem(photoId: Long) {
        val req = TLRPC.TL_profile_uploadPortfolioItem().apply {
            file_id = photoId
            type = "photo"
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("savePortfolioItem error: ${error.text}")
                        setState {
                            copy(
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_profile_portfolioItem) {
                        FileLog.d("savePortfolioItem success, id: ${response.id}")
                        setState {
                            copy(
                                portfolioItems = listOf(response) + portfolioItems,
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                    } else {
                        FileLog.e("savePortfolioItem unexpected response: ${response?.javaClass?.simpleName}")
                        setState {
                            copy(
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                    }
                }
            }
        )
    }

    private fun uploadMediaToServer(
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
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        onError(error.text ?: "Upload failed")
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_messageMediaPhoto) {
                        val photoId = response.photo?.id ?: 0L
                        if (photoId != 0L) {
                            FileLog.d("uploadMediaToServer success, photoId: $photoId")
                            onSuccess(photoId)
                        } else {
                            onError("Failed to get photo ID")
                        }
                    } else {
                        onError("Unexpected response type: ${response?.javaClass?.simpleName}")
                    }
                }
            }
        )
    }

    private fun setBackgroundPhoto(photoId: Long) {
        val req = TLRPC.TL_profile_updateProfile().apply {
            flags = 65536 // FLAG for background_id
            background_id = photoId
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("setBackgroundPhoto error: ${error.text}")
                        setState {
                            copy(
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                        return@runOnUIThread
                    }

                    if (response is TLRPC.TL_userProfile) {
                        FileLog.d("setBackgroundPhoto success")
                        // Refresh profile to get updated background
                        applyUserProfileToState(response)
                        setState {
                            copy(
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                    } else {
                        FileLog.e("setBackgroundPhoto unexpected response: ${response?.javaClass?.simpleName}")
                        setState {
                            copy(
                                portfolioUploading = false,
                                portfolioUploadLocalPath = null
                            )
                        }
                    }
                }
            }
        )
    }
}
