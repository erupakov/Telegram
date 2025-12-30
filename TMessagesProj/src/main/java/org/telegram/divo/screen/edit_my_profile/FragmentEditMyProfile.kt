package org.telegram.divo.screen.edit_my_profile

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.ImageUpdater
import android.content.Intent
import android.os.Bundle
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.ImageUpdater.TYPE_SET_PHOTO_FOR_USER

class FragmentEditMyProfile : BaseFragment(){



    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        return ComposeView(context).apply {
            setContent {
                EditMyProfileScreen(
                    messageStorage = messagesStorage,
                    onCloseScreen = { finishFragment() },
                    onEditImageClicked = {  }
                )
            }
        }
    }

}
