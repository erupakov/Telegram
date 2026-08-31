package org.telegram.divo.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.common.arch.OffsetPaginator
import org.telegram.divo.common.arch.PaginatedResult
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.flatMap
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.AgencyModelStatus
import org.telegram.divo.entity.AgencySearchModelStatus
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.screen.profile.ProfileEffect.NavigateBack
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToCreateEvent
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToEdit
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToEditLinks
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToEvent
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToFindSimilarProfiles
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToGallery
import org.telegram.divo.screen.profile.ProfileEffect.NavigateToProfile
import org.telegram.divo.screen.profile.ProfileEffect.ShowAppearances
import org.telegram.divo.screen.profile.ProfileEffect.ShowError
import org.telegram.divo.screen.profile.ProfileEffect.ShowWorkHistory
import org.telegram.divo.screen.profile.components.StatsType
import org.telegram.divo.usecase.EngagementInteractor
import org.telegram.divo.usecase.GetEventListUseCase
import org.telegram.divo.usecase.GetUserGalleryUseCase
import org.telegram.divo.usecase.GetUserVideosUseCase
import org.telegram.divo.usecase.ToggleBookmarkUseCase
import org.telegram.messenger.R
import java.io.File

class ProfileViewModel(
    private val userId: Int,
    private val isOwnProfile: Boolean,
) : BaseViewModel<ProfileViewState, ProfileIntent, ProfileEffect>() {

    private var searchModelsJob: Job? = null
    private var engagementLoaded = false

    private val toggleBookmarkUseCase = ToggleBookmarkUseCase()
    private val toggleLikeUseCase = org.telegram.divo.usecase.ToggleLikeUseCase()

    private val eventPaginator = GetEventListUseCase(creatorId = userId).paginator

    private val portfolioPaginator by lazy {
        GetUserGalleryUseCase(userId = state.value.userId).paginator
    }

    private val videoPaginator by lazy {
        GetUserVideosUseCase(userId = state.value.userId).paginator
    }

    private val engagement by lazy {
        EngagementInteractor(
            userId = state.value.userId,
            limit = PAGE_SIZE,
            onFollowersCount = { count ->
                setState { copy(statistic = statistic.copy(followers = count)) }
            },
            onViewsCount = { count ->
                setState { copy(statistic = statistic.copy(views = count)) }
            },
            onFollowingCount = { count ->
                setState { copy(statistic = statistic.copy(following = count)) }
            },
        )
    }

    private val agencyModelsPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.userRepository.getAgencyModels(
            offset = offset,
            limit = limit,
            agencyId = state.value.userInfo.agency?.id ?: -1
        )) {
            is DivoResult.Success -> {
                val data = result.value
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }
            else -> { throw Exception(result.getErrorMessage()) }
        }
    }

    override fun createInitialState(): ProfileViewState {
        return ProfileViewState(
            userId = userId,
            isOwnProfile = isOwnProfile
        )
    }

    private val searchModelsPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.userRepository.searchAgencyModels(
            query = state.value.searchModelsQuery,
            offset = offset,
            limit = limit,
            currentAgencyId = state.value.userInfo.agency?.id
        )) {
            is DivoResult.Success -> {
                val currentAgencyModels = state.value.agencyModels
                val paginatedResult = result.value
                val mappedItems = paginatedResult.items.map { searchModel ->
                    val existing = currentAgencyModels.find { it.userId == searchModel.userId }
                    if (existing != null) {
                        if (existing.status == AgencyModelStatus.PENDING) {
                            searchModel.copy(status = AgencySearchModelStatus.RequestPending)
                        } else {
                            searchModel.copy(status = AgencySearchModelStatus.AlreadyAdded)
                        }
                    } else {
                        searchModel
                    }
                }
                paginatedResult.copy(items = mappedItems)
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    init {
        DivoAnalytics.logEvent(AnalyticsEvent.ProfileOpened(userId.toLong()))
        loadData()
        viewModelScope.launch {
            DivoApi.eventRepository.eventsUpdatedFlow.collect {
                if (!state.value.isModel) {
                    eventPaginator.reset()
                    eventPaginator.loadInitial()
                }
            }
        }
        viewModelScope.launch {
            DivoApi.eventRepository.eventParticipationFlow.collect { update ->
                val savedEvents = state.value.events
                setState {
                    copy(
                        events = savedEvents.map { event ->
                            if (event.id == update.eventId)
                                event.copy(isApplied = update.isApplied, appliesCount = update.appliesCount)
                            else event
                        }
                    )
                }
            }
        }
        viewModelScope.launch {
            DivoApi.authRepository.authStateFlow.collect { isLoggedIn ->
                setState { ProfileViewState(userId = userId, isOwnProfile = isOwnProfile) }
                eventPaginator.reset()
                agencyModelsPaginator.reset()
                if (engagementLoaded) {
                    engagement.likedPaginator.reset()
                    engagement.viewedPaginator.reset()
                    engagement.followedPaginator.reset()
                    engagement.searchPaginator.reset()
                }
                portfolioPaginator.reset()
                videoPaginator.reset()
                if (isLoggedIn) {
                    setIntent(ProfileIntent.OnLoad)
                }
            }
        }
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                DivoApi.workHistory.cache,
                DivoApi.workHistory.cachedUserIdFlow
            ) { cached, cachedId ->
                if (cachedId == userId) cached else null
            }.collect { cached ->
                if (cached != null) {
                    val latest = cached.filter { it.id != -1 }.maxByOrNull { it.startDate }
                    setState { copy(latestWorkExperience = latest) }
                }
            }
        }
    }

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnLoad -> loadData()
            is ProfileIntent.OnRefresh -> refreshData()
            is ProfileIntent.OpenSocialLink -> openLink(intent.socialNetworkType)
            is ProfileIntent.OnBackgroundPhotoSelected -> { changeBackground(intent.file) }
            is ProfileIntent.OnPortfolioPhotoSelected -> {
                DivoAnalytics.logEvent(AnalyticsEvent.ProfileMediaUploadTapped("photo", state.value.userId))
                uploadPhoto(intent.file)
            }
            is ProfileIntent.OnVideoSelected -> {
                DivoAnalytics.logEvent(AnalyticsEvent.ProfileMediaUploadTapped("video", state.value.userId))
                uploadVideo(intent.file)
            }
            is ProfileIntent.OnClearPortfolioUpload -> {}
            is ProfileIntent.OnLoadMoreEngagementStats -> loadMoreEngagement(intent.type)
            is ProfileIntent.OnStatsTabOpened -> onStatsTabOpened(intent.type)
            is ProfileIntent.OnSearchQueryChanged -> onSearchQueryChanged(intent.query)
            is ProfileIntent.OnLoadMoreSearchResults -> loadMoreSearchResults()
            is ProfileIntent.OnLoadMorePortfolio -> loadMorePortfolio()
            is ProfileIntent.OnLoadMoreVideos -> viewModelScope.launch { videoPaginator.loadMore() }
            ProfileIntent.OnLoadMoreEvents -> viewModelScope.launch { eventPaginator.loadMore() }
            is ProfileIntent.OnEditClicked -> sendEffect(NavigateToEdit(state.value.isModel, intent.initialPage))
            is ProfileIntent.OnEditLinksClicked -> sendEffect(NavigateToEditLinks)

            is ProfileIntent.OnNavigateBack -> sendEffect(NavigateBack)
            is ProfileIntent.OnShowWorkHistory -> {
                DivoAnalytics.logEvent(AnalyticsEvent.WorkHistoryOpened(state.value.userId))
                sendEffect(ShowWorkHistory(state.value.userId))
            }
            is ProfileIntent.OnGalleryClicked -> {
                val index = getGalleryItemIndex(intent.url, intent.isVideo)
                sendEffect(NavigateToGallery(index, intent.isVideo))
            }
            is ProfileIntent.OnProfileClicked -> sendEffect(NavigateToProfile(intent.profileId))
            is ProfileIntent.ConfirmWithdraw -> confirmWithdraw(intent.id)
            is ProfileIntent.OnEventClicked -> sendEffect(NavigateToEvent(intent.eventId))
            is ProfileIntent.OnFindSimilarProfiles -> {
                DivoAnalytics.logEvent(AnalyticsEvent.FaceRecognitionOpened("profile", intent.profileId))
                sendEffect(NavigateToFindSimilarProfiles(intent.photoUrl, intent.profileId))
            }
            is ProfileIntent.OnSendDMClicked -> {
                val currentUserId = DivoApi.userRepository.currentUserFlow.value?.id ?: 0
                DivoAnalytics.logEvent(AnalyticsEvent.DirectMessageStarted(currentUserId, intent.telegramId))
                sendEffect(ProfileEffect.NavigateToChat(intent.telegramId, intent.telegramAccessHash, intent.telegramUsername))
            }
            ProfileIntent.OnShowAppearances -> sendEffect(ShowAppearances)
            ProfileIntent.OnBackgroundReady -> setState { copy(hasBackgroundReady = true) }
            ProfileIntent.OnEventCreate -> {
                DivoAnalytics.logEvent(AnalyticsEvent.EventCreateStarted(state.value.userId))
                sendEffect(NavigateToCreateEvent)
            }
            ProfileIntent.OnLoadMoreSearchModels -> loadMoreSearchModels()
            is ProfileIntent.OnSearchModelsQueryChanged -> onSearchModelsQueryChanged(intent.query)
            ProfileIntent.OnLoadMoreAgencyModels -> viewModelScope.launch { agencyModelsPaginator.loadMore() }
            ProfileIntent.OnBookmarkClick -> toggleBookmark()
            ProfileIntent.OnLikeClick -> toggleLike()
            is ProfileIntent.OnEventApplied -> applyEvent(intent.eventId)
            is ProfileIntent.OnAddAgencyModel -> addAgencyModel(intent.userId, intent.note)
            is ProfileIntent.OnCancelAgencyModelRequest -> cancelAgencyModelRequest(intent.modelId)
            is ProfileIntent.OnToggleAgencySearch -> {
                if (intent.visible) {
                    DivoAnalytics.logEvent(AnalyticsEvent.AddModelStarted(state.value.userId))
                }
                setState { copy(isAgencySearchSheetVisible = intent.visible) }
            }
            is ProfileIntent.OnSelectAgencyModelForAdd -> selectAgencyModelForAdd(intent.model)
            ProfileIntent.OnReportProfileClicked -> onReportProfileClicked()
            ProfileIntent.OnDismissReportSheet -> setState { copy(showReportSheet = false) }
            is ProfileIntent.OnReportOptionSelected -> reportProfile(intent.reportKey)
            ProfileIntent.OnCreateChannelClicked -> {
                DivoAnalytics.logEvent(AnalyticsEvent.ChannelCreateStarted(state.value.userId))
                sendEffect(ProfileEffect.NavigateToCreateChannel())
            }
            ProfileIntent.OnDeleteProfileConfirmed -> deleteProfile()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            loadUserProfile(isOwnProfile = isOwnProfile)
            loadLatestWorkExperience(userId)
            val validState = state.first { it.userId > 0 && !it.isLoading && it.userInfo.role != RoleType.UNKNOWN }
            if (validState.userId <= 0) return@launch

            launch { portfolioPaginator.loadInitial() }
            launch { videoPaginator.loadInitial() }

            observeEngagementPaginators()
            observeSearchPaginator()
            observeGalleryListPaginator()
            observeVideoPaginator()

            if (!state.value.isModel) {
                launch { eventPaginator.loadInitial() }
                launch { agencyModelsPaginator.loadInitial() }
                observeEvents()
                observeAgencyModelsPaginator()
                observeSearchModelsPaginator()
            }
        }
    }

    // TODO: (Hack) Remove when backend fixes it
    private fun loadLatestWorkExperience(userId: Int) {
        viewModelScope.launch {
            val whResult = DivoApi.workHistory.getWorkHistory(userId)
            if (whResult is DivoResult.Success) {
                val latest = whResult.value.filter { it.id != -1 }.maxByOrNull { it.startDate }
                setState { copy(latestWorkExperience = latest) }
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }
            
            if (isOwnProfile) {
                val result = DivoApi.userRepository.getCurrentUserInfo(forceRefresh = true)
                if (result !is DivoResult.Success) {
                    setState { copy(isLoading = false, errorMessage = result.getErrorMessage()) }
                } else {
                    setState { copy(isLoading = false) } // The flow will automatically emit the new value
                }
            } else {
                val userResult = DivoApi.userRepository.getUserById(state.value.userId)
                if (userResult !is DivoResult.Success) {
                    setState { copy(isLoading = false, errorMessage = userResult.getErrorMessage()) }
                } else {
                    val userData = userResult.value
                    setState {
                        copy(
                            userInfo = userData,
                            userId = userData.id,
                            physicalParams = mapPhysicalParams(userData),
                            isLoading = false
                        )
                    }
                    if (userData.avatarId != 0L) {
                        val similarResult = DivoApi.faceRecognitionRepository.searchSimilar(userData.avatarId)
                        if (similarResult is DivoResult.Success) {
                            setState { copy(similarProfiles = similarResult.value) }
                        } else {
                            setState { copy(similarProfiles = emptyList()) }
                        }
                    }
                }
            }

            launch { portfolioPaginator.loadInitial() }
            launch { videoPaginator.loadInitial() }
            
            if (!state.value.isModel) {
                launch { eventPaginator.loadInitial() }
                launch { agencyModelsPaginator.loadInitial() }
                launch { searchModelsPaginator.loadInitial() }
            }
            if (engagementLoaded) {
                loadEngagement()
            }
            loadLatestWorkExperience(state.value.userId)
        }
    }

    private fun observeEngagementPaginators() {
        viewModelScope.launch {
            engagement.likedPaginator.state.collect { pState ->
                setState {
                    copy(
                        likedItems = pState.items,
                        isLoadingStats = pState.isLoading,
                        isLoadingLiked = pState.isLoading || pState.isLoadingMore,
                        hasMoreLiked = pState.hasMore
                    )
                }
            }
        }
        viewModelScope.launch {
            engagement.viewedPaginator.state.collect { pState ->
                setState {
                    copy(
                        viewedItems = pState.items,
                        isLoadingStats = pState.isLoading,
                        isLoadingViewed = pState.isLoading || pState.isLoadingMore,
                        hasMoreViewed = pState.hasMore
                    )
                }
            }
        }
        viewModelScope.launch {
            engagement.followedPaginator.state.collect { pState ->
                setState {
                    copy(
                        followedItems = pState.items,
                        isLoadingStats = pState.isLoading,
                        isLoadingFollowed = pState.isLoading || pState.isLoadingMore,
                        hasMoreFollowed = pState.hasMore
                    )
                }
            }
        }
    }

    private fun observeSearchPaginator() {
        viewModelScope.launch {
            engagement.searchPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResults = pState.items,
                        isLoadingSearch = pState.isLoading,
                        isLoadingMoreSearch = pState.isLoadingMore,
                        searchHasMore = pState.hasMore,
                    )
                }
            }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        searchModelsJob?.cancel()
        setState { copy(searchQuery = query, isSearchMode = query.isNotBlank()) }

        if (query.isBlank()) {
            engagement.searchPaginator.reset()
            setState { copy(searchResults = emptyList(), isLoadingSearch = false) }
            return
        }

        setState { copy(isLoadingSearch = true) }

        searchModelsJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            engagement.currentSearchQuery = query
            engagement.searchPaginator.reset()
            engagement.searchPaginator.loadInitial()

            val hasResults = engagement.searchPaginator.state.value.items.isNotEmpty()

            DivoAnalytics.logEvent(
                AnalyticsEvent.EngagementSearchPerformed(
                    state.value.activeStatsType?.name?.lowercase() ?: "unknown",
                    state.value.userId,
                    query,
                    hasResults
                )
            )
        }
    }

    private fun loadMoreSearchResults() {
        viewModelScope.launch {
            engagement.searchPaginator.loadMore()
        }
    }


    private fun observeAgencyModelsPaginator() {
        viewModelScope.launch {
            agencyModelsPaginator.state.collect { pState ->
                setState {
                    copy(
                        agencyModels = pState.items,
                        isLoadingAgencyModels = pState.isLoading,
                        isLoadingMoreAgencyModels = pState.isLoadingMore,
                        hasMoreAgencyModels = pState.hasMore,
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }
    }

    private fun observeGalleryListPaginator() {
        viewModelScope.launch {
            portfolioPaginator.state.collect { pState ->
                setState {
                    copy(
                        isLoadingMoreImages = pState.isLoadingMore,
                        hasMoreImages = pState.hasMore,
                        isLoadingImages = pState.isLoading
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }

        viewModelScope.launch {
            DivoApi.userRepository.galleryFlow(state.value.userId)
                .filterNotNull()
                .collect { gallery ->
                    setState {
                        copy(userGalleryItems = gallery.items)
                    }
                }
        }
    }

    private fun observeVideoPaginator() {
        viewModelScope.launch {
            videoPaginator.state.collect { pState ->
                setState {
                    copy(
                        isLoadingMoreVideos = pState.isLoadingMore,
                        hasMoreVideos = pState.hasMore,
                        isLoadingVideos = pState.isLoading
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }

        viewModelScope.launch {
            videoPaginator.state.first { !it.isLoading && !it.isLoadingMore }

            DivoApi.publicationRepository.publicationFlow(state.value.userId)
                .filterNotNull()
                .collect { data ->
                    setState {
                        copy(
                            videoItems = data.items.toPersistentList()
                        )
                    }
                }
        }
    }

    private fun observeSearchModelsPaginator() {
        viewModelScope.launch {
            searchModelsPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchModels = pState.items,
                        isLoadingSearchModels = pState.isLoading,
                        isLoadingMoreSearchModels = pState.isLoadingMore,
                        hasMoreSearchModels = pState.hasMore,
                        searchModelsError = pState.error
                    )
                }
                pState.error?.let { sendEffect(ShowError(it)) }
            }
        }
    }

    private fun onSearchModelsQueryChanged(query: String) {
        searchModelsJob?.cancel()
        setState { copy(searchModelsQuery = query) }

        if (query.isBlank()) {
            searchModelsPaginator.reset()

            viewModelScope.launch {
                if (state.value.searchModels.isEmpty()) {
                    searchModelsPaginator.loadInitial()
                }
            }
            return
        }

        searchModelsJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchModelsPaginator.reset()
            searchModelsPaginator.loadInitial()
            val hasResults = searchModelsPaginator.state.value.items.isNotEmpty()
            DivoAnalytics.logEvent(AnalyticsEvent.SearchPerformed("agency_models_search", hasResults, query))
        }
    }

    private fun loadMoreSearchModels() {
        viewModelScope.launch {
            searchModelsPaginator.loadMore()
        }
    }

    private fun addAgencyModel(userId: Int, note: String?) {
        viewModelScope.launch {
            setState { copy(isAddingAgencyModel = true) }
            val result = DivoApi.userRepository.addAgencyModel(userId, note)
            if (result is DivoResult.Success) {
                val agencyId = state.value.userInfo.agency?.id ?: 0
                // Refresh models list or update search status locally
                DivoAnalytics.logEvent(AnalyticsEvent.AddModelSuccess(userId, agencyId))
                kotlinx.coroutines.joinAll(
                    launch { agencyModelsPaginator.loadInitial(clearItems = false) },
                    launch { searchModelsPaginator.loadInitial(clearItems = false) }
                )
                sendEffect(ProfileEffect.AgencyModelAdded)
            } else {
                sendEffect(ShowError(result.getErrorMessage()))
            }
            setState { copy(isAddingAgencyModel = false) }
        }
    }

    private fun cancelAgencyModelRequest(modelId: Int) {
        viewModelScope.launch {
            val agencyId = state.value.userInfo.agency?.id ?: return@launch
            val result = DivoApi.userRepository.deleteAgencyModel(agencyId, modelId)
            if (result is DivoResult.Success) {
                DivoAnalytics.logEvent(AnalyticsEvent.RemoveModelSuccess(modelId, agencyId))
                agencyModelsPaginator.loadInitial(clearItems = false)
                searchModelsPaginator.loadInitial(clearItems = false)
            } else {
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun loadMorePortfolio() {
        viewModelScope.launch {
            portfolioPaginator.loadMore()
        }
    }

    private fun loadUserProfile(isOwnProfile: Boolean) {
        if (isOwnProfile) {
            observeOwnProfile()
        } else {
            loadOtherUserProfile()
        }
    }

    private fun observeOwnProfile() {
        setState { copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            DivoApi.userRepository.currentUserFlow
                .filterNotNull()
                .collect { userData ->
                    val params = mapPhysicalParams(userData)

                    setState {
                        copy(
                            isLoading = false,
                            userInfo = userData,
                            userId = userData.id,
                            physicalParams = params,
                        )
                    }
                }
        }

        viewModelScope.launch {
            val currUser = DivoApi.userRepository.currentUserFlow.value
            if (currUser == null || currUser.fullName.isEmpty()) {
                val result = DivoApi.userRepository.getCurrentUserInfo()
                if (result !is DivoResult.Success) {
                    val errorMsg = result.getErrorMessage()
                    setState { copy(isLoading = false, errorMessage = errorMsg) }
                    sendEffect(ShowError(errorMsg, true))
                }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            state.first { it.userInfo.role != RoleType.UNKNOWN && !it.isLoading }

            if (!state.value.isModel) {
                eventPaginator.loadInitial()
            }

            eventPaginator.state.collect { paginatorState ->
                setState {
                    copy(
                        events = paginatorState.items,
                        isLoadingEvents = paginatorState.isLoading,
                        isLoadingMoreEvents = paginatorState.isLoadingMore,
                        hasMoreEvents = paginatorState.hasMore,
                    )
                }
                paginatorState.error?.let {
                    sendEffect(ShowError(it))
                }
            }
        }
    }

    private fun loadOtherUserProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            val userResult = DivoApi.userRepository.getUserById(state.value.userId)

            if (userResult !is DivoResult.Success) {
                val errorMsg = userResult.getErrorMessage()
                setState { copy(isLoading = false, errorMessage = errorMsg) }
                sendEffect(ShowError(errorMsg, true))
                return@launch
            }

            val userData = userResult.value
            setState {
                copy(
                    userInfo = userData,
                    userId = userData.id,
                    physicalParams = mapPhysicalParams(userData),
                )
            }

            if (userData.avatarId != 0L) {
                val similarResult = DivoApi.faceRecognitionRepository.searchSimilar(userData.avatarId)

                when (similarResult) {
                    is DivoResult.Success -> setState {
                        copy(isLoading = false, similarProfiles = similarResult.value)
                    }
                    else -> {
                        setState { copy(isLoading = false, similarProfiles = emptyList()) }
                    }
                }
            } else {
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun loadEngagement() {
        viewModelScope.launch {
            listOf(
                async { engagement.likedPaginator.loadInitial() },
                async { engagement.viewedPaginator.loadInitial() },
                async { engagement.followedPaginator.loadInitial() }
            ).awaitAll()
        }
    }

    fun loadMoreEngagement(statsType: StatsType) {
        viewModelScope.launch {
            when (statsType) {
                StatsType.LIKES -> engagement.likedPaginator.loadMore()
                StatsType.VIEWS -> engagement.viewedPaginator.loadMore()
                StatsType.SAVES -> engagement.followedPaginator.loadMore()
            }
        }
    }

    private fun toggleBookmark() {
        val info = state.value.userInfo

        viewModelScope.launch {
            toggleBookmarkUseCase.execute(
                userId = info.id,
                isFollowed = info.isFollowed,
                currentFollowersCount = info.statistic.followersCount,
                screenName = "profile",
                onUpdate = { newFavorite, newCount ->
                    setState {
                        copy(
                            userInfo = userInfo.copy(
                                isFollowed = newFavorite,
                                statistic = userInfo.statistic.copy(followersCount = newCount)
                            )
                        )
                    }
                },
                onRollback = { setState { copy(userInfo = info) } },
                onSuccess = { newFavorite ->
                    sendEffect(ProfileEffect.ActionChanged(
                        resDrawableId = R.drawable.ic_divo_bookmark_glass_selected,
                        resStringId = if (newFavorite) R.string.BookmarkSaved else R.string.BookmarkUnsaved
                    ))
                },
                onError = { sendEffect(ShowError(it)) }
            )
        }
    }

    private fun toggleLike() {
        val info = state.value.userInfo

        viewModelScope.launch {
            toggleLikeUseCase.execute(
                userId = info.id,
                isLiked = info.isLikedByUser,
                currentCount = info.statistic.likesCount,
                screenName = "profile",
                onUpdate = { newLiked, newCount ->
                    setState {
                        copy(
                            userInfo = userInfo.copy(
                                isLikedByUser = newLiked,
                                statistic = userInfo.statistic.copy(likesCount = newCount)
                            )
                        )
                    }
                },
                onRollback = { setState { copy(userInfo = info) } },
                onSuccess = { newLiked ->
                    sendEffect(ProfileEffect.ActionChanged(
                        resDrawableId = R.drawable.ic_divo_favorite_selected,
                        resStringId = if (newLiked) R.string.Liked else R.string.Unliked
                    ))
                },
                onError = { sendEffect(ShowError(it)) }
            )
        }
    }

    private fun deleteProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.deleteAccount()
            if (result is DivoResult.Success) {
                setState { copy(isLoading = false) }
                sendEffect(ProfileEffect.NavigateToLogout)
            } else {
                setState { copy(isLoading = false) }
                sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
            }
        }
    }

    private fun reportProfile(reportKey: String) {
        viewModelScope.launch {
            setState { copy(showReportSheet = false, isLoading = true) }
            val res = DivoApi.userRepository.reportProfile(state.value.userId, reportKey)
            setState { copy(isLoading = false) }
            if (res is DivoResult.Success) {
                DivoAnalytics.logEvent(AnalyticsEvent.UserReported(state.value.userId.toLong(), reportKey))
                // Profile reported successfully
                sendEffect(ProfileEffect.SaveSuccess(R.string.ReportSent))
            } else {
                sendEffect(ShowError(res.getErrorMessage()))
            }
        }
    }

    private fun onStatsTabOpened(type: StatsType) {
        DivoAnalytics.logEvent(AnalyticsEvent.EngagementTabViewed(type.name.lowercase(), state.value.userId))
        setState {
            copy(
                activeStatsType = type,
                searchQuery = "",
                searchResults = emptyList(),
                isSearchMode = false,
                isLoadingSearch = false
            )
        }
        engagement.currentStatsType = when (type) {
            StatsType.VIEWS -> "viewed"
            StatsType.SAVES -> "followed"
            else -> "liked"
        }
        engagement.searchPaginator.reset()
        if (!engagementLoaded) {
            engagementLoaded = true
            viewModelScope.launch { loadEngagement() }
        }
    }

    private fun selectAgencyModelForAdd(model: org.telegram.divo.entity.AgencySearchModel?) {
        setState { copy(selectedAgencyModelForAdd = model) }
        if (model != null) {
            DivoAnalytics.logEvent(AnalyticsEvent.SearchModelTapped(model.userId, "agency_search"))
            viewModelScope.launch {
                val res = DivoApi.userRepository.getUserById(model.userId)
                if (res is DivoResult.Success) {
                    setState { copy(selectedAgencyModelInfo = res.value) }
                }
            }
        } else {
            setState { copy(selectedAgencyModelInfo = null) }
        }
    }

    private fun onReportProfileClicked() {
        if (state.value.reportTypes == null) {
            viewModelScope.launch {
                setState { copy(isLoading = true) }
                val typesRes = DivoApi.userRepository.getFeedReportTypes()
                setState { copy(isLoading = false) }
                if (typesRes is DivoResult.Success) {
                    setState { copy(reportTypes = typesRes.value, showReportSheet = true) }
                } else {
                    sendEffect(ShowError(typesRes.getErrorMessage()))
                }
            }
        } else {
            setState { copy(showReportSheet = true) }
        }
    }

    private fun uploadPhoto(file: Result<File>) {
        viewModelScope.launch {
            setState { copy(mediaUploading = true) }

            val result = file
                .fold(
                    onSuccess = { DivoApi.userRepository.uploadPhoto(it) },
                    onFailure = { DivoResult.UnknownError(it) }
                )
                .flatMap { DivoApi.userRepository.addToGallery(it.uuid) }

            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.ProfileMediaUploaded("photo", state.value.userId))
                    setState {
                        copy(mediaUploading = false)
                    }
                }
                else -> {
                    setState {
                        copy(mediaUploading = false)
                    }
                    sendEffect(ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun uploadVideo(file: Result<File>) {
        viewModelScope.launch {
            setState { copy(mediaUploading = true) }

            val result = file
                .fold(
                    onSuccess = { DivoApi.userRepository.uploadPhoto(it) },
                    onFailure = { DivoResult.UnknownError(it) }
                )
                .flatMap {
                    DivoApi.publicationRepository.createPublication(
                        title = "",
                        description = "",
                        type = "educational",
                        fileUuids = listOf(it.uuid),
                        userId = state.value.userId
                    )
                }

            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.ProfileMediaUploaded("video", state.value.userId))
                    setState { copy(mediaUploading = false) }
                }
                else -> {
                    setState { copy(mediaUploading = false) }
                    sendEffect(ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun changeBackground(file: Result<File>) {
        viewModelScope.launch {
            setState { copy(backgroundChanging = true) }

            val result = file
                .fold(
                    onSuccess = { DivoApi.userRepository.uploadPhoto(it) },
                    onFailure = { DivoResult.UnknownError(it) }
                )
                .flatMap  {
                    DivoApi.userRepository.updateProfile(
                        userInfo = state.value.userInfo.copy(photoUuid = it.uuid)
                    )
                }

            when (result) {
                is DivoResult.Success -> {
                    DivoAnalytics.logEvent(AnalyticsEvent.ProfileBackgroundChanged())
                    setState {
                        copy(
                            backgroundChanging = false,
                            userInfo = userInfo.copy(
                                photoUrl = result.value.photoUrl
                            )
                        )
                    }
                }
                else -> {
                    setState {
                        copy(backgroundChanging = false)
                    }
                    sendEffect(ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun mapPhysicalParams(user: UserInfo): PhysicalParams {
        val appearance = user.model?.appearance

        val storedSystem = appearance?.measuringSystem
            ?.takeIf { it.isNotBlank() }
            ?: user.measuringSystem.takeIf { it.isNotBlank() }

        return PhysicalParams(
            gender = user.gender?.title.orEmpty(),
            age = user.birthday,
            height = appearance?.height ?: 0f,
            weight = appearance?.weight ?: 0f,
            waist = appearance?.waist?.toInt() ?: 0,
            hips = appearance?.hips?.toInt() ?: 0,
            shoeSize = appearance?.shoesSize ?: 0f,
            hairLength = appearance?.hairLength?.title.orEmpty(),
            hairColor = appearance?.hairColor?.title.orEmpty(),
            eyeColor = appearance?.eyeColor?.title.orEmpty(),
            skinColor = appearance?.skinColor?.title.orEmpty(),
            breastSize = appearance?.breastSize.orEmpty(),
            measuringSystem = storedSystem.orEmpty(),
        )
    }


    private fun openLink(socialNetworkType: SocialNetworkType) {
        val url = when (socialNetworkType) {
            SocialNetworkType.TIKTOK -> state.value.userInfo.model?.tiktokUrl ?: state.value.userInfo.agency?.tiktokUrl.orEmpty()
            SocialNetworkType.INSTAGRAM -> state.value.userInfo.model?.instagramUrl ?: state.value.userInfo.agency?.instagramUrl.orEmpty()
            SocialNetworkType.WEBSITE -> state.value.userInfo.model?.websiteUrl ?: state.value.userInfo.agency?.websiteUrl.orEmpty()
            SocialNetworkType.YOUTUBE -> state.value.userInfo.model?.youtubeUrl ?: state.value.userInfo.agency?.youtubeUrl.orEmpty()
        }

        sendEffect(ProfileEffect.OpenUrl(url))
    }

    private fun getGalleryItemIndex(url: String, isVideo: Boolean): Int {
        return if (isVideo) {
            state.value.videoItems
                .indexOfFirst { it.files.any { f -> f.fullUrl == url } }
                .coerceAtLeast(0)
        } else {
            state.value.userGalleryItems
                .indexOfFirst { it.photoUrl == url }
                .coerceAtLeast(0)
        }
    }

    private fun applyEvent(id: Int) {
        val event = state.value.events.find { it.id == id } ?: return
        val isCurrentlyApplied = event.isApplied

        if (isCurrentlyApplied) {
            sendEffect(ProfileEffect.ShowWithdrawConfirmation(id))
        } else {
            sendEffect(ProfileEffect.NavigateToApplyConfirmation(id))
        }
    }

    private fun confirmWithdraw(id: Int) {
        val event = state.value.events.find { it.id == id } ?: return
        val isCurrentlyApplied = event.isApplied
        if (!isCurrentlyApplied) return

        val expectedNewCount = event.appliesCount - 1
        
        fun updateEventLocally(isApp: Boolean, count: Int) {
            setState {
                copy(events = events.map { if (it.id == id) it.copy(isApplied = isApp, appliesCount = count) else it })
            }
            eventPaginator.updateItem({ it.id == id }) {
                it.copy(isApplied = isApp, appliesCount = count)
            }
        }
        
        updateEventLocally(false, expectedNewCount)
        DivoApi.eventRepository.notifyEventParticipationChanged(id, false, expectedNewCount)

        val currentUserId = DivoApi.userRepository.currentUserFlow.value?.id ?: 0
        DivoAnalytics.logEvent(AnalyticsEvent.EventWithdraw(id.toLong(), currentUserId.toLong()))

        viewModelScope.launch {
            val result = DivoApi.eventRepository.unapplyEvent(id)

            if (result !is DivoResult.Success) {
                updateEventLocally(true, event.appliesCount)
                DivoApi.eventRepository.notifyEventParticipationChanged(id, true, event.appliesCount)
                sendEffect(ShowError(result.getErrorMessage()))
            }
        }
    }

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L

        fun factory(userId: Int, isOwnProfile: Boolean) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(userId, isOwnProfile) as T
            }
        }
    }
}
