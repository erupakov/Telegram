package org.telegram.divo.screen.event_create

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.event_create.components.FirstPage
import org.telegram.divo.screen.event_create.components.SecondPage
import org.telegram.divo.screen.event_create.components.ThirdPage
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R


@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel = viewModel(),
    editingEventId: Int? = null,
    onBack: () -> Unit = {},
    onPreviewClicked: () -> Unit,
    onEventPublished: () -> Unit = {},
) {
    val snackbarState = remember { AppSnackbarHostState() }
    val successMessage = if (editingEventId == null) "Event published" else "Event updated"

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> {
                    onBack()
                }
                is Effect.ShowError -> {
                    snackbarState.show(SnackbarEvent.Error(it.message))
                }

                Effect.NavigateToPreview -> { onPreviewClicked() }
                Effect.EventPublished -> {
                    launch {
                        snackbarState.show(SnackbarEvent.Success(successMessage))
                    }
                    delay(300)
                    onEventPublished()
                }
            }
        }
    }
    val state = viewModel.state.collectAsState().value
    LaunchedEffect(editingEventId) {
        viewModel.setIntent(Intent.OnInitEdit(editingEventId))
    }
    CreateEventScreenView(
        state = state,
        snackbarState = snackbarState,
        onIntent = {
            viewModel.setIntent(it)
        },
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CreateEventScreenView(
    state: State = State(),
    snackbarState: AppSnackbarHostState = remember { AppSnackbarHostState() },
    onIntent: (Intent) -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val scope = rememberCoroutineScope()
    val isActionEnabled = when (pagerState.currentPage) {
        0 -> state.isFirstPageValid
        1 -> true
        else -> state.isThirdPageValid
    }

    LaunchedEffect(state.resetPagerToFirstPage) {
        if (state.resetPagerToFirstPage) {
            pagerState.scrollToPage(0)
            onIntent(Intent.OnFirstPageReached)
        }
    }

    val onBack: () -> Unit = {
        if (pagerState.currentPage > 0) {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        } else {
            onIntent(Intent.OnBackClicked)
        }
    }

    val isLastPage = pagerState.currentPage == pagerState.pageCount - 1
    val isEdit = state.editingEventId != null

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    Scaffold(
        topBar = {
            TopBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = pagerState.pageCount,
                onBack = onBack,
                isEdit = state.editingEventId != null
            )
        },
        containerColor = AppTheme.colors.backgroundLight,
        snackbarHost = {
            AppSnackbarHost(
                state = snackbarState,
                bottomPadding = if (isLastPage && isEdit) 140.dp else 76.dp
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                userScrollEnabled = false,
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> {
                        FirstPage(
                            state = state,
                            onEventTypeSelected = { onIntent(Intent.OnEventTypeSelected(it)) },
                            onEventNameChanged = { onIntent(Intent.OnEventNameChanged(it)) },
                            onEventDescriptionChanged = { onIntent(Intent.OnEventDescriptionChanged(it)) },
                            onAvatarSelected = { onIntent(Intent.OnAvatarSelected(it)) },
                            onEventDateChanged = { onIntent(Intent.OnEventDateChanged(it)) },
                            onEventTimeChanged = { onIntent(Intent.OnEventTimeChanged(it)) },
                            onCountriesChanged = { onIntent(Intent.OnCountriesChanged(it)) }
                        )
                    }
                    1 -> {
                        SecondPage(
                            state = state,
                            onIntent = onIntent
                        )
                    }
                    else -> {
                        ThirdPage(
                            state = state,
                            isEdit = isLastPage && isEdit,
                            onIntent = onIntent
                        )
                    }
                }
            }

            if (isLastPage && isEdit) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .align(Alignment.BottomCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UIButtonNew(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.SaveChanges),
                        enabled = isActionEnabled && !state.isUploading,
                        onClick = { onIntent(Intent.OnPublishClicked) }
                    )
                    UIButtonNew(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.DiscardChanges),
                        background = AppTheme.colors.buttonSecondary,
                        onClick = { onIntent(Intent.OnBackClicked) }
                    )
                }
            } else {
                UIButtonNew(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        .align(Alignment.BottomCenter),
                    text = if (isLastPage) stringResource(R.string.EventPreview) else stringResource(R.string.EventContinue),
                    enabled = isActionEnabled,
                    onClick = {
                        if (!isActionEnabled) return@UIButtonNew
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onIntent(Intent.OnPreviewClicked)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    currentPage: Int,
    totalPages: Int,
    isEdit: Boolean = false
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(if (isEdit) R.string.EditEvent else R.string.CreateEvent).uppercase(),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            RoundedButton(
                modifier = Modifier.padding(start = 16.dp),
                onClick = onBack
            )
        },
        actions = {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(R.string.EventStepProgress, currentPage, totalPages),
                color = AppTheme.colors.textPrimary,
                fontSize = 15.sp,
                style = AppTheme.typography.helveticaNeueRegular
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.backgroundLight
        )
    )
}
