package org.telegram.divo.screen.event_create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.ImageUpdater

class FragmentEventCreate : BaseFragment(), ImageUpdater.ImageUpdaterDelegate {

    private var imageUpdater: ImageUpdater? = null
    private val viewModel by lazy { CreateEventViewModel() }

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        if (imageUpdater == null) {
            imageUpdater = ImageUpdater(true, ImageUpdater.FOR_TYPE_CHANNEL, false).apply {
                setUseAttachMenu(false)
                parentFragment = this@FragmentEventCreate
                setDelegate(this@FragmentEventCreate)
            }
        }

        val composeView = ComposeView(context)
        composeView.setContent {
            CreateEventScreen(
                viewModel = viewModel,
                onBack = {
                    finishFragment()
                },
                onPickCover = {
                    imageUpdater?.openMenu(
                        /* hasAvatar = */ false,
                        /* onDeleteAvatar = */ Runnable {
                            viewModel.setIntent(CreateEventViewModel.Intent.OnClearCoverPhoto)
                        },
                        /* onDismiss = */ null,
                        /* type = */ ImageUpdater.TYPE_DEFAULT
                    )
                }
            )
        }
        return composeView
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
        if (photo == null) return

        viewModel.setIntent(
            CreateEventViewModel.Intent.OnCoverPhotoSelected(
                photo = photo,
                localPath = imageUpdater?.currentPicturePath
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
