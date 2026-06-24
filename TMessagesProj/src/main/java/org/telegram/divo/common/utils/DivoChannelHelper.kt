package org.telegram.divo.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.telegram.divo.dal.network.DivoApi

object DivoChannelHelper {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @JvmStatic
    fun onChannelCreated(chatId: Long, currentAccount: Int, username: String?, inviteLink: String?) {
        scope.launch {
            try {
                val resolvedUsername = username?.takeIf { it.isNotBlank() }
                val resolvedInviteLink = inviteLink?.takeIf { it.isNotBlank() } ?: resolvedUsername?.let { "https://t.me/$it" }
                
                DivoApi.userRepository.addChannel(
                    telegramChatId = chatId,
                    username = resolvedUsername,
                    inviteLink = resolvedInviteLink
                )
            } catch (e: Exception) { }
        }
    }
}
