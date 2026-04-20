package org.telegram.divo.screen.profile

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.EngagementUser
import org.telegram.divo.entity.Event
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.SimilarFace
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.screen.profile.components.Destination
import org.telegram.divo.screen.profile.components.StatsType
import java.io.File

data class ProfileViewState(
    val userId: Int = -1,
    val isOwnProfile: Boolean = false,
    val userInfo: UserInfo = UserInfo(),

    val userGalleryItems: List<UserGalleryItem> = listOf(),
    val isLoadingMoreImages: Boolean = false,
    val hasMoreImages: Boolean = true,
    val isLoadingImages: Boolean = false,

    val videoItems: PersistentList<Publication> = persistentListOf(),
    val isLoadingVideos: Boolean = false,
    val isLoadingMoreVideos: Boolean = false,
    val hasMoreVideos: Boolean = true,

    val events: List<Event> = emptyList(),
    val isLoadingEvents: Boolean = false,
    val isLoadingMoreEvents: Boolean = false,
    val hasMoreEvents: Boolean = true,

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val portfolioLoading: Boolean = false,
    val mediaUploading: Boolean = false,
    val isLoadingAllUsers: Boolean = false,
    val isLoadingStats: Boolean = false,
    val backgroundChanging: Boolean = false,

    val searchQuery: String = "",
    val searchResults: List<FeedlineItem> = emptyList(),
    val isSearchMode: Boolean = false,
    val searchHasMore: Boolean = true,
    val isLoadingMoreSearch: Boolean = false,

    val likedItems: List<EngagementUser> = emptyList(),
    val viewedItems: List<EngagementUser> = emptyList(),
    val followedItems: List<EngagementUser> = emptyList(),
    val isLoadingLiked: Boolean = false,
    val isLoadingViewed: Boolean = false,
    val isLoadingFollowed: Boolean = false,
    val hasMoreLiked: Boolean = true,
    val hasMoreViewed: Boolean = true,
    val hasMoreFollowed: Boolean = true,

    val similarProfiles: List<SimilarFace> = emptyList(),
    val socialLinks: SocialLinks = SocialLinks(),
    val physicalParams: PhysicalParams = PhysicalParams(),
    val statistic: UserStatistic = UserStatistic()
) : ViewState {

    val isModel: Boolean
        get() = userInfo.role.isModel()

    val pageCount: Int
        get() = when {
            !isModel -> 5
            isModel && isOwnProfile -> 2
            else -> 3
        }

    val destinationTabs: List<Destination>
        get() = when (pageCount) {
            2 -> buildList {
                add(Destination.SONGS)
                add(Destination.ALBUM)
            }
            3 -> buildList {
                add(Destination.SONGS)
                add(Destination.ALBUM)
                add(Destination.PLAYLISTS)
            }
            else -> buildList {
                add(Destination.SONGS)
                add(Destination.ALBUM)
                add(Destination.AGENCY)
                add(Destination.PLAYLISTS)
                add(Destination.EVENT)
            }
        }

    val instagramUser get() = userInfo.model?.instagramUrl?.trimEnd('/')?.substringAfterLast("/").orEmpty()
    val tiktokUser get() = userInfo.model?.tiktokUrl?.trimEnd('/')?.substringAfterLast("/").orEmpty()
    val youtubeUser get() = userInfo.model?.youtubeUrl?.trimEnd('/')?.substringAfterLast("/").orEmpty()
    val website get() = userInfo.model?.websiteUrl?.removePrefix("https://")?.removePrefix("http://").orEmpty()
}

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
)

sealed class ProfileIntent : ViewIntent {
    data object OnLoad : ProfileIntent()
    object OnClearPortfolioUpload : ProfileIntent()
    class OnLoadMoreEngagementStats(
        val type: StatsType
    ) : ProfileIntent()

    class OnPortfolioPhotoSelected(
        val file: Result<File>
    ) : ProfileIntent()

    class OnVideoSelected(
        val file: Result<File>
    ) : ProfileIntent()

    class OnBackgroundPhotoSelected(
        val file: Result<File>
    ) : ProfileIntent()

    class OpenSocialLink(val socialNetworkType: SocialNetworkType) : ProfileIntent()
    class OnSearchQueryChanged(val query: String) : ProfileIntent()
    object OnLoadMoreSearchResults : ProfileIntent()
    object OnLoadMorePortfolio : ProfileIntent()
    object OnLoadMoreVideos : ProfileIntent()
    object OnLoadMoreEvents : ProfileIntent()
}

sealed class ProfileEffect : ViewEffect {
    class OpenUrl(val url: String) : ProfileEffect()
    class ShowError(val message: String) : ProfileEffect()
    object NavigateToSearch : ProfileEffect()
}