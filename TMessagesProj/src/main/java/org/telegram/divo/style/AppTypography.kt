package org.telegram.divo.style

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.DivoFont.HelveticaNeue
import org.telegram.divo.style.DivoFont.HelveticaNeueLtCom77
import org.telegram.divo.style.DivoFont.ManropeRegular
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

    val ManropeRegular = FontFamily(
        Font(
            resId = R.font.manrope_regular
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
    val manropeRegular: TextStyle,
    val displayLarge: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
)

val LocalCustomTypography = staticCompositionLocalOf {
    AppTypography(
        title1 = TextStyle(
            fontSize = 20.sp,
            letterSpacing = (-4).sp,
            lineHeight = 28.8.sp,
        ),
        appBar = TextStyle(
            fontFamily = HelveticaNeueLtCom77,
            fontSize = 20.sp,
            color = Color.Black,
        ),
        textButtonSmall = TextStyle(
            fontFamily = HelveticaNeueLtCom77,
            fontSize = 12.sp,
        ),
        textButton = TextStyle(
            fontSize = 20.sp,
            lineHeight = 20.sp
        ),
        textEventTitle = TextStyle(
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontFamily = HelveticaNeueLtCom77
        ),
        textItemDate = TextStyle(
            fontSize = 10.sp,
        ),
        helveticaNeueLtCom = TextStyle(
            fontFamily = HelveticaNeueLtCom77,
            fontWeight = FontWeight.Normal,
            color = Color.Black
        ),
        helveticaNeueRegular = TextStyle(
            fontFamily = HelveticaNeue,
            fontWeight = FontWeight.Normal,
        ),
        manropeRegular = TextStyle(
            fontFamily = ManropeRegular
        ),
        displayLarge = TextStyle(
            fontFamily = HelveticaNeueLtCom77,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyLarge = TextStyle(
            fontFamily = HelveticaNeue,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 21.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = HelveticaNeue,
            fontSize = 14.sp,
            lineHeight = 18.sp
        )
    )
}
