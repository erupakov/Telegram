package org.telegram.divo.entity

data class FeedlineSearchResult(
    val items: List<FeedlineItem>,
    val pagination: Pagination? = null
)

data class FeedlineItem(
    val id: Int,
    val title: String,
    val user: FeedlineUser?,
    val searchImageUrl: String?
)

data class FeedlineUser(
    val id: Int,
    val fullName: String,
    val role: String,
    val roleLabel: String,
)

data class PaginationMeta(
    val limit: Int,
    val currentOffset: Int,
    val totalCount: Int
)