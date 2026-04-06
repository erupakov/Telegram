package org.telegram.divo.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class SnackbarEvent {
    data class Error(val message: String) : SnackbarEvent()
    data class ErrorWithRetry(val message: String, val actionLabel: String, val onRetry: () -> Unit) : SnackbarEvent()
    data class Success(val message: String) : SnackbarEvent()
}

val SnackbarEvent.message: String get() = when (this) {
    is SnackbarEvent.Error -> message
    is SnackbarEvent.ErrorWithRetry -> message
    is SnackbarEvent.Success -> message
}

class AppSnackbarHostState {
    val hostState = SnackbarHostState()
    var currentEvent: SnackbarEvent? by mutableStateOf(null)
        private set

    suspend fun show(event: SnackbarEvent) {
        currentEvent = event
        val result = hostState.showSnackbar(
            message = event.message,
            actionLabel = if (event is SnackbarEvent.ErrorWithRetry) event.actionLabel else null,
        )
        if (result == SnackbarResult.ActionPerformed && event is SnackbarEvent.ErrorWithRetry) {
            event.onRetry()
        }
        currentEvent = null
    }
}

@Composable
fun AppSnackbarHost(
    modifier: Modifier = Modifier,
    state: AppSnackbarHostState,
    bottomPadding: Dp = 16.dp,
) {
    SnackbarHost(
        modifier = modifier.padding(bottom = bottomPadding),
        hostState = state.hostState
    ) { data ->
        val backgroundColor = when (state.currentEvent) {
            is SnackbarEvent.Success -> Color(0xFF0C8A51)
            is SnackbarEvent.Error,
            is SnackbarEvent.ErrorWithRetry -> Color(0xFFDF1C41)
            null -> MaterialTheme.colorScheme.inverseSurface
        }
        Snackbar(
            snackbarData = data,
            containerColor = backgroundColor,
            contentColor = Color.White,
            actionColor = Color.White,
        )
    }
}