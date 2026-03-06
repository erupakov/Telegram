package org.telegram.divo.screen.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.navigation.NavController
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import org.telegram.divo.screen.edit_my_profile.FragmentEditMyProfile
import org.telegram.divo.screen.profile_social_links.FragmentProfileSocialLinks
import org.telegram.divo.screen.work_history.FragmentWorkHistory
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC
import org.telegram.ui.ActionBar.BaseFragment
import org.telegram.ui.Components.ImageUpdater

class FragmentProfileN : BaseFragment() {

    //private var imageUpdater: ImageUpdater? = null
    private val viewModel by lazy { ProfileViewModel() }
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

        val composeView = ComposeView(context).apply {
            (getParentActivity() as? androidx.activity.ComponentActivity)?.let { activity ->
                setViewTreeLifecycleOwner(activity)
                setViewTreeViewModelStoreOwner(activity)
                setViewTreeSavedStateRegistryOwner(activity)
            }

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProfileNavGraph(
                    initialUserId = targetUserId,
                    initialIsOwnProfile = isOwnProfile,
                    onEditLinksClicked = {
                        presentFragment(FragmentProfileSocialLinks())
                    },
                    showWorkHistory = {
                        presentFragment(FragmentWorkHistory())
                    },
                    onNavigateBack = { finishFragment() },
                    onNavControllerReady = { navController = it },
                )
            }
        }

        return composeView
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