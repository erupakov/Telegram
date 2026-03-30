package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName

class CreatePublicationRequest(
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val type: String,
    @SerializedName("files") val files: List<CreatePublicationFileRequest>
)