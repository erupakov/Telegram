package org.telegram.divo.screen.profile

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.navigation.NavController
import org.telegram.divo.common.utils.FragmentLifecycleOwner
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.divo.style.setDivoContent
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
    
    private val composeLifecycleOwner = FragmentLifecycleOwner().apply {
        onCreate()
        onStart()
        onResume()
    }

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

        fragmentView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(composeLifecycleOwner)
            setViewTreeViewModelStoreOwner(composeLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(composeLifecycleOwner)
            setViewCompositionStrategy(object : androidx.compose.ui.platform.ViewCompositionStrategy {
                override fun installFor(view: androidx.compose.ui.platform.AbstractComposeView): () -> Unit {
                    return {} // Prevent disposal on detach
                }
            })
            setDivoContent {
                CompositionLocalProvider(
                    LocalOnBackPressedDispatcherOwner provides composeLifecycleOwner
                ) {
                    ProfileNavGraph(
                        userId = targetUserId,
                        isOwnProfile = true,
                        onNavControllerReady = { navController = it },
                        onNavigateToChat = { tgId, tgHash, tgUsername ->
                            val currentAccount = org.telegram.messenger.UserConfig.selectedAccount
                            var user = org.telegram.messenger.MessagesController.getInstance(currentAccount).getUser(tgId)
                            if (user == null) {
                                user = org.telegram.tgnet.TLRPC.TL_user()
                                user.id = tgId
                                user.first_name = tgUsername ?: "User"
                                user.username = tgUsername
                                user.access_hash = tgHash ?: 0L
                                org.telegram.messenger.MessagesController.getInstance(currentAccount).putUser(user, false)
                            }
                            val args = android.os.Bundle()
                            args.putLong("user_id", tgId)
                            presentFragment(org.telegram.ui.ChatActivity(args))
                        },
                        onNavigateToCreateChannel = {
                            val args = android.os.Bundle()
                            args.putInt("step", 0)
                            presentFragment(org.telegram.ui.ChannelCreateActivity(args))
                        },
                        onNavigateBack = { finishFragment() }
                    )
                }
            }
        }
        return fragmentView
    }

    override fun onBackPressed(invoked: Boolean): Boolean {
        if (composeLifecycleOwner.onBackPressed()) {
            return false // Handled by Compose
        }
        return super.onBackPressed(invoked)
    }

    override fun onFragmentDestroy() {
        super.onFragmentDestroy()
        composeLifecycleOwner.onDestroy()
        (fragmentView as? ComposeView)?.disposeComposition()
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