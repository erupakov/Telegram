package org.telegram.divo.entity

data class Engagement(
    val liked: EngagementList,
    val viewed: EngagementList,
    val followed: EngagementList,
)

data class EngagementList(
    val items: List<EngagementUser>,
    val totalCount: Int
)

data class EngagementUser(
    val id: Int,
    val fullName: String,
    val role: String,
    val roleLabel: String,
    val photoUrl: String
)