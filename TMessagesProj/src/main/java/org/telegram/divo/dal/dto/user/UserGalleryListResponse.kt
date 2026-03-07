package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.GalleryItemDto
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.UserGalleryList

class UserGalleryListResponse(
    @SerializedName("data") val data: UserGalleryListData? = null,
)

class UserGalleryListData(
    @SerializedName("items") val items: List<GalleryItemDto> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationDto
)

fun UserGalleryListResponse.toEntities(): UserGalleryList = UserGalleryList(
    items = data?.items?.map { it.toEntity() } ?: emptyList(),
    pagination = data?.pagination?.meta?.toEntity()
)
