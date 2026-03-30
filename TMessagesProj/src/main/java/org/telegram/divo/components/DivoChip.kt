package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme

@Composable
fun DivoChip(
    modifier: Modifier = Modifier,
    text: String,
    @DrawableRes resId: Int,
    background: Color,
    textColor: Color,
    iconSize: Dp = 16.dp,
    border: Dp = 0.dp
) {
    val borderModifier = Modifier.then(
        if (border > 0.dp) {
            Modifier.border(border, AppTheme.colors.backgroundLight, RoundedCornerShape(24.dp))
        } else {
            Modifier
        }
    )

    Row(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .then(borderModifier)
            .background(background)
            .padding(start = 6.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(resId),
            modifier = Modifier.size(iconSize),
            tint = textColor,
            contentDescription = null
        )
        Spacer(Modifier.width(2.dp))
        Text(
            modifier = Modifier.padding(top = 1.dp),
            text = text,
            style = AppTheme.typography.helveticaNeueRegular,
            fontSize = 11.sp,
            color = textColor
        )
    }
}