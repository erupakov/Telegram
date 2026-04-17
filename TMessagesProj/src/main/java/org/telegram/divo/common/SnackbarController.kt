package org.telegram.divo.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme

sealed class SnackbarEvent {
    data class Error(val message: String) : SnackbarEvent()
    data class ErrorWithRetry(val message: String, val actionLabel: String, val onRetry: () -> Unit) : SnackbarEvent()
    data class Success(val message: String) : SnackbarEvent()
    data class SuccessWithIcon(val resId: Int, val message: String) : SnackbarEvent()
}

val SnackbarEvent.message: String get() = when (this) {
    is SnackbarEvent.Error -> message
    is SnackbarEvent.ErrorWithRetry -> message
    is SnackbarEvent.Success -> message
    is SnackbarEvent.SuccessWithIcon -> message
}

class AppSnackbarHostState {
    val hostState = SnackbarHostState()
    var currentEvent: SnackbarEvent? by mutableStateOf(null)
        private set

    suspend fun show(event: SnackbarEvent) {
        hostState.currentSnackbarData?.dismiss()
        currentEvent = event

        try {
            val result = hostState.showSnackbar(
                message = event.message,
                actionLabel = if (event is SnackbarEvent.ErrorWithRetry) event.actionLabel else null,
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed && event is SnackbarEvent.ErrorWithRetry) {
                event.onRetry()
            }
        } finally {
            if (currentEvent == event) {
                currentEvent = null
            }
        }
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
            is SnackbarEvent.Error, is SnackbarEvent.ErrorWithRetry -> Color(0xFFDF1C41)
            is SnackbarEvent.SuccessWithIcon -> AppTheme.colors.textPrimary
            null -> MaterialTheme.colorScheme.inverseSurface
        }

        Snackbar(
            modifier = Modifier.padding(horizontal = 16.dp),
            containerColor = backgroundColor,
            contentColor = Color.White,
            actionContentColor = Color.White,
            action = if (data.visuals.actionLabel != null) {
                {
                    Text(
                        modifier = Modifier.padding(end = 8.dp).clickableWithoutRipple { data.performAction() },
                        text = data.visuals.actionLabel.orEmpty(),
                        color = AppTheme.colors.textColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else null
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val event = state.currentEvent
                if (event is SnackbarEvent.SuccessWithIcon) {
                    Icon(
                        painter = painterResource(id = event.resId),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = data.visuals.message,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}