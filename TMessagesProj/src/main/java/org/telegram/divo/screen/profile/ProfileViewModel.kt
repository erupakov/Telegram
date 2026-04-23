package org.telegram.divo.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.flatMap
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.screen.profile.ProfileEffect.*
import org.telegram.divo.screen.profile.components.StatsType
import org.telegram.divo.usecase.EngagementInteractor
import org.telegram.divo.usecase.GetEventListUseCase
import org.telegram.divo.usecase.GetUserGalleryUseCase
import org.telegram.divo.usecase.GetUserVideosUseCase
import java.io.File

class ProfileViewModel(
    private val userId: Int,
    private val isOwnProfile: Boolean,
) : BaseViewModel<ProfileViewState, ProfileIntent, ProfileEffect>() {

    //private var searchJob: Job? = null

    private val eventPaginator = GetEventListUseCase().paginator

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

    override fun createInitialState(): ProfileViewState {
        return ProfileViewState(
            userId = userId,
            isOwnProfile = isOwnProfile
        )
    }

    init {
        loadData()
    }

//    private val searchPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
//        when (val result = DivoApi.publicationRepository.searchFeeds(
//            offset = offset,
//            limit = limit,
//            query = state.value.searchQuery
//        )) {
//            is DivoResult.Success -> {
//                val data = result.value
//                PaginatedResult(
//                    items = data.items,
//                    totalCount = data.pagination?.totalCount ?: data.items.size
//                )
//            }
//            else -> throw Exception(result.getErrorMessage())
//        }
//    }

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnLoad -> loadData()
            is ProfileIntent.OpenSocialLink -> openLink(intent.socialNetworkType)
            is ProfileIntent.OnBackgroundPhotoSelected -> { changeBackground(intent.file) }
            is ProfileIntent.OnPortfolioPhotoSelected -> { uploadPhoto(intent.file) }
            is ProfileIntent.OnVideoSelected -> uploadVideo(intent.file)
            is ProfileIntent.OnClearPortfolioUpload -> {}
            is ProfileIntent.OnLoadMoreEngagementStats -> loadMoreEngagement(intent.type)
            is ProfileIntent.OnSearchQueryChanged -> {}//onSearchQueryChanged(intent.query)
            is ProfileIntent.OnLoadMoreSearchResults -> {}//loadMoreSearchResults()
            is ProfileIntent.OnLoadMorePortfolio -> loadMorePortfolio()
            is ProfileIntent.OnLoadMoreVideos -> viewModelScope.launch { videoPaginator.loadMore() }
            ProfileIntent.OnLoadMoreEvents -> viewModelScope.launch { eventPaginator.loadMore() }
            ProfileIntent.OnEditClicked -> sendEffect(ProfileEffect.NavigateToEdit)
            is ProfileIntent.OnEditLinksClicked -> sendEffect(ProfileEffect.NavigateToEditLinks)

            is ProfileIntent.OnNavigateBack -> sendEffect(ProfileEffect.NavigateBack)
            is ProfileIntent.OnShowWorkHistory -> sendEffect(ShowWorkHistory(state.value.isOwnProfile))
            is ProfileIntent.OnGalleryClicked -> {
                val index = getGalleryItemIndex(intent.url, intent.isVideo)
                sendEffect(NavigateToGallery(index, intent.isVideo))
            }
            is ProfileIntent.OnProfileClicked -> sendEffect(NavigateToProfile(intent.profileId))
            is ProfileIntent.OnAddModelClicked -> sendEffect(ProfileEffect.NavigateToAddModel)
            is ProfileIntent.OnEventClicked -> sendEffect(NavigateToEvent(intent.eventId))
            is ProfileIntent.OnFindSimilarProfiles -> sendEffect(NavigateToFindSimilarProfiles(state.value.userInfo.photoUrl))
            ProfileIntent.OnShowAppearances -> sendEffect(ProfileEffect.ShowAppearances)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            loadUserProfile(isOwnProfile = isOwnProfile)
            val validUserId = state.first { it.userId > 0 && !it.isLoading }.userId
            if (validUserId <= 0) return@launch

            launch { loadEngagement() }
            launch { portfolioPaginator.loadInitial() }
            launch { videoPaginator.loadInitial() }
            observeEvents()
            observeEngagementPaginators()
            //observeSearchPaginator()
            observeGalleryListPaginator()
            observeVideoPaginator()
        }
    }

    private fun observeEngagementPaginators() {
        viewModelScope.launch {
            engagement.likedPaginator.state.collect { pState ->
                setState {
                    copy(
                        likedItems = pState.items,
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
                        isLoadingFollowed = pState.isLoading || pState.isLoadingMore,
                        hasMoreFollowed = pState.hasMore
                    )
                }
            }
        }
    }

    //TODO пока нет ручки для поиска Engagement
//    private fun observeSearchPaginator() {
//        viewModelScope.launch {
//            searchPaginator.state.collect { pState ->
//                setState {
//                    copy(
//                        searchResults = pState.items,
//                        isLoadingMoreSearch = pState.isLoadingMore,
//                        searchHasMore = pState.hasMore,
//                    )
//                }
//            }
//        }
//    }

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

    //TODO пока нет ручки для поиска
//    private fun onSearchQueryChanged(query: String) {
//        searchJob?.cancel()
//        setState { copy(searchQuery = query) }
//
//        if (query.isBlank()) {
//            searchPaginator.reset()
//            setState { copy(isSearchMode = false) }
//
//            viewModelScope.launch {
//                if (state.value.feedItems.isEmpty()) {
//                    setState { copy(isLoadingStats = true) }
//                    feedPaginator.loadInitial()
//                    setState { copy(isLoadingStats = false) }
//                }
//            }
//            return
//        }
//
//        searchJob = viewModelScope.launch {
//            delay(SEARCH_DEBOUNCE_MS)
//            setState { copy(isSearchMode = true, isLoadingStats = true) }
//            searchPaginator.reset()
//            searchPaginator.loadInitial()
//            setState { copy(isLoadingStats = false) }
//        }
//    }
//
//    private fun loadMoreSearchResults() {
//        viewModelScope.launch {
//            searchPaginator.loadMore()
//        }
//    }

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
                    if (userData.fullName.isNotEmpty()) {
                        val params = mapPhysicalParams(userData)

                        setState {
                            copy(
                                isLoading = false,
                                userInfo = userData,
                                userId = userData.id,
                                physicalParams = params,
                            )
                        }
                    } else {
                        // User exists in basic cache but lack details, UI stays in loading state
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
                    sendEffect(ProfileEffect.ShowError(errorMsg))
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
                    sendEffect(ProfileEffect.ShowError(it))
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
                sendEffect(ProfileEffect.ShowError(errorMsg))
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

            // Загружаем похожие профили
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
                is DivoResult.Success -> setState {
                    copy(mediaUploading = false)
                }
                else -> {
                    setState {
                        copy(mediaUploading = false)
                    }
                    sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
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
                is DivoResult.Success -> setState { copy(mediaUploading = false) }
                else -> {
                    setState { copy(mediaUploading = false) }
                    sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
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
                is DivoResult.Success -> setState {
                    copy(
                        backgroundChanging = false,
                        userInfo = userInfo.copy(
                            photoUrl = result.value.photoUrl
                        )
                    )
                }
                else -> {
                    setState {
                        copy(backgroundChanging = false)
                    }
                    sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun mapPhysicalParams(user: UserInfo): PhysicalParams {
        val appearance = user.model?.appearance

        return PhysicalParams(
            gender = user.gender?.title.orEmpty(),
            age = user.birthday,
            height = appearance?.height ?: 0f,
            waist = appearance?.waist?.toInt() ?: 0,
            hips = appearance?.hips?.toInt() ?: 0,
            shoeSize = appearance?.shoesSize ?: 0f,
            hairLength = appearance?.hairLength?.title.orEmpty(),
            hairColor = appearance?.hairColor?.title.orEmpty(),
            eyeColor = appearance?.eyeColor?.title.orEmpty(),
            skinColor = appearance?.skinColor?.title.orEmpty(),
            breastSize = appearance?.breastSize.orEmpty()
        )
    }


    private fun openLink(socialNetworkType: SocialNetworkType) {
        val url = when (socialNetworkType) {
            SocialNetworkType.TIKTOK -> state.value.userInfo.model?.tiktokUrl.orEmpty()
            SocialNetworkType.INSTAGRAM -> state.value.userInfo.model?.instagramUrl.orEmpty()
            SocialNetworkType.WEBSITE -> state.value.userInfo.model?.websiteUrl.orEmpty()
            SocialNetworkType.YOUTUBE -> state.value.userInfo.model?.youtubeUrl.orEmpty()
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
