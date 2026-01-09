package com.example.arweld.core.domain.sync

/**
 * Simple writer facade for enqueueing offline sync payloads.
 */
class SyncQueueWriter(
    private val repository: SyncQueueRepository,
) {
    suspend fun enqueue(payloadJson: String): SyncQueueItem {
        return repository.enqueue(payloadJson)
    }
}
