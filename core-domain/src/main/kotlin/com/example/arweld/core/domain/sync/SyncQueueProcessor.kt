package com.example.arweld.core.domain.sync

/**
 * Processes pending sync queue items using the provided handler.
 */
class SyncQueueProcessor(
    private val repository: SyncQueueRepository,
    private val handler: SyncQueueWorkHandler,
) {

    suspend fun processPending(limit: Int = 25) {
        val pendingItems = repository.listPending(limit)
        pendingItems.forEach { item ->
            repository.updateStatus(
                id = item.id,
                status = SyncQueueStatus.PROCESSING,
                retryCount = item.retryCount,
            )

            val result = runCatching { handler.process(item) }
                .getOrElse { SyncQueueResult.failure(retryable = true) }

            if (result.success) {
                repository.updateStatus(
                    id = item.id,
                    status = SyncQueueStatus.DONE,
                    retryCount = item.retryCount,
                )
            } else {
                repository.updateStatus(
                    id = item.id,
                    status = SyncQueueStatus.ERROR,
                    retryCount = item.retryCount + 1,
                )
            }
        }
    }
}

data class SyncQueueResult(
    val success: Boolean,
    val retryable: Boolean,
) {
    companion object {
        fun success(): SyncQueueResult = SyncQueueResult(success = true, retryable = false)
        fun failure(retryable: Boolean): SyncQueueResult = SyncQueueResult(success = false, retryable = retryable)
    }
}

fun interface SyncQueueWorkHandler {
    suspend fun process(item: SyncQueueItem): SyncQueueResult
}
