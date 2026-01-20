package com.example.arweld.logging

import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.logging.CrashReporter
import com.example.arweld.core.domain.model.User
import javax.inject.Inject
import timber.log.Timber

class TimberAppLogger @Inject constructor(
    private val crashReporter: CrashReporter,
    private val diagnosticsRecorder: DiagnosticsRecorder,
) : AppLogger {

    override fun logNavigation(route: String) {
        Timber.tag(TAG).i("Navigation → %s", route)
        diagnosticsRecorder.recordEvent(
            name = "navigation",
            attributes = mapOf("route" to route),
        )
    }

    override fun logLoginAttempt(userId: String) {
        Timber.tag(TAG).i("Login attempt for userId=%s", userId)
        diagnosticsRecorder.recordEvent(
            name = "login_attempt",
            attributes = mapOf("userId" to userId),
        )
    }

    override fun logLoginSuccess(user: User) {
        Timber.tag(TAG).i("Login success for userId=%s role=%s", user.id, user.role)
        crashReporter.setUserId(user.id)
        diagnosticsRecorder.recordEvent(
            name = "login_success",
            attributes = mapOf("userId" to user.id, "role" to user.role.name),
        )
    }

    override fun logRepositoryError(operation: String, throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Repository error during %s", operation)
        crashReporter.recordException(throwable, mapOf("operation" to operation))
        diagnosticsRecorder.recordEvent(
            name = "repository_error",
            attributes = mapOf("operation" to operation, "message" to (throwable.message ?: "unknown")),
        )
    }

    override fun logUnhandledError(throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Unhandled UI error")
        crashReporter.recordException(throwable)
        diagnosticsRecorder.recordEvent(
            name = "unhandled_error",
            attributes = mapOf("message" to (throwable.message ?: "unknown")),
        )
    }

    override fun logSyncRetryAllAttempt(itemCount: Int) {
        Timber.tag(TAG).i("Sync retry requested for %d items", itemCount)
        diagnosticsRecorder.recordEvent(
            name = "sync_retry_all_attempt",
            attributes = mapOf("itemCount" to itemCount.toString()),
        )
    }

    override fun logSyncRetrySingleAttempt(itemId: String) {
        Timber.tag(TAG).i("Sync retry requested for itemId=%s", itemId)
        diagnosticsRecorder.recordEvent(
            name = "sync_retry_single_attempt",
            attributes = mapOf("itemId" to itemId),
        )
    }

    private companion object {
        const val TAG = "AppLogger"
    }
}
