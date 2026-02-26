package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Photo

class PhotoDto(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
)

fun PhotoDto.toEntity(): Photo =
    Photo(
        fileName = fileName,
        fullUrl = fullUrl,
        extension = extension,
        fileUuid = fileUuid
    )