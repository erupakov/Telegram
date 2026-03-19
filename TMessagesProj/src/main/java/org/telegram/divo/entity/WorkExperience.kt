package org.telegram.divo.entity

data class WorkExperience(
    val id: Int,
    val agencyId: Int?,
    val agencyName: String?,
    val agencyDisplayName: String?,
    val startDate: String,
    val endDate: String?,
    val isCurrent: Boolean,
)