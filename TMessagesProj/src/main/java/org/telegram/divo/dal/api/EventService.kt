package org.telegram.divo.dal.api

import org.telegram.divo.dal.dto.common.EmptyResponse
import org.telegram.divo.dal.dto.event.CreateEventRequest
import org.telegram.divo.dal.dto.event.EventDetailsResponse
import org.telegram.divo.dal.dto.event.EventIdRequest
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.dto.event.EventListResponse
import org.telegram.divo.dal.dto.event.EventTypesResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
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
    suspend fun applyEvent(@Body request: EventIdRequest): EmptyResponse

    @POST("event/unapply")
    suspend fun unapplyEvent(@Body request: EventIdRequest): EmptyResponse

    @POST("event/types")
    suspend fun getEventTypes(@Body request: EventListRequest): EventTypesResponse

    @POST("event/create")
    suspend fun createEvent(@Body request: CreateEventRequest): EventDetailsResponse

    @POST("event/update/{id}")
    suspend fun updateEvent(
        @Path("id") id: Int,
        @Body request: CreateEventRequest
    ): EventDetailsResponse

    @DELETE("event/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): EmptyResponse

    @POST("event/{id}/like")
    suspend fun likeEvent(@Path("id") id: Int): EmptyResponse

    @POST("event/{id}/unlike")
    suspend fun unlikeEvent(@Path("id") id: Int): EmptyResponse

    @POST("event/{id}/close-applications")
    suspend fun closeApplications(@Path("id") id: Int): EventDetailsResponse

    @POST("event/{id}/cancel")
    suspend fun cancelEvent(@Path("id") id: Int): EmptyResponse

}


