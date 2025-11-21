package org.telegram.divo.screen.models

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import org.telegram.divo.components.TextTitle
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont.HelveticaNeue
import org.telegram.messenger.R


@Composable
fun ModelsHomeScreen(
    viewModel: ModelsViewModel = androidx.lifecycle.viewmodel.compose.viewModel<ModelsViewModel>(),
    onSearch: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { state.models.size })

    LaunchedEffect(Unit) {
        viewModel.setIntent(ModelsViewIntent.LoadInitialData)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                backgroundColor = Color.White,
                title = {
                    TextTitle("Models".uppercase())
                },
                actions = {
                    IconButton(onClick = {}) {
                        androidx.compose.material3.Icon(
                            modifier = Modifier,
                            painter = painterResource(R.drawable.ic_ab_search),
                            contentDescription = "",
                            tint = AppTheme.colors.buttonColor
                        )
                    }
                },
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {

                Spacer(modifier = Modifier.height(8.dp))

                StoriesRow()

                TabsRow(
                    tabs = Tab.entries.map { it.displayName.uppercase() },
                    selectedIndex = state.selectedTab.ordinal,
                    onTabSelected = {
                        viewModel.setIntent(ModelsViewIntent.OnTabSelected(Tab.entries.get(it)))
                    },
                )

                VerticalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    reverseLayout = false,
                    userScrollEnabled = true,
                    flingBehavior = PagerDefaults.flingBehavior(state = pagerState)
                ) { page ->
                    ModelPage(
                        model = state.models[page],
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun StoriesRow(
    stories: List<Story> = ModelsViewState.preview.stories,
    viewModel: ModelsViewModel? = null
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(stories) { story ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (story.id == "0") {
                                viewModel?.setIntent(ModelsViewIntent.OnAddStoryClick)
                            } else {
                                viewModel?.setIntent(ModelsViewIntent.OnStoryClick(story.id))
                            }
                        }
                        .then(
                            if (story.id == "0") {
                                Modifier.background(Color(0xFFE7E7E8))
                            } else {
                                Modifier.border(
                                    width = 3.dp, brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF990000),
                                            Color(0xFF000000),
                                        ),
                                        tileMode = TileMode.Repeated
                                    ), shape = CircleShape
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFE7E7E8)),
                        shape = CircleShape,
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFE7E7E8)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (story.id == "0") {
                                Icon(
                                    painter = rememberVectorPainter(Icons.Default.Add),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color(0xFFE7E7E8))
                                )
                            } else {
                                Image(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(Color(0xFFE7E7E8)),
                                    painter = rememberAsyncImagePainter(story.imageUrl),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (story.id == "0") "Add Story" else story.userName,
                    color = if (story.id == "0") Color(0xFFB0B4BA) else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TabsRow(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF000000),
    inactiveColor: Color = Color(0xFFBEBEBE)
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val style = AppTheme.typography.textButtonSmall.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
    )

    val tabWidths = remember(tabs) {
        tabs.map {
            with(density) {
                textMeasurer.measure(it, style).size.width.toDp()
            }
        }
    }

    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        backgroundColor = Color.White,
        contentColor = activeColor,
        indicator = { tabPositions ->
            val targetIndicatorWidth = tabWidths[selectedIndex]
            val animatedIndicatorWidth by animateDpAsState(
                targetValue = targetIndicatorWidth,
                label = "indicatorWidth"
            )

            val currentTabPosition = tabPositions[selectedIndex]
            val targetIndicatorOffset =
                currentTabPosition.left + (currentTabPosition.width - targetIndicatorWidth) / 2
            val animatedIndicatorOffset by animateDpAsState(
                targetValue = targetIndicatorOffset,
                label = "indicatorOffset"
            )

            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.BottomStart)
                    .offset(x = animatedIndicatorOffset)
                    .width(animatedIndicatorWidth)
                    .height(2.dp)
                    .background(color = activeColor)
            )
        },
        divider = {
            TabRowDefaults.Divider(
                thickness = 1.dp,
                color = Color(0x11000000)
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        fontFamily = HelveticaNeue,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                selectedContentColor = activeColor,
                unselectedContentColor = inactiveColor
            )
        }
    }
}

@Composable
private fun ModelPage(
    model: Model,
    viewModel: ModelsViewModel,
) {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopEnd
    ) {
        Image(
            painter = rememberAsyncImagePainter(model.imageUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )

        Column(
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            model.emotions.forEach { emotion ->
                ReactionPill(
                    emotion = emotion,
                    onClick = {
                        viewModel.setIntent(ModelsViewIntent.OnEmotionClick(model.id, emotion))
                    }
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f), // bottom strong
                            Color.Transparent                 // top invisible
                        ),
                        startY = Float.POSITIVE_INFINITY, // start bottom
                        endY = 0f                          // end top
                    )
                )
                .padding(vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = .9f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model.avatarUrl),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        model.name.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.ExtraBold),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))
                    RoleChip(model.roleLabel)
                }
                Spacer(Modifier.width(10.dp))
            }

            Row(
                Modifier
                    .padding(top = 24.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DMButton(onClick = {
                    viewModel.setIntent(ModelsViewIntent.OnSendDmClick(model.id))
                })

                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = .15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_share_model),
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_divo_save_model),
                        contentDescription = "Bookmark",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                viewModel.setIntent(ModelsViewIntent.OnBookmarkClick(model.id))
                            }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            ThumbsRow(model, viewModel)
        }
    }
}

@Composable
private fun ReactionPill(
    emotion: Emotions,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (emotion.selected) {
        Color(0xFFFFFFFF)
    } else {
        Color(0x3DFFFFFF)
    }

    Card(
        modifier = Modifier.size(height = 30.dp, width = 50.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
        ),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            val textColor = if (emotion.selected) {
                Color(0xFF222222)
            } else {
                Color(0xFFFFFFFF)
            }

            Text(emotion.emoji, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(Modifier.weight(1f))
            Text("${emotion.count}", color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RoleChip(text: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFBB7148))
            .border(1.dp, Color.White.copy(alpha = .65f), RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            painter = painterResource(R.drawable.ic_divo_model_icon),
            modifier = Modifier.size(12.dp),
            tint = Color.White,
            contentDescription = null
        )
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White)
    }
}

@Composable
private fun DMButton(onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .15f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_divo_model_send_message),
            null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text("Send DM", color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ThumbsRow(
    model: Model,
    viewModel: ModelsViewModel,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        items(model.photos) { thumb ->
            Image(
                painter = rememberAsyncImagePainter(thumb),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 126.dp, height = 136.dp)
                    .clickable {
                        viewModel.setIntent(ModelsViewIntent.OnPhotoClick(model.id, thumb))
                    }
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF121922)
@Composable
private fun PreviewModelsHome() {
    ModelsHomeScreen()
}
