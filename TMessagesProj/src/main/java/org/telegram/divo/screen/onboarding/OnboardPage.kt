package org.telegram.divo.screen.onboarding

import androidx.annotation.DrawableRes

data class OnboardPage(
    @DrawableRes val imageRes: Int,
    val titleRes: Int,
    val subtitleRes: Int
)
