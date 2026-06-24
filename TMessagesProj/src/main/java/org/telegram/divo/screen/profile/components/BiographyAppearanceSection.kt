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
import org.telegram.divo.common.MeasuringUnits
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.labelRes
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.profile.PhysicalParams
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun BiographyContent(
    isOwnProfile: Boolean,
    bio: String,
    onEditClick: () -> Unit
) {
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
                bottom = if (bio.isEmpty() || !isOverflowing) 20.dp else 10.dp
            )
    ) {
        if (bio.isEmpty()) {
            ProfileInfoEmptyContent(
                iconResId = R.drawable.ic_divo_bio_bage,
                isOwnProfile = isOwnProfile,
                text = if (isOwnProfile) stringResource(R.string.ThereAreNoBioAddedYet) else stringResource(R.string.NotBio),
                textButton = stringResource(R.string.FillInBio),
                onEditClick = onEditClick
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
    isOwnProfile: Boolean,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val storedSystem = params.measuringSystem
    
    val isAnyFilled = params.gender.isNotEmpty() || params.age.isNotEmpty() || params.height > 0 || 
        params.weight > 0 || params.waist > 0 || params.hips > 0 || params.shoeSize > 0 || 
        (params.hairLength.isNotEmpty() && params.hairLength != "0") || 
        params.hairColor.isNotEmpty() || params.eyeColor.isNotEmpty() || params.skinColor.isNotEmpty() || params.breastSize.isNotEmpty()

    val allAvailableItems = if (!isAnyFilled) emptyList() else buildList {
        add(stringResource(R.string.LabelGender) to (params.gender.takeIf { it.isNotEmpty() } ?: "-"))
        add(stringResource(R.string.LabelAge) to (params.age.takeIf { it.isNotEmpty() }?.formattedAge(context) ?: "-"))
        add(
            stringResource(ParametersType.HEIGHT.labelRes()) to
                if (params.height > 0) MeasuringUnits.formatStoredNumber(ParametersType.HEIGHT, params.height, storedSystem) else "-"
        )
        add(
            stringResource(ParametersType.WEIGHT.labelRes()) to
                if (params.weight > 0) MeasuringUnits.formatStoredNumber(ParametersType.WEIGHT, params.weight, storedSystem) else "-"
        )
        add(
            stringResource(ParametersType.WAIST.labelRes()) to
                if (params.waist > 0) MeasuringUnits.formatStoredNumber(ParametersType.WAIST, params.waist, storedSystem) else "-"
        )
        add(
            stringResource(ParametersType.HIPS.labelRes()) to
                if (params.hips > 0) MeasuringUnits.formatStoredNumber(ParametersType.HIPS, params.hips, storedSystem) else "-"
        )
        add(
            stringResource(ParametersType.SHOE_SIZE.labelRes()) to
                if (params.shoeSize > 0) MeasuringUnits.formatStoredNumber(ParametersType.SHOE_SIZE, params.shoeSize, storedSystem) else "-"
        )
        add(stringResource(R.string.LabelHairLength) to if (params.hairLength.isNotEmpty() && params.hairLength != "0") params.hairLength else "-")
        add(stringResource(R.string.LabelHairColor) to (params.hairColor.takeIf { it.isNotEmpty() } ?: "-"))
        add(stringResource(R.string.LabelEyeColor) to (params.eyeColor.takeIf { it.isNotEmpty() } ?: "-"))
        add(stringResource(R.string.LabelSkinColor) to (params.skinColor.takeIf { it.isNotEmpty() } ?: "-"))
        
        if (params.gender.equals(stringResource(R.string.Female), ignoreCase = true) || params.gender.equals("female", ignoreCase = true)) {
            add(
                stringResource(ParametersType.BREAST_SIZE.labelRes()) to
                    if (params.breastSize.isNotEmpty()) MeasuringUnits.formatStoredValue(ParametersType.BREAST_SIZE, params.breastSize, storedSystem) else "-"
            )
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
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = if (allAvailableItems.isEmpty()) 16.dp else 10.dp,
                bottom = if (allAvailableItems.isEmpty()) 16.dp else 0.dp
            )
    ) {
        if (allAvailableItems.isEmpty()) {
            ProfileInfoEmptyContent(
                iconResId = R.drawable.ic_divo_appearance_bage,
                isOwnProfile = isOwnProfile,
                text = if (isOwnProfile) stringResource(R.string.ThereAreNoYourParametersYet) else stringResource(R.string.NotAppearance),
                textButton = stringResource(R.string.AddYourParameters),
                onEditClick = onEditClick
            )
        } else {
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
    isOwnProfile: Boolean,
    text: String,
    textButton: String,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isOwnProfile) {
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
        }

        Text(
            text = text.uppercase(),
            color = AppTheme.colors.textPrimary,
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        if (isOwnProfile) {
            Spacer(Modifier.height(10.dp))
            UIButtonNew(
                text = textButton,
                height = 36.dp,
                paddingTop = 1.dp,
                leadingIcon = R.drawable.ic_divo_plus,
                leadingIconTint = AppTheme.colors.onBackground,
                textStyle = AppTheme.typography.helveticaNeueLtCom.copy(
                    fontSize = 14.sp,
                    color = AppTheme.colors.onBackground
                ),
                onClick = onEditClick
            )
        }
    }
}