package org.telegram.divo.screen.profile

import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.OffsetPaginator
import org.telegram.divo.common.PaginatedResult
import org.telegram.divo.common.formattedAge
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.flatMap
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.entity.UserSocialNetwork
import org.telegram.divo.screen.profile.components.StatsType
import java.io.File
import kotlin.random.Random

class ProfileViewModel : BaseViewModel<ProfileViewState, ProfileIntent, ProfileEffect>() {

    companion object {
        private const val PAGE_SIZE = 10
        private const val SEARCH_DEBOUNCE_MS = 400L
    }

    private var searchJob: Job? = null

    private val feedPaginator = OffsetPaginator(
        limit = PAGE_SIZE
    ) { offset, limit ->
        when (val result = DivoApi.publicationRepository.getFeed(offset, limit)) {
            is DivoResult.Success -> {
                val feed = result.value
                PaginatedResult(
                    items = feed.items,
                    totalCount = feed.pagination?.totalCount ?: feed.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    private val searchPaginator = OffsetPaginator(limit = PAGE_SIZE) { offset, limit ->
        when (val result = DivoApi.publicationRepository.searchFeeds(
            offset = offset,
            limit = limit,
            query = state.value.searchQuery
        )) {
            is DivoResult.Success -> {
                val data = result.value
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    private val portfolioPaginator = OffsetPaginator(limit = 12) { offset, limit ->
        when (val result = DivoApi.userRepository.getUserGalleryList(
            userId = state.value.userId,
            limit = limit,
            offset = offset,
        )) {
            is DivoResult.Success -> {
                val data = result.value
                PaginatedResult(
                    items = data.items,
                    totalCount = data.pagination?.totalCount ?: data.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    private val videoPaginator = OffsetPaginator(limit = 15) { offset, limit ->
        when (val result = DivoApi.publicationRepository.getPublicationList(
            offset = offset,
            limit = limit,
            userId = state.value.userId
        )) {
            is DivoResult.Success -> {
                PaginatedResult(
                    items = result.value.items.filter { it.files.any { f -> f.isVideo } },
                    totalCount = result.value.pagination?.totalCount ?: result.value.items.size
                )
            }
            else -> throw Exception(result.getErrorMessage())
        }
    }

    override fun createInitialState(): ProfileViewState {
        return ProfileViewState()
    }

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnLoad -> loadData(userId = intent.userId, isOwnProfile = intent.isOwnProfile)
            is ProfileIntent.OpenSocialLink -> { } // openLink(intent.url)
            is ProfileIntent.OnBackgroundPhotoSelected -> { changeBackground(intent.file) }
            is ProfileIntent.OnPortfolioPhotoSelected -> { uploadPhoto(intent.file) }
            is ProfileIntent.OnClearPortfolioUpload -> {}
            is ProfileIntent.OnLoadEngagementStats -> loadEngagementStats(intent.type, false)
            is ProfileIntent.OnLoadMoreEngagementStats -> loadEngagementStats(intent.type, true)
            is ProfileIntent.OnSearchQueryChanged -> onSearchQueryChanged(intent.query)
            is ProfileIntent.OnLoadMoreSearchResults -> loadMoreSearchResults()
            is ProfileIntent.OnLoadMorePortfolio -> loadMorePortfolio()
            is ProfileIntent.OnLoadMoreVideos -> viewModelScope.launch { videoPaginator.loadMore() }
        }
    }

    private fun loadData(userId: Int, isOwnProfile: Boolean) {
        viewModelScope.launch {
            setState { copy(userId = userId, isOwnProfile = isOwnProfile) }

            launch { loadUserProfile(isOwnProfile = isOwnProfile) }
            launch { portfolioPaginator.loadInitial() }
            launch { loadSimilarProfiles() }
            launch { videoPaginator.loadInitial() }
            observePaginator()
            observeSearchPaginator()
            observeGalleryListPaginator()
            observeVideoPaginator()
        }
    }

    private fun observePaginator() {
        viewModelScope.launch {
            feedPaginator.state.collect { pState ->
                setState {
                    copy(
                        feedItems = pState.items,
                        isLoadingAllUsers = pState.isLoading,
                        isLoadingMoreFeed = pState.isLoadingMore,
                        feedHasMore = pState.hasMore,
                        errorMessage = pState.error ?: state.value.errorMessage
                    )
                }
            }
        }
    }

    private fun observeSearchPaginator() {
        viewModelScope.launch {
            searchPaginator.state.collect { pState ->
                setState {
                    copy(
                        searchResults = pState.items,
                        isLoadingMoreSearch = pState.isLoadingMore,
                        searchHasMore = pState.hasMore,
                    )
                }
            }
        }
    }

    private fun observeGalleryListPaginator() {
        viewModelScope.launch {
            portfolioPaginator.state.collect { pState ->
                setState {
                    copy(
                        userGalleryItems = pState.items,
                        isLoadingMoreImages = pState.isLoadingMore,
                        hasMoreImages = pState.hasMore
                    )
                }
            }
        }
    }

    private fun observeVideoPaginator() {
        viewModelScope.launch {
            videoPaginator.state.collect { pState ->
                setState {
                    copy(
                        videoItems = pState.items.distinctBy { it.id }.toPersistentList(),
                        isLoadingMoreVideos = pState.isLoadingMore,
                        hasMoreVideos = pState.hasMore,
                    )
                }
            }
        }
    }

    private fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        setState { copy(searchQuery = query) }

        if (query.isBlank()) {
            searchPaginator.reset()
            setState { copy(isSearchMode = false) }

            viewModelScope.launch {
                if (state.value.feedItems.isEmpty()) {
                    setState { copy(isLoadingStats = true) }
                    feedPaginator.loadInitial()
                    setState { copy(isLoadingStats = false) }
                }
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            setState { copy(isSearchMode = true, isLoadingStats = true) }
            searchPaginator.reset()
            searchPaginator.loadInitial()
            setState { copy(isLoadingStats = false) }
        }
    }

    private fun loadMoreSearchResults() {
        viewModelScope.launch {
            searchPaginator.loadMore()
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
                    val social = mapSocialLinks(userData.userSocialNetworks)
                    val stats = UserStatistic(
                        followers = userData.statistic.followersCount,
                        following = userData.statistic.followingCount,
                        views = userData.statistic.viewsCount,
                    )
                    setState {
                        copy(
                            isLoading = false,
                            userInfo = userData,
                            physicalParams = params,
                            socialLinks = social,
                            statistic = stats,
                        )
                    }
                }
        }

        viewModelScope.launch {
            if (DivoApi.userRepository.currentUserFlow.value == null) {
                val result = DivoApi.userRepository.getCurrentUserInfo()
                if (result !is DivoResult.Success) {
                    val errorMsg = result.getErrorMessage()
                    setState { copy(isLoading = false, errorMessage = errorMsg) }
                    sendEffect(ProfileEffect.ShowError(errorMsg))
                }
            }
        }
    }

    private fun loadOtherUserProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            val result = DivoApi.userRepository.getUserById(state.value.userId)

            if (result is DivoResult.Success) {
                val userData = result.value
                setState {
                    copy(
                        isLoading = false,
                        userInfo = userData,
                        physicalParams = mapPhysicalParams(userData),
                        socialLinks = mapSocialLinks(userData.userSocialNetworks),
                        statistic = UserStatistic(
                            followers = userData.statistic.followersCount,
                            following = userData.statistic.followingCount,
                            views = userData.statistic.viewsCount,
                        ),
                    )
                }
            } else {
                val errorMsg = result.getErrorMessage()
                setState { copy(isLoading = false, errorMessage = errorMsg) }
                sendEffect(ProfileEffect.ShowError(errorMsg))
            }
        }
    }

    //TODO временно
    private fun loadEngagementStats(type: StatsType, loadMore: Boolean) {
        viewModelScope.launch {
            if (loadMore) {
                feedPaginator.loadMore()
            } else {
                setState { copy(isLoadingStats = true) }
                feedPaginator.loadInitial()
                setState { copy(isLoadingStats = false) }
            }
        }
    }

    //TODO временно
    private suspend fun loadSimilarProfiles() {
        val limit = Random.nextInt(4, 7)

        val result = DivoApi.userRepository.getAgencyModels(limit = limit)
        if (result is DivoResult.Success) {
            setState {
                copy(
                    similarModels = result.value.items,
                )
            }
        } else {
            sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
        }
    }

    private fun uploadPhoto(file: Result<File>) {
        viewModelScope.launch {
            setState { copy(portfolioUploading = true) }

            val result = file
                .fold(
                    onSuccess = { DivoApi.userRepository.uploadPhoto(it) },
                    onFailure = { DivoResult.UnknownError(it) }
                )
                .flatMap { DivoApi.userRepository.addToGallery(it.uuid) }

            when (result) {
                is DivoResult.Success -> setState {
                    copy(
                        portfolioUploading = false,
                        userGalleryItems = listOf(result.value) + userGalleryItems
                    )
                }
                else -> {
                    setState {
                        copy(portfolioUploading = false, errorMessage = result.getErrorMessage())
                    }
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
                        copy(backgroundChanging = false, errorMessage = result.getErrorMessage())
                    }
                    sendEffect(ProfileEffect.ShowError(result.getErrorMessage()))
                }
            }
        }
    }

    private fun mapPhysicalParams(user: UserInfo): PhysicalParams {
        val appearance = user.model.appearance

        return PhysicalParams(
            gender = user.gender.title,
            age = user.birthday.formattedAge(),
            height = appearance.height,
            waist = appearance.waist.toInt(),
            hips = appearance.hips.toInt(),
            shoeSize = appearance.shoesSize,
            hairLength = appearance.hairLength.title,
            hairColor = appearance.hairColor.title,
            eyeColor = appearance.eyeColor.title,
            skinColor = appearance.skinColor.title,
            breastSize = appearance.breastSize
        )
    }

    private fun mapSocialLinks(socials: List<UserSocialNetwork>): SocialLinks {
        return SocialLinks(
            instagram = socials
                .firstOrNull { it.provider == "instagram" }
                ?.name
                .orEmpty(),

            tiktok = socials
                .firstOrNull { it.provider == "facebook" }
                ?.name
                .orEmpty(),

            youtube = socials
                .firstOrNull { it.provider == "youtube" }
                ?.name
                .orEmpty(),

            website = socials
                .firstOrNull { it.provider == "website" }
                ?.name
                .orEmpty()
        )
    }

//    private fun openLink(url: String) {
//        if (url.isNotEmpty()) {
//            sendEffect(ProfileEffect.OpenUrl(url))
//        }
//    }
}
