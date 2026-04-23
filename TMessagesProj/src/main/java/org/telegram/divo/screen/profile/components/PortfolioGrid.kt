package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.flow.distinctUntilChanged
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.entity.SimilarFace
import org.telegram.divo.entity.UserGalleryItem

@Composable
fun PortfolioGrid(
    modifier: Modifier = Modifier,
    portfolioItems: List<UserGalleryItem>,
    similarItems: List<SimilarFace>,
    isOwnProfile: Boolean,
    isUploading: Boolean,
    isLoadingMore: Boolean,
    isFirstLoading: Boolean,
    hasMore: Boolean,
    topPadding: Dp = 0.dp,
    onLoadMore: () -> Unit,
    onPhotoClicked: (String) -> Unit,
    onSimilarClicked: (Int) -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    val gridState = rememberLazyGridState()
    val context = LocalContext.current

    val displayMetrics = context.resources.displayMetrics
    val cellSizePx = remember {
        (displayMetrics.widthPixels / 3)
    }

    val currentHasMore by rememberUpdatedState(hasMore)
    val currentLoading by rememberUpdatedState(isLoadingMore)

    LaunchedEffect(gridState) {
        snapshotFlow { 
            val layoutInfo = gridState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItemsCount = layoutInfo.totalItemsCount

            lastVisibleItemIndex >= (totalItemsCount - 3).coerceAtLeast(0)
        }
        .distinctUntilChanged()
        .collect { reachedEnd ->
            if (reachedEnd && currentHasMore && !currentLoading) {
                onLoadMore()
            }
        }
    }

    Box() {
        val bottomPadding = if (isOwnProfile) 72.dp else 16.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() + bottomPadding
            )
        ) {
            items(
                items = portfolioItems,
                key = { it.id },
                contentType = { "photo" }
            ) { item ->

                val imageRequest = remember(item.previewUrl) {
                    ImageRequest.Builder(context)
                        .data(item.previewUrl)
                        .size(cellSizePx, cellSizePx)
                        .crossfade(false)
                        .allowHardware(true)
                        .diskCacheKey("${item.previewUrl}_${cellSizePx}")
                        .memoryCacheKey("${item.previewUrl}_${cellSizePx}")
                        .build()
                }

                DivoAsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickableWithoutRipple { onPhotoClicked(item.photoUrl) },
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                )
            }

            if (isFirstLoading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    MediaLoadingContent()
                }
            }

            if (isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LottieProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }

            if (!isOwnProfile && similarItems.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SimilarProfilesRow(
                        similarItems = similarItems,
                        onClicked = onSimilarClicked
                    )
                }
            }
        }

        if (portfolioItems.isNotEmpty() && isOwnProfile) {
            PortfolioAddButton(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                isUploading = isUploading,
                onMediaSelected = onImageSelected
            )
        }
    }
}