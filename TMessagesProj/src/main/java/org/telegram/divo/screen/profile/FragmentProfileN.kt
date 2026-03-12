package org.telegram.divo.screen.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.BaseFragment

class FragmentProfileN : BaseFragment() {

    //private var imageUpdater: ImageUpdater? = null
    private var isUploadingBackground = false

    private val targetUserId: Int by lazy {
        arguments?.getInt(ARG_USER_ID, -1) ?: -1
    }
    private val isOwnProfile: Boolean by lazy {
        arguments?.getBoolean(ARG_OWN_PROFILE, false) ?: false
    }
    private var navController: NavController? = null

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val account = UserConfig.selectedAccount
        val clientUserId = UserConfig.getInstance(account).clientUserId
        val user = MessagesController.getInstance(account).getUser(clientUserId)

//        if (imageUpdater == null) {
//            imageUpdater = ImageUpdater(true, ImageUpdater.FOR_TYPE_USER, false).apply {
//                setUseAttachMenu(false)
//                parentFragment = this@FragmentProfileN
//                setDelegate(this@FragmentProfileN)
//            }
//        }
//        if (user != null) {
//            imageUpdater?.setUser(user)
//        }

        return ComposeView(context).apply {
            setContent {
                ProfileNavGraph(
                    userId = targetUserId,
                    isOwnProfile = true,
                    onNavControllerReady = { navController = it },
                    onNavigateBack = { finishFragment() }
                )
            }
        }
    }

    override fun onBackPressed(): Boolean {
        val nav = navController
        if (nav != null && nav.previousBackStackEntry != null) {
            nav.popBackStack()
            return false
        }
        return super.onBackPressed()
    }

//    override fun didUploadPhoto(
//        photo: TLRPC.InputFile?,
//        video: TLRPC.InputFile?,
//        videoStartTimestamp: Double,
//        videoPath: String?,
//        bigSize: TLRPC.PhotoSize?,
//        smallSize: TLRPC.PhotoSize?,
//        isVideo: Boolean,
//        emojiMarkup: TLRPC.VideoSize?
//    ) {
//        if (photo == null) return
//
//        if (isUploadingBackground) {
//            viewModel.setIntent(
//                ProfileIntent.OnBackgroundPhotoSelected(
//                    photo = photo,
//                    localPath = imageUpdater?.currentPicturePath
//                )
//            )
//        } else {
////            viewModel.setIntent(
////                ProfileIntent.OnPortfolioPhotoSelected(
////                    photo = photo,
////                    localPath = imageUpdater?.currentPicturePath
////                )
////            )
//        }
//    }

//    override fun onActivityResultFragment(requestCode: Int, resultCode: Int, data: Intent?) {
//        imageUpdater?.onActivityResult(requestCode, resultCode, data)
//    }
//
//    override fun saveSelfArgs(args: Bundle) {
//        imageUpdater?.currentPicturePath?.let { args.putString("path", it) }
//    }
//
//    override fun restoreSelfArgs(args: Bundle) {
//        imageUpdater?.currentPicturePath = args.getString("path")
//    }
//
//    override fun onFragmentDestroy() {
//        super.onFragmentDestroy()
//        imageUpdater?.clear()
//        imageUpdater = null
//    }

    companion object {
        private const val ARG_USER_ID = "user_id"
        private const val ARG_OWN_PROFILE = "own_profile"

        fun newInstance(userId: Int, isOwnProfile: Boolean = false): FragmentProfileN {
            return FragmentProfileN().apply {
                arguments = Bundle().apply {
                    putInt(ARG_USER_ID, userId)
                    putBoolean(ARG_OWN_PROFILE, isOwnProfile)
                }
            }
        }
    }
}