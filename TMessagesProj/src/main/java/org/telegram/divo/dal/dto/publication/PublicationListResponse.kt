package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Publication
import org.telegram.divo.entity.PublicationFile
import org.telegram.divo.entity.PublicationList

class PublicationListResponse(
    @SerializedName("data") val data: PublicationListDto
)

class PublicationListDto(
    @SerializedName("items") val items: List<PublicationItemDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

class PublicationItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("type") val type: String,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("files") val files: List<PublicationFileDto>
)

class PublicationFileDto(
    @SerializedName("order") val order: Int,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("fileUuid") val fileUuid: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("description") val description: String?
)

fun PublicationListResponse.toEntities(): PublicationList =
    PublicationList(
        items = data.items.map { it.toEntity() },
        pagination = data.pagination?.meta?.toEntity()
    )

fun PublicationItemDto.toEntity() = Publication(
    id = id,
    title = title,
    description = description,
    type = type,
    likesCount = likesCount,
    isLikedByUser = isLikedByUser,
    files = files.map { it.toEntity() }
)

fun PublicationFileDto.toEntity() = PublicationFile(
    order = order,
    fileName = fileName,
    fullUrl = fullUrl,
    fileUuid = fileUuid,
    extension = extension,
    description = description
)