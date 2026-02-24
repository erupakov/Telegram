package org.telegram.divo.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.DivoFont.HelveticaNeue
import org.telegram.divo.style.DivoFont.HelveticaNeueLtCom77
import org.telegram.messenger.R

object DivoFont {
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
    val appBar:TextStyle,
    val textButtonSmall: TextStyle,
    val textButton: TextStyle,
    val textEventTitle: TextStyle,
    val textItemDate: TextStyle,
    val helveticaNeueLtCom: TextStyle,
    val helveticaNeueRegular: TextStyle,
)

val LocalCustomTypography = staticCompositionLocalOf {
    AppTypography(
        title1 = TextStyle(
            fontSize = 20.sp,
            letterSpacing = (-4).sp,
            lineHeight = 28.8.sp,
        ),
        appBar = TextStyle(
            letterSpacing = (-4).sp,
            fontSize = 34.sp,
        ),
        textButtonSmall = TextStyle(
            fontSize = 11.sp,
        ),
        textButton = TextStyle(
            fontSize = 20.sp,
            lineHeight = 20.sp
        ),
        textEventTitle = TextStyle(
            fontSize = 16.sp,
            fontFamily = HelveticaNeueLtCom77
        ),
        textItemDate = TextStyle(
            fontSize = 10.sp,
        ),
        helveticaNeueLtCom = TextStyle(
            fontFamily = HelveticaNeueLtCom77,
            fontWeight = FontWeight.Normal
        ),
        helveticaNeueRegular = TextStyle(
            fontFamily = HelveticaNeue,
            fontWeight = FontWeight.Normal,
        ),
    )
}
