package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName

/**
 * Generic error payload as described in openapi.yaml components.schemas.Error.
 */
data class ErrorResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("errors") val errors: Map<String, Any>?
)

