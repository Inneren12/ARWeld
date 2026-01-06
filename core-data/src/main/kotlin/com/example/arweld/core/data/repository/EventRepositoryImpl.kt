package com.example.arweld.core.data.repository

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.data.event.toEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EventRepository using Room database.
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
) : EventRepository {

    override suspend fun appendEvent(event: Event) {
        eventDao.insert(event.toEntity())
    }

    override suspend fun appendEvents(events: List<Event>) {
        eventDao.insertAll(events.map { it.toEntity() })
    }

    override suspend fun getEventsForWorkItem(workItemId: String): List<Event> {
        return eventDao.getByWorkItemId(workItemId).map { it.toDomain() }
    }

    override suspend fun getLastEventsByUser(userId: String): List<Event> {
        return eventDao.getLastEventsByUser(userId).map { it.toDomain() }
    }
}
