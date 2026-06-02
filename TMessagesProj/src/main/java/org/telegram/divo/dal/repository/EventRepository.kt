package org.telegram.divo.dal.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.telegram.divo.dal.api.EventService
import org.telegram.divo.dal.dto.event.CreateEventRequest
import org.telegram.divo.dal.dto.event.EventIdRequest
import org.telegram.divo.dal.dto.event.EventListRequest
import org.telegram.divo.dal.dto.event.toEntities
import org.telegram.divo.dal.dto.event.toEntity
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.map
import org.telegram.divo.dal.network.resultOf
import org.telegram.divo.entity.EventDetails
import org.telegram.divo.entity.EventList
import org.telegram.divo.entity.EventType

class EventRepository(
    private val service: EventService
) {

    private val _eventsUpdatedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val eventsUpdatedFlow = _eventsUpdatedFlow.asSharedFlow()

    data class EventParticipationUpdate(val eventId: Int, val isApplied: Boolean, val appliesCount: Int)
    
    private val _eventParticipationFlow = MutableSharedFlow<EventParticipationUpdate>(extraBufferCapacity = 1)
    val eventParticipationFlow = _eventParticipationFlow.asSharedFlow()

    fun notifyEventParticipationChanged(eventId: Int, isApplied: Boolean, appliesCount: Int) {
        _eventParticipationFlow.tryEmit(EventParticipationUpdate(eventId, isApplied, appliesCount))
    }

    suspend fun listEvents(request: EventListRequest): DivoResult<EventList> =
        resultOf { service.listEvents(request) }.map { it.toEntity() }

    suspend fun getEvent(id: Int): DivoResult<EventDetails> = resultOf {
        service.getEvent(id)
    }.map {
        requireNotNull(it.toEntity()) { it.message ?: "Event data is null" }
    }

    suspend fun applyEvent(id: Int): DivoResult<Unit> = resultOf {
        service.applyEvent(EventIdRequest(id))
    }

    suspend fun unapplyEvent(id: Int): DivoResult<Unit> = resultOf {
        service.unapplyEvent(EventIdRequest(id))
    }

    suspend fun getEventTypes(): DivoResult<List<EventType>> =
        resultOf { service.getEventTypes(EventListRequest(0, 100, null)).toEntities() }

    suspend fun createEvent(request: CreateEventRequest): DivoResult<EventDetails> = resultOf {
        service.createEvent(request)
    }.map {
        requireNotNull(it.toEntity()) { it.message ?: "Event creation failed" }
    }.also {
        if (it is DivoResult.Success) _eventsUpdatedFlow.emit(Unit)
    }

    suspend fun updateEvent(id: Int, request: CreateEventRequest): DivoResult<EventDetails> = resultOf {
        service.updateEvent(id, request)
    }.map {
        requireNotNull(it.toEntity()) { it.message ?: "Event update failed" }
    }.also {
        if (it is DivoResult.Success) _eventsUpdatedFlow.emit(Unit)
    }

    suspend fun deleteEvent(id: Int): DivoResult<Unit> = resultOf {
        service.deleteEvent(id)
        Unit
    }.also {
        if (it is DivoResult.Success) _eventsUpdatedFlow.emit(Unit)
    }

    suspend fun likeEvent(id: Int): DivoResult<Unit> = resultOf {
        service.likeEvent(id)
        Unit
    }

    suspend fun unlikeEvent(id: Int): DivoResult<Unit> = resultOf {
        service.unlikeEvent(id)
        Unit
    }
}

