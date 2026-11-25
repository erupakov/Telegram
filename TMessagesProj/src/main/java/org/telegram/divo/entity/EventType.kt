package org.telegram.divo.entity

data class EventType(
    val id: Int,
    val title: String?,
    /** Localized category label (High Fashion, Commercial, …); null for legacy types without one */
    val category: String? = null
)