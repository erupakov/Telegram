package org.telegram.divo.screen.reg_form.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.RegTextField
import org.telegram.divo.screen.reg_form.RegFormsIntent
import org.telegram.divo.screen.reg_form.RegistrationFormData
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun StepThree(
    formData: RegistrationFormData,
    onIntent: (RegFormsIntent) -> Unit,
) {
    val config = formData.subRole.stepThreeConfig()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(config.titleRes).uppercase(),
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            color = AppTheme.colors.textPrimary
        )
        config.subtitleRes?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(it),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary.copy(0.8f)
            )
        }
        Spacer(Modifier.height(24.dp))

        if (config.showWebsiteUrl) {
            RegTextField(
                value = formData.websiteUrl,
                placeholder = stringResource(R.string.RegFormWebsiteUrl),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(websiteUrl = it) })
                }
            )
            TextHint(stringResource(R.string.RegFormWebsiteUrlHint))
            Spacer(Modifier.height(16.dp))
        }

        RegTextFieldBlock(
            formData = formData,
            config = config,
            onIntent = onIntent
        )

        if (config.showAgencyName) {
            RegTextField(
                value = formData.agencyName,
                placeholder = stringResource(R.string.RegFormAgencyOrganisation),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(agencyName = it) })
                }
            )
            TextHint(
                stringResource(
                    if (formData.subRole == SubRole.MODEL) R.string.RegFormAgencyHintModel
                    else R.string.RegFormAgencyHintPro
                )
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showInstagramUrl) {
            RegTextField(
                value = formData.instagramUrl,
                placeholder = stringResource(R.string.RegFormInstagramHandle),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(instagramUrl = it) })
                }
            )
            TextHint(stringResource(R.string.RegFormInstagramHint))
            Spacer(Modifier.height(16.dp))
        }

        if (config.showPortfolioUrl) {
            RegTextField(
                value = formData.portfolioUrl,
                placeholder = stringResource(R.string.RegFormPortfolioUrl),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(portfolioUrl = it) })
                }
            )
            config.subtitleRes?.let { TextHint(stringResource(R.string.RegFormPortfolioUrlHint)) }
            Spacer(Modifier.height(16.dp))
        }

        if (config.showShowreelUrl) {
            RegTextField(
                value = formData.showreelUrl,
                placeholder = stringResource(R.string.RegFormShowreelUrl),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(showreelUrl = it) })
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        if (config.showCastingProfileUrl) {
            RegTextField(
                value = formData.castingProfileUrl,
                placeholder = stringResource(R.string.RegFormCastingProfileUrl),
                onValueChange = {
                    onIntent(RegFormsIntent.OnFieldChanged { copy(castingProfileUrl = it) })
                }
            )
            Spacer(Modifier.height(16.dp))
        }

        Spacer(Modifier.height(72.dp))
    }
}

@Composable
private fun RegTextFieldBlock(
    formData: RegistrationFormData,
    config: StepThreeConfig,
    onIntent: (RegFormsIntent) -> Unit,
) {
    if (config.showContactFirstName || config.showContactLastName || config.showContactRole) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.onBackground)
        ) {
            Spacer(Modifier.height(4.dp))
            if (config.showContactFirstName) {
                RegTextField(
                    value = formData.firstName,
                    placeholder = stringResource(R.string.RegFormContactFirstName),
                    focusedBorderColor = Color.Transparent,
                    onValueChange = {
                        onIntent(RegFormsIntent.OnFieldChanged { copy(firstName = it) })
                    }
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = AppTheme.colors.textPrimary.copy(0.4f),
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(4.dp))
            }

            if (config.showContactLastName) {
                RegTextField(
                    value = formData.lastName,
                    placeholder = stringResource(R.string.RegFormContactLastName),
                    focusedBorderColor = Color.Transparent,
                    onValueChange = {
                        onIntent(RegFormsIntent.OnFieldChanged { copy(lastName = it) })
                    }
                )
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = AppTheme.colors.textPrimary.copy(0.4f),
                    thickness = 0.5.dp
                )
                Spacer(Modifier.height(4.dp))
            }

            if (config.showContactRole) {
                RegTextField(
                    value = formData.contactRole,
                    placeholder = stringResource(R.string.RegFormContactRole),
                    focusedBorderColor = Color.Transparent,
                    onValueChange = {
                        onIntent(RegFormsIntent.OnFieldChanged { copy(contactRole = it) })
                    }
                )
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

data class StepThreeConfig(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int? = null,
    // Company contact
    val showWebsiteUrl: Boolean = false,
    val showContactFirstName: Boolean = false,
    val showContactLastName: Boolean = false,
    val showContactRole: Boolean = false,
    // Professional links
    val showAgencyName: Boolean = false,
    val showInstagramUrl: Boolean = false,
    val showPortfolioUrl: Boolean = false,
    // Talent specific
    val showShowreelUrl: Boolean = false,
    val showCastingProfileUrl: Boolean = false,
)

fun SubRole.stepThreeConfig(): StepThreeConfig = when (this) {
    // Companies (A) — Verification & Contact
    SubRole.MODELING_AGENCY,
    SubRole.FASHION_BRAND,
    SubRole.BEAUTY_BRAND,
    SubRole.BRAND_OR_BUSINESS,
    SubRole.EVENT_AGENCY,
    SubRole.MAGAZINE -> StepThreeConfig(
        titleRes = R.string.RegFormVerificationContact,
        showWebsiteUrl = true,
        showContactFirstName = true,
        showContactLastName = true,
        showContactRole = true,
    )
    // Industry Pros (B) — Professional Links
    SubRole.SCOUT,
    SubRole.BOOKER,
    SubRole.CASTING_DIRECTOR,
    SubRole.TALENT_MANAGER -> StepThreeConfig(
        titleRes = R.string.RegFormProfessionalLinks,
        showAgencyName = true,
        showPortfolioUrl = true,
    )
    // Creative C1 — Add your portfolio
    SubRole.PHOTOGRAPHER,
    SubRole.STYLIST,
    SubRole.MUA,
    SubRole.HAIR_STYLIST,
    SubRole.VIDEOGRAPHER,
    SubRole.CREATIVE_DIRECTOR,
    SubRole.FASHION_DESIGNER -> StepThreeConfig(
        titleRes = R.string.RegFormAddYourPortfolio,
        subtitleRes = R.string.RegFormPortfolioSubtitle,
        showPortfolioUrl = true,
    )
    // Talent Model (D1) — Professional Links
    SubRole.MODEL -> StepThreeConfig(
        titleRes = R.string.RegFormProfessionalLinks,
        showAgencyName = true,
        showInstagramUrl = true,
    )
    // Talent New Talent (D2) — Professional Links
    SubRole.NEW_TALENT -> StepThreeConfig(
        titleRes = R.string.RegFormProfessionalLinks,
        showInstagramUrl = true,
    )
    // Talent Actor/Dancer/Singer (D3/D4/D5)
    SubRole.ACTOR,
    SubRole.DANCER,
    SubRole.SINGER -> StepThreeConfig(
        titleRes = R.string.RegFormProfessionalLinks,
        showShowreelUrl = true,
        showCastingProfileUrl = true,
    )
    SubRole.FAN,
    SubRole.STUDIO -> StepThreeConfig(titleRes = R.string.RegFormProfessionalLinks)
}