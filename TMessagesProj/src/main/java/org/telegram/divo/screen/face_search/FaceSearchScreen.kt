package org.telegram.divo.screen.face_search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.rememberCameraCapture
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.common.utils.ImageCacheHelper
import org.telegram.divo.components.PhotoSourceBottomSheet
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.SearchImageAction
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.face_search.components.FaceDetectionStatus
import org.telegram.divo.screen.face_search.components.FaceOverlayImage
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun FaceSearchScreen(
    uri: String,
    onNavigateSimilarProfiles: (String, Float?, Float?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: FaceSearchViewModel = viewModel(
        factory = FaceSearchViewModel.factory(uri, context)
    )

    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    BackHandler {
        viewModel.setIntent(Intent.OnBackClicked)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
                is Effect.NavigateToSimilarProfiles -> onNavigateSimilarProfiles(it.uri, it.fx, it.fy)
                Effect.NavigateToSearch -> onNavigateToSearch()
            }
        }
    }

    FaceSearchContent(
        uiState = state,
        snackbarHostState = snackbarState,
        onIntent = { viewModel.setIntent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FaceSearchContent(
    uiState: State,
    snackbarHostState: AppSnackbarHostState,
    onIntent: (Intent) -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.FaceSearch).uppercase(),
                        style = AppTheme.typography.appBar,
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = { onIntent(Intent.OnBackClicked) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarHostState,
                bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 86.dp
            )
        }
    ) { paddingValues ->
        var showBottomSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val camera = rememberCameraCapture { uri ->
            onIntent(Intent.OnChangePhoto(uri))
        }

        val openGallery = rememberGalleryLauncher { uri ->
            onIntent(Intent.OnChangePhoto(uri))
        }

        if (showBottomSheet) {
            PhotoSourceBottomSheet(
                value = uiState.query,
                sheetState = sheetState,
                searchResults = uiState.searchResults,
                isLoading = uiState.isLoading,
                isLoadingMore = uiState.isLoadingMore,
                hasMore = uiState.hasMore,
                onValueChanged = { onIntent(Intent.OnQueryChanged(it)) },
                onClicked = {
                    scope.launch {
                        onIntent(Intent.OnChangePhoto(ImageCacheHelper.getLocalUri(context, it.photo)))
                        showBottomSheet = false
                    }
                },
                onLoadMore = { onIntent(Intent.OnLoadMore) },
                onDismiss = {
                    onIntent(Intent.OnQueryChanged(""))
                    showBottomSheet = false
                },
                onActionSelected = { action ->
                    showBottomSheet = false
                    when (action) {
                        SearchImageAction.CAMERA -> camera.launch()
                        SearchImageAction.GALLERY -> openGallery()
                    }
                }
            )
        }

        if (uiState.isSearching) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FaceOverlayImage(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    imageUri = uiState.imageUri.toString(),
                    detectionResult = uiState.detectionResult,
                    isSearching = true
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.AnalysingFacialFeatures),
                    fontSize = 20.sp,
                    style = AppTheme.typography.helveticaNeueLtCom,
                    color = AppTheme.colors.textPrimary
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(48.dp))

                    FaceOverlayImage(
                        imageUri = uiState.imageUri.toString(),
                        detectionResult = uiState.detectionResult,
                        modifier = Modifier.fillMaxWidth(),
                        selectedFaceIndex = uiState.selectedFaceIndex,
                        onFaceClick = { onIntent(Intent.OnFaceSelected(it)) }
                    )

                    FaceDetectionStatus(uiState.detectionResult)
                    Spacer(Modifier.height(16.dp))

                    UIButtonNew(
                        text = stringResource(R.string.ChangePhoto),
                        height = 40.dp,
                        textStyle = AppTheme.typography.textButton.copy(
                            fontSize = 16.sp
                        ),
                        paddingTop = 0.dp,
                        background = AppTheme.colors.textPrimary,
                        onClick = { showBottomSheet = true }
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UIButtonNew(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.detectionResult != FaceDetectionResult.Loading && uiState.detectionResult != FaceDetectionResult.NoFace && uiState.selectedFaceIndex != null,
                        text = stringResource(R.string.FindSimilarProfiles),
                        onClick = { onIntent(Intent.OnFindClicked) }
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.SimilarityScore),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 12.sp,
                        color = AppTheme.colors.textPrimary.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}