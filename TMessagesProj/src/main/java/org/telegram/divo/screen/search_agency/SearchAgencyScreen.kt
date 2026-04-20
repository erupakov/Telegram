package org.telegram.divo.screen.search_agency

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent.Error
import org.telegram.divo.screen.search_agency.component.SearchAgencyContent

@Composable
fun SearchAgencyScreen(
    viewModel: SearchAgencyViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val state = viewModel.state.collectAsState().value
    val snackbarState = remember { AppSnackbarHostState() }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect {
            when (it) {
                Effect.NavigateBack -> onBack()
                is Effect.ShowError -> snackbarState.show(Error(it.message))
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        SearchAgencyContent(
            State = state,
            onIntent = { viewModel.setIntent(it) }
        )

        AppSnackbarHost(
            modifier = Modifier.align(Alignment.BottomCenter),
            state = snackbarState,
            bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 56.dp
        )
    }
}

