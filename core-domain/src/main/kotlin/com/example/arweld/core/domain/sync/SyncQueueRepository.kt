package com.example.arweld.core.domain.sync

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.evidence.Evidence
import kotlinx.coroutines.flow.Flow

/**
 * Domain-facing repository for offline sync queue operations.
 */
interface SyncQueueRepository {
    /**
     * Enqueue a domain event for later sync/export.
     */
    suspend fun enqueueEvent(event: Event): SyncQueueItem

    /**
     * Enqueue an evidence file for later sync/export.
     */
    suspend fun enqueueEvidence(
        evidence: Evidence,
        mimeType: String,
        status: SyncQueueStatus,
    ): SyncQueueItem

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
     * Observe pending item count as a flow.
     */
    fun observePendingCount(): Flow<Int>

    /**
     * Observe error item count as a flow.
     */
    fun observeErrorCount(): Flow<Int>

    /**
     * Observe the last enqueue timestamp.
     */
    fun observeLastEnqueuedAt(): Flow<Long?>

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
    val fileUri: String,
    val mimeType: String,
    val sizeBytes: Long,
    val status: SyncQueueStatus,
    val createdAt: Long,
)

enum class SyncQueueStatus {
    PENDING,
    ERROR,
}

enum class SyncQueueType {
    EVENT,
    EVIDENCE,
}
