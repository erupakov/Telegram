package org.telegram.divo.screen.profile.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.screen.profile.PhysicalParams
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun BiographyContent(bio: String) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }
    var isFirstLayout by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isFirstLayout) Modifier
                else Modifier.animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = if (bio.isEmpty()) 20.dp else 10.dp
            )
    ) {
        if (bio.isEmpty()) {
            ProfileInfoEmptyContent(
                R.drawable.ic_divo_bio_bage,
                stringResource(R.string.NotBio),
            )
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bio,
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.textPrimary,
                fontSize = if (bio.isEmpty()) 16.sp else 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.4.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { result ->
                    if (!expanded) {
                        isOverflowing = result.hasVisualOverflow
                        isFirstLayout = false
                    }
                },
                textAlign = if (bio.isEmpty()) TextAlign.Center else TextAlign.Start
            )

            if (isOverflowing || expanded) {
                Text(
                    text = stringResource(if (expanded) R.string.SeeLess else R.string.SeeMore),
                    color = AppTheme.colors.textPrimary,
                    fontSize = 11.sp,
                    style = AppTheme.typography.helveticaNeueLtCom,
                    modifier = Modifier
                        .align(Alignment.End)
                        .clickableWithoutRipple { expanded = !expanded }
                        .padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun AppearanceContent(
    params: PhysicalParams,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val allAvailableItems = buildList {
        if (params.gender.isNotEmpty()) add(stringResource(R.string.LabelGender) to params.gender)
        if (params.age.isNotEmpty()) add(stringResource(R.string.LabelAge) to params.age.formattedAge(context))
        if (params.height > 0) add(stringResource(R.string.LabelHeight) to params.height.toString())
        if (params.waist > 0) add(stringResource(R.string.LabelWaist) to params.waist.toString())
        if (params.hips > 0) add(stringResource(R.string.LabelHips) to params.hips.toString())
        if (params.shoeSize > 0) add(stringResource(R.string.LabelShoeSize) to params.shoeSize.toString())
        if (params.hairLength.isNotEmpty() && params.hairLength != "0") add(stringResource(R.string.LabelHairLength) to params.hairLength)
        if (params.hairColor.isNotEmpty()) add(stringResource(R.string.LabelHairColor) to params.hairColor)
        if (params.eyeColor.isNotEmpty()) add(stringResource(R.string.LabelEyeColor) to params.eyeColor)
        if (params.skinColor.isNotEmpty()) add(stringResource(R.string.LabelSkinColor) to params.skinColor)
        if (params.breastSize.isNotEmpty() && params.gender == stringResource(R.string.Female)) {
            add(stringResource(R.string.LabelBreastSize) to params.breastSize)
        }
    }

    val visibleItems = if (expanded) allAvailableItems else allAvailableItems.take(4)

    val leftItems = visibleItems.filterIndexed { index, _ -> index % 2 == 0 }
    val rightItems = visibleItems.filterIndexed { index, _ -> index % 2 != 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    leftItems.forEach { (label, value) ->
                        AppearanceRow(label, value)
                    }
                }

                Spacer(modifier = Modifier.width(22.dp))

                Column(modifier = Modifier.weight(1f)) {
                    rightItems.forEach { (label, value) ->
                        AppearanceRow(label, value)
                    }
                }
            }

            if (allAvailableItems.size > 4) {
                Text(
                    text = stringResource(if (expanded) R.string.SeeLess else R.string.SeeMore),
                    style = AppTheme.typography.helveticaNeueLtCom,
                    color = AppTheme.colors.textPrimary,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .clickableWithoutRipple { expanded = !expanded }
                        .padding(top = 1.dp, bottom = 10.dp)
                )
            }
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
            color = AppTheme.colors.textPrimary.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppTheme.colors.textPrimary.copy(alpha = 0.2f))
    )
}

@Composable
fun ProfileInfoEmptyContent(
    iconResId: Int,
    text: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(iconResId),
                contentDescription = null,
                tint = AppTheme.colors.textPrimary.copy(0.8f)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = text.uppercase(),
            color = AppTheme.colors.textPrimary,
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}