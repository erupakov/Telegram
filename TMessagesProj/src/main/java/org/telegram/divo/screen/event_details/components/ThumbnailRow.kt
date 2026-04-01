package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.EventFile
import org.telegram.divo.screen.gallery.GalleryItem

@Composable
fun ThumbnailRow(
    files: List<EventFile>,
    onClicked: (List<GalleryItem>, Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(
            items = files,
            key = { _, it -> it.order }
        ) { index, thumb ->
            DivoAsyncImage(
                modifier = Modifier
                    .width(134.dp)
                    .height(136.dp)
                    .clickableWithoutRipple {
                        val items = files.map { GalleryItem(it.order, it.fullUrl, false) }
                        onClicked(items, index)
                    },
                model = thumb.fullUrl
            )
        }
    }
}