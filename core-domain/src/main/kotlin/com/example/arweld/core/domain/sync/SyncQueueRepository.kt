package com.example.arweld.core.domain.sync

import com.example.arweld.core.domain.event.Event

/**
 * Domain-facing repository for offline sync queue operations.
 */
interface SyncQueueRepository {
    /**
     * Enqueue a domain event for later sync/export.
     */
    suspend fun enqueueEvent(event: Event): SyncQueueItem

    /**
     * Fetch pending items ordered by creation time.
     */
    suspend fun listPending(limit: Int = 100): List<SyncQueueItem>

    /**
     * Fetch items that failed processing.
     */
    suspend fun listErrors(limit: Int = 100): List<SyncQueueItem>

    /**
     * Count pending items.
     */
    suspend fun getPendingCount(): Int

    /**
     * Count items marked with an error.
     */
    suspend fun getErrorCount(): Int

    /**
     * Update the status for a queue item.
     */
    suspend fun updateStatus(id: String, status: SyncQueueStatus)
}

data class SyncQueueItem(
    val id: String,
    val type: SyncQueueType,
    val eventType: String,
    val workItemId: String?,
    val payloadJson: String,
    val status: SyncQueueStatus,
    val createdAt: Long,
)

enum class SyncQueueStatus {
    PENDING,
    ERROR,
}

enum class SyncQueueType {
    EVENT,
}
