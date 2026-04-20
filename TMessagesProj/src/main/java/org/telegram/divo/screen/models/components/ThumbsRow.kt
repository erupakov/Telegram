package org.telegram.divo.screen.models.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.FeedItem

@Composable
fun ThumbsRow(
    feed: FeedItem,
    onPhotoClicked: (Int) -> Unit
) {
    val context = LocalContext.current
    val images = remember(feed.files) { feed.files.drop(1) }

    LaunchedEffect(images) {
        images.forEach { file ->
            val request = ImageRequest.Builder(context)
                .data(file.url)
                .size(Size(126.dp.value.toInt(), 136.dp.value.toInt()))
                .build()
            context.imageLoader.enqueue(request)
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        itemsIndexed(items = images, key = { _, it -> it.uuid }) { index, thumb ->
            DivoAsyncImage(
                modifier = Modifier
                    .size(width = 100.dp, height = 100.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickableWithoutRipple { onPhotoClicked(index) },
                model = thumb.url,
                placeholderColor = Color.Transparent,
                loadingContent = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shimmer(
                                shimmerColor = Color.Black.copy(0.2f),
                                highlightColor = Color.White.copy(0.1f)
                            ),
                    )
                }
            )
        }
    }
}