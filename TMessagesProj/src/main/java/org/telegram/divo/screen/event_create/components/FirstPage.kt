package org.telegram.divo.screen.event_create.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.rememberGalleryLauncher
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.dal.dto.event.EventTypeBottomSheet
import org.telegram.divo.entity.EventType
import org.telegram.divo.screen.add_model.CountryPickerSheet
import org.telegram.divo.screen.event_create.State
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPage(
    state: State,
    onEventTypeSelected: (EventType) -> Unit,
    onEventNameChanged: (String) -> Unit,
    onEventDescriptionChanged: (String) -> Unit,
    onAvatarSelected: (android.net.Uri) -> Unit,
    onEventDateChanged: (String) -> Unit,
    onEventTimeChanged: (String) -> Unit,
    onCountriesChanged: (List<org.telegram.divo.screen.add_model.LocalCountry>) -> Unit,
) {
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var showEventTypesSheet by rememberSaveable { mutableStateOf(false) }
    var showDatePickerSheet by rememberSaveable { mutableStateOf(false) }
    var showTimePickerSheet by rememberSaveable { mutableStateOf(false) }

    val selectedAvatarUri = state.galleryUris.firstOrNull()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val openGallery = rememberGalleryLauncher { uri ->
            onAvatarSelected(uri)
        }

        Spacer(Modifier.height(8.dp))
        TelegramUserAvatarEditable(
            localUri = selectedAvatarUri,
            size = 94.dp,
            background = AppTheme.colors.onBackground,
            borderColor = AppTheme.colors.accentOrange,
            isVisibleSmallIcon = false,
            placeholderIconSize = 24.dp,
            onEditClick = openGallery
        )
        Spacer(Modifier.height(24.dp))
        EventItem(
            title = stringResource(R.string.EventType),
            value = state.selectedEventType?.title.orEmpty(),
            placeholder = stringResource(R.string.EventChooseAnEvent),
            onClick = { showEventTypesSheet = true }
        )
        Spacer(Modifier.height(16.dp))
        DivoTextField(
            value = state.eventName,
            onValueChange = onEventNameChanged,
            height = 46.dp,
            cornerRadius = 99.dp,
            trailingIcon = if (state.eventName.isNotBlank()) R.drawable.ic_divo_clear else null,
            onTrailingIconClick = { onEventNameChanged("") },
            placeholder = stringResource(R.string.EventName),
            placeholderColor = AppTheme.colors.textPrimary.copy(0.4f),
            textStyle = TextStyle(fontSize = 16.sp),
            backgroundColor = AppTheme.colors.onBackground,
            horizontalContentPadding = 16.dp
        )
        Spacer(Modifier.height(16.dp))
        SectionHeader(text = stringResource(R.string.EventDescription))
        Spacer(Modifier.height(6.dp))
        DivoTextField(
            value = state.eventDescription,
            onValueChange = onEventDescriptionChanged,
            placeholder = stringResource(R.string.EventDescriptionAbout),
            backgroundColor = AppTheme.colors.onBackground,
            cornerRadius = 16.dp,
            height = 114.dp,
            placeholderColor = AppTheme.colors.textPrimary.copy(0.4f),
            minLines = 5,
            maxLines = 5,
            textStyle = AppTheme.typography.bodyLarge.copy(
                color = AppTheme.colors.textPrimary
            ),
            horizontalContentPadding = 16.dp
        )
        Spacer(Modifier.height(16.dp))
        // Date & Time
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DateTimeField(
                modifier = Modifier.weight(1f),
                date = state.eventDate,
                title = stringResource(R.string.EventDate),
                placeholder = stringResource(R.string.EventDatePlaceholder),
                trailingIcon = R.drawable.ic_divo_calendar_20,
                onClick = { showDatePickerSheet = true }
            )
            DateTimeField(
                modifier = Modifier.weight(1f),
                date = state.eventTime,
                title = stringResource(R.string.EventTime),
                placeholder = stringResource(R.string.EventTimePlaceholder),
                trailingIcon = R.drawable.ic_divo_clock_20,
                onClick = { showTimePickerSheet = true },
            )
        }
        Spacer(Modifier.height(16.dp))
        EventItem(
            title = stringResource(R.string.CountryLabel),
            value = if (state.selectedCountries.isEmpty()) "" else "${state.selectedCountries.first().flag} ${state.selectedCountries.first().name}",
            placeholder = stringResource(R.string.EventChooseCountry),
            onClick = { showCountrySheet = true }
        )
        Spacer(Modifier.height(72.dp))
    }

    if (showCountrySheet) {
        CountryPickerSheet(
            list = state.allCountries,
            selectedCountries = state.selectedCountries,
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                onCountriesChanged(selectedList)
                showCountrySheet = false
            }
        )
    }

    if (showEventTypesSheet) {
        EventTypeBottomSheet(
            title = stringResource(R.string.EventType),
            items = state.eventTypes,
            selectedItem = state.selectedEventType,
            onApply = {
                onEventTypeSelected(it)
                showEventTypesSheet = false
            },
            onDismiss = { showEventTypesSheet = false }
        )
    }

    if (showDatePickerSheet) {
        EventDatePickerSheet(
            initialDate = state.eventDate,
            onDismiss = { showDatePickerSheet = false },
            onDateSelected = { date ->
                onEventDateChanged(date)
                showDatePickerSheet = false
            }
        )
    }

    if (showTimePickerSheet) {
        EventTimePickerSheet(
            initialTime = state.eventTime,
            onDismiss = { showTimePickerSheet = false },
            onTimeSelected = { time ->
                onEventTimeChanged(time)
                showTimePickerSheet = false
            }
        )
    }
}