package org.telegram.divo.entity

data class AgencySearchModel(
    val id: Int,
    val userId: Int,
    val name: String,
    val username: String?,
    val photoUrl: String,
    val city: City?,
    val role: String,
    val isPremium: Boolean,
    val birthday: String?,
    val status: AgencySearchModelStatus
)

sealed class AgencySearchModelStatus {
    object Available : AgencySearchModelStatus()
    object AlreadyAdded : AgencySearchModelStatus()
    data class RepresentedByOther(val agencyName: String?) : AgencySearchModelStatus()
    object RequestPending : AgencySearchModelStatus()
}
