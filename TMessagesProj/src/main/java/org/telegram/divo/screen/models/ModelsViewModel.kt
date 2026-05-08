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
import org.telegram.divo.dal.repository.UserActionEvent
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.entity.RoleType
import org.telegram.divo.screen.models.ModelsViewIntent.LoadInitialData
import org.telegram.divo.screen.models.ModelsViewIntent.OnAddStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnBookmarkClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnLikeClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnSearchClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnStoryClick
import org.telegram.divo.screen.models.ModelsViewIntent.OnTabSelected
import org.telegram.divo.screen.models.ModelsViewIntent.Refresh
import org.telegram.divo.usecase.GetFeedUseCase
import org.telegram.divo.usecase.ToggleBookmarkUseCase
import org.telegram.divo.usecase.ToggleLikeUseCase
import org.telegram.messenger.R

class ModelsViewModel : BaseViewModel<ModelsViewState, ModelsViewIntent, ModelsViewEffect>() {

    companion object {
        private const val PAGE_SIZE = 5
    }

    override fun createInitialState(): ModelsViewState = ModelsViewState()

    private val toggleLikeUseCase = ToggleLikeUseCase()
    private val toggleBookmarkUseCase = ToggleBookmarkUseCase()

    private val modelsPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, role = RoleType.MODEL.value
    ).paginator

    private val newTalentsPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, role = RoleType.NEW_FACE.value
    ).paginator

    private val agenciesPaginator = GetFeedUseCase(
        limit = PAGE_SIZE, role = RoleType.AGENCY.value
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
        viewModelScope.launch {
            DivoApi.publicationRepository.events.collect { event ->
                when (event) {
                    is UserActionEvent.BookmarkChanged -> setState {
                        copy(tabFeeds = tabFeeds.mapValues { (_, items) ->
                            items.map { item ->
                                if (item.user.id == event.userId)
                                    item.copy(
                                        isFavorite = event.isFavorite,
                                        user = item.user.copy(followersCount = event.newFollowersCount)
                                    )
                                else item
                            }
                        })
                    }
                    is UserActionEvent.LikeChanged -> setState {
                        copy(tabFeeds = tabFeeds.mapValues { (_, items) ->
                            items.map { item ->
                                if (item.feedId == event.feedId)
                                    item.copy(
                                        isLiked = event.isLiked,
                                        user = item.user.copy(likesCount = event.newLikesCount)
                                    )
                                else item
                            }
                        })
                    }
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
            is Refresh -> refresh()
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

    private fun onLikeClick(tab: Tab, feedId: Int, isLiked: Boolean) {
        val targetItem = state.value.tabFeeds[tab]?.find { it.feedId == feedId } ?: return
        val savedState = state.value.tabFeeds

        fun updateAll(newLiked: Boolean, newCount: Int): Map<Tab, List<FeedItem>> =
            state.value.tabFeeds.mapValues { (_, items) ->
                items.map { if (it.feedId == feedId) it.copy(isLiked = newLiked, user = it.user.copy(likesCount = newCount)) else it }
            }

        viewModelScope.launch {
            toggleLikeUseCase.execute(
                feedId = feedId,
                isLiked = isLiked,
                currentCount = targetItem.user.likesCount,
                onUpdate = { newLiked, newCount -> setState { copy(tabFeeds = updateAll(newLiked, newCount)) } },
                onRollback = { setState { copy(tabFeeds = savedState) } },
                onSuccess = { newLiked ->
                    sendEffect(ModelsViewEffect.ActionChanged(
                        R.drawable.ic_divo_favorite_selected,
                        if (newLiked) R.string.Liked else R.string.Unliked
                    ))
                },
                onError = { sendEffect(ModelsViewEffect.ShowError(it)) }
            )
        }
    }

    private fun bookmarkModel(modelId: Int) {
        var targetItem: FeedItem? = null
        for (items in state.value.tabFeeds.values) {
            targetItem = items.find { it.user.id == modelId }
            if (targetItem != null) break
        }
        val item = targetItem ?: return
        val savedState = state.value.tabFeeds

        fun updateAll(newFavorite: Boolean, newCount: Int): Map<Tab, List<FeedItem>> =
            state.value.tabFeeds.mapValues { (_, items) ->
                items.map { if (it.user.id == modelId) it.copy(isFavorite = newFavorite, user = it.user.copy(followersCount = newCount)) else it }
            }

        viewModelScope.launch {
            toggleBookmarkUseCase.execute(
                userId = item.user.id,
                entity = item.user.role.value,
                isFavorite = item.isFavorite,
                currentFollowersCount = item.user.followersCount,
                onUpdate = { newFavorite, newCount -> setState { copy(tabFeeds = updateAll(newFavorite, newCount)) } },
                onRollback = { setState { copy(tabFeeds = savedState) } },
                onSuccess = { newFavorite ->
                    sendEffect(ModelsViewEffect.ActionChanged(
                        resDrawableId = R.drawable.ic_divo_bookmark_glass_selected,
                        resStringId = if (newFavorite) R.string.BookmarkSaved else R.string.BookmarkUnsaved
                    ))
                },
                onError = { sendEffect(ModelsViewEffect.ShowError(it)) }
            )
        }
    }

    private fun refresh() {
        setState { copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            listOf(
                modelsPaginator,
                newTalentsPaginator,
                agenciesPaginator
            ).map { paginator ->
                async { paginator.loadInitial() }
            }.awaitAll()
            setState { copy(isRefreshing = false) }
        }
    }
}