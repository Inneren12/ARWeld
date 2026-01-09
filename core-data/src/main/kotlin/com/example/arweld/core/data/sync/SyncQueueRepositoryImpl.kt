package com.example.arweld.core.data.sync

import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.entity.SyncQueueEntity
import com.example.arweld.core.domain.sync.SyncQueueItem
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.sync.SyncQueueStatus
import com.example.arweld.core.domain.system.TimeProvider
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncQueueRepositoryImpl @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val timeProvider: TimeProvider,
) : SyncQueueRepository {

    override suspend fun enqueue(payloadJson: String): SyncQueueItem {
        val item = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            payloadJson = payloadJson,
            createdAt = timeProvider.nowMillis(),
            status = SyncQueueStatus.PENDING.name,
            retryCount = 0,
        )
        syncQueueDao.insert(item)
        return item.toDomain()
    }

    override suspend fun listPending(limit: Int): List<SyncQueueItem> {
        return syncQueueDao.getPending(limit = limit).map { it.toDomain() }
    }

    override suspend fun listErrors(limit: Int): List<SyncQueueItem> {
        return syncQueueDao.getErrors(limit = limit).map { it.toDomain() }
    }

    override suspend fun updateStatus(id: String, status: SyncQueueStatus, retryCount: Int) {
        syncQueueDao.updateStatus(id = id, status = status.name, retryCount = retryCount)
    }
}

private fun SyncQueueEntity.toDomain(): SyncQueueItem = SyncQueueItem(
    id = id,
    payloadJson = payloadJson,
    createdAt = createdAt,
    status = SyncQueueStatus.valueOf(status),
    retryCount = retryCount,
)
