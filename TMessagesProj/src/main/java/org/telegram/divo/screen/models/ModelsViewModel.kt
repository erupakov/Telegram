package org.telegram.divo.screen.models

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.divo.screen.models.ModelsViewAction.NavigateToAddStory
import org.telegram.divo.screen.models.ModelsViewAction.NavigateToDirectMessage
import org.telegram.divo.screen.models.ModelsViewAction.NavigateToSearch
import org.telegram.divo.screen.models.ModelsViewAction.NavigateToStory
import org.telegram.divo.screen.models.ModelsViewIntent.LoadInitialData
import org.telegram.divo.screen.models.ModelsViewIntent.OnTabSelected
import org.telegram.divo.screen.models.ModelsViewIntent.OnStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnEmotionClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSendDmClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSearchClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnAddStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnBookmarkClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnPhotoClick


data class Story(
    val id: String,
    val imageUrl: String,
    val userName: String,
    val watched: Boolean = false,
)

data class Model(
    val id: String,
    val name: String,
    val imageUrl: String,
    val avatarUrl: String,
    val emotions: List<Emotions> = emptyList(),
    val photos: List<String>,
    val roleLabel: String
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
    val error: String? = null
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
}

// Action
sealed class ModelsViewAction : ViewAction {
    data class NavigateToStory(val storyId: String) : ModelsViewAction()
    data class NavigateToDirectMessage(val modelId: String) : ModelsViewAction()
    data object NavigateToSearch : ModelsViewAction()
    data object NavigateToAddStory : ModelsViewAction()
    data class ShowError(val message: String) : ModelsViewAction()
}


class ModelsViewModel : BaseViewModel<ModelsViewState, ModelsViewIntent, ModelsViewAction>() {

    override fun createInitialState(): ModelsViewState = ModelsViewState()

    init {
        setIntent(LoadInitialData)
    }

    override fun handleIntent(intent: ModelsViewIntent) {
        when (intent) {
            is LoadInitialData -> loadInitialData()
            is OnTabSelected -> selectTab(intent.tab)
            is OnStoryClick -> sendAction(NavigateToStory(intent.storyId))
            is OnEmotionClick -> reactToModel(intent.modelId, intent.emotion)
            is OnSendDmClick -> sendAction(NavigateToDirectMessage(intent.modelId))
            is OnSearchClick -> sendAction(NavigateToSearch)
            is OnAddStoryClick -> sendAction(NavigateToAddStory)
            is OnBookmarkClick -> bookmarkModel(intent.modelId)
            is OnPhotoClick -> zoomPhoto(intent.modelId, intent.photoUrl)
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
        setState { copy(selectedTab = tab, isLoading = true) }
        // TODO: Load models for the selected tab from repository
        setState { copy(isLoading = false, models = ModelsViewState.preview.models) }
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