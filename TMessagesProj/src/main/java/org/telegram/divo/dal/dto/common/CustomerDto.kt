package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Customer

class CustomerDto(
    @SerializedName("site") val site: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("background") val backgroundUuid: UuidContainerDto? = null
)

fun CustomerDto.toEntity(): Customer =
    Customer(
        site = site.orEmpty(),
        description = description.orEmpty(),
        backgroundUuid = backgroundUuid?.uuid.orEmpty()
    )

fun Customer.toDto(): CustomerDto =
    CustomerDto(
        site = site,
        description = description,
        backgroundUuid = UuidContainerDto(backgroundUuid.orEmpty())
    )