package org.telegram.divo.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val splashBgColor: Color = Color(0xFFDBFE01),
    val buttonColor: Color = Color(0xFFBF7A54),
    val backButton: Color = Color(0xFF343434),

    val buttonTextColor: Color = Color(0xFFFEFEFE),
    val textColor: Color = Color(0xFFFFFFFF),
    val textSubtitleColor: Color = Color(0x99FFFFFF),
    val textHintColor: Color = Color(0xFF8D8D8D),
    val containerDuration: Color = Color(0x4DFFFFFF),
    val switchTumb: Color = Color(0xff222222),
    val switchBackgroundChecked: Color = Color(0xFFBF7A54),
    val switchBackgroundUnChecked: Color = Color(0xffAFAFAF),


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

