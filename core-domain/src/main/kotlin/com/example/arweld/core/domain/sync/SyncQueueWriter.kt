package com.example.arweld.core.domain.sync

import com.example.arweld.core.domain.event.Event

/**
 * Simple writer facade for enqueueing offline sync events.
 */
class SyncQueueWriter(
    private val repository: SyncQueueRepository,
) {
    suspend fun enqueueEvent(event: Event): SyncQueueItem {
        return repository.enqueueEvent(event)
    }
}
