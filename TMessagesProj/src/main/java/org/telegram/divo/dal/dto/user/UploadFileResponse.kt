package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.UploadedFile

class UploadFileResponse(
    @SerializedName("data") val data: UploadedFileData,
)

class UploadedFileData(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("status") val status: String? = null,
    @SerializedName("fileName") val fileName: String? = null,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String? = null,
    @SerializedName("type") val type: String? = null
)

fun UploadFileResponse.toEntity() = UploadedFile(
    uuid = data.uuid,
    fullUrl = data.fullUrl,
)