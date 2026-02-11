package org.telegram.divo.screen.onboarding

import androidx.annotation.DrawableRes

data class OnboardPage(
    @DrawableRes val imageRes: Int,
    val subtitle: String
)
