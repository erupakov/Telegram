package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun PromoCard(
    headline: String,
    body: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.img_divo_settings_item_cover),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(110.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xCC000000), Color.Transparent),
                            startY = 400f, endY = 0f
                        )
                    )
            )
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(4.dp))
                Image(
                    painter = painterResource(R.drawable.divo_logo_onboarding),
                    contentDescription = null,
                    modifier = Modifier.height(30.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    text = headline,
                    style = AppTheme.typography.helveticaNeueLtCom.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = AppTheme.typography.helveticaNeueLtCom.copy(
                        color = Color(0xFFEDEDED)
                    ),
                    maxLines = 2,
                    lineHeight = 14.sp,
                    fontSize = 12.sp,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                TextButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(99.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.Red,
                    )
                ) {
                    Text(
                        modifier = Modifier.offset(y = 1.dp),
                        text = buttonText,
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 16.sp,
                        color = AppTheme.colors.buttonTextColor
                    )
                }
            }
        }
    }
}