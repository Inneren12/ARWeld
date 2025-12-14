package com.example.arweld.core.data.repository

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.entity.EventEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EventRepository using Room database.
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override suspend fun insert(event: Event) {
        eventDao.insert(event.toEntity())
    }

    override suspend fun getByWorkItem(workItemId: String): List<Event> {
        return eventDao.observeByWorkItem(workItemId).first().map { it.toDomain() }
    }

    override fun observeByWorkItem(workItemId: String): Flow<List<Event>> {
        return eventDao.observeByWorkItem(workItemId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun Event.toEntity() = EventEntity(
        id = id,
        workItemId = workItemId,
        type = type.name,
        timestamp = timestamp,
        actorId = actorId,
        actorRole = actorRole.name,
        deviceId = deviceId,
        payloadJson = payloadJson
    )

    private fun EventEntity.toDomain() = Event(
        id = id,
        workItemId = workItemId,
        type = EventType.valueOf(type),
        timestamp = timestamp,
        actorId = actorId,
        actorRole = Role.valueOf(actorRole),
        deviceId = deviceId,
        payloadJson = payloadJson
    )
}
