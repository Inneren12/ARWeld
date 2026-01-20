package com.example.arweld.core.domain.sync

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.evidence.Evidence

/**
 * Simple writer facade for enqueueing offline sync events.
 */
class SyncQueueWriter(
    private val repository: SyncQueueRepository,
) {
    suspend fun enqueueEvent(event: Event): SyncQueueItem {
        return repository.enqueueEvent(event)
    }

    suspend fun enqueueEvidence(
        evidence: Evidence,
        mimeType: String,
        status: SyncQueueStatus,
    ): SyncQueueItem {
        return repository.enqueueEvidence(evidence, mimeType, status)
    }
}
