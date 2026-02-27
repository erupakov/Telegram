package org.telegram.divo.screen.models

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.divo.screen.photo.FragmentPhotoViewer
import org.telegram.divo.screen.profile.FragmentProfileN
import org.telegram.ui.ActionBar.BaseFragment

class FragmentModels : BaseFragment() {

    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            ModelsHomeScreen(
                onSearch = {},
                onClick = { userId ->
                    presentFragment(FragmentProfileN.newInstance(userId))
                },
                onPhotoClicked = { url ->
                    presentFragment(FragmentPhotoViewer.newInstance(url))
                }
            )
        }
        return composeView
    }

    override fun isLightStatusBar(): Boolean {
        return true
    }
}