package com.example.arweld.core.data.repository

import androidx.room.withTransaction
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.data.event.toEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventJson
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.sync.SyncQueueStatus
import com.example.arweld.core.domain.sync.SyncQueueType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of EventRepository using Room database.
 */
@Singleton
class EventRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
    private val eventDao: EventDao,
    private val syncQueueDao: SyncQueueDao,
) : EventRepository {

    override suspend fun appendEvent(event: Event) {
        database.withTransaction {
            eventDao.insert(event.toEntity())
            syncQueueDao.enqueueEvent(event.toSyncQueueEntity())
        }
    }

    override suspend fun appendEvents(events: List<Event>) {
        if (events.isEmpty()) return
        database.withTransaction {
            eventDao.insertAll(events.map { it.toEntity() })
            syncQueueDao.insertAll(events.map { it.toSyncQueueEntity() })
        }
    }

    override suspend fun getEventsForWorkItem(workItemId: String): List<Event> {
        return eventDao.getByWorkItemId(workItemId).map { it.toDomain() }
    }

    override suspend fun getLastEventsByUser(userId: String): List<Event> {
        return eventDao.getLastEventsByUser(userId).map { it.toDomain() }
    }

    private fun Event.toSyncQueueEntity(): SyncQueueEntity = SyncQueueEntity(
        id = UUID.randomUUID().toString(),
        type = SyncQueueType.EVENT.name,
        eventType = type.name,
        workItemId = workItemId,
        payloadJson = EventJson.encode(this),
        status = SyncQueueStatus.PENDING.name,
        createdAt = timestamp,
    )
}
