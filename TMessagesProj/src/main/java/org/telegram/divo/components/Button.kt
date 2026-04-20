package org.telegram.divo.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont
import org.telegram.messenger.R

@Preview
@Composable
private fun ButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UIButtonSmall()
        UIButton()
        UIButtonNew()
    }
}

@Composable
fun UIButtonSmall(
    text: String = stringResource(R.string.ButtonApply),
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .height(24.dp)
            .width(48.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.accentOrange)
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
    text: String = stringResource(R.string.ButtonApply),
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        enabled = enabled,
        modifier = modifier.height(52.dp),
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colors.accentOrange
        )
    ) {
        Text(
            modifier = Modifier.padding(top = 1.dp),
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
fun UIButtonNew(
    modifier: Modifier = Modifier,
    text: String = stringResource(R.string.ButtonSave),
    height: Dp = 56.dp,
    textStyle: TextStyle = AppTheme.typography.textButton.copy(
        color = AppTheme.colors.buttonTextColor,
        fontFamily = DivoFont.HelveticaNeueLtCom77,
        fontWeight = FontWeight.Bold,
    ),
    shape: Shape = RoundedCornerShape(99.dp),
    background: Color = AppTheme.colors.accentOrange,
    paddingTop: Dp = 3.dp,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Button(
        enabled = enabled,
        modifier = modifier
            .height(height),
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = background
        ),
    ) {
        Text(
            modifier = Modifier.padding(top = paddingTop),
            text = text,
            style = textStyle,
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
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.buttonSecondary)
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

@Preview
@Composable
fun RoundedGlassButton(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int = R.drawable.ic_divo_back,
    shadowEnabled: Boolean = true,
    borderColor: Color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
    background: Color = AppTheme.colors.onBackground.copy(alpha = 0.2f),
    iconTint: Color = AppTheme.colors.onBackground,
    onClick: () -> Unit = {},
) {
    RoundedButton(
        modifier = modifier,
        resId = resId,
        shadowEnabled = shadowEnabled,
        borderColor = borderColor,
        background = background,
        iconTint = iconTint,
        onClick = onClick
    )
}

@Preview
@Composable
fun RoundedButton(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int = R.drawable.ic_divo_back,
    iconSize: Dp = 16.dp,
    paddingEnd: Dp = 0.dp,
    shadowEnabled: Boolean = true,
    borderColor: Color = Color.Unspecified,
    background: Color = AppTheme.colors.onBackground,
    iconTint: Color = AppTheme.colors.textPrimary,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .then(
                if (shadowEnabled) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.1f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .then(
                if (borderColor.isSpecified) {
                    Modifier.border(0.5.dp, borderColor, CircleShape)
                } else Modifier
            )
            .background(background)
            .clickableWithoutRipple(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier
                .size(iconSize)
                .padding(end = if (resId == R.drawable.ic_divo_back) 2.dp else paddingEnd),
            painter = painterResource(resId),
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Composable
fun RoundedGlassContainer(
    modifier: Modifier = Modifier,
    space: Dp = 0.dp,
    height: Dp = 40.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp),
    borderColor: Color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
    background: Color = AppTheme.colors.onBackground.copy(alpha = 0.2f),
    content: @Composable (RowScope.() -> Unit)
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .border(0.5.dp, borderColor, CircleShape)
            .background(background)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space)
    ) {
        content()
    }
}





