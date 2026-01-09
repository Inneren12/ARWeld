package com.example.arweld.sync

import com.example.arweld.core.domain.sync.SyncQueueItem
import com.example.arweld.core.domain.sync.SyncQueueResult
import com.example.arweld.core.domain.sync.SyncQueueWorkHandler
import javax.inject.Inject

class NoOpSyncQueueWorkHandler @Inject constructor() : SyncQueueWorkHandler {
    override suspend fun process(item: SyncQueueItem): SyncQueueResult {
        return SyncQueueResult.failure(retryable = true)
    }
}
