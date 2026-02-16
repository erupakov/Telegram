package org.telegram.divo.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.edit_my_profile.FragmentEditMyProfile
import org.telegram.divo.screen.profile_social_links.FragmentProfileSocialLinks
import org.telegram.divo.screen.work_history.FragmentWorkHistory
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.ImageUpdater

class FragmentProfileN : BaseFragment(), ImageUpdater.ImageUpdaterDelegate {

    private var imageUpdater: ImageUpdater? = null
    private val viewModel by lazy { ProfileViewModel() }
    private var isUploadingBackground = false

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val account = UserConfig.selectedAccount
        val clientUserId = UserConfig.getInstance(account).clientUserId
        val user = MessagesController.getInstance(account).getUser(clientUserId)

        if (imageUpdater == null) {
            imageUpdater = ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, false).apply {
                setUseAttachMenu(false)
                parentFragment = this@FragmentProfileN
                setDelegate(this@FragmentProfileN)
            }
        }
        if (user != null) {
            imageUpdater?.setUser(user)
        }

        val composeView = ComposeView(context)
        composeView.setContent {
            ProfileScreen(
                viewModel = viewModel,
                onEditClicked = {
                    presentFragment(FragmentEditMyProfile())
                },
                onNavigateBack = {
                    finishFragment()
                },
                onEditLinksClicked = {
                    presentFragment(FragmentProfileSocialLinks())
                },
                showWorkHistory = {
                    presentFragment(FragmentWorkHistory())
                },
                onAddPortfolioClicked = {
                    isUploadingBackground = false
                    imageUpdater?.openMenu(
                        false,
                        Runnable {
                            viewModel.setIntent(ProfileViewModel.ProfileIntent.OnClearPortfolioUpload)
                        },
                        null,
                        ImageUpdater.TYPE_DEFAULT
                    )
                },
                onEditBackgroundClicked = {
                    isUploadingBackground = true
                    imageUpdater?.openMenu(
                        false,
                        Runnable {
                            viewModel.setIntent(ProfileViewModel.ProfileIntent.OnClearPortfolioUpload)
                        },
                        null,
                        ImageUpdater.TYPE_DEFAULT
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

        if (isUploadingBackground) {
            viewModel.setIntent(
                ProfileViewModel.ProfileIntent.OnBackgroundPhotoSelected(
                    photo = photo,
                    localPath = imageUpdater?.currentPicturePath
                )
            )
        } else {
            viewModel.setIntent(
                ProfileViewModel.ProfileIntent.OnPortfolioPhotoSelected(
                    photo = photo,
                    localPath = imageUpdater?.currentPicturePath
                )
            )
        }
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
