package org.telegram.divo.screen.edit_my_profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.components.TextTitle
import org.telegram.divo.components.UIButton
import org.telegram.divo.components.UiDarkTextField
import org.telegram.divo.screen.event_list.EventListViewModel
import org.telegram.divo.screen.profile.Destination
import org.telegram.divo.screen.your_parameters.YourParametersScreen
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.R


enum class ProfileDestination(
    val route: String,
    val contentDescription: String
) {
    BIOGRAPHY("BIOGRAPHY", "BIOGRAPHY"),
    APPEARANCE("APPEARANCE", "APPEARANCE"),
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreen(
    viewModel: EditMyProfileViewModel = viewModel(),
    messageStorage: MessagesStorage,
    onCloseScreen: () -> Unit = {},
    onEditImageClicked: () -> Unit = {}
) {
    LaunchedEffect(messageStorage) {
        viewModel.setMessageStorage(messageStorage)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                EditMyProfileViewModel.Effect.NavigateBack -> onCloseScreen()
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ProfileDestination.entries.size }
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.padding(top = 36.dp),
        containerColor = AppTheme.colors.backgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Profile", color = AppTheme.colors.textColor) },
                navigationIcon = {
                    IconButton(onClick = onCloseScreen) {
                        Icon(
                            painter = painterResource(R.drawable.ic_divo_back),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = AppTheme.colors.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundDark
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier,
                containerColor = Color.Transparent,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                        width = Dp.Unspecified,
                        color = Color.White
                    )
                }

            ) {
                ProfileDestination.entries.forEachIndexed { index, destination ->
                    Tab(
                        modifier = Modifier.width(100.dp),
                        selected = pagerState.currentPage == index,
                        onClick = {
                            selectedTab = index
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(text = destination.name, color = Color.White) },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                userScrollEnabled = false
            ) { page ->
                if (page == 0) {
                    EditMyProfileScreenView(
                        uiState,
                        onIntent = { viewModel.setIntent(it) },
                        onCloseScreen = onCloseScreen,
                        onEditImageClicked = onEditImageClicked
                    )
                } else {
                    YourParametersScreen(
                        showTitle = false,
                        onSaved = {

                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreenView(
    uiState: EditMyProfileViewModel.EventListViewState,
    onIntent: (EditMyProfileViewModel.EditMyProfileIntent) -> Unit = {},
    onCloseScreen: () -> Unit = {},
    onEditImageClicked: () -> Unit = {}
) {
    var fName by remember { mutableStateOf(uiState.fName) }
    var lName by remember { mutableStateOf(uiState.lName) }
    var bio by remember { mutableStateOf(uiState.bio) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        TelegramUserAvatarEditable(
            user = uiState.userFull.user,
            modifier = Modifier,
            onEditClick = { onEditImageClicked() }
        )

        UiDarkTextField(
            value = fName,
            onValueChange = {
                fName = it
            },
            label = "First Name",
            modifier = Modifier.padding(top = 8.dp)
        )
        UiDarkTextField(
            label = "Last Name",
            value = lName,
            onValueChange = {
                lName = it
            },
            modifier = Modifier.padding(top = 8.dp)

        )
        UiDarkTextField(
            label = "Biography",
            value = bio,
            onValueChange = {
                bio = it
            },
            minLines = 5,
            singleLine = false,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.size(16.dp))

        UIButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onIntent(
                    EditMyProfileViewModel.EditMyProfileIntent.OnSaveClicked(
                        fName = fName,
                        lName = lName,
                        bio = bio
                    )
                )
            })
    }
}




