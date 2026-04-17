package org.telegram.divo.screen.models.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ModelPage(
    modifier: Modifier = Modifier,
    feed: FeedItem,
    cardHeight: Dp,
    onClick: (Int) -> Unit,
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit,
    onLikeClick: (Int, Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickableWithoutRipple { onClick(feed.id) }
    ) {

        CardBlurredBackground(
            imageUrl = feed.files.first().url,
            isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = feed.user.fullName,
                        color = AppTheme.colors.textColor,
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 20.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DivoChip(
                            text = feed.user.roleLabel,
                            resId = if (feed.user.role.isModel()) R.drawable.ic_divo_person_heart else R.drawable.ic_divo_agency,
                            background = Color(0xFF2262D8),
                            textColor = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Не хватает полей в ответе сервера",
                            style = AppTheme.typography.bodyMedium,
                            color = Color.Red,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column {
                    RoundedGlassContainer(
                        modifier = Modifier.width(65.dp),
                        height = 30.dp,
                        background = if (feed.isLiked) AppTheme.colors.onBackground else AppTheme.colors.onBackground.copy(alpha = 0.3f),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickableWithoutRipple { onLikeClick(feed.feedId, feed.isLiked) }
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = if (feed.isLiked) painterResource(R.drawable.ic_divo_favorite_selected) else painterResource(R.drawable.ic_divo_favorite),
                                contentDescription = null,
                                tint = if (feed.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground,
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                modifier = Modifier.offset(y = 1.dp),
                                text = feed.likesCount.toShortString(),
                                style = AppTheme.typography.helveticaNeueRegular,
                                color = if (feed.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    RoundedGlassContainer(
                        modifier = Modifier.width(65.dp),
                        height = 30.dp,
                        background = Color.Red,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_divo_visibility),
                            contentDescription = null,
                            tint = AppTheme.colors.onBackground
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            modifier = Modifier.offset(y = 1.dp),
                            text = "----",
                            style = AppTheme.typography.helveticaNeueRegular,
                            color = AppTheme.colors.onBackground,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    RoundedGlassContainer(
                        modifier = Modifier.width(65.dp),
                        height = 30.dp,
                        background = Color.Red,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_divo_bookmark_glass),
                            contentDescription = null,
                            tint = AppTheme.colors.onBackground
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            modifier = Modifier.offset(y = 1.dp),
                            text = "----",
                            style = AppTheme.typography.helveticaNeueRegular,
                            color = AppTheme.colors.onBackground,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 16.dp)
            ) {
                ThumbsRow(
                    feed = feed,
                    onPhotoClicked = {
                        val items = feed.files.map { GalleryItem(it.order, it.url, false) }
                        onPhotoClicked(items, it)
                    }
                )
            }
        }
    }
}

@Composable
private fun CardBlurredBackground(
    imageUrl: String,
    isBlurSupported: Boolean
) {
    DivoAsyncImage(
        modifier = Modifier.fillMaxSize(),
        model = imageUrl,
        contentScale = ContentScale.Crop
    )

    if (isBlurSupported) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()

                    val h = size.height
                    val topBlurHeight = 94.dp.toPx()    // Высота размытия сверху
                    val bottomBlurHeight = 160.dp.toPx() // Высота размытия снизу

                    drawRect(
                        brush = Brush.verticalGradient(
                            // Маска для верхней части (сглаженная)
                            0.0f to Color.Black,
                            (topBlurHeight / h) * 0.5f to Color.Black,
                            (topBlurHeight / h) * 0.8f to Color.Black.copy(alpha = 0.5f), // Промежуточная точка
                            (topBlurHeight / h) to Color.Transparent,

                            // Маска для нижней части (сглаженная)
                            // Начинаем проявлять блюр гораздо раньше
                            (1f - bottomBlurHeight / h) to Color.Transparent,
                            (1f - (bottomBlurHeight / h) * 0.95f) to Color.Black.copy(alpha = 0.3f),
                            (1f - (bottomBlurHeight / h) * 0.9f) to Color.Black.copy(alpha = 0.6f),
                            (1f - (bottomBlurHeight / h) * 0.80f) to Color.Black.copy(alpha = 0.9f),
                            (1f - (bottomBlurHeight / h) * 0.2f) to Color.Black,
                            1.0f to Color.Black
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            DivoAsyncImage(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(35.dp),
                model = imageUrl,
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.2f), // Затемнение самого верха
                            0.3f to Color.Transparent,
                            0.7f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.3f)  // Затемнение самого низа
                        )
                    )
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.5f),
                        0.2f to Color.Transparent,
                        0.7f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.7f)
                    )
                )
        )
    }
}