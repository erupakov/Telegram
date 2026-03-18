package org.telegram.divo.screen.profile_social_links

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.entity.UserInfo

data class UiViewState(
    val userFull: UserInfo = UserInfo(),
    val instagramUrl: String? = null,
    val tiktokUrl: String? = null,
    val youtubeUrl: String? = null,
    val websiteUrl: String? = null,
    val isUploading: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : ViewState {

    val instagramUser get() = instagramUrl?.trimEnd('/')?.substringAfterLast("/")?.removePrefix("@")
    val tiktokUser get() = tiktokUrl?.trimEnd('/')?.substringAfterLast("/")?.removePrefix("@")
    val youtubeUser get() = youtubeUrl?.trimEnd('/')?.substringAfterLast("/")?.removePrefix("@")
    val website get() = websiteUrl.orEmpty()
}

sealed class Intent : ViewIntent {
    data object OnSaveClicked : Intent()

    data class OnNicknameChanged(
        val socialNetworkType: SocialNetworkType,
        val nickname: String
    ) : Intent()

    data object OnLoad : Intent()
}

sealed class Effect : ViewEffect {
    data object NavigateBack : Effect()
}