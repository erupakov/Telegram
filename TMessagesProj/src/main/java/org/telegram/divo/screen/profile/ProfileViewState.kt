package org.telegram.divo.screen.profile

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.dto.user.GalleryItem
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.entity.AgencyModels
import org.telegram.divo.entity.Feed
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserInfo
import org.telegram.tgnet.TLRPC
import java.time.LocalDate
import java.time.Period
import java.util.Locale

data class ProfileViewState(
    val userId: Int = -1,
    val isOwnProfile: Boolean = false,
    val userInfo: UserInfo? = null,
    val userGalleryItems: List<UserGalleryItem> = listOf(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val portfolioLoading: Boolean = false,
    val portfolioUploading: Boolean = false,
    val isLoadingAllUsers: Boolean = false,

    val similarModels: List<AgencyModel> = emptyList(),
    val socialLinks: SocialLinks = SocialLinks(),
    val physicalParams: PhysicalParams = PhysicalParams(),
    val statistic: UserStatistic = UserStatistic()
) : ViewState

data class SocialLinks(
    val instagram: String = "",
    val tiktok: String = "",
    val youtube: String = "",
    val website: String = ""
)

data class PhysicalParams(
    val gender: String = "",
    val age: String = "",
    val height: Float = 0f,
    val waist: Int = 0,
    val hips: Int = 0,
    val shoeSize: Float = 0f,
    val hairLength: String = "",
    val hairColor: String = "",
    val eyeColor: String = "",
    val skinColor: String = "",
    val breastSize: String = ""
)

data class UserStatistic(
    val followers: Int = 0,
    val following: Int = 0,
    val views: Int = 0,
    val likes: Int = 0,
    val saves: Int = 0
)

sealed class ProfileIntent : ViewIntent {
    data class OnLoad(val userId: Int, val isOwnProfile: Boolean) : ProfileIntent()
    object OnClearPortfolioUpload : ProfileIntent()

    class OnPortfolioPhotoSelected(
        val photo: TLRPC.InputFile,
        val localPath: String?
    ) : ProfileIntent()
    class OnBackgroundPhotoSelected(
        val photo: TLRPC.InputFile,
        val localPath: String?
    ) : ProfileIntent()
    class OpenSocialLink(val url: String) : ProfileIntent()
}

sealed class ProfileEffect : ViewEffect {
    class OpenUrl(val url: String) : ProfileEffect()
    class ShowError(val message: String) : ProfileEffect()
    object NavigateToSearch : ProfileEffect()
}