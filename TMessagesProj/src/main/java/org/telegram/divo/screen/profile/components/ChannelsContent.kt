package org.telegram.divo.screen.profile.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.common.compose.shimmer
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.LocaleController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.BackupImageView

@Composable
fun ChannelsContent(
    channels: List<org.telegram.divo.entity.UserChannel>,
    isModel: Boolean,
    isOwnProfile: Boolean,
    showBackButton: Boolean,
    isEvent: Boolean = false,
    transitionProgress: Float = 1f,
    topPadding: Dp = 0.dp,
    isRefreshing: Boolean = false,
    onAddChannel: () -> Unit = {}
) {
    val inset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomPadding = if (!showBackButton) 68.dp + inset else inset + 8.dp

    if (channels.isEmpty()) {
        EmptyChannels(
            isOwnProfile = isOwnProfile,
            isModel = isModel,
            transitionProgress = transitionProgress,
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            onClick = onAddChannel
        )
    } else {
        val context = androidx.compose.ui.platform.LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight),
            contentPadding = PaddingValues(
                top = topPadding,
                bottom = bottomPadding + 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = channels,
                key = { it.telegramChatId }
            ) { channel ->
                ChannelItem(
                    channel = channel,
                    isEvent = isEvent,
                    isRefreshing = isRefreshing,
                    onClicked = { 
                        val link = channel.inviteLink ?: "https://t.me/${channel.username}"
                        org.telegram.messenger.browser.Browser.openUrl(context, link)
                    }
                )
            }
        }
    }
}

@Composable
private fun ChannelItem(
    channel: org.telegram.divo.entity.UserChannel,
    isEvent: Boolean,
    isRefreshing: Boolean = false,
    onClicked: () -> Unit,
) {
    val account = org.telegram.messenger.UserConfig.selectedAccount
    val initialChat = MessagesController.getInstance(account).getChat(channel.telegramChatId)
    val initialChatFull = MessagesController.getInstance(account).getChatFull(channel.telegramChatId)

    var chatTitle by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialChat?.title ?: channel.username ?: "Channel ${channel.telegramChatId}") }
    var chatParticipants by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(initialChatFull?.participants_count ?: initialChat?.participants_count ?: 0) }
    var chatObject by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<org.telegram.tgnet.TLObject?>(initialChat) }

    androidx.compose.runtime.DisposableEffect(channel.telegramChatId) {
        val observer = org.telegram.messenger.NotificationCenter.NotificationCenterDelegate { id, _, args ->
            if (id == org.telegram.messenger.NotificationCenter.updateInterfaces) {
                val updatedChat = MessagesController.getInstance(account).getChat(channel.telegramChatId)
                val updatedChatFull = MessagesController.getInstance(account).getChatFull(channel.telegramChatId)
                if (updatedChat != null) {
                    chatTitle = updatedChat.title
                    chatParticipants = updatedChatFull?.participants_count ?: updatedChat.participants_count
                    chatObject = updatedChat
                }
            } else if (id == org.telegram.messenger.NotificationCenter.chatInfoDidLoad) {
                val chatFull = args[0] as? TLRPC.ChatFull
                if (chatFull != null && chatFull.id == channel.telegramChatId) {
                    chatParticipants = chatFull.participants_count
                }
            }
        }
        
        org.telegram.messenger.NotificationCenter.getInstance(account).addObserver(observer, org.telegram.messenger.NotificationCenter.updateInterfaces)
        org.telegram.messenger.NotificationCenter.getInstance(account).addObserver(observer, org.telegram.messenger.NotificationCenter.chatInfoDidLoad)
        
        onDispose {
            org.telegram.messenger.NotificationCenter.getInstance(account).removeObserver(observer, org.telegram.messenger.NotificationCenter.updateInterfaces)
            org.telegram.messenger.NotificationCenter.getInstance(account).removeObserver(observer, org.telegram.messenger.NotificationCenter.chatInfoDidLoad)
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, channel.telegramChatId) {
        val lifecycleObserver = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                val updatedChat = MessagesController.getInstance(account).getChat(channel.telegramChatId)
                val updatedChatFull = MessagesController.getInstance(account).getChatFull(channel.telegramChatId)
                if (updatedChat != null) {
                    chatTitle = updatedChat.title
                    chatParticipants = updatedChatFull?.participants_count ?: updatedChat.participants_count
                    chatObject = updatedChat
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        }
    }


    androidx.compose.runtime.LaunchedEffect(channel.telegramChatId, isRefreshing) {
        if (initialChat == null || isRefreshing) {
            if (!channel.username.isNullOrEmpty()) {
                val req = TLRPC.TL_contacts_resolveUsername()
                req.username = channel.username
                ConnectionsManager.getInstance(account).sendRequest(req) { response, _ ->
                    AndroidUtilities.runOnUIThread {
                        if (response is TLRPC.TL_contacts_resolvedPeer) {
                            val resolvedChat = response.chats.firstOrNull { it.id == channel.telegramChatId } ?: response.chats.firstOrNull()
                            if (resolvedChat != null) {
                                MessagesController.getInstance(account).putChat(resolvedChat, false)
                                chatTitle = resolvedChat.title
                                chatParticipants = resolvedChat.participants_count
                                chatObject = resolvedChat
                                MessagesController.getInstance(account).loadFullChat(resolvedChat.id, 0, true)
                            }
                        }
                    }
                }
            } else if (!channel.inviteLink.isNullOrEmpty()) {
                val hash = channel.inviteLink.substringAfterLast("/+")
                if (hash.isNotEmpty() && hash != channel.inviteLink) {
                    val req = TLRPC.TL_messages_checkChatInvite()
                    req.hash = hash
                    ConnectionsManager.getInstance(account).sendRequest(req) { response, _ ->
                        AndroidUtilities.runOnUIThread {
                            if (response is TLRPC.ChatInvite) {
                                if (response is TLRPC.TL_chatInviteAlready) {
                                    MessagesController.getInstance(account).putChat(response.chat, false)
                                    chatTitle = response.chat.title
                                    chatParticipants = response.chat.participants_count
                                    chatObject = response.chat
                                    MessagesController.getInstance(account).loadFullChat(response.chat.id, 0, true)
                                } else {
                                    chatTitle = response.title
                                    chatParticipants = response.participants_count
                                    chatObject = response
                                }
                            }
                        }
                    }
                }
            }
        } else if (initialChatFull == null) {
            MessagesController.getInstance(account).loadFullChat(channel.telegramChatId, 0, true)
        }
    }

    val displayFollowers = LocaleController.formatPluralStringSpaced("Followers", chatParticipants)

    if (chatObject == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Box(
                    modifier = Modifier
                        .size(120.dp, 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmer()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp, 14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .shimmer()
                )
            }
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f).clickableWithoutRipple { onClicked() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { ctx ->
                        BackupImageView(ctx).apply {
                            setRoundRadius(AndroidUtilities.dp(30f))
                        }
                    },
                    update = { view ->
                        if (chatObject != null) {
                            val avatarDrawable = org.telegram.ui.Components.AvatarDrawable()
                            if (chatObject is TLRPC.Chat) {
                                avatarDrawable.setInfo(account, chatObject as TLRPC.Chat)
                            } else if (chatObject is TLRPC.ChatInvite) {
                                val invite = chatObject as TLRPC.ChatInvite
                                avatarDrawable.setInfo(0L, invite.title ?: "", null)
                            }
                            view.setForUserOrChat(chatObject, avatarDrawable)
                        } else {
                            view.setImageDrawable(ContextCompat.getDrawable(view.context, R.drawable.divo_avatar_placeholder))
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        modifier = Modifier,
                        text = chatTitle,
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 16.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = displayFollowers,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 14.sp,
                    color = Color.Black.copy(0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isEvent) {
            UIButton(
                modifier = Modifier
                    .height(32.dp),
                text = "Apply",
                textStyle = AppTheme.typography.textButton.copy(
                    fontSize = 14.sp
                ),
                shape = RoundedCornerShape(6.dp),
                onClick = {}
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun EmptyChannels(
    isOwnProfile: Boolean,
    isModel: Boolean,
    transitionProgress: Float,
    topPadding: Dp,
    bottomPadding: Dp,
    onClick: () -> Unit = {}
) {
    val textId = when {
        isOwnProfile -> R.string.YouHaveNotCreatedChannels
        !isOwnProfile && isModel -> R.string.ThisProfileHasNotCreatedChannels
        else -> R.string.ThisAgencyHasNotCreatedChannels
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.backgroundLight)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val startOffset = topPadding + 16.dp
        val endOffset = maxHeight / 2 - 100.dp
        val currentOffset = startOffset + (endOffset - startOffset) * transitionProgress

        Column(
            modifier = Modifier
                .offset(y = currentOffset),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.textPrimary.copy(0.1f))
            ) {
                Icon(
                    modifier = Modifier.size(24.dp).align(Alignment.Center),
                    painter = painterResource(R.drawable.divo_profile_tab_3),
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.ThereAreNoChannels).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(textId),
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
            )
        }

        if (isOwnProfile) {
            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = bottomPadding + 8.dp)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.CreateNewChannel),
                onClick = onClick
            )
        }
    }
}