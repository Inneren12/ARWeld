package com.example.arweld.core.data.sync

import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventJson
import com.example.arweld.core.domain.sync.SyncQueueItem
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.sync.SyncQueueStatus
import com.example.arweld.core.domain.sync.SyncQueueType
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
) : SyncQueueRepository {

    override suspend fun enqueueEvent(event: Event): SyncQueueItem {
        val item = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            type = SyncQueueType.EVENT.name,
            eventType = event.type.name,
            workItemId = event.workItemId,
            payloadJson = EventJson.encode(event),
            status = SyncQueueStatus.PENDING.name,
            createdAt = event.timestamp,
        )
        syncQueueDao.enqueueEvent(item)
        return item.toDomain()
    }

    override suspend fun listPending(limit: Int): List<SyncQueueItem> {
        return syncQueueDao.getPending(limit = limit).map { it.toDomain() }
    }

    override suspend fun listErrors(limit: Int): List<SyncQueueItem> {
        return syncQueueDao.getErrors(limit = limit).map { it.toDomain() }
    }

    override suspend fun getPendingCount(): Int = syncQueueDao.getPendingCount()

    override suspend fun getErrorCount(): Int = syncQueueDao.getErrorCount()

    override suspend fun updateStatus(id: String, status: SyncQueueStatus) {
        syncQueueDao.updateStatus(id = id, status = status.name)
    }
}

private fun SyncQueueEntity.toDomain(): SyncQueueItem = SyncQueueItem(
    id = id,
    type = SyncQueueType.valueOf(type),
    eventType = eventType,
    workItemId = workItemId,
    payloadJson = payloadJson,
    status = SyncQueueStatus.valueOf(status),
    createdAt = createdAt,
)
