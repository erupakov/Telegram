package org.telegram.divo.screen.gallery

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState

data class GalleryViewerState(
    val source: GallerySource? = null,
    val initialIndex: Int = 0,
    val items: List<GalleryItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
) : ViewState

sealed class GalleryIntent : ViewIntent {
    data class OnLoad(val source: GallerySource) : GalleryIntent()
    object OnLoadMore : GalleryIntent()
}

sealed class GalleryEffect : ViewEffect {
    data class ShowError(val message: String) : GalleryEffect()
}

sealed class GallerySource {
    data class Portfolio(val userId: Int, val initialIndex: Int) : GallerySource()
    data class Video(val userId: Int, val initialIndex: Int) : GallerySource()
    data class Feed(val items: List<GalleryItem>, val initialIndex: Int) : GallerySource()
}

data class GalleryItem(
    val url: String,
    val isVideo: Boolean,
)