package org.telegram.divo.components.inputs

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.media.LottieProgressIndicator
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoFont
import org.telegram.messenger.R

@Composable
fun UIButton(
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
    isLoading: Boolean = false,
    leadingIcon: Int? = null,
    leadingIconSize: Int = 12,
    leadingIconTint: Color = LocalContentColor.current,
    onClick: () -> Unit = {},
) {
    Button(
        enabled = enabled,
        modifier = modifier
            .height(height),
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            disabledContainerColor = background.copy(0.7f)
        ),
    ) {
        if (!isLoading) {
            leadingIcon?.let {
                Icon(
                    modifier = Modifier.size(leadingIconSize.dp),
                    painter = painterResource(leadingIcon),
                    tint = leadingIconTint,
                    contentDescription = null,
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                modifier = Modifier.padding(top = paddingTop),
                text = text,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            LottieProgressIndicator(
                color = Color.White
            )
        }
    }
}

@Composable
fun RoundedGlassButton(
    modifier: Modifier = Modifier,
    @DrawableRes resId: Int = R.drawable.ic_divo_back,
    shadowEnabled: Boolean = true,
    borderColor: Color = AppTheme.colors.onBackground.copy(alpha = 0.4f),
    background: Color = AppTheme.colors.onBackground.copy(alpha = 0.2f),
    iconTint: Color = AppTheme.colors.onBackground,
    iconSize: Dp = 16.dp,
    onClick: () -> Unit = {},
) {
    RoundedButton(
        modifier = modifier,
        resId = resId,
        shadowEnabled = shadowEnabled,
        borderColor = borderColor,
        background = background,
        iconTint = iconTint,
        iconSize = iconSize,
        onClick = onClick
    )
}

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

@Preview
@Composable
private fun ButtonPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        UIButton()
        RoundedButton()
        RoundedGlassButton()
    }
}




