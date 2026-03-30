package org.telegram.divo.screen.city

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentCityList : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            EventCitiesScreen (
                onBack = {},
                onDone = { city ->

                }
            )
        }
        return composeView
    }

}