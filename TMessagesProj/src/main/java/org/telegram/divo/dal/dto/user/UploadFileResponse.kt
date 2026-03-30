package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.UploadedFile

class UploadFileResponse(
    @SerializedName("data") val data: UploadedFileData,
)

class UploadedFileData(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("fullUrl") val fullUrl: String,
)

fun UploadFileResponse.toEntity() = UploadedFile(
    uuid = data.uuid,
    fullUrl = data.fullUrl,
)