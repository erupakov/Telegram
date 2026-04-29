package org.telegram.divo.screen.profile.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun AgencyDescriptionSection(
    text: String,
    isOwnProfile: Boolean,
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
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 20.dp,
                bottom = if (text.isEmpty() || !isOverflowing) 20.dp else 10.dp
            )
    ) {
        if (text.isEmpty()) {
            ProfileInfoEmptyContent(
                iconResId = R.drawable.ic_divo_bio_bage,
                isOwnProfile = isOwnProfile,
                text = if (isOwnProfile) stringResource(R.string.ThereAreNoBioAddedYet) else stringResource(R.string.NotAgencyBio),
                textButton = stringResource(R.string.FillInBio),
                onEditClick = onEditClick
            )
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                style = AppTheme.typography.helveticaNeueRegular,
                color = AppTheme.colors.textPrimary,
                fontSize = if (text.isEmpty()) 16.sp else 14.sp,
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
                textAlign = if (text.isEmpty()) TextAlign.Center else TextAlign.Start
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