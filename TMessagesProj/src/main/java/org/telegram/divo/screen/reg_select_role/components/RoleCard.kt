package org.telegram.divo.screen.reg_select_role.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme

@Composable
fun RoleCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) AppTheme.colors.accentOrange else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioCircle(selected = selected)

        Spacer(modifier = Modifier.width(14.dp))

        // Текст
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title.uppercase(),
                fontSize = 20.sp,
                style = AppTheme.typography.helveticaNeueLtCom,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary.copy(0.8f),
            )
        }
    }
}

@Composable
private fun RadioCircle(selected: Boolean) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(top = 3.dp)
            .size(18.dp)
            .clip(CircleShape)
            .then(
                if (selected) Modifier.background(AppTheme.colors.accentOrange)
                else Modifier.border(1.dp, Color.LightGray, CircleShape)
            )
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}