package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Publication

class CreatePublicationResponse(
    @SerializedName("data") val data: PublicationItemDto
)

class CreatePublicationFileRequest(
    @SerializedName("order") val order: Int,
    @SerializedName("fileUuid") val fileUuid: String
)

fun CreatePublicationResponse.toEntity(): Publication = data.toEntity()