package org.telegram.divo.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.common.rememberCameraCapture
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.PhotoSourceBottomSheet
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.SearchImageAction
import org.telegram.divo.screen.search.components.SearchRow
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchScreen(
    viewModel: SearchViewModel = viewModel(),
    onBack: () -> Unit,
    onPhotoSelected: (String) -> Unit = {},
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
                is Effect.NavigateToFaceSearch -> onPhotoSelected(it.uri)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SearchContent(
            state = state,
            onIntent = { viewModel.setIntent(it) }
        )

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 56.dp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchContent(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val camera = rememberCameraCapture { uri ->
        onIntent(Intent.OnPhotoSelected(uri))
    }

    val openGallery = rememberGalleryLauncher { uri ->
        onIntent(Intent.OnPhotoSelected(uri))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        camera.rationaleDialog()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            SearchRow(
                value = state.query,
                onSearchFaceClicked = { showBottomSheet = true },
                onValueChanged = { onIntent(Intent.OnQueryChanged(it)) },
                onBack = { onIntent(Intent.OnBackClicked) }
            )
        }

        RoundedButton(
            modifier = Modifier
                .padding(bottom = 32.dp, end = 16.dp)
                .size(52.dp)
                .align(Alignment.BottomEnd),
            resId = R.drawable.ic_divo_ai,
            iconSize = 26.dp,
            paddingEnd = 0.dp,
            background = AppTheme.colors.onBackground,
            iconTint = AppTheme.colors.accentOrange,
            onClick = { },
        )

        if (showBottomSheet) {
            PhotoSourceBottomSheet(
                sheetState = sheetState,
                onDismiss = { showBottomSheet = false },
                onActionSelected = { action ->
                    showBottomSheet = false
                    when (action) {
                        SearchImageAction.CAMERA -> camera.launch()
                        SearchImageAction.GALLERY -> openGallery()
                        SearchImageAction.DIVO_PHOTO -> {
                            //TODO пока не понятно какое фото юзать
//                            val photoUrl = state.profilePhotoUrl ?: return@MainSearchBottomSheet
//                            scope.launch {
//                                val uri = ImageCacheHelper.getLocalUri(context, photoUrl)
//                                if (uri != null) {
//                                    Log.d("VideoGrid", uri.toString())
//                                    //onIntent(Intent.OnPhotoSelected(uri))
//                                }
//                            }
                        }
                    }
                }
            )
        }
    }
}

