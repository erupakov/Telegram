package org.telegram.divo.screen.profile

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.items.PofileNameItem
import org.telegram.divo.items.ProfileBioItem
import org.telegram.divo.items.ProfileSocialItem
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.BackupImageView


@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val uiState = viewModel.state.collectAsState().value
    ProfileScreenParts(
        uiState = uiState,
        onEditClicked = {
            onEditClicked()
        },
        onEditLinksClicked = {
            onEditLinksClicked()
        },
        onNavigateBack = onNavigateBack
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreenParts(
    modifier: Modifier = Modifier,
    uiState: ProfileViewModel.ProfileViewState,
    onEditClicked: () -> Unit = {},
    onEditLinksClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val listState = rememberLazyListState()

    val titleVisible by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 40
        }
    }
    OnePhotoTopSharpBottomBlur(
        modifier = Modifier.padding(bottom = 200.dp),
        painter = painterResource(R.drawable.divo_profile_background_test)
    )
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 36.dp),
    ) {

        ToolBar(
            title = "BRITNEY SPORTLING",
            titleVisible = titleVisible,
            modifier = Modifier
                .background(Color.Transparent),
            onEditClicked = {
                onEditClicked()
            },
            onNavigateBack = {
                onNavigateBack()
            }
        )
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item {
                ProfileHeaderContent(
                    uiState,
                    onEditLinksClicked
                )
            }
            stickyHeader {
                TabContainer()
            }

            // 3) ProfileContent (твой список)
            items(40) { i ->
                Box(Modifier.background(color = Color.White)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .padding(horizontal = 16.dp, vertical = 10.dp)

                            .clip(MaterialTheme.shapes.medium)
                            .background(Color(0xFFF2F2F2)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text("Content item $i", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun OnePhotoTopSharpBottomBlur(
    painter: Painter,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 100.dp
) {
    Box(modifier) {

        // Слой 1: та же картинка — резкая
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Слой 2: та же картинка — blurred, но видна только снизу через градиент-маску
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(blurRadius)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height * 0.45f, // где начинается blur
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
        )
    }
}


@Composable
private fun ToolBar(
    modifier: Modifier = Modifier,
    title: String,
    titleVisible: Boolean = false,
    onEditClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                onNavigateBack()
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Row(modifier = Modifier.weight(1f)) {
                AnimatedVisibility(
                    titleVisible
                ) {
                    Text(title)
                }

            }

            IconButton(onClick = { onEditClicked() }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
            IconButton(onClick = { /* settings/edit */ }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Settings", tint = Color.White)
            }
        }
    }
}


@Composable
private fun ProfileHeaderContent(
    uiState: ProfileViewModel.ProfileViewState,
    onEditLinksClicked: () -> Unit,
) {
    Column {
        PofileNameItem(
            modifier = Modifier.padding(top = 150.dp),
            avatarUrl = "https://randomuser.me/api/portraits/women/46.jpg",
            firstName = uiState.userFull.user.first_name ?: "",
            lastName = uiState.userFull.user.last_name ?: "",
            roleLabel = "Model",
            uiState
        )
//        Row(modifier = Modifier.padding(top = 16.dp)) {
//            DMButton(
//                onClick = {
//
//                }
//            )
//        }
        ProfileBioItem(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            bio = uiState.userFull.about
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("My Links", color = Color.White)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = {

                }) {
                    Text("Edit Links".uppercase(), color = Color.White)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_instagram
                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_tiktok

                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_youtube

                )

                ProfileSocialItem(
                    modifier = Modifier.weight(1f),
                    iconResId = R.drawable.icon_divo_web
                )

            }
        }



        Spacer(modifier = Modifier.height(8.dp))
    }
}

enum class Destination(
    val route: String,
    @DrawableRes
    val iconResId: Int,
    val contentDescription: String
) {
    SONGS("songs", R.drawable.divo_profile_tab_1, "Songs"),
    ALBUM("album", R.drawable.divo_profile_tab_2, "Album"),
    PLAYLISTS("playlist", R.drawable.divo_profile_tab_3, "Playlist")
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun TabContainer() {
    Box(
        Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.blackAlpha12)
    ) {

        val startDestination = Destination.SONGS
        var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

        PrimaryTabRow(
            selectedTabIndex = selectedDestination, modifier = Modifier,
            containerColor = Color.Transparent,
            indicator = {
                Spacer(
                    Modifier
                        .width(56.dp)
                        .height(4.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(
                                topStart = 5.dp,
                                topEnd = 5.dp,
                                bottomStart = 0.dp,
                                bottomEnd = 0.dp
                            )
                        )
                )
            }
        ) {
            Destination.entries.forEachIndexed { index, destination ->
                Tab(
                    modifier = Modifier.width(100.dp),
                    selected = selectedDestination == index,
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(destination.iconResId),
                            modifier = Modifier.size(24.dp),
                            contentDescription = destination.contentDescription
                        )
                    },
                    onClick = {
                        selectedDestination = index
                    },
                    selectedContentColor = Color.White
                )
            }
        }


    }
}

@Composable
private fun ProfileContent(
    listState: LazyListState,
    topPadding: Dp,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = PaddingValues(top = topPadding)
    ) {
        items(40) {

        }
    }
}






