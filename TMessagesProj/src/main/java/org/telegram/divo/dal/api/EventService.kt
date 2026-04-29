package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.common.EmptyResponse
import org.telegram.divo.dal.dto.event.EventDetailsResponse
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.dto.event.EventListResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Event-related endpoints from the Divo backend.
 */
interface EventService {

    @POST("event/list")
    suspend fun listEvents(@Body request: EventListRequest): EventListResponse

    @GET("event/{id}")
    suspend fun getEvent(@Path("id") id: Int): EventDetailsResponse

    @POST("event/apply")
    suspend fun applyEvent(
        @Query("id") id: Int
    ): EmptyResponse
}


