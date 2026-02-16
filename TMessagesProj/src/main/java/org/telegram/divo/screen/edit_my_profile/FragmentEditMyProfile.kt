package org.telegram.divo.screen.edit_my_profile

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.ImageUpdater
import android.content.Intent
import android.os.Bundle
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.Components.ImageUpdater.TYPE_SET_PHOTO_FOR_USER

class FragmentEditMyProfile : BaseFragment(), ImageUpdater.ImageUpdaterDelegate {

    private var imageUpdater: ImageUpdater? = null
    private val viewModel by lazy { EditMyProfileViewModel() }


    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)
        val account = UserConfig.selectedAccount
        val clientUserId = UserConfig.getInstance(account).clientUserId
        val user = MessagesController.getInstance(account).getUser(clientUserId)

        if (imageUpdater == null) {
            imageUpdater = ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, true).apply {
                setUseAttachMenu(false)
                setOpenWithFrontfaceCamera(true)
                parentFragment = this@FragmentEditMyProfile
                setDelegate(this@FragmentEditMyProfile)
            }
        }
        if (user != null) {
            imageUpdater?.setUser(user)
        }

        return ComposeView(context).apply {
            setContent {
                EditMyProfileScreen(
                    viewModel = viewModel,
                    messageStorage = messagesStorage,
                    onCloseScreen = { finishFragment() },
                    onEditImageClicked = {
                        imageUpdater?.openMenu(
                            /* hasAvatar = */ false,
                            /* onDeleteAvatar = */ Runnable { },
                            /* onDismiss = */ null,
                            /* type = */ TYPE_SET_PHOTO_FOR_USER
                        )
                    }
                )
            }
        }
    }

    override fun didUploadPhoto(
        photo: TLRPC.InputFile?,
        video: TLRPC.InputFile?,
        videoStartTimestamp: Double,
        videoPath: String?,
        bigSize: TLRPC.PhotoSize?,
        smallSize: TLRPC.PhotoSize?,
        isVideo: Boolean,
        emojiMarkup: TLRPC.VideoSize?
    ) {
        viewModel.setIntent(
            EditMyProfileViewModel.EditMyProfileIntent.OnAvatarUploaded(
                photo,
                video,
                videoStartTimestamp,
                videoPath,
                bigSize,
                smallSize,
                isVideo,
                emojiMarkup
            )
        )
    }

    override fun onActivityResultFragment(requestCode: Int, resultCode: Int, data: Intent?) {
        imageUpdater?.onActivityResult(requestCode, resultCode, data)
    }

    override fun saveSelfArgs(args: Bundle) {
        imageUpdater?.currentPicturePath?.let { args.putString("path", it) }
    }

    override fun restoreSelfArgs(args: Bundle) {
        imageUpdater?.currentPicturePath = args.getString("path")
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        imageUpdater?.clear()
        imageUpdater = null
    }
}
