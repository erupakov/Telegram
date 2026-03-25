package org.telegram.divo.screen.edit_my_profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.uriToFile
import org.telegram.divo.components.BackButton
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.components.UIButton
import org.telegram.divo.screen.your_parameters.YourParametersScreen
import org.telegram.divo.style.AppTheme
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
    //messageStorage: MessagesStorage,
    onCloseScreen: () -> Unit = {},
) {
//    LaunchedEffect(messageStorage) {
//        viewModel.setMessageStorage(messageStorage)
//    }
    val context = LocalContext.current
    val parametersSavedText = stringResource(R.string.ParametersSaved)

    val uiState = viewModel.state.collectAsState().value
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { ProfileDestination.entries.size }
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    LifecycleResumeEffect(Unit) {
        viewModel.getData()
        onPauseOrDispose { }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                Effect.NavigateBack -> onCloseScreen()
                Effect.SaveSuccess -> {
                    Toast.makeText(context, parametersSavedText, Toast.LENGTH_SHORT).show()
                    val page = ProfileDestination.APPEARANCE.ordinal
                    selectedTab = page
                    pagerState.scrollToPage(page)
                }
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.backgroundDark,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = stringResource(R.string.Profile).uppercase(),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        color = AppTheme.colors.textColor,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    BackButton(
                        modifier = Modifier
                            .padding(start = 16.dp),
                        onBackClicked = onCloseScreen
                    )
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
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = Color.Transparent,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab, matchContentSize = true),
                        width = Dp.Unspecified,
                        color = Color.White
                    )
                },
                divider = { }
            ) {
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    ProfileDestination.entries.forEachIndexed { index, destination ->
                        Tab(
                            modifier = Modifier.width(100.dp).height(22.dp),
                            selected = pagerState.currentPage == index,
                            onClick = {
                                selectedTab = index
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                val tabText = when (destination) {
                                    ProfileDestination.BIOGRAPHY -> stringResource(R.string.TabBiography)
                                    ProfileDestination.APPEARANCE -> stringResource(R.string.TabAppearance)
                                }
                                Text(
                                    text = tabText,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    style = AppTheme.typography.helveticaNeueLtCom
                                )
                            },
                            selectedContentColor = Color.White,
                            unselectedContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    }
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
                    )
                } else {
                    YourParametersScreen(
                        showTitle = false,
                        onSaved = onCloseScreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreenView(
    uiState: EventListViewState,
    onIntent: (EditMyProfileIntent) -> Unit = {},
) {
    var fName by rememberSaveable { mutableStateOf(uiState.fName) }
    var lName by rememberSaveable { mutableStateOf(uiState.lName) }
    var bio by rememberSaveable { mutableStateOf(uiState.bio) }
    var selectedAvatarUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val openGallery = rememberGalleryLauncher { uri ->
        selectedAvatarUri = uri
    }
    val context = LocalContext.current

    // Sync local state when uiState changes (e.g., after loading or update)
    LaunchedEffect(uiState.fName, uiState.lName, uiState.bio) {
        fName = uiState.fName
        lName = uiState.lName
        bio = uiState.bio
    }

    if (uiState.isLoading) {
        EditMyProfileLoadingContent()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            key(uiState.avatarUrl, selectedAvatarUri) {
                Spacer(Modifier.height(16.dp))

                TelegramUserAvatarEditable(
                    avatarUrl = uiState.avatarUrl,
                    localUri = selectedAvatarUri,
                    onEditClick = { openGallery() }
                )
            }

            DivoTextField(
                modifier = Modifier.padding(top = 24.dp),
                value = fName,
                onValueChange = { fName = it },
                placeholder = stringResource(R.string.FirstNameEditProfileScreen),
                backgroundColor = Color.White.copy(0.05f),
                borderColor = Color.White.copy(0.40f),
                cornerRadius = 10.dp,
                placeholderColor = Color.White.copy(0.50f),
                cursorColor = Color.White,
                textStyle = AppTheme.typography.helveticaNeueRegular.copy(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 13.dp)
            )

            DivoTextField(
                modifier = Modifier.padding(top = 12.dp),
                value = lName,
                onValueChange = { lName = it },
                placeholder = stringResource(R.string.LastNameEditProfileScreen),
                backgroundColor = Color.White.copy(0.05f),
                borderColor = Color.White.copy(0.40f),
                cornerRadius = 10.dp,
                placeholderColor = Color.White.copy(0.50f),
                cursorColor = Color.White,
                textStyle = AppTheme.typography.helveticaNeueRegular.copy(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 13.dp)
            )

            DivoTextField(
                modifier = Modifier.padding(top = 12.dp),
                value = bio,
                onValueChange = { bio = it },
                placeholder = stringResource(R.string.BiographyPlaceholder),
                innerLabel = stringResource(R.string.Biography),
                innerLabelColor = Color.White.copy(0.50f),
                backgroundColor = Color.White.copy(0.05f),
                borderColor = Color.White.copy(0.40f),
                cornerRadius = 10.dp,
                placeholderColor = Color.White.copy(0.50f),
                cursorColor = Color.White,
                minLines = 5,
                maxLines = 5,
                textStyle = AppTheme.typography.helveticaNeueRegular.copy(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 13.dp)
            )

            Spacer(modifier = Modifier.size(16.dp))

            if (uiState.isSaved) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppTheme.colors.accentOrange),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                }
            } else {
                UIButton(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    text = stringResource(R.string.SaveEditProfileScreen),
                    onClick = {
                        onIntent(
                            EditMyProfileIntent.OnSaveClicked(
                                fName = fName,
                                lName = lName,
                                bio = bio,
                                file = selectedAvatarUri?.let { context.uriToFile(it) }
                            )
                        )
                    })
            }
        }
    }
}




