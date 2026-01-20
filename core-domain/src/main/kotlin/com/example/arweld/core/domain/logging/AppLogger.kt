package com.example.arweld.core.domain.logging

import com.example.arweld.core.domain.model.User

/**
 * Lightweight logging facade used by presentation layers to record key app events
 * without leaking implementation details (e.g., Timber or Crashlytics) to callers.
 */
interface AppLogger {

    /**
     * Records a navigation event for observability of screen flows.
     */
    fun logNavigation(route: String)

    /**
     * Records an attempt to authenticate as the specified user.
     */
    fun logLoginAttempt(userId: String)

    /**
     * Records a successful authentication and associates the user with crash reports.
     */
    fun logLoginSuccess(user: User)

    /**
     * Records repository-level failures that should surface during debugging.
     */
    fun logRepositoryError(operation: String, throwable: Throwable)

    /**
     * Records unexpected errors that bubble up to the UI.
     */
    fun logUnhandledError(throwable: Throwable)

    /**
     * Records a retry attempt for the offline sync queue.
     */
    fun logSyncRetryAllAttempt(itemCount: Int)

    /**
     * Records a retry attempt for a single sync queue item.
     */
    fun logSyncRetrySingleAttempt(itemId: String)
}
