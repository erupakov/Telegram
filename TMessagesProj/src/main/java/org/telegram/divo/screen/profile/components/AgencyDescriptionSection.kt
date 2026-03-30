package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AgencyDescriptionSection(
    text: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.blackAlpha12)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 10.dp),
    ) {
        Text(
            text = text,
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )

        if (text.length > 100) {
            val textButton = if (expanded) R.string.SeeLess else R.string.SeeMore
            Text(
                text = stringResource(textButton),
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickableWithoutRipple { expanded = !expanded }
                    .padding(top = 2.dp)
            )
        }
    }
}