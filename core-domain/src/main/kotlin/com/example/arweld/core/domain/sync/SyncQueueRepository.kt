package com.example.arweld.core.domain.sync

/**
 * Domain-facing repository for offline sync queue operations.
 */
interface SyncQueueRepository {
    /**
     * Enqueue a JSON payload for later sync.
     */
    suspend fun enqueue(payloadJson: String): SyncQueueItem

    /**
     * Fetch pending items ordered by creation time.
     */
    suspend fun listPending(limit: Int = 100): List<SyncQueueItem>

    /**
     * Fetch items that failed processing.
     */
    suspend fun listErrors(limit: Int = 100): List<SyncQueueItem>

    /**
     * Update the status and retry count for a queue item.
     */
    suspend fun updateStatus(id: String, status: SyncQueueStatus, retryCount: Int)
}

data class SyncQueueItem(
    val id: String,
    val payloadJson: String,
    val createdAt: Long,
    val status: SyncQueueStatus,
    val retryCount: Int,
)

enum class SyncQueueStatus {
    PENDING,
    PROCESSING,
    ERROR,
    DONE,
}
