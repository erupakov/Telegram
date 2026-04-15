package org.telegram.divo.screen.models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.screen.models.ModelsViewIntent.LoadInitialData
import org.telegram.divo.screen.models.ModelsViewIntent.OnAddStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnBookmarkClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnLikeClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSearchClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnTabSelected
import org.telegram.divo.usecase.GetFeedUseCase
import org.telegram.messenger.R

class ModelsViewModel : BaseViewModel<ModelsViewState, ModelsViewIntent, ModelsViewEffect>() {

    companion object {
        private const val PAGE_SIZE = 5
    }

    override fun createInitialState(): ModelsViewState = ModelsViewState()

    private val modelsPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, modelsOnly = true
    ).paginator

    private val newTalentsPaginator = GetFeedUseCase(
        limit = PAGE_SIZE
    ).paginator

    private val agenciesPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, subscribedOnly = true
    ).paginator

    init {
        setIntent(LoadInitialData)
        viewModelScope.launch {
            merge(
                modelsPaginator.state.map { it to Tab.MODELS },
                newTalentsPaginator.state.map { it to Tab.NEW_TALENTS },
                agenciesPaginator.state.map { it to Tab.AGENCIES }
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
        Tab.NEW_TALENTS -> newTalentsPaginator
        Tab.MODELS -> modelsPaginator
        Tab.AGENCIES -> agenciesPaginator
    }

    override fun handleIntent(intent: ModelsViewIntent) {
        when (intent) {
            is LoadInitialData -> loadInitialData()
            is OnTabSelected -> onTabSelected(intent.tab)
            is OnStoryClick -> {}
            is OnSearchClick -> {}
            is OnAddStoryClick -> {}
            is OnBookmarkClick -> bookmarkModel(intent.modelId)
            is OnLikeClick -> onLikeClick(intent.tab, intent.feedId, intent.isLiked)
            is ModelsViewIntent.LoadMore -> loadMoreFeed(intent.tab)
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
                modelsPaginator,
                newTalentsPaginator,
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

    private fun loadMoreFeed(tab: Tab) {
        viewModelScope.launch {
            val paginator = when (tab) {
                Tab.NEW_TALENTS -> newTalentsPaginator
                Tab.MODELS -> modelsPaginator
                Tab.AGENCIES -> agenciesPaginator
            }
            paginator.loadMore()
        }
    }

    private fun bookmarkModel(modelId: String) {
        // TODO: Call repository to bookmark the model
        // Optimistically update the UI
    }

    private fun onLikeClick(tab: Tab, feedId: Int, isLiked: Boolean) {
        val oldState = state.value.tabFeeds

        fun updateItemInAllTabs(targetFeedId: Int, newLiked: Boolean, newCount: Int): Map<Tab, List<FeedItem>> {
            return state.value.tabFeeds.mapValues { (_, items) ->
                items.map { item ->
                    if (item.feedId == targetFeedId) {
                        item.copy(isLiked = newLiked, likesCount = newCount)
                    } else item
                }
            }
        }

        val targetItem = state.value.tabFeeds[tab]?.find { it.feedId == feedId } ?: return
        val newLiked = !isLiked
        val newCount = if (isLiked) (targetItem.likesCount - 1).coerceAtLeast(0) else targetItem.likesCount + 1

        setState {
            copy(tabFeeds = updateItemInAllTabs(feedId, newLiked, newCount))
        }

        viewModelScope.launch {
            val repository = DivoApi.publicationRepository

            val result = if (newLiked) {
                repository.likePost(feedId)
            } else {
                repository.unlikePost(feedId)
            }

            if (result is DivoResult.Success) {
                sendEffect(
                    ModelsViewEffect.ActionChanged(
                        R.drawable.ic_divo_favorite_selected,
                        if (newLiked) R.string.Liked else R.string.Unliked
                    )
                )
            } else {
                setState { copy(tabFeeds = oldState) }
                sendEffect(ModelsViewEffect.ShowError(result.getErrorMessage()))
            }
        }
    }
}