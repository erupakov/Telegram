package org.telegram.divo.screen.models

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.FeedItem
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC

data class Story(
    val id: String,
    val imageUrl: String,
    val userName: String,
    val watched: Boolean = false,
)

data class Model(
    val id: String,
    val name: String,
    val imageUrl: String = "",
    val avatarUrl: String = "",
    val photos: List<String> = emptyList(),
    val roleLabel: String = "",
    val userProfile: TLRPC.TL_userProfile? = null
)

enum class Tab(val displayResId: Int) {
    MODELS(displayResId = R.string.Models),
    NEW_TALENTS(displayResId = R.string.NewTalents),
    AGENCIES(displayResId = R.string.Agencies)
}

data class ModelsViewState(
    val tabFeeds: Map<Tab, List<FeedItem>> = emptyMap(),
    val tabLoadingStates: Map<Tab, Boolean> = emptyMap(),
    val tabLoadingMoreStates: Map<Tab, Boolean> = emptyMap(),
    val tabHasMore: Map<Tab, Boolean> = Tab.entries.associateWith { true },
    val stories: List<Story> = emptyList(),
    val models: List<Model> = emptyList(),
    val selectedTab: Tab = Tab.MODELS,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val allUserModels: List<Model> = emptyList(),
) : ViewState {

    companion object {
        val preview = ModelsViewState(
            stories = List(5) { index ->
                Story(
                    id = "$index",
                    imageUrl = "https://randomuser.me/api/portraits/women/5$index.jpg",
                    userName = "User $index",
                    watched = false
                )
            },
            selectedTab = Tab.MODELS,
            isLoading = false,
            error = null
        )
        val default = ModelsViewState(
            stories = emptyList(),
            models = emptyList(),
            selectedTab = Tab.MODELS,
            isLoading = false,
            error = null
        )
    }
}

sealed class ModelsViewIntent : ViewIntent {
    data object LoadInitialData : ModelsViewIntent()
    data class OnTabSelected(val tab: Tab) : ModelsViewIntent()
    data class OnStoryClick(val storyId: String) : ModelsViewIntent()
    data class OnBookmarkClick(val modelId: String) : ModelsViewIntent()
    data class OnLikeClick(val tab: Tab, val feedId: Int, val isLiked: Boolean) : ModelsViewIntent()
    data object OnSearchClick : ModelsViewIntent()
    data object OnAddStoryClick : ModelsViewIntent()
    data class LoadMore(val tab: Tab) : ModelsViewIntent()
    data object Refresh : ModelsViewIntent()
}

sealed class ModelsViewEffect : ViewEffect {
    data class ShowError(val message: String) : ModelsViewEffect()
    data class ActionChanged(val resDrawableId: Int, val resStringId: Int) : ModelsViewEffect()
}
