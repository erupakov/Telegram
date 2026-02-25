package org.telegram.divo.entity

data class Pagination(
    val limit: Int,
    val currentOffset: Int,
    val totalCount: Int
)