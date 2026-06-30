package org.telegram.divo.screen.profile_social_links

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.SocialNetworkType

class ProfileSocialLinksViewModel : BaseViewModel<UiViewState, Intent, Effect>() {

    override fun createInitialState(): UiViewState = UiViewState()

    init {
        DivoAnalytics.logEvent(AnalyticsEvent.SocialLinksOpened())
    }

    override fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.OnLoad -> getProfile()
            is Intent.OnSaveClicked -> {
                updateLinks()
            }
            is Intent.OnNicknameChanged -> {
                updateLink(intent.socialNetworkType, intent.nickname)
            }
        }
    }

    fun getProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()
            if (result is DivoResult.Success) {
                setState {
                    copy(
                        isLoading = false,
                        userFull = result.value,
                        instagramUrl = result.value.model?.instagramUrl ?: result.value.agency?.instagramUrl.orEmpty(),
                        tiktokUrl = result.value.model?.tiktokUrl ?: result.value.agency?.tiktokUrl.orEmpty(),
                        youtubeUrl = result.value.model?.youtubeUrl ?: result.value.agency?.youtubeUrl.orEmpty(),
                        websiteUrl = result.value.model?.websiteUrl ?: result.value.agency?.websiteUrl.orEmpty(),
                        errorMessage = null
                    )
                }
            } else {
                setState { copy(isLoading = false, errorMessage = result.getErrorMessage()) }
            }
        }
    }

    private fun updateLink(
        socialNetworkType: SocialNetworkType,
        nickname: String
    ) {
        setState {
            when (socialNetworkType) {
                SocialNetworkType.WEBSITE -> copy(websiteUrl = nickname)
                SocialNetworkType.INSTAGRAM -> copy(instagramUrl = "https://${socialNetworkType.value}$nickname")
                SocialNetworkType.TIKTOK -> copy(tiktokUrl = "https://${socialNetworkType.value}$nickname")
                SocialNetworkType.YOUTUBE -> copy(youtubeUrl = "https://${socialNetworkType.value}$nickname")
            }
        }
    }

    fun updateLinks() {
        viewModelScope.launch {
            setState { copy(isUploading = true, errorMessage = null) }

            val result = DivoApi.userRepository.updateProfile(
                userInfo = state.value.userFull.copy(
                    model = state.value.userFull.model?.copy(
                        instagramUrl = buildUrl(state.value.instagramUser, state.value.instagramUrl),
                        tiktokUrl = buildUrl(state.value.tiktokUser, state.value.tiktokUrl),
                        youtubeUrl = buildUrl(state.value.youtubeUser, state.value.youtubeUrl),
                        websiteUrl = state.value.website,
                    ),
                    agency = state.value.userFull.agency?.copy(
                        instagramUrl = buildUrl(state.value.instagramUser, state.value.instagramUrl),
                        tiktokUrl = buildUrl(state.value.tiktokUser, state.value.tiktokUrl),
                        youtubeUrl = buildUrl(state.value.youtubeUser, state.value.youtubeUrl),
                        websiteUrl = state.value.website,
                    )
                )
            )

            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.SocialLinksSaved())
                    setState { copy(isUploading = false) }
                    sendEffect(Effect.NavigateBack)
                }
                else -> {
                    setState {
                        copy(
                            isUploading = false,
                            errorMessage = result.getErrorMessage()
                        )
                    }
                }
            }
        }
    }

    private fun buildUrl(nick: String?, fullUrl: String?): String =
        if (nick.isNullOrBlank()) "" else fullUrl.orEmpty()
}
