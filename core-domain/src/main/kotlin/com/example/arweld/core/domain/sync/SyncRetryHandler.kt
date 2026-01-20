package com.example.arweld.core.domain.sync

/**
 * Retry handler for offline sync queue operations.
 *
 * This is a no-op pipeline placeholder until server sync exists.
 */
interface SyncRetryHandler {
    suspend fun retryAllPending()
    suspend fun retrySingle(id: String)
}
