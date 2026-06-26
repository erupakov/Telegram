package org.telegram.divo.screen.models.components

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.telegram.divo.common.utils.toCountryFlagEmoji
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.components.DivoAvatar
import org.telegram.divo.components.DivoChip
import org.telegram.divo.components.RoundedGlassContainer
import org.telegram.divo.components.shimmer
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
    onPhotoClicked: (List<GalleryItem>, Int, Int) -> Unit,
    onLikeClick: (Int, Boolean) -> Unit,
    onBookmarkClick: (Int) -> Unit,
) {
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val totalImages = if (isBlurSupported) 2 else 1
    var readyCount by remember(feed.id) { mutableIntStateOf(0) }
    val allImagesReady = readyCount >= totalImages

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .clickableWithoutRipple { onClick(feed.user.id) }
    ) {

        val bgAlpha by animateFloatAsState(
            targetValue = if (allImagesReady) 1f else 0f,
            animationSpec = tween(durationMillis = 300),
            label = "bgAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgAlpha }
        ) {
            CardBlurredBackground(
                feed = feed,
                isBlurSupported = isBlurSupported,
                isModel = feed.user.role.isModel(),
                onMainImageReady = { readyCount++ },
                onBlurImageReady = { readyCount++ }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }

        AnimatedVisibility(
            visible = !allImagesReady,
            exit = fadeOut(animationSpec = tween(durationMillis = 300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmer()
                    .background(Color.LightGray.copy(alpha = 0.2f))
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 20.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DivoAvatar(
                        imageUrl = feed.user.photoUrl,
                        isOnline = feed.user.isOnline ?: false
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
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

                        feed.user.age?.let {
                            Text(
                                text = org.telegram.messenger.LocaleController.formatPluralString("Years", it),
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onBackground,
                            )
                        }
                        if (feed.user.age != null && feed.user.countryCode != null) {
                            Text(
                                text = " ·",
                                color = AppTheme.colors.onBackground
                            )
                        }
                        feed.user.countryCode?.let {
                            Text(
                                text = " ${it.toCountryFlagEmoji()}",
                            )
                        }
                        feed.user.countryName?.let {
                            Text(
                                text = " $it",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                }

                Column {
                    RoundedGlassContainer(
                        modifier = Modifier.width(67.dp),
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
                                text = feed.user.likesCount.toShortString(),
                                style = AppTheme.typography.helveticaNeueRegular,
                                color = if (feed.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                     RoundedGlassContainer(
                        modifier = Modifier.width(67.dp),
                        height = 30.dp,
                        background = AppTheme.colors.onBackground.copy(alpha = 0.3f),
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
                            text = feed.user.viewsCount.toShortString(),
                            style = AppTheme.typography.helveticaNeueRegular,
                            color = AppTheme.colors.onBackground,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    RoundedGlassContainer(
                        modifier = Modifier.width(67.dp),
                        height = 30.dp,
                        background = if (feed.isFollowed) AppTheme.colors.onBackground else AppTheme.colors.onBackground.copy(alpha = 0.3f),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickableWithoutRipple { onBookmarkClick(feed.user.id) }
                        ) {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = if (feed.isFollowed) painterResource(R.drawable.ic_divo_bookmark_glass_selected) else painterResource(R.drawable.ic_divo_bookmark_glass),
                                contentDescription = null,
                                tint = if (feed.isFollowed) AppTheme.colors.textPrimary else AppTheme.colors.onBackground
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                modifier = Modifier.offset(y = 1.dp),
                                text = feed.user.followersCount.toShortString(),
                                style = AppTheme.typography.helveticaNeueRegular,
                                color = if (feed.isFollowed) AppTheme.colors.textPrimary else AppTheme.colors.onBackground,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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
                        onPhotoClicked(items, it, feed.user.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun CardBlurredBackground(
    feed: FeedItem,
    isBlurSupported: Boolean,
    isModel: Boolean,
    onMainImageReady: () -> Unit = {},
    onBlurImageReady: () -> Unit = {},
) {
    var hasError by remember { mutableStateOf(false) }
    val imageUrl = feed.files.firstOrNull()?.url
    var isImageLoaded by remember { mutableStateOf(false) }

    if (imageUrl.isNullOrEmpty() || hasError) {
        Image(
            painter = painterResource(if (isModel) R.drawable.divo_models_placeholder else R.drawable.divo_agency_placeholder),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        LaunchedEffect(Unit) {
            onMainImageReady()
            if (isBlurSupported) {
                onBlurImageReady()
            }
        }
    } else {
        DivoAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = imageUrl,
            contentScale = ContentScale.Crop,
            onReady = onMainImageReady,
            onError = { hasError = true },
            loadingContent = { Box(Modifier.fillMaxSize()) }
        )

        if (isBlurSupported && feed.files.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()

                        val h = size.height
                        val topBlurHeight = 94.dp.toPx()
                        val bottomBlurHeight = 160.dp.toPx()

                        drawRect(
                            brush = Brush.verticalGradient(
                                0.0f to Color.Black,
                                (topBlurHeight / h) * 0.5f to Color.Black,
                                (topBlurHeight / h) * 0.8f to Color.Black.copy(alpha = 0.5f),
                                (topBlurHeight / h) to Color.Transparent,
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
                    contentScale = ContentScale.Crop,
                    onReady = onBlurImageReady,
                    loadingContent = { Box(Modifier.fillMaxSize()) }
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = 0.2f),
                                0.3f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.3f)
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
            LaunchedEffect(Unit) {
                if (isBlurSupported) {
                    onBlurImageReady()
                }
            }
        }
    }
}
