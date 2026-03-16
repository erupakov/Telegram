package org.telegram.divo.screen.profile_social_links

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.entity.UserSocialNetwork
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC

class ProfileSocialLinksViewModel :
    BaseViewModel<ProfileSocialLinksViewModel.UiViewState,
            ProfileSocialLinksViewModel.Intent,
            ProfileSocialLinksViewModel.Effect>() {


    data class UiViewState(
        val socialLinks: List<UserSocialNetwork> = listOf(),
        val instagramUser: String = "",
        val tiktokUser: String = "",
        val youtubePath: String = "",
        val website: String = "",
        val isUploading: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    sealed class Intent : ViewIntent {
        data class OnSaveClicked(
            val socialLinks: List<UserSocialNetwork>
        ) : Intent()

        data class OnNicknameChanged(
            val socialNetworkId: Int,
            val nickname: String
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
                updateLinks(intent.socialLinks)
            }
            is Intent.OnNicknameChanged -> {
                updateNickname(intent.socialNetworkId, intent.nickname)
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
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.getUserSocialNetworks()
            if (result is DivoResult.Success) {
                setState {
                    copy(isLoading = false, socialLinks = result.value)
                }
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun updateNickname(
        socialNetworkId: Int,
        nickname: String
    ) {
        setState {
            copy(
                socialLinks = socialLinks.map { link ->
                    if (link.id == socialNetworkId) {
                        link.copy(name = nickname)
                    } else {
                        link
                    }
                }
            )
        }
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
                isUploading = false,
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

    //TODO временно
    fun updateLinks(socialLinks: List<UserSocialNetwork>) {
        viewModelScope.launch {
            setState { copy(isUploading = true, errorMessage = null) }

            val results = socialLinks.map { link ->
                async {
                    DivoApi.userRepository.upsertSocialNetwork(
                        socialNetworkId = link.id,
                        nickname = link.name
                    )
                }
            }.awaitAll()

            val hasError = results.any { it !is DivoResult.Success }

            if (hasError) {
                setState {
                    copy(
                        isUploading = false,
                        errorMessage = "Не удалось обновить некоторые соцсети"
                    )
                }
            } else {
                setState { copy(isUploading = false) }
            }
        }
    }
}
