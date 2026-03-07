package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.profile.PhysicalParams
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.UserConfig

@Composable
fun BiographyAppearanceSection(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    uiState: ProfileViewState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(AppTheme.colors.blackAlpha12)
    ) {
        // Tab buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.12f)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabButton(
                text = "BIOGRAPHY",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "APPEARANCE",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }

        // Tab content
        when (selectedTab) {
            0 -> BiographyContent(bio = uiState.userInfo?.model?.agency?.description.orEmpty())
            1 -> AppearanceContent(params = uiState.physicalParams)
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp, 6.dp))
            .clickableWithoutRipple { onClick() }
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(bottom = 7.dp),
                text = text,
                style = AppTheme.typography.helveticaNeueLtCom,
                color = Color.White.copy(if (isSelected) 1f else 0.6f),
                fontSize = 10.sp,
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(4.dp, 4.dp))
                        .background(Color.White)
                )
            }
        }
    }
}

@Composable
private fun BiographyContent(bio: String) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = bio.ifEmpty { "No biography available" },
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis
        )

        if (bio.length > 100) {
            Text(
                text = if (expanded) "SEE LESS" else "SEE MORE",
                color = Color.White,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickableWithoutRipple { expanded = !expanded }
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun AppearanceContent(params: PhysicalParams) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
        .fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left column
                Column(modifier = Modifier.weight(1f)) {
                    if (params.gender.isNotEmpty()) {
                        AppearanceRow("Gender", params.gender)
                    }
                    if (params.height > 0) {
                        AppearanceRow("Height (cm)", params.height.toString())
                    }
                    if (params.hips > 0) {
                        AppearanceRow("Hips (cm)", params.hips.toString())
                    }
                    if (params.hairLength > "0" || expanded) {
                        AppearanceRow("Hair length (cm)", params.hairLength.toString())
                    }
                    if (params.eyeColor.isNotEmpty()) {
                        AppearanceRow("Eye color", params.eyeColor)
                    }
                    if (params.breastSize.isNotEmpty() && params.gender == "Female") {
                        AppearanceRow("Breast size", params.breastSize)
                    }
                }
                Spacer(modifier = Modifier.width(32.dp))
                // Right column
                Column(modifier = Modifier.weight(1f)) {
                    if (params.age.isNotEmpty()) {
                        AppearanceRow("Age (y.o)", params.age.toString())
                    }
                    if (params.waist > 0) {
                        AppearanceRow("Waist (cm)", params.waist.toString())
                    }
                    if (params.shoeSize > 0) {
                        AppearanceRow("Shoe size (EU)", params.shoeSize.toString())
                    }
                    if (params.hairColor.isNotEmpty()) {
                        AppearanceRow("Hair color", params.hairColor)
                    }
                    if (params.skinColor.isNotEmpty()) {
                        AppearanceRow("Skin color", params.skinColor)
                    }
                }
            }

            Text(
                text = "SEE MORE",
                style = AppTheme.typography.helveticaNeueLtCom,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickableWithoutRipple { expanded = !expanded }
                    .padding(top = 4.dp, end = 16.dp, bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun AppearanceRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = AppTheme.typography.helveticaNeueRegular,
            color = Color.White,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.6f))
    )
}