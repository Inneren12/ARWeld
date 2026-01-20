package com.example.arweld.sync

import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.sync.SyncRetryHandler
import javax.inject.Inject

class NoOpSyncRetryHandler @Inject constructor(
    private val repository: SyncQueueRepository,
    private val appLogger: AppLogger,
) : SyncRetryHandler {

    override suspend fun retryAllPending() {
        val pendingItems = repository.listPending()
        val errorItems = repository.listErrors()
        val allItems = pendingItems + errorItems
        val timestamp = System.currentTimeMillis()
        allItems.forEach { item ->
            repository.updateLastAttempt(item.id, timestamp)
        }
        appLogger.logSyncRetryAllAttempt(allItems.size)
    }

    override suspend fun retrySingle(id: String) {
        val timestamp = System.currentTimeMillis()
        repository.updateLastAttempt(id, timestamp)
        appLogger.logSyncRetrySingleAttempt(id)
    }
}
