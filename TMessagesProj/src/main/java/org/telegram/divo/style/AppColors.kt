package org.telegram.divo.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val splashBgColor: Color = Color(0xFFDBFE01),
    val accentOrange: Color = Color(0xFFFF772D),
    val buttonSecondary: Color = Color(0xFF343434),

    val buttonTextColor: Color = Color(0xFFFEFEFE),
    val textColor: Color = Color(0xFFFFFFFF),
    val textPrimary: Color = Color(0xFF222222),
    val textSubtitleColor: Color = Color(0x99FFFFFF),
    val textHintColor: Color = Color(0xFF8D8D8D),
    val containerDuration: Color = Color(0x4DFFFFFF),
    val switchTumb: Color = Color(0xff222222),
    val switchBackgroundUnChecked: Color = Color(0xffAFAFAF),

    val blackAlpha12: Color = Color(0x1F000000),
    val textLabelDark: Color = Color(0x80FFFFFF),

    val backgroundLight: Color = Color(0xFFF0F0F0),
    val backgroundDark: Color = Color(0xFF222222),
    val onBackground: Color = Color(0xFFFFFFFF)
)

val MyLocalCustomColors = staticCompositionLocalOf {
    AppColors(
        splashBgColor = Color(0xFFDBFE01),
    )
}

val DarkColorPalette = AppColors(
    splashBgColor = Color(0xFFBDBB99),
)

val LightColorPalette = AppColors(
    splashBgColor = Color(0xFFDCDBC9),
)

