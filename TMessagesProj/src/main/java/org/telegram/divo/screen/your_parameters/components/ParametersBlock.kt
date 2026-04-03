package org.telegram.divo.screen.your_parameters.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.utils.formatWeird
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

data class ProfileParameter(
    val type: ParametersType,
    val value: String,
    val id: Int? = null
)

@Composable
fun ParametersBlock(
    items: List<ProfileParameter>,
    onClick: (ProfileParameter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.onBackground)
            .padding(horizontal = 16.dp)
    ) {
        items.forEachIndexed { index, item ->
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickableWithoutRipple { onClick(item) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(0.4f)
                        .padding(top = 2.dp),
                    text = stringResource(item.type.titleRes),
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(
                    modifier = Modifier
                        .height(46.dp)
                        .weight(0.6f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp),
                        text = if (item.type == ParametersType.AGE) {
                            item.value.formattedAge().substringBefore(" ")
                        } else {
                            item.value.formatWeird()
                        },
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.textPrimary.copy(0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Icon(
                        painter = painterResource(R.drawable.ic_divo_arrow_right_20),
                        contentDescription = null
                    )
                }
            }

            if (index != items.size - 1) {
                Divider()
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

enum class ParametersType(
    @field:StringRes val titleRes: Int
) {
    GENDER(R.string.LabelGender),
    AGE(R.string.LabelAge),
    HEIGHT(R.string.LabelHeight),
    WAIST(R.string.LabelWaist),
    HIPS(R.string.LabelHips),
    SHOE_SIZE(R.string.LabelShoeSize),
    HAIR_LENGTH(R.string.ChooseHairLength),
    HAIR_COLOR(R.string.ChooseHairColor),
    EYE_COLOR(R.string.ChooseEyeColor),
    SKIN_COLOR(R.string.ChooseSkinColor),
    BREAST_SIZE(R.string.ChooseBreastSize)
}