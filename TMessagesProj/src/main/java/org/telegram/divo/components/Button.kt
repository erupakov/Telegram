package org.telegram.divo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont
import org.telegram.messenger.R

@Preview
@Composable
private fun ButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UIButtonSmall()
        UIButton()
    }
}

@Composable
fun UIButtonSmall(
    text: String = "Apply",
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .height(24.dp)
            .width(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.buttonColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = AppTheme.typography.textButtonSmall,
                color = AppTheme.colors.buttonTextColor
            )
        }
    }
}


@Composable
fun UIButton(
    modifier: Modifier = Modifier,
    text: String = "Apply",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        enabled = enabled,
        modifier = modifier.height(52.dp),
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.buttonColor
        )
    ) {
        Text(
            text = text,
            style = AppTheme.typography.textButton,
            color = AppTheme.colors.buttonTextColor,
            fontFamily = DivoFont.HelveticaNeueLtCom77,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview
@Composable
fun UIButtonBack(
    modifier: Modifier = Modifier,
    text: String = "Apply",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier
            .height(52.dp)
            .defaultMinSize(92.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.backButton)
    ) {
        Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center){
            Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(28.dp),
                    tint = AppTheme.colors.textColor,
                    painter = painterResource(R.drawable.preview_back),
                    contentDescription = null)

                Text(
                    text = text,
                    style = AppTheme.typography.textButton,
                    color = AppTheme.colors.buttonTextColor,
                    fontFamily = DivoFont.HelveticaNeueLtCom77,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}




