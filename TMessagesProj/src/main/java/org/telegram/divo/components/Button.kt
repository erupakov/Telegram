package org.telegram.divo.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
        backgroundColor = AppTheme.colors.buttonColor
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
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
        modifier = modifier .height(52.dp),
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = AppTheme.colors.buttonColor
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

@Composable
fun UIButtonBack(
    modifier: Modifier = Modifier,
    text: String = "Apply",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color(0xFF2F2F2F),
        ),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier .height(52.dp)
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




