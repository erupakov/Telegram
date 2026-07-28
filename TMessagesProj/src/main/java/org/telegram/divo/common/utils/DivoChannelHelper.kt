package org.telegram.divo.common.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.dal.network.DivoApi

object DivoChannelHelper {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @JvmStatic
    fun onChannelCreated(chatId: Long, username: String?, inviteLink: String?) {
        scope.launch {
            try {
                val resolvedUsername = username?.takeIf { it.isNotBlank() }
                val resolvedInviteLink = inviteLink?.takeIf { it.isNotBlank() } ?: resolvedUsername?.let { "https://t.me/$it" }
                
                DivoApi.userRepository.addChannel(
                    telegramChatId = chatId,
                    username = resolvedUsername,
                    inviteLink = resolvedInviteLink
                )
                
                val user = DivoApi.userRepository.currentUserFlow.value
                val userId = user?.id ?: 0
                DivoAnalytics.logEvent(AnalyticsEvent.ChannelCreateSuccess(userId, chatId))
            } catch (e: Exception) { }
        }
    }
}
