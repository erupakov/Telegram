package org.telegram.divo.screen.reg_select_role.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.StatusBarIconColorEffect
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.reg_select_role.RoleSelectionEffect
import org.telegram.divo.screen.reg_select_role.RoleSelectionIntent
import org.telegram.divo.screen.reg_select_role.RoleSelectionViewModel
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun QuizResultScreen(
    viewModel: RoleSelectionViewModel = viewModel(),
    subRole: SubRole,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    StatusBarIconColorEffect(false)

    val content = roleResultContentMap[subRole] ?: return

    LaunchedEffect(Unit) {
        viewModel.effect.collect {
            if (it is RoleSelectionEffect.NavigateBack) onBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(content.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(content.title).uppercase(),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 32.sp,
                color = AppTheme.colors.textColor
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(content.description),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textColor
            )
            Spacer(Modifier.height(32.dp))
            UIButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.RoleResultSoundsRight),
                onClick = onContinue
            )
            Spacer(Modifier.height(16.dp))
            UIButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.RoleResultChooseDifferentRole),
                background = AppTheme.colors.buttonSecondary,
                onClick = { viewModel.setIntent(RoleSelectionIntent.OnChangeDifferentRole) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

data class RoleResultContent(
    @DrawableRes val background: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
)

private val roleResultContentMap = mapOf(
    // Talent
    SubRole.MODEL to RoleResultContent(
        background = R.drawable.divo_result_model,
        title = R.string.RoleResultTitleModel,
        description = R.string.RoleResultDescriptionModel
    ),
    SubRole.NEW_TALENT to RoleResultContent(
        background = R.drawable.divo_result_new_talent,
        title = R.string.RoleResultTitleRisingTalent,
        description = R.string.RoleResultDescriptionRisingTalent
    ),
    SubRole.ACTOR to RoleResultContent(
        background = R.drawable.divo_result_actor,
        title = R.string.RoleResultTitleActor,
        description = R.string.RoleResultDescriptionActor
    ),
    SubRole.DANCER to RoleResultContent(
        background = R.drawable.divo_result_dancer,
        title = R.string.RoleResultTitleDancer,
        description = R.string.RoleResultDescriptionDancer
    ),
    SubRole.SINGER to RoleResultContent(
        background = R.drawable.divo_result_singer,
        title = R.string.RoleResultTitlePerformer,
        description = R.string.RoleResultDescriptionPerformer
    ),
    // Creative — один экран на всех
    SubRole.PHOTOGRAPHER to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.STYLIST to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.MUA to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.HAIR_STYLIST to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.VIDEOGRAPHER to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.CREATIVE_DIRECTOR to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),
    SubRole.FASHION_DESIGNER to RoleResultContent(
        background = R.drawable.divo_result_creative,
        title = R.string.RoleResultTitleCreativeProfessional,
        description = R.string.RoleResultDescriptionCreativeProfessional
    ),

    // Companies — один экран
    SubRole.MODELING_AGENCY to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),
    SubRole.FASHION_BRAND to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),
    SubRole.BEAUTY_BRAND to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),
    SubRole.BRAND_OR_BUSINESS to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),
    SubRole.EVENT_AGENCY to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),
    SubRole.MAGAZINE to RoleResultContent(
        background = R.drawable.divo_result_hiring,
        title = R.string.RoleResultTitleFindTalent,
        description = R.string.RoleResultDescriptionFindTalent
    ),

    // Industry Pros — один экран
    SubRole.SCOUT to RoleResultContent(
        background = R.drawable.divo_result_industry,
        title = R.string.RoleResultTitleIndustryProfessional,
        description = R.string.RoleResultDescriptionIndustryProfessional
    ),
    SubRole.BOOKER to RoleResultContent(
        background = R.drawable.divo_result_industry,
        title = R.string.RoleResultTitleIndustryProfessional,
        description = R.string.RoleResultDescriptionIndustryProfessional
    ),
    SubRole.CASTING_DIRECTOR to RoleResultContent(
        background = R.drawable.divo_result_industry,
        title = R.string.RoleResultTitleIndustryProfessional,
        description = R.string.RoleResultDescriptionIndustryProfessional
    ),
    SubRole.TALENT_MANAGER to RoleResultContent(
        background = R.drawable.divo_result_industry,
        title = R.string.RoleResultTitleIndustryProfessional,
        description = R.string.RoleResultDescriptionIndustryProfessional
    ),

    // Fan
    SubRole.FAN to RoleResultContent(
        background = R.drawable.divo_fan_result,
        title = R.string.RoleResultTitleFashionFan,
        description = R.string.RoleResultDescriptionFashionFan
    ),
)