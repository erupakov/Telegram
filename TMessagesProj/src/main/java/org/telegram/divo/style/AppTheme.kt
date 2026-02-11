package org.telegram.divo.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun AppTheme (
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    typography: AppTypography = LocalCustomTypography.current,
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) DarkColorPalette else LightColorPalette

    CompositionLocalProvider(
        MyLocalCustomColors provides colors,
        LocalCustomTypography provides typography,
        content = content
    )
}

object AppTheme {
    val colors: AppColors
        @Composable
        get() = MyLocalCustomColors.current

    val typography: AppTypography
        @Composable
        get() = LocalCustomTypography.current
}