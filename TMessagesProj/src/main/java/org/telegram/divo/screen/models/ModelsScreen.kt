package org.telegram.divo.screen.models

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.flow.distinctUntilChanged
import org.telegram.divo.common.LaunchedEffectOnce
import org.telegram.divo.common.LockScreenOrientation
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.rememberIsOnline
import org.telegram.divo.screen.gallery.GalleryItem
import org.telegram.divo.screen.models.components.AnimatedCollapsingTopBar
import org.telegram.divo.screen.models.components.AnimatedLargeStoriesOverlay
import org.telegram.divo.screen.models.components.ModelPage
import org.telegram.divo.screen.models.components.TabsRow
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModelsHomeScreen(
    viewModel: ModelsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ModelsViewModel>(),
    onSearch: () -> Unit = {},
    onClick: (Int) -> Unit = {},
    onPhotoClicked: (List<GalleryItem>, Int) -> Unit = { _, _ -> },
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val currentRestModels = state.feedItems

    val listState = rememberLazyListState()

    val bottomInset = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val cardHeight = remember(bottomInset, statusBarHeight) {
        screenHeight - statusBarHeight - bottomInset - 284.dp
    }
    var titleWidthPx by remember { mutableFloatStateOf(0f) }

    val isOnline = rememberIsOnline()
    if (!isOnline) {
        Log.d("MyTag", "Ожидание сети...")
    } else {
        Log.d("MyTag", "Связь установлена!")
    }

    LockScreenOrientation()

    LaunchedEffectOnce {
        viewModel.setIntent(ModelsViewIntent.LoadInitialData)
    }

    LaunchedEffect(state.error) {
        if (!state.error.isNullOrBlank()) {
            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
        }
    }


    val density = LocalDensity.current
    val maxScrollOffsetPx = remember { with(density) { 104.dp.toPx() } }

    val collapseFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val offset = listState.firstVisibleItemScrollOffset.toFloat()
                (offset / maxScrollOffsetPx).coerceIn(0f, 1f)
            }
        }
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            if (listState.firstVisibleItemIndex == 0) {
                val currentOffset = listState.firstVisibleItemScrollOffset
                if (currentOffset > 0 && currentOffset < maxScrollOffsetPx) {
                    val triggerPoint = maxScrollOffsetPx / 2
                    if (currentOffset > triggerPoint) {
                        listState.animateScrollToItem(1)
                    } else {
                        listState.animateScrollToItem(0)
                    }
                }
            }
        }
    }

    val storiesForAnimation = ModelsViewState.preview.stories

    LaunchedEffect(
        listState,
        currentRestModels.size,
        state.feedHasMore,
        state.isLoadingAllUsers,
        state.isLoadingMoreFeed,
        state.selectedTab
    ) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (currentRestModels.isNotEmpty() &&
                    lastVisibleIndex >= currentRestModels.size - 3 &&
                    state.feedHasMore &&
                    !state.isLoadingAllUsers &&
                    !state.isLoadingMoreFeed
                ) {
                    viewModel.setIntent(ModelsViewIntent.LoadMoreAllUsers)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppTheme.colors.backgroundNew,
            contentColor = AppTheme.colors.backgroundNew,
            topBar = {
                AnimatedCollapsingTopBar(
                    collapseFraction = collapseFraction,
                    stories = storiesForAnimation,
                    onSearch = onSearch,
                    onTitleMeasured = { measuredWidth -> titleWidthPx = measuredWidth }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
                    .background(AppTheme.colors.backgroundNew),
                contentPadding = PaddingValues(bottom = bottomInset + 66.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(104.dp))
                }

                stickyHeader {
                    TabsRow(
                        tabs = Tab.entries.map { it.displayName.uppercase() },
                        selectedIndex = state.selectedTab.ordinal,
                        onTabSelected = { viewModel.setIntent(ModelsViewIntent.OnTabSelected(Tab.entries[it])) }
                    )
                }

            when {
                currentRestModels.isEmpty() && state.isLoadingAllUsers -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .height(cardHeight)
                                    .padding(top = 18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                LottieProgressIndicator(modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.LoadingModelsList).uppercase(),
                                    style = AppTheme.typography.helveticaNeueLtCom,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                else -> {
                    itemsIndexed(
                        items = currentRestModels,
                        key = { i, d -> d.id }
                    ) { i, feedItem ->
                        ModelPage(
                            feed = feedItem,
                            cardHeight = cardHeight,
                            models = state.models,
                            onClick = onClick,
                            onPhotoClicked = onPhotoClicked
                        )
                    }
                }
            }
            }
        }

        AnimatedLargeStoriesOverlay(
            collapseFraction = collapseFraction,
            stories = storiesForAnimation,
            titleWidthPx = titleWidthPx
        )
    }
}

//@Composable
//private fun TelegramPhotoCover(
//    photo: TLRPC.Photo,
//    modifier: Modifier = Modifier
//) {
//    AndroidView(
//        modifier = modifier,
//        factory = { context ->
//            BackupImageView(context).apply {
//            }
//        },
//        update = { view ->
//            val fullSize = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 640)
//                ?: return@AndroidView
//            if (photo.dc_id != 0) {
//                fullSize.location.dc_id = photo.dc_id
//                fullSize.location.file_reference = photo.file_reference
//            }
//            val fullLoc = ImageLocation.getForPhoto(fullSize, photo) ?: return@AndroidView
//            var thumbSize: TLRPC.PhotoSize? = FileLoader.getClosestPhotoSizeWithSize(photo.sizes, 50)
//            for (i in 0 until photo.sizes.size) {
//                val ps = photo.sizes[i]
//                if (ps is TLRPC.TL_photoStrippedSize) {
//                    thumbSize = ps
//                    break
//                }
//            }
//            val thumbLoc = thumbSize?.let { ImageLocation.getForPhoto(it, photo) }
//            val thumbFilter = if (thumbSize is TLRPC.TL_photoStrippedSize) "b" else null
//            view.setImage(fullLoc, "640_640", thumbLoc, thumbFilter, null, 0, 1, photo)
//        }
//    )
//}

@Preview(showBackground = true, backgroundColor = 0xFF121922)
@Composable
private fun PreviewModelsHome() {
    ModelsHomeScreen()
}
