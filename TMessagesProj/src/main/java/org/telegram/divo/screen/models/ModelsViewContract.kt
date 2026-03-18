package org.telegram.divo.screen.models

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.FeedItem
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
    val emotions: List<Emotions> = emptyList(),
    val photos: List<String> = emptyList(),
    val roleLabel: String = "",
    val userProfile: TLRPC.TL_userProfile? = null
)

enum class Tab(val displayName: String) {
    SUBSCRIBED(displayName = "Subscribed Models"),
    ALL_USERS(displayName = "All Users"),
    AGENCIES_PRO(displayName = "Agencies & Pro Members")
}

sealed class Emotions(
    open val selected: Boolean,
    open val emoji: String,
    open val count: Int
) {
    data class Like(
        override val selected: Boolean,
        override val emoji: String,
        override val count: Int,
    ) : Emotions(selected, emoji, count)

    data class Fire(
        override val selected: Boolean,
        override val emoji: String,
        override val count: Int,
    ) : Emotions(selected, emoji, count)

    data class Hearth(
        override val selected: Boolean,
        override val emoji: String,
        override val count: Int,
    ) : Emotions(selected, emoji, count)
}

// State
data class ModelsViewState(
    val tabFeeds: Map<Tab, List<FeedItem>> = emptyMap(),
    val tabLoadingStates: Map<Tab, Boolean> = emptyMap(),
    val tabLoadingMoreStates: Map<Tab, Boolean> = emptyMap(),
    val tabHasMore: Map<Tab, Boolean> = Tab.entries.associateWith { true },
    val stories: List<Story> = emptyList(),
    val models: List<Model> = emptyList(),
    val selectedTab: Tab = Tab.SUBSCRIBED,
    val isLoading: Boolean = false,
    val error: String? = null,
    val allUserModels: List<Model> = emptyList(),
    val showMockData: Boolean = false,
) : ViewState {

    val feedItems get() = tabFeeds[selectedTab] ?: emptyList()
    val isLoadingAllUsers get() = tabLoadingStates[selectedTab] ?: false
    val isLoadingMoreFeed get() = tabLoadingMoreStates[selectedTab] ?: false
    val feedHasMore get() = tabHasMore[selectedTab] ?: true

    val emptyText = when (selectedTab) {
        Tab.SUBSCRIBED -> "You have no subscriptions yet"
        Tab.ALL_USERS -> "No models found"
        Tab.AGENCIES_PRO -> "No agencies yet"
    }

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
            models = List(10) { index ->
                Model(
                    id = "model_$index",
                    name = "Model $index",
                    imageUrl = "https://randomuser.me/api/portraits/women/46.jpg",
                    avatarUrl = "https://randomuser.me/api/portraits/women/42.jpg",
                    emotions = listOf(
                        Emotions.Like(selected = true, emoji = "👍", count = 10),
                        Emotions.Fire(selected = false, emoji = "🔥", count = 5),
                        Emotions.Hearth(selected = false, emoji = "❤️", count = 2)
                    ),
                    photos = List(5) { photoIndex -> "https://randomuser.me/api/portraits/women/4$photoIndex.jpg" },
                    roleLabel = "Model"
                )
            },
            selectedTab = Tab.SUBSCRIBED,
            isLoading = false,
            error = null
        )
        val default = ModelsViewState(
            stories = emptyList(),
            models = emptyList(),
            selectedTab = Tab.SUBSCRIBED,
            isLoading = false,
            error = null
        )
    }
}

// Intent
sealed class ModelsViewIntent : ViewIntent {
    data object LoadInitialData : ModelsViewIntent()
    data class OnTabSelected(val tab: Tab) : ModelsViewIntent()
    data class OnStoryClick(val storyId: String) : ModelsViewIntent()
    data class OnEmotionClick(val modelId: String, val emotion: Emotions) : ModelsViewIntent()
    data class OnSendDmClick(val modelId: String) : ModelsViewIntent()
    data class OnBookmarkClick(val modelId: String) : ModelsViewIntent()
    data object OnSearchClick : ModelsViewIntent()
    data object OnAddStoryClick : ModelsViewIntent()
    data class OnPhotoClick(val modelId: String, val photoUrl: String) : ModelsViewIntent()
    data object LoadMoreAllUsers : ModelsViewIntent()
    data object OnShowMockDataClicked : ModelsViewIntent()
}

// Action
sealed class ModelsViewEffect : ViewEffect {
    data class NavigateToStory(val storyId: String) : ModelsViewEffect()
    data class NavigateToDirectMessage(val modelId: String) : ModelsViewEffect()
    data object NavigateToSearch : ModelsViewEffect()
    data object NavigateToAddStory : ModelsViewEffect()
    data class ShowError(val message: String) : ModelsViewEffect()
}
