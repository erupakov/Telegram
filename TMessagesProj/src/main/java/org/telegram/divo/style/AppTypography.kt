package org.telegram.divo.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.telegram.messenger.R

object DivoFont{
    val HelveticaNeueLtCom77 = FontFamily(
        Font(
            resId = R.font.helvetica_neue_lt_com_77_bold_condensed,
            weight = FontWeight.Bold
        )
    )

    val HelveticaNeue = FontFamily(
        Font(
            resId = R.font.helvetica_neue_medium,
        )
    )
}



@Immutable
data class AppTypography(
    val title1: TextStyle,
    val textButtonSmall: TextStyle,
    val textButton: TextStyle,
    val textEventTitle: TextStyle,
    val textItemDate: TextStyle,
    val helvetica_neue_regular_10: TextStyle
)

val LocalCustomTypography = staticCompositionLocalOf {
    AppTypography(
        title1 = TextStyle(
            fontSize = 20.sp,
            letterSpacing = (-4).sp,
            lineHeight = 28.8.sp,
        ),
        textButtonSmall = TextStyle(
            fontSize = 11.sp,

            ),
        textButton = TextStyle(
            fontSize = 20.sp,

            ),
        textEventTitle = TextStyle(
            fontSize = 16.sp,
        ),
        textItemDate = TextStyle(
            fontSize = 10.sp,
        ),
        helvetica_neue_regular_10 = TextStyle(
            fontSize = 10.sp,
        )
    )
}
