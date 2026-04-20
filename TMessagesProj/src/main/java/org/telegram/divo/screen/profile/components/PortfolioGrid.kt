package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.AgencyModel
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
    val currentIsLoadingMore by rememberUpdatedState(isLoadingMore)

    val currentItems by rememberUpdatedState(portfolioItems)

    LaunchedEffect(gridState) {
        snapshotFlow {
            val info = gridState.layoutInfo
            val lastVisible = info.visibleItemsInfo
                .lastOrNull { it.index < currentItems.size }
                ?.index ?: 0
            Triple(lastVisible, currentItems.size, info.totalItemsCount)
        }.collect { (lastVisible, itemCount, _) ->
            if (itemCount > 0
                && lastVisible >= itemCount - 4
                && currentHasMore
                && !currentIsLoadingMore
            ) {
                onLoadMore()
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars
                .asPaddingValues()
                .calculateBottomPadding() + 16.dp
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

            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickableWithoutRipple { onPhotoClicked(item.photoUrl) },
                model = imageRequest,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.White),
            )
        }

        if (isFirstLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                MediaLoadingContent()
            }
        }

        if (isOwnProfile && !isFirstLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PortfolioAddButton(
                    isUploading = isUploading,
                    onMediaSelected = onImageSelected
                )
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
}