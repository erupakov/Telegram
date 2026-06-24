package org.telegram.divo.screen.reg_form.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.DivoAsyncImage
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.screen.reg_form.RegFormsIntent
import org.telegram.divo.screen.reg_form.RegistrationFormData
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

@Composable
fun StepPhoto(
    formData: RegistrationFormData,
    onIntent: (RegFormsIntent) -> Unit,
) {
    val config = formData.subRole.stepPhotoConfig()
    val galleryLauncher = rememberGalleryLauncher { uri ->
        onIntent(RegFormsIntent.OnFieldChanged { copy(photoUri = uri) })
    }

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

        // Зона загрузки фото
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.colors.onBackground)
                .clickable { galleryLauncher() },
            contentAlignment = Alignment.Center
        ) {
            if (formData.photoUri != null) {
                DivoAsyncImage(
                    model = formData.photoUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    errorContent = { UploadPlaceholder(config) }
                )
            } else {
                UploadPlaceholder(config)
            }
        }

        if (config.showTfpHint) {
            Spacer(Modifier.height(24.dp))
            TfpHintBlock()
        }

        Spacer(Modifier.height((72 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp))
    }
}

@Composable
private fun TfpHintBlock() {
    Text(
        text = stringResource(R.string.RegFormTfpHintTitle),
        style = AppTheme.typography.helveticaNeueLtCom,
        fontSize = 20.sp,
        color = AppTheme.colors.textPrimary
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text = stringResource(R.string.RegFormTfpHintDescription),
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colors.textPrimary.copy(0.8f)
    )
}

@Composable
private fun UploadPlaceholder(config: StepPhotoConfig) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(
                    if (config.isLogo) R.drawable.ic_divo_upload_logo
                    else R.drawable.ic_divo_upload_photo
                ),
                tint = AppTheme.colors.textPrimary,
                contentDescription = null
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(
                if (config.isLogo) R.string.RegFormUploadLogo
                else R.string.RegFormUploadPhoto
            ),
            style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.textPrimary
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.RegFormAnyFileFormat),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.textPrimary.copy(0.8f)
        )
    }
}

data class StepPhotoConfig(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int? = null,
    @StringRes val buttonRes: Int = R.string.ButtonDone,
    val showTfpHint: Boolean = false,
    val canSkip: Boolean = false,
    val isLogo: Boolean = false,
)

fun SubRole.stepPhotoConfig(): StepPhotoConfig = when (this) {
    SubRole.MODELING_AGENCY,
    SubRole.FASHION_BRAND,
    SubRole.BEAUTY_BRAND,
    SubRole.BRAND_OR_BUSINESS,
    SubRole.EVENT_AGENCY,
    SubRole.MAGAZINE -> StepPhotoConfig(
        titleRes = R.string.RegFormAddLogoOrPhoto,
    )
    SubRole.SCOUT,
    SubRole.BOOKER,
    SubRole.CASTING_DIRECTOR,
    SubRole.TALENT_MANAGER,
    SubRole.PHOTOGRAPHER,
    SubRole.STYLIST,
    SubRole.MUA,
    SubRole.HAIR_STYLIST,
    SubRole.VIDEOGRAPHER,
    SubRole.CREATIVE_DIRECTOR,
    SubRole.FASHION_DESIGNER,
    SubRole.ACTOR,
    SubRole.DANCER,
    SubRole.SINGER -> StepPhotoConfig(
        titleRes = R.string.RegFormAddProfilePhoto,
        subtitleRes = R.string.RegFormProfilePhotoSubtitle,
        isLogo = true,
    )
    SubRole.STUDIO -> StepPhotoConfig(
        titleRes = R.string.RegFormShowYourMainSpace,
        subtitleRes = R.string.RegFormStudioPhotoSubtitle,
    )
    SubRole.MODEL -> StepPhotoConfig(
        titleRes = R.string.RegFormShowTheWorld,
        subtitleRes = R.string.RegFormModelPhotoSubtitle,
        isLogo = true,
    )
    SubRole.NEW_TALENT -> StepPhotoConfig(
        titleRes = R.string.RegFormAddProfilePhoto,
        buttonRes = R.string.ButtonLetsGo,
        isLogo = true,
        showTfpHint = true,
    )
    SubRole.FAN -> StepPhotoConfig(
        titleRes = R.string.RegFormAddProfilePhoto,
        subtitleRes = R.string.RegFormProfilePhotoSubtitle,
        isLogo = true,
        canSkip = true,
    )
}