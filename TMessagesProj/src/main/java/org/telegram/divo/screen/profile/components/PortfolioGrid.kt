package org.telegram.divo.screen.profile.components

import android.net.Uri
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    onPhotoClicked: (String) -> Unit,
    onSimilarClicked: (Int) -> Unit,
    onImageSelected: (Uri) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
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