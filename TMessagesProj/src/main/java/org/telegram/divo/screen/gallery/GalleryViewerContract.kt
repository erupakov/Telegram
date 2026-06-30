package org.telegram.divo.screen.gallery

import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState

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
    data class OnDelete(val id: Int) : GalleryIntent()
    object OnLoadMore : GalleryIntent()
}

sealed class GalleryEffect : ViewEffect {
    data class ShowError(val message: String) : GalleryEffect()
    object Deleted : GalleryEffect()
}

sealed class GallerySource {
    abstract val screenName: String
    abstract val userId: Int
    data class Portfolio(override val userId: Int, val initialIndex: Int, override val screenName: String) : GallerySource()
    data class Video(override val userId: Int, val initialIndex: Int, override val screenName: String) : GallerySource()
    data class Feed(val items: List<GalleryItem>, val initialIndex: Int, override val userId: Int, override val screenName: String) : GallerySource()
}

data class GalleryItem(
    val id: Int,
    val url: String,
    val isVideo: Boolean,
)

object GallerySourceHolder {
    var pendingSource: GallerySource? = null
}