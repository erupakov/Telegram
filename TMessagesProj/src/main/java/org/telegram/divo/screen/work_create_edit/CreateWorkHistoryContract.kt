package org.telegram.divo.screen.work_create_edit

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import java.time.LocalDate

data class State(
    val editId: Int? = null,
    val isLoading: Boolean = false,
    val agencyName: String = "",
    val avatarUrl: String = "",
    val startDate: String? = LocalDate.now().toString(),
    val startDateMil: Long = System.currentTimeMillis(),
    val endDate: String? = null,
    val endDateMil: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false,
    val isEditMode: Boolean = false
) : ViewState {

    val createEnabled: Boolean
        get() = agencyName.isNotEmpty() &&
                startDate != null &&
                (isCurrent || endDate != null)
}

sealed interface Intent : ViewIntent {
    data object OnBackClicked : Intent
    data class OnStartDateSelected(val date: String, val mil: Long) : Intent
    data class OnEndDateSelected(val date: String, val mil: Long) : Intent
    data class OnCurrentChanged(val isCurrent: Boolean): Intent
    data object OnCreateExperience: Intent
    data object OnSearchSelected : Intent

}

sealed interface Effect : ViewEffect {
    data object NavigateBack : Effect
    data object NavigateToSearch : Effect
    data object ShowSuccess : Effect
    data class ShowError(val message: String) : Effect
}
