package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AboutCard(
    text: String
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = AppTheme.colors.onBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 20.dp,
                    bottom = if (text.length > 100) 10.dp else 20.dp
                )
        ) {
            Text(
                text = text,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary,
                lineHeight = 16.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            if (text.length > 100) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(if (expanded) R.string.SeeLess else R.string.SeeMore),
                    color = AppTheme.colors.textPrimary,
                    style = AppTheme.typography.helveticaNeueLtCom,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickableWithoutRipple { expanded = !expanded }
                        .padding(top = 4.dp)
                )
            }
        }
    }
}