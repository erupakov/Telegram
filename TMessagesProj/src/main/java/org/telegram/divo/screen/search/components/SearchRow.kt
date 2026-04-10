package org.telegram.divo.screen.search.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun SearchRow(
    value: String,
    onSearchFaceClicked: () -> Unit,
    onValueChanged: (String) -> Unit,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DivoTextField(
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            value = value,
            onValueChange = onValueChanged,
            cornerRadius = 99.dp,
            leadingIcon = R.drawable.ic_divo_search_24,
            trailingIcon = R.drawable.ic_divo_face_rec,
            trailingIconColor = AppTheme.colors.onBackground,
            onTrailingIconClick = onSearchFaceClicked,
            backgroundColor = AppTheme.colors.onBackground,
            horizontalContentPadding = 12.dp,
            textStyle = TextStyle(fontSize = 14.sp),
            placeholder = stringResource(R.string.SearchByName),
        )
        Spacer(Modifier.width(10.dp))
        RoundedButton(
            resId = R.drawable.ic_divo_filter,
            iconSize = 24.dp,
            paddingEnd = 0.dp,
            iconTint = AppTheme.colors.textPrimary,
            onClick = { }
        )
        Spacer(Modifier.width(10.dp))
        RoundedButton(
            resId = R.drawable.ic_divo_close,
            iconTint = AppTheme.colors.textPrimary,
            paddingEnd = 0.dp,
            onClick = onBack
        )
    }
}