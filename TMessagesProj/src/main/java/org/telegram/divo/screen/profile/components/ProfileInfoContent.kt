package org.telegram.divo.screen.profile.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.telegram.divo.components.TabConfig
import org.telegram.divo.entity.Agency
import org.telegram.divo.screen.profile.PhysicalParams
import kotlin.math.absoluteValue
import kotlin.math.floor

@Composable
fun ProfileInfoTabs(
    pagerInfoState: PagerState,
    destinationInfoTabs: List<TabConfig>,
) {
    TabContainer(
        pagerState = pagerInfoState,
        destinations = destinationInfoTabs,
    )
}

@Composable
fun ProfileInfoPager(
    isModel: Boolean,
    pagerInfoState: PagerState,
    bio: String,
    physicalParams: PhysicalParams,
    agency: Agency?,
    isOwnProfile: Boolean,
    onWorkHistoryClicked: () -> Unit,
    onAppearanceClicked: () -> Unit,
    onEditClicked: (Int) ->Unit,
) {
    if (isModel) {
        Spacer(Modifier.height(10.dp))

        val pageHeights = remember { mutableStateMapOf<Int, Int>() }
        val density = LocalDensity.current

        val currentPagerHeight by remember {
            derivedStateOf {
                val exactPage = pagerInfoState.currentPage + pagerInfoState.currentPageOffsetFraction
                val lowerPage = floor(exactPage).toInt()
                val upperPage = lowerPage + 1
                val fraction = exactPage - lowerPage

                val lowerHeight = pageHeights[lowerPage]
                val upperHeight = pageHeights[upperPage]

                val h1 = lowerHeight ?: upperHeight ?: 0
                val h2 = upperHeight ?: lowerHeight ?: 0

                val interpolatedHeightPx = h1 + (h2 - h1) * fraction
                with(density) { interpolatedHeightPx.toDp() }
            }
        }

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (currentPagerHeight > 0.dp) Modifier.height(currentPagerHeight)
                    else Modifier.wrapContentHeight()
                )
                .clipToBounds()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
            state = pagerInfoState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 16.dp,
            verticalAlignment = Alignment.Top
        ) { page ->
            val pageOffset = (pagerInfoState.currentPage - page) + pagerInfoState.currentPageOffsetFraction

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .graphicsLayer {
                        this.alpha = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
                        this.translationY = pageOffset * 24f
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Top, unbounded = true)
                        .onGloballyPositioned { coordinates ->
                            val height = coordinates.size.height
                            if (pageHeights[page] != height) {
                                pageHeights[page] = height
                            }
                        }
                ) {
                    when (page) {
                        0 -> BiographyContent(isOwnProfile = isOwnProfile, bio = bio, onEditClick = { onEditClicked(0) })
                        1 -> AppearanceContent(isOwnProfile = isOwnProfile, params = physicalParams, onEditClick = { onEditClicked(1) })
                        else -> AgencyInfoSection(isOwnProfile = isOwnProfile, agency = agency, onClicked = onWorkHistoryClicked, onEditClick = { onEditClicked(2) })
                    }
                }
            }
        }
    } else {
        Spacer(Modifier.height(20.dp))
        AgencyDescriptionSection(
            text = agency?.description.orEmpty(),
            isOwnProfile = isOwnProfile,
            onEditClick = { onEditClicked(0) }
        )
    }
}