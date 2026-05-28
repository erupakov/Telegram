package org.telegram.divo.screen.reg_select_role.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.reg_select_role.QuizOption
import org.telegram.divo.screen.reg_select_role.RoleSelectionState
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

private val talentOptions = listOf(
    QuizOption(R.drawable.ic_divo_quiz_model, SubRole.MODEL),
    QuizOption(R.drawable.ic_divo_quiz_actor, SubRole.ACTOR),
    QuizOption(R.drawable.ic_divo_quiz_dancer, SubRole.DANCER),
    QuizOption(R.drawable.ic_divo_quiz_singer, SubRole.SINGER)
)

private val creativeOptions = listOf(
    QuizOption(R.drawable.ic_divo_quiz_photographer, SubRole.PHOTOGRAPHER),
    QuizOption(R.drawable.ic_divo_quiz_stylist, SubRole.STYLIST),
    QuizOption(R.drawable.ic_divo_quiz_mua, SubRole.MUA),
    QuizOption(R.drawable.ic_divo_quiz_hair_stylist, SubRole.HAIR_STYLIST),
    QuizOption(R.drawable.ic_divo_quiz_videographer, SubRole.VIDEOGRAPHER),
    QuizOption(R.drawable.ic_divo_quiz_creative_director, SubRole.CREATIVE_DIRECTOR),
    QuizOption(R.drawable.ic_divo_quiz_fashion_designer, SubRole.FASHION_DESIGNER),
    QuizOption(R.drawable.ic_divo_quiz_studio, SubRole.STUDIO),
)

@Composable
fun GetHiredPage(
    page: Int,
    state: RoleSelectionState,
    onSubRoleSelected: (SubRole) -> Unit,
    onExperienceSelected: (Boolean) -> Unit,
) {
    when (page) {
        0 -> TalentSubRolePickerPage(
            selectedSubRole = state.subRole,
            onSubRoleSelected = onSubRoleSelected
        )
        1 -> ModelExperiencePage(
            hasExperience = state.hasModelingExperience,
            onSelected = onExperienceSelected
        )
    }
}

@Composable
private fun TalentSubRolePickerPage(
    selectedSubRole: SubRole? = null,
    onSubRoleSelected: (SubRole) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(8.dp))
        HeadText(
            title = stringResource(R.string.QuizGetHiredTitle),
            subTitle = stringResource(R.string.QuizGetHiredSubtitle)
        )
        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.QuizSectionTalents),
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.textPrimary.copy(0.8f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        talentOptions.forEach { option ->
            PickerItem(
                quizOption = option,
                selected = option.subRole == selectedSubRole,
                onClick = { onSubRoleSelected(option.subRole) }
            )
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.QuizSectionCreative),
            style = AppTheme.typography.helveticaNeueRegular,
            color = AppTheme.colors.textPrimary.copy(0.8f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(10.dp))

        creativeOptions.forEach { option ->
            PickerItem(
                quizOption = option,
                selected = option.subRole == selectedSubRole,
                onClick = { onSubRoleSelected(option.subRole) }
            )
            Spacer(Modifier.height(8.dp))
        }
        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

@Composable
private fun ModelExperiencePage(
    hasExperience: Boolean?,
    onSelected: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        HeadText(
            title = stringResource(R.string.QuizModelExperienceTitle),
            subTitle = stringResource(R.string.QuizModelExperienceSubtitle)
        )
        Spacer(Modifier.height(24.dp))
        RoleCard(
            title = stringResource(R.string.QuizModelExperienceYesLabel),
            description = stringResource(R.string.QuizModelExperienceYesDescription),
            selected = hasExperience == true,
            onClick = { onSelected(true) }
        )
        Spacer(Modifier.height(10.dp))
        RoleCard(
            title = stringResource(R.string.QuizModelExperienceNoLabel),
            description = stringResource(R.string.QuizModelExperienceNoDescription),
            selected = hasExperience == false,
            onClick = { onSelected(false) }
        )
        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

@Composable
private fun PickerItem(
    quizOption: QuizOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) AppTheme.colors.accentOrange else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .clickableWithoutRipple { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(quizOption.iconRes),
            tint = if (selected) AppTheme.colors.accentOrange else AppTheme.colors.textPrimary,
            contentDescription = null
        )
        Spacer(Modifier.width(12.dp))
        Text(
            modifier = Modifier.offset(y = 2.dp),
            text = stringResource(quizOption.subRole.labelRes).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 14.sp,
            color = if (selected) AppTheme.colors.accentOrange else AppTheme.colors.textPrimary,
        )
    }
}
