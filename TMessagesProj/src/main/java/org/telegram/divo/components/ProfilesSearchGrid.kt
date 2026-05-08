package org.telegram.divo.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.DivoShareType
import org.telegram.divo.common.utils.DivoSharingHelper
import org.telegram.divo.common.utils.toCountryFlagEmoji
import org.telegram.divo.common.utils.toShortString
import org.telegram.divo.entity.SearchedProfile
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ProfilesSearchGrid(
    modifier: Modifier = Modifier,
    profiles: List<SearchedProfile>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onMarkClicked: (Int) -> Unit,
    onLikeClicked: (Int) -> Unit,
    onProfileClicked: (SearchedProfile) -> Unit,
    onLoadMore: () -> Unit,
    header: @Composable () -> Unit,
) {
    val lazyGridState = rememberLazyGridState()
    val bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 8.dp

    val shouldLoadMore by remember {
        derivedStateOf {
            val layoutInfo = lazyGridState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            val total = layoutInfo.totalItemsCount

            lastVisible >= total - 1 && hasMore && !isLoadingMore
        }
    }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { shouldLoadMore }
            .distinctUntilChanged()
            .filter { it }
            .collect { onLoadMore() }
    }

    if (isLoading) {
        EmptyContent()
    } else {
        LazyVerticalGrid(
            modifier = modifier,
            columns = GridCells.Fixed(2),
            state = lazyGridState,
            contentPadding = PaddingValues(top = 8.dp, bottom = bottomPadding),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                header()
            }

            items(
                items = profiles,
                key = { "${it.id}${it.index}" }
            ) {
                ProfileItem(
                    profile = it,
                    onMarkClicked = { onMarkClicked(it.id) },
                    onLikeClicked = { onLikeClicked(it.id) },
                    onProfileClicked = { onProfileClicked(it) },
                )
            }

            if (isLoadingMore) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileItem(
    profile: SearchedProfile,
    onMarkClicked: () -> Unit,
    onLikeClicked: () -> Unit,
    onProfileClicked: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hazeState = remember { HazeState() }
    var componentHeight by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(16.dp))
            .onSizeChanged { componentHeight = it.height.toFloat() }
            .clickableWithoutRipple { onProfileClicked() }
    ) {
        DivoAsyncImage(
            modifier = Modifier
                .matchParentSize()
                .hazeSource(state = hazeState),
            model = profile.photo
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .hazeEffect(
                    state = hazeState,
                    style = HazeStyle(
                        backgroundColor = Color.Black,
                        blurRadius = 30.dp,
                        tints = listOf(HazeTint(Color.Black.copy(alpha = 0.2f)))
                    )
                ) {
                    progressive = HazeProgressive.verticalGradient(
                        startY = componentHeight * 0.65f,
                        startIntensity = 0f,
                        endY = componentHeight * 0.9f,
                        endIntensity = 1f,
                        easing = LinearEasing
                    )
                }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(0.15f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (profile.similarity != null) {
                    Box(
                        modifier = Modifier
                            .height(22.dp)
                            .clip(CircleShape)
                            .border(1.dp, AppTheme.colors.accentOrange, CircleShape)
                            .background(AppTheme.colors.onBackground)
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.SimilarityFormat, profile.similarity),
                            color = AppTheme.colors.accentOrange,
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Spacer(Modifier)
                }

                RoundedGlassContainer(
                    space = 10.dp,
                    height = 22.dp,
                    background = if (profile.isMarked) AppTheme.colors.onBackground else AppTheme.colors.onBackground.copy(alpha = 0.2f),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .clickableWithoutRipple {
                                DivoSharingHelper.share(
                                    context = context,
                                    scope = scope,
                                    type = DivoShareType.PROFILE,
                                    id = profile.id,
                                    customMessage = "${profile.name} - ${profile.roleLabel}",
                                    imageUrl = profile.photo
                                )
                            },
                        painter = painterResource(R.drawable.ic_divo_share_model),
                        contentDescription = null,
                        tint = if (profile.isMarked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground
                    )
                    Icon(
                        modifier = Modifier
                            .size(16.dp)
                            .clickableWithoutRipple { onMarkClicked() },
                        painter = painterResource(if (profile.isMarked) R.drawable.ic_divo_bookmark_glass_selected else R.drawable.ic_divo_bookmark_glass),
                        contentDescription = null,
                        tint = if (profile.isMarked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Box(Modifier.fillMaxWidth()) {
                RoundedGlassContainer(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickableWithoutRipple { onLikeClicked() },
                    height = 22.dp,
                    space = 4.dp,
                    background = if (profile.isLiked) AppTheme.colors.onBackground else AppTheme.colors.onBackground.copy(alpha = 0.2f),
                    contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                    Icon(
                        modifier = Modifier.size(12.dp),
                        painter = painterResource(if (profile.isLiked) R.drawable.ic_divo_favorite_selected else R.drawable.ic_divo_favorite),
                        contentDescription = null,
                        tint = if (profile.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground
                    )
                    Text(
                        modifier = Modifier.offset(y = 0.5.dp),
                        text = profile.likes.toShortString(),
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = if (profile.isLiked) AppTheme.colors.textPrimary else AppTheme.colors.onBackground,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(10.dp)
        ) {
            DivoChip(
                text = profile.roleLabel,
                resId = R.drawable.ic_divo_person_heart
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = profile.name,
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                color = AppTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            val age = profile.age?.let { "${profile.age} ${context.getString(R.string.YearsOld)}" }
            val countryCode = profile.countryCode?.toCountryFlagEmoji()
            val county = profile.country
            val sep = if (age != null && countryCode != null) " · " else ""
            if (age != null || (countryCode != null && county != null)) {
                Text(
                    text = "${age.orEmpty()}$sep${countryCode.orEmpty()} ${county.orEmpty()}",
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                    color = AppTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(135.dp)
                    .height(19.dp)
                    .clip(CircleShape)
                    .shimmer()
            )

            Box(
                modifier = Modifier
                    .width(104.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
        }
        Spacer(Modifier.height(16.dp))
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(16.dp))
                        .shimmer()
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}