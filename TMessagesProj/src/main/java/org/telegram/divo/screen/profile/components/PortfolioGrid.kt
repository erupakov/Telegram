package org.telegram.divo.screen.profile.components

import android.net.Uri
import android.util.Log
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
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

    LaunchedEffect(gridState) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo
                .lastOrNull()?.index ?: return@snapshotFlow false

            val footerCount = 1
            val contentItemsCount = layoutInfo.totalItemsCount - footerCount

            lastVisible >= contentItemsCount - 3
        }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                if (currentHasMore && !currentIsLoadingMore) onLoadMore()
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = portfolioItems,
            key = { it.id }
        ) { item ->
            DivoAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple { (onPhotoClicked(item.photoUrl)) }
                    .aspectRatio(1f),
                url = item.previewUrl
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