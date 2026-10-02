package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.arch.OffsetPaginator
import org.telegram.divo.components.media.DivoAsyncImage
import org.telegram.divo.components.media.LottieProgressIndicator
import org.telegram.divo.entity.SavedProfile
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedProfilesBottomSheet(
    paginator: OffsetPaginator<SavedProfile>,
    onDismiss: () -> Unit,
    onProfileClick: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val paginatorState by paginator.state.collectAsState()
    val scope = rememberCoroutineScope()
    val heightFraction by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (paginatorState.items.isEmpty()) 0.5f else 0.95f,
        label = "heightFraction"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .width(56.dp)
                    .height(5.dp)
                    .background(Color(0xA0A0A099), RoundedCornerShape(100.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(heightFraction),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.SavedProfilesLabel),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (paginatorState.isLoading && paginatorState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    LottieProgressIndicator(Modifier.size(32.dp))
                }
            } else if (!paginatorState.isLoading && paginatorState.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = stringResource(R.string.SavedProfilesEmpty),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 24.sp,
                        color = AppTheme.colors.textPrimary
                    )
                }
            } else {
                val lazyListState = rememberLazyListState()

                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleIndex = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val total = lazyListState.layoutInfo.totalItemsCount
                        lastVisibleIndex >= total - 3 && total > 0
                    }
                }

                LaunchedEffect(shouldLoadMore) {
                    if (shouldLoadMore && paginatorState.hasMore && !paginatorState.isLoading && !paginatorState.isLoadingMore) {
                        scope.launch {
                            paginator.loadMore()
                        }
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f)
                ) {
                    items(paginatorState.items, key = { it.id }) { profile ->
                        SavedProfileItem(
                            profile = profile,
                            onClick = {
                                onProfileClick(profile.id)
                                onDismiss()
                            }
                        )
                    }

                    if (paginatorState.isLoadingMore) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                LottieProgressIndicator(Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedProfileItem(
    profile: SavedProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DivoAsyncImage(
            model = profile.avatarUrl,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = profile.fullName,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = profile.roleLabel.ifEmpty { profile.role },
                style = AppTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = AppTheme.colors.textHintColor
            )
        }
    }
}
