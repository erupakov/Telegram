package org.telegram.divo.screen.photo

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentPhotoViewer : BaseFragment() {

    private val photoUrl: String by lazy {
        arguments?.getString(ARG_PHOTO_URL) ?: ""
    }

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        return ComposeView(context).apply {
            setContent {
                PhotoViewerScreen(
                    url = photoUrl,
                    onBack = { finishFragment() }
                )
            }
        }
    }

    companion object {
        private const val ARG_PHOTO_URL = "photo_url"

        fun newInstance(photoUrl: String): FragmentPhotoViewer {
            return FragmentPhotoViewer().apply {
                arguments = Bundle().apply {
                    putString(ARG_PHOTO_URL, photoUrl)
                }
            }
        }
    }
}