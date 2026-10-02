package org.telegram.divo.screen.reg_select_role.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.screen.reg_select_role.HiringType
import org.telegram.divo.screen.reg_select_role.QuizOption
import org.telegram.divo.screen.reg_select_role.RoleSelectionState
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

private val companyOptions = listOf(
    QuizOption(subRole = SubRole.MODELING_AGENCY, descriptionRes = R.string.SubRoleModelingAgencyDescription),
    QuizOption(subRole = SubRole.FASHION_BRAND, descriptionRes = R.string.SubRoleFashionBrandDescription),
    QuizOption(subRole = SubRole.BEAUTY_BRAND, descriptionRes = R.string.SubRoleBeautyBrandDescription),
    QuizOption(subRole = SubRole.BRAND_OR_BUSINESS, descriptionRes = R.string.SubRoleBrandOrBusinessDescription),
    QuizOption(subRole = SubRole.EVENT_AGENCY, descriptionRes = R.string.SubRoleEventAgencyDescription),
    QuizOption(subRole = SubRole.MAGAZINE, descriptionRes = R.string.SubRoleMagazineDescription),
)

private val individualOptions = listOf(
    QuizOption(subRole = SubRole.SCOUT, descriptionRes = R.string.SubRoleScoutDescription),
    QuizOption(subRole = SubRole.BOOKER, descriptionRes = R.string.SubRoleBookerDescription),
    QuizOption(subRole = SubRole.CASTING_DIRECTOR, descriptionRes = R.string.SubRoleCastingDirectorDescription),
    QuizOption(subRole = SubRole.TALENT_MANAGER, descriptionRes = R.string.SubRoleTalentManagerDescription),
)

@Composable
fun LookingForTalentPage(
    page: Int,
    state: RoleSelectionState,
    onHiringTypeSelected: (HiringType) -> Unit,
    onSubRoleSelected: (SubRole) -> Unit,
) {
    when (page) {
        0 -> HiringTypePage(
            selectedType = state.hiringType,
            onSelected = onHiringTypeSelected
        )
        1 -> when (state.hiringType) {
            HiringType.COMPANY -> CompanySubRolePage(
                selectedSubRole = state.subRole,
                onSelected = onSubRoleSelected
            )
            HiringType.INDIVIDUAL -> IndividualSubRolePage(
                selectedSubRole = state.subRole,
                onSelected = onSubRoleSelected
            )
            null -> Unit
        }
    }
}

@Composable
private fun HiringTypePage(
    selectedType: HiringType?,
    onSelected: (HiringType) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(32.dp))
        HeadText(
            title = stringResource(R.string.QuizHiringTypeTitle),
            subTitle = stringResource(R.string.QuizHiringTypeSubtitle)
        )
        Spacer(Modifier.height(24.dp))
        RoleCard(
            title = stringResource(R.string.QuizHiringTypeCompanyLabel),
            description = stringResource(R.string.QuizHiringTypeCompanyDescription),
            selected = selectedType == HiringType.COMPANY,
            onClick = { onSelected(HiringType.COMPANY) }
        )
        Spacer(Modifier.height(10.dp))
        RoleCard(
            title = stringResource(R.string.QuizHiringTypeIndividualLabel),
            description = stringResource(R.string.QuizHiringTypeIndividualDescription),
            selected = selectedType == HiringType.INDIVIDUAL,
            onClick = { onSelected(HiringType.INDIVIDUAL) }
        )
        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

@Composable
private fun CompanySubRolePage(
    selectedSubRole: SubRole?,
    onSelected: (SubRole) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        HeadText(
            title = stringResource(R.string.QuizCompanyTitle),
            subTitle = stringResource(R.string.QuizCompanySubtitle)
        )
        Spacer(Modifier.height(24.dp))
        companyOptions.forEach {
            RoleCard(
                title = stringResource(it.subRole.labelRes),
                description = stringResource(it.descriptionRes),
                selected = selectedSubRole == it.subRole,
                onClick = { onSelected(it.subRole) }
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

@Composable
private fun IndividualSubRolePage(
    selectedSubRole: SubRole?,
    onSelected: (SubRole) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        HeadText(
            title = stringResource(R.string.QuizIndustryTitle),
            subTitle = stringResource(R.string.QuizIndustrySubtitle)
        )
        Spacer(Modifier.height(24.dp))
        individualOptions.forEach {
            RoleCard(
                title = stringResource(it.subRole.labelRes),
                description = stringResource(it.descriptionRes),
                selected = selectedSubRole == it.subRole,
                onClick = { onSelected(it.subRole) }
            )
            Spacer(Modifier.height(10.dp))
        }
        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}