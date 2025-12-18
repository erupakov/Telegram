package org.telegram.divo.screen.country

import android.content.Context
import android.view.View
import androidx.compose.ui.platform.ComposeView
import org.telegram.ui.ActionBar.BaseFragment

class FragmentCountryList : BaseFragment() {
    override fun createView(context: Context): View {
        actionBar.setAddToContainer(false)

        val composeView = ComposeView(context)
        composeView.setContent {
            EventCountriesScreen(
                onBack = {},
                onDone = {}
            )
        }
        return composeView
    }

}