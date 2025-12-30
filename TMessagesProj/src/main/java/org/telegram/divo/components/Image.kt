package org.telegram.divo.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.ImageLocation
import org.telegram.messenger.R
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.AvatarDrawable
import org.telegram.ui.Components.BackupImageView

@Composable
fun TelegramUserAvatar(
    user: TLRPC.User?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56
) {
    if (user == null) {
    }

    AndroidView(
        modifier = modifier.size(sizeDp.dp),
        factory = { context ->
            BackupImageView(context).apply {
                setRoundRadius(AndroidUtilities.dp(sizeDp / 2f)) // круг
            }
        },
        update = { view ->
            val location = ImageLocation.getForUser(user, ImageLocation.TYPE_BIG)
            val placeholder = AvatarDrawable(user)

            // ВАЖНО: вызываем overload с parentObject (user), иначе Kotlin выбирает String-версию
            view.setImage(location, "50_50", placeholder, user)
        }
    )
}

@Composable
fun TelegramUserAvatarEditable(
    user: TLRPC.User?,
    modifier: Modifier = Modifier,
    sizeDp: Int = 56,
    onEditClick: ()-> Unit
) {
    Box(modifier = modifier.padding(), contentAlignment = Alignment.BottomEnd) {
        TelegramUserAvatar(
            user = user,
            modifier = Modifier.padding(8.dp).clickable(onClick = {onEditClick()}),
            sizeDp = sizeDp
        )
        Card(
            border = BorderStroke(width = 2.dp, color = AppTheme.colors.accentColor),
            shape = CircleShape,
            modifier = Modifier.clickable(onClick = {onEditClick()})
        ) {
            Icon(
                painter = painterResource(R.drawable.msg_edit),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppTheme.colors.accentColor
            )
        }
    }
}