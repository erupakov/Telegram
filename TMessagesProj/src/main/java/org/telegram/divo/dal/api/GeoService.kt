package org.telegram.divo.dal.api

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.CityDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoService {

    @GET("geo/search-by-address-name")
    suspend fun searchByAddressName(
        @Query("query") query: String
    ): GeoSearchResponse
}

class GeoSearchResponse(
    @SerializedName("data") val data: List<GeoSearchItem>?
)

class GeoSearchItem(
    @SerializedName("city") val city: CityDto?,
    @SerializedName("formatted") val formatted: String?,
)
