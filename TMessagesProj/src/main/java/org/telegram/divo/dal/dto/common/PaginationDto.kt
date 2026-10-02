package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Pagination

class PaginationDto(
    @SerializedName("meta") val meta: PaginationMetaDto
)

class PaginationMetaDto(
    @SerializedName("limit") val limit: Int,
    @SerializedName("currentOffset") val currentOffset: Int,
    @SerializedName("totalCount") val totalCount: Int
)

fun PaginationMetaDto.toEntity(): Pagination =
    Pagination(
        limit = limit,
        currentOffset = currentOffset,
        totalCount = totalCount
    )