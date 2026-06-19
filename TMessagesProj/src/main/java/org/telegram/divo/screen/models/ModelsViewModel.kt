package org.telegram.divo.screen.models

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
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
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.R
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.messenger.DialogObject

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

    private var currentLanguage = org.telegram.messenger.LocaleController.getInstance().currentLocale?.language ?: ""

    private val languageObserver = NotificationCenter.NotificationCenterDelegate { id, _, _ ->
        if (id == NotificationCenter.reloadInterface) {
            val newLanguage = org.telegram.messenger.LocaleController.getInstance().currentLocale?.language ?: ""
            if (newLanguage != currentLanguage) {
                currentLanguage = newLanguage
                viewModelScope.launch {
                    delay(300)
                    refresh()
                }
            }
        }
    }

    private val storiesObserver = NotificationCenter.NotificationCenterDelegate { id, _, _ ->
        if (id == NotificationCenter.storiesUpdated || id == NotificationCenter.storiesReadUpdated || 
            id == NotificationCenter.storiesListUpdated || id == NotificationCenter.uploadStoryProgress ||
            id == NotificationCenter.fileUploaded || id == NotificationCenter.fileUploadFailed) {
            updateStories()
        }
    }

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
                                if (item.user.id == event.userId) {
                                    item.copy(
                                        isFollowed = event.isFavorite,
                                        user = item.user.copy(followersCount = event.newFollowersCount)
                                    )
                                } else {
                                    item
                                }
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

        NotificationCenter.getGlobalInstance().addObserver(languageObserver, NotificationCenter.reloadInterface)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.storiesUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.storiesReadUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.storiesListUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.uploadStoryProgress)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.fileUploaded)
        NotificationCenter.getInstance(UserConfig.selectedAccount).addObserver(storiesObserver, NotificationCenter.fileUploadFailed)
    }

    override fun onCleared() {
        super.onCleared()
        NotificationCenter.getGlobalInstance().removeObserver(languageObserver, NotificationCenter.reloadInterface)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.storiesUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.storiesReadUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.storiesListUpdated)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.uploadStoryProgress)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.fileUploaded)
        NotificationCenter.getInstance(UserConfig.selectedAccount).removeObserver(storiesObserver, NotificationCenter.fileUploadFailed)
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
        if (DivoApi.accessTokenProvider.getAccessToken().isNullOrEmpty()) {
            setState { copy(isLoading = false) }
            return
        }
        setState { copy(isLoading = true) }
        setState {
            copy(
                isLoading = false,
                models = ModelsViewState.preview.models
            )
        }
        val account = UserConfig.selectedAccount
        MessagesController.getInstance(account).getStoriesController().loadStories()
        updateStories()
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

    private fun updateStories() {
        val account = UserConfig.selectedAccount
        val controller = MessagesController.getInstance(account).getStoriesController()
        
        val dialogStories = controller.dialogListStories ?: emptyList()
        val mappedStories = mutableListOf<Story>()
        
        val selfId = UserConfig.getInstance(account).clientUserId
        var hasSelfStories = false

        for (peerStories in dialogStories) {
            val dialogId = DialogObject.getPeerDialogId(peerStories.peer)
            val isSelf = dialogId == selfId
            if (isSelf) {
                hasSelfStories = true
            }
            
            val userName = if (dialogId > 0) {
                MessagesController.getInstance(account).getUser(dialogId)?.first_name ?: ""
            } else {
                MessagesController.getInstance(account).getChat(-dialogId)?.title ?: ""
            }
            
            mappedStories.add(
                Story(
                    id = dialogId.toString(),
                    dialogId = dialogId,
                    imageUrl = null,
                    userName = if (isSelf) org.telegram.messenger.LocaleController.getString(R.string.MyStory) else userName,
                    watched = !controller.hasUnreadStories(dialogId),
                    hasUnread = controller.hasUnreadStories(dialogId),
                    hasStories = true,
                    isSelf = isSelf,
                    isLoading = controller.hasUploadingStories(dialogId),
                    unreadCount = controller.getUnreadStoriesCount(dialogId),
                    totalCount = peerStories.stories.size
                )
            )
        }
        
        if (!hasSelfStories) {
            mappedStories.add(0, Story(
                id = selfId.toString(),
                dialogId = selfId,
                imageUrl = null,
                userName = org.telegram.messenger.LocaleController.getString(R.string.AddStoryLabel),
                watched = true,
                hasUnread = false,
                hasStories = false,
                isSelf = true,
                isLoading = controller.hasUploadingStories(selfId),
                unreadCount = 0,
                totalCount = 0
            ))
        } else {
            val selfStoryIndex = mappedStories.indexOfFirst { it.isSelf }
            if (selfStoryIndex > 0) {
                val selfStory = mappedStories.removeAt(selfStoryIndex)
                mappedStories.add(0, selfStory)
            }
        }
        
        setState { copy(stories = mappedStories) }
    }

    private fun loadFeed(loadMore: Boolean) {
        if (DivoApi.accessTokenProvider.getAccessToken().isNullOrEmpty()) {
            return
        }
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
                items.map { if (it.user.id == modelId) it.copy(isFollowed = newFavorite, user = it.user.copy(followersCount = newCount)) else it }
            }

        viewModelScope.launch {
            toggleBookmarkUseCase.execute(
                userId = item.user.id,
                isFollowed = item.isFollowed,
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
        if (DivoApi.accessTokenProvider.getAccessToken().isNullOrEmpty()) {
            return
        }
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