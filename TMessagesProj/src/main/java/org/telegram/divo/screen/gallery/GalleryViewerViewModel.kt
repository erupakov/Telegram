package org.telegram.divo.screen.gallery

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.PublicationList
import org.telegram.divo.entity.UserGalleryList

class GalleryViewerViewModel : BaseViewModel<GalleryViewerState, GalleryIntent, GalleryEffect>() {

    companion object {
        private const val PAGE_SIZE = 12
        private const val PRELOAD_THRESHOLD = 3
    }

    override fun createInitialState() = GalleryViewerState()

    override fun handleIntent(intent: GalleryIntent) {
        when (intent) {
            is GalleryIntent.OnLoad -> load(intent.source)
            is GalleryIntent.OnLoadMore -> loadMore()
        }
    }

    private fun load(source: GallerySource) {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            if (source is GallerySource.Feed) {
                Log.d("MyTag", "${source.initialIndex}")
                setState {
                    copy(
                        source = source,
                        initialIndex = source.initialIndex,
                        items = source.items,
                        hasMore = false,
                        isLoading = false,
                    )
                }
                return@launch
            }

            val initialIndex = when (source) {
                is GallerySource.Portfolio -> source.initialIndex
                is GallerySource.Video -> source.initialIndex
                else -> 0
            }

            setState {
                copy(source = source, initialIndex = initialIndex)
            }

            // Показываем кеш сразу если есть
            val cachedItems = getCachedItems(source)
            val cachedHasMore = getCachedHasMore(source)

            if (cachedItems != null) {
                setState {
                    copy(
                        items = cachedItems,
                        hasMore = cachedHasMore,
                        isLoading = false
                    )
                }
            }

            observeCache(source)

            if (cachedItems == null) {
                fetchPage(source, offset = 0)
                setState { copy(isLoading = false) }
                return@launch
            }

            if (initialIndex >= cachedItems.size - PRELOAD_THRESHOLD && cachedHasMore) {
                loadMore()
            }
        }
    }

    private fun observeCache(source: GallerySource) {
        viewModelScope.launch {
            when (source) {
                is GallerySource.Portfolio -> {
                    DivoApi.userRepository.galleryFlow(source.userId)
                        .filterNotNull()
                        .collect { data ->
                            setState {
                                copy(
                                    items = data.items.map { GalleryItem(it.photoUrl, isVideo = false) },
                                    hasMore = data.hasMore(),
                                )
                            }
                        }
                }
                is GallerySource.Video -> {
                    DivoApi.publicationRepository.publicationFlow(source.userId)
                        .filterNotNull()
                        .collect { data ->
                            setState {
                                copy(
                                    items = data.items.flatMap { publication ->
                                        publication.files
                                            .filter { it.isVideo }
                                            .map { GalleryItem(it.fullUrl, isVideo = true) }
                                    },
                                    hasMore = data.hasMore(),
                                )
                            }
                        }
                }
                else -> {}
            }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val s = state.value
            if (s.isLoadingMore || !s.hasMore || s.source == null) return@launch

            setState { copy(isLoadingMore = true) }
            fetchPage(s.source, offset = s.items.size)
            setState { copy(isLoadingMore = false) }
        }
    }

    private suspend fun fetchPage(source: GallerySource, offset: Int) {
        when (source) {
            is GallerySource.Portfolio -> {
                val result = DivoApi.userRepository.getUserGalleryList(
                    userId = source.userId,
                    offset = offset,
                    limit = PAGE_SIZE,
                )

                if (result !is DivoResult.Success) {
                    sendEffect(GalleryEffect.ShowError(result.getErrorMessage()))
                }
            }
            is GallerySource.Video -> {
                val result = DivoApi.publicationRepository.getPublicationList(
                    userId = source.userId,
                    offset = offset,
                    limit = PAGE_SIZE,
                )
                if (result !is DivoResult.Success) {
                    sendEffect(GalleryEffect.ShowError(result.getErrorMessage()))
                }
            }
            is GallerySource.Feed -> return
        }
    }

    private fun getCachedItems(source: GallerySource): List<GalleryItem>? {
        return when (source) {
            is GallerySource.Portfolio -> DivoApi.userRepository
                .getGalleryCache(source.userId)
                ?.items
                ?.map { GalleryItem(it.photoUrl, isVideo = false) }
            is GallerySource.Video -> DivoApi.publicationRepository
                .getPublicationCache(source.userId)
                ?.items
                ?.flatMap { publication ->
                    publication.files
                        .filter { it.isVideo }
                        .map { GalleryItem(it.fullUrl, isVideo = true) }
                }
                ?.takeIf { it.isNotEmpty() }
            is GallerySource.Feed -> null
        }
    }

    private fun getCachedHasMore(source: GallerySource): Boolean {
        return when (source) {
            is GallerySource.Portfolio -> DivoApi.userRepository
                .getGalleryCache(source.userId)?.hasMore() ?: false
            is GallerySource.Video -> DivoApi.publicationRepository
                .getPublicationCache(source.userId)?.hasMore() ?: false
            else -> false
        }
    }

    private fun UserGalleryList.hasMore(): Boolean {
        return (pagination?.totalCount ?: 0) > items.size
    }

    private fun PublicationList.hasMore(): Boolean {
        return (pagination?.totalCount ?: 0) > items.size
    }
}