package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class UploadFilesResponse(
    @SerializedName("data") val data: List<UploadedFileData>
)
