package org.telegram.divo.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager

@Composable
fun rememberIsOnline(currentAccount: Int = UserConfig.selectedAccount): Boolean {
    val state = rememberTelegramConnectionState(currentAccount)
    return state == ConnectionsManager.ConnectionStateConnected
}

@Composable
fun rememberTelegramConnectionState(currentAccount: Int): Int {
    var connectionState by remember {
        mutableIntStateOf(ConnectionsManager.getInstance(currentAccount).connectionState)
    }
    DisposableEffect(currentAccount) {
        val notificationCenter = NotificationCenter.getInstance(currentAccount)
        val observer = NotificationCenter.NotificationCenterDelegate { id, _, _ ->
            if (id == NotificationCenter.didUpdateConnectionState) {
                connectionState = ConnectionsManager.getInstance(currentAccount).connectionState
            }
        }
        notificationCenter?.addObserver(observer, NotificationCenter.didUpdateConnectionState)
        onDispose {
            notificationCenter?.removeObserver(observer, NotificationCenter.didUpdateConnectionState)
        }
    }
    return connectionState
}