package org.telegram.divo.screen.profile_social_links

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class ProfileSocialLinksViewModel :
    BaseViewModel<ProfileSocialLinksViewModel.UiViewState,
            ProfileSocialLinksViewModel.Intent,
            ProfileSocialLinksViewModel.Effect>() {


    data class UiViewState(
        val instagramUser: String = "",
        val tiktokUser: String = "",
        val youtubePath: String = "",
        val website: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    sealed class Intent : ViewIntent {
        data class OnSaveClicked(
            val instagramUrl: String,
            val tiktokUrl: String,
            val youtubeUrl: String,
            val website: String
        ) : Intent()

        data object OnLoad : Intent()
    }

    sealed class Effect : ViewEffect {
        data object NavigateBack : Effect()
    }

    override fun createInitialState(): UiViewState = UiViewState()

    private val currentAccount: Int = UserConfig.selectedAccount

    init {
        setState {
            copy(
                errorMessage = null
            )
        }
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnLoad -> getProfile()
            is Intent.OnSaveClicked -> {
                updateLinks(
                    intent.instagramUrl,
                    intent.tiktokUrl,
                    intent.youtubeUrl,
                    intent.website
                )
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

        // 2) Update UserFull.about + persist to storage (если UserFull уже загружен)
        val userFull = mc.getUserFull(uc.clientUserId)
        if (userFull != null) {
            userFull.about = about
            messageStorage?.updateUserInfo(userFull, false)
        }

        // 3) Notify UI (как делает UserInfoActivity)
        val nc = NotificationCenter.getInstance(currentAccount)
        nc.postNotificationName(NotificationCenter.mainUserInfoChanged)
        nc.postNotificationName(
            NotificationCenter.updateInterfaces,
            MessagesController.UPDATE_MASK_NAME
        )

        sendEffect(Effect.NavigateBack)
    }

    fun getProfile() {
        setState { copy(isLoading = true) }

        val req = TLRPC.TL_profile_getUserProfile()
        val user = TLRPC.TL_inputUser()

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        if (userFull?.user == null) {
            AndroidUtilities.runOnUIThread {
                setState { copy(isLoading = false) }
            }
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
                        FileLog.d("TL_profile_getUserProfile social links response -> $response")
                        applySocialLinksToState(response.social_links)
                    } else {
                        FileLog.e("TL_profile_getUserProfile failed: ${error?.text}")
                        setState { copy(isLoading = false) }
                    }
                }
            },
        )
    }

    private fun applySocialLinksToState(socialLinks: TLRPC.TL_profile_socialLinks?) {
        var instagram = ""
        var tiktok = ""
        var youtube = ""
        var website = ""

        socialLinks?.links?.forEach { keyVal ->
            when (keyVal.key.lowercase()) {
                "instagram" -> instagram = extractUsername(keyVal.`val`, "instagram.com")
                "tiktok" -> tiktok = extractUsername(keyVal.`val`, "tiktok.com")
                "youtube" -> youtube = extractPath(keyVal.`val`, "youtube.com")
                "website" -> website = keyVal.`val`
            }
        }

        setState {
            copy(
                isLoading = false,
                instagramUser = instagram,
                tiktokUser = tiktok,
                youtubePath = youtube,
                website = website
            )
        }
    }

    private fun extractUsername(url: String, domain: String): String {
        if (url.isBlank()) return ""
        return url
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replace("$domain/", "")
            .replace("$domain/@", "")
            .removePrefix("@")
            .trim('/')
    }

    private fun extractPath(url: String, domain: String): String {
        if (url.isBlank()) return ""
        return url
            .replace("https://", "")
            .replace("http://", "")
            .replace("www.", "")
            .replace("$domain/", "")
            .trim('/')
    }

    fun updateLinks(instagram: String, tiktok: String, youtube: String, web: String) {
        setState { copy(isLoading = true, errorMessage = null) }

        val req = TLRPC.TL_profile_updateSocialLinks()

        var flags = 0

        if (instagram.isNotBlank()) {
            flags = flags or 1
            req.instagram = instagram
        }

        if (tiktok.isNotBlank()) {
            flags = flags or 2
            req.tiktok = tiktok
        }

        if (youtube.isNotBlank()) {
            flags = flags or 4
            req.youtube = youtube
        }

        if (web.isNotBlank()) {
            flags = flags or 8
            req.website = web
        }

        req.flags = flags


        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (response is TLRPC.TL_user) {
                        response.social_links
                    }

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

                    setState {
                        copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    FileLog.d("updateSocialLinks success")
                    sendEffect(Effect.NavigateBack)
                }
            },
        )
    }

}
