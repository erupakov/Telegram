package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class ReportProfileRequest(
    @SerializedName("report_user_id") val reportUserId: Int,
    @SerializedName("report_text") val reportText: String
)
