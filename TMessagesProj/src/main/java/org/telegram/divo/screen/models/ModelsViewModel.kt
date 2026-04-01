package org.telegram.divo.screen.models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.usecase.GetFeedUseCase
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
import org.telegram.divo.screen.models.ModelsViewIntent.LoadMoreAllUsers
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC

class ModelsViewModel : BaseViewModel<ModelsViewState, ModelsViewIntent, ModelsViewEffect>() {

    companion object {
        private const val PAGE_SIZE = 5
    }

    private val currentAccount = UserConfig.selectedAccount

    override fun createInitialState(): ModelsViewState = ModelsViewState()

    private val subscribedPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, subscribedOnly = true
    ).paginator

    private val allUsersPaginator = GetFeedUseCase(
        limit = PAGE_SIZE
    ).paginator

    private val agenciesPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, modelsOnly = true
    ).paginator

    init {
        setIntent(LoadInitialData)
        viewModelScope.launch {
            merge(
                subscribedPaginator.state.map { it to Tab.SUBSCRIBED },
                allUsersPaginator.state.map { it to Tab.ALL_USERS },
                agenciesPaginator.state.map { it to Tab.AGENCIES_PRO }
            ).collect { (paginatorState, tab) ->
                setState {
                    copy(
                        tabFeeds = tabFeeds + (tab to paginatorState.items),
                        tabLoadingStates = tabLoadingStates + (tab to paginatorState.isLoading),
                        tabLoadingMoreStates = tabLoadingMoreStates + (tab to paginatorState.isLoadingMore),
                        tabHasMore = tabHasMore + (tab to paginatorState.hasMore),
                        error = paginatorState.error
                    )
                }
            }
        }
    }

    private fun currentPaginator(): OffsetPaginator<FeedItem> = when (state.value.selectedTab) {
        Tab.ALL_USERS -> allUsersPaginator
        Tab.SUBSCRIBED -> subscribedPaginator
        Tab.AGENCIES_PRO -> agenciesPaginator
    }

    override fun handleIntent(intent: ModelsViewIntent) {
        when (intent) {
            is LoadInitialData -> loadInitialData()
            is OnTabSelected -> onTabSelected(intent.tab)
            is OnStoryClick -> sendEffect(NavigateToStory(intent.storyId))
            is OnEmotionClick -> reactToModel(intent.modelId, intent.emotion)
            is OnSendDmClick -> sendEffect(NavigateToDirectMessage(intent.modelId))
            is OnSearchClick -> sendEffect(NavigateToSearch)
            is OnAddStoryClick -> sendEffect(NavigateToAddStory)
            is OnBookmarkClick -> bookmarkModel(intent.modelId)
            is LoadMoreAllUsers -> loadFeed(loadMore = true)
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
        viewModelScope.launch {
            listOf(
                subscribedPaginator,
                allUsersPaginator,
                agenciesPaginator
            ).map { paginator ->
                async { paginator.loadInitial() }
            }.awaitAll()
        }
    }

    private fun onTabSelected(tab: Tab) {
        setState { copy(selectedTab = tab) }

        if (currentPaginator().state.value.items.isEmpty()) {
            loadFeed(loadMore = false)
        }
    }

    private fun loadFeed(loadMore: Boolean) {
        viewModelScope.launch {
            if (loadMore) {
                currentPaginator().loadMore()
            } else {
                currentPaginator().loadInitial()
            }
        }
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
}