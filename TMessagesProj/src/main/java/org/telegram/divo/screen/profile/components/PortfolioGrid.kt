package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.entity.UserGalleryItem

@Composable
fun PortfolioGrid(
    modifier: Modifier = Modifier,
    portfolioItems: List<UserGalleryItem>,
    similarItems: List<AgencyModel>,
    isOwnProfile: Boolean,
    isUploading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onPhotoClicked: (String) -> Unit,
    onSimilarClicked: (Int) -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    val gridState = rememberLazyGridState()

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
    ) {
        items(
            items = portfolioItems,
            key = { it.id },
            contentType = { "photo" }
        ) { item ->
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple { (onPhotoClicked(item.photoUrl)) }
                    .aspectRatio(1f),
                model = item.previewUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(Color.White)
            )
        }

        if (isOwnProfile) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PortfolioAddButton(
                    isUploading = isUploading,
                    onImageSelected = onImageSelected
                )
            }
        }

        if (!isOwnProfile) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SimilarProfilesRow(
                    similarItems = similarItems,
                    onClicked = onSimilarClicked
                )
            }
        }
    }
}