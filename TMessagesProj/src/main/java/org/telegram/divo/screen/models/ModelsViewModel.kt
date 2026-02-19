package org.telegram.divo.screen.models

import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.screen.models.ModelsViewEffect.NavigateToAddStory
import org.telegram.divo.screen.models.ModelsViewEffect.NavigateToDirectMessage
import org.telegram.divo.screen.models.ModelsViewEffect.NavigateToSearch
import org.telegram.divo.screen.models.ModelsViewEffect.NavigateToStory
import org.telegram.divo.screen.models.ModelsViewIntent.LoadInitialData
import org.telegram.divo.screen.models.ModelsViewIntent.OnTabSelected
import org.telegram.divo.screen.models.ModelsViewIntent.OnStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnEmotionClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSendDmClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSearchClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnAddStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnBookmarkClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnPhotoClick
import org.telegram.divo.screen.models.ModelsViewIntent.LoadMoreAllUsers
import org.telegram.divo.screen.models.ModelsViewIntent.OnShowMockDataClicked
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC
import org.telegram.tgnet.TLRPC.TL_error


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
    val stories: List<Story> = emptyList(),
    val models: List<Model> = emptyList(),
    val selectedTab: Tab = Tab.SUBSCRIBED,
    val isLoading: Boolean = false,
    val error: String? = null,
    val allUserModels: List<Model> = emptyList(),
    val isLoadingAllUsers: Boolean = false,
    val allUsersHasMore: Boolean = true,
    val showMockData: Boolean = false,
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


class ModelsViewModel : BaseViewModel<ModelsViewState, ModelsViewIntent, ModelsViewEffect>() {

    companion object {
        const val PAGE_SIZE = 100
    }

    private val currentAccount = UserConfig.selectedAccount

    override fun createInitialState(): ModelsViewState = ModelsViewState()

    init {
        setIntent(LoadInitialData)
    }

    override fun handleIntent(intent: ModelsViewIntent) {
        when (intent) {
            is LoadInitialData -> loadInitialData()
            is OnTabSelected -> selectTab(intent.tab)
            is OnStoryClick -> sendEffect(NavigateToStory(intent.storyId))
            is OnEmotionClick -> reactToModel(intent.modelId, intent.emotion)
            is OnSendDmClick -> sendEffect(NavigateToDirectMessage(intent.modelId))
            is OnSearchClick -> sendEffect(NavigateToSearch)
            is OnAddStoryClick -> sendEffect(NavigateToAddStory)
            is OnBookmarkClick -> bookmarkModel(intent.modelId)
            is OnPhotoClick -> zoomPhoto(intent.modelId, intent.photoUrl)
            is LoadMoreAllUsers -> loadAllUsers(loadMore = true)
            is OnShowMockDataClicked -> setState { copy(showMockData = true) }
        }
    }

    private fun loadInitialData() {
        setState { copy(isLoading = true) }
        // TODO: Load stories from repository
        // TODO: Load models for the initial tab from repository
        setState {
            copy(
                isLoading = false,
                stories = ModelsViewState.preview.stories,
                models = ModelsViewState.preview.models
            )
        }
    }

    private fun selectTab(tab: Tab) {
        setState { copy(selectedTab = tab, showMockData = false) }
        when (tab) {
            Tab.ALL_USERS -> {
                if (state.value.allUserModels.isEmpty() && !state.value.isLoadingAllUsers) {
                    loadAllUsers(loadMore = false)
                }
            }
            else -> {
                setState { copy(isLoading = true) }
                // TODO: Load models for the selected tab from repository
                setState { copy(isLoading = false, models = ModelsViewState.preview.models) }
            }
        }
    }

    private fun loadAllUsers(loadMore: Boolean) {
        if (state.value.isLoadingAllUsers) return
        if (loadMore && !state.value.allUsersHasMore) return

        setState { copy(isLoadingAllUsers = true) }

        val offset = if (loadMore) state.value.allUserModels.size else 0

        val req = TLRPC.TL_profile_searchUsers().apply {
            flags = 0
            this.offset = offset
            this.limit = PAGE_SIZE
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        FileLog.e("loadAllUsers error: ${error.text}")
                        setState { copy(isLoadingAllUsers = false, error = error.text) }
                        return@runOnUIThread
                    }
                    if (response is TLRPC.TL_profile_foundUsers) {
                        val newModels = response.users.map { mapUserProfileToModel(it) }
                        setState {
                            copy(
                                allUserModels = if (loadMore) allUserModels + newModels else newModels,
                                isLoadingAllUsers = false,
                                allUsersHasMore = newModels.size >= PAGE_SIZE,
                            )
                        }
                    } else {
                        setState { copy(isLoadingAllUsers = false) }
                    }
                }
            }
        )
    }

    private fun mapUserProfileToModel(profile: TLRPC.TL_userProfile): Model {
        val user = profile.user
        val firstName = user?.first_name.orEmpty()
        val lastName = user?.last_name.orEmpty()
        val roleLabel = when (profile.role) {
            is TLRPC.TL_agencyRoleAgent -> "Agent"
            is TLRPC.TL_agencyRoleBooker -> "Booker"
            is TLRPC.TL_agencyRoleScout -> "Scout"
            is TLRPC.TL_agencyRoleOwner -> "Owner"
            is TLRPC.TL_agencyRoleAll -> "All"
            else -> ""
        }
        return Model(
            id = profile.user_id.toString(),
            name = "$firstName $lastName".trim(),
            roleLabel = roleLabel,
            userProfile = profile
        )
    }

    private fun reactToModel(modelId: String, emotion: Emotions) {
        // TODO: Call repository to react to the model
        // Optimistically update the UI
        setState {
            copy(models = models.map { model ->
                if (model.id == modelId) {
                    model.copy(emotions = model.emotions.map {
                        if (it::class == emotion::class) {
                            when (it) {
                                is Emotions.Like -> it.copy(
                                    selected = !it.selected,
                                    count = if (it.selected) it.count - 1 else it.count + 1
                                )

                                is Emotions.Fire -> it.copy(
                                    selected = !it.selected,
                                    count = if (it.selected) it.count - 1 else it.count + 1
                                )

                                is Emotions.Hearth -> it.copy(
                                    selected = !it.selected,
                                    count = if (it.selected) it.count - 1 else it.count + 1
                                )
                            }
                        } else {
                            it
                        }
                    })
                } else {
                    model
                }
            })
        }
    }

    private fun bookmarkModel(modelId: String) {
        // TODO: Call repository to bookmark the model
        // Optimistically update the UI
    }

    private fun zoomPhoto(modelId: String, photoUrl: String) {
        // TODO: Call repository to zoom photo
        // Optimistically update the UI
    }
}