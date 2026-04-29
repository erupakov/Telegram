package org.telegram.divo.entity

data class AgencyModels(
    val items: List<AgencyModel>,
    val pagination: Pagination?
)

data class AgencyModel(
    val id: Int,
    val name: String,
    val birthday: String?,
    val photoUrl: String,
    val city: City?,
    val userId: Int
)