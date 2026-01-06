package com.example.arweld.logging

import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.logging.CrashReporter
import com.example.arweld.core.domain.model.User
import javax.inject.Inject
import timber.log.Timber

class TimberAppLogger @Inject constructor(
    private val crashReporter: CrashReporter,
) : AppLogger {

    override fun logNavigation(route: String) {
        Timber.tag(TAG).i("Navigation → %s", route)
    }

    override fun logLoginAttempt(userId: String) {
        Timber.tag(TAG).i("Login attempt for userId=%s", userId)
    }

    override fun logLoginSuccess(user: User) {
        Timber.tag(TAG).i("Login success for userId=%s role=%s", user.id, user.role)
        crashReporter.setUserId(user.id)
    }

    override fun logRepositoryError(operation: String, throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Repository error during %s", operation)
        crashReporter.recordException(throwable, mapOf("operation" to operation))
    }

    override fun logUnhandledError(throwable: Throwable) {
        Timber.tag(TAG).e(throwable, "Unhandled UI error")
        crashReporter.recordException(throwable)
    }

    private companion object {
        const val TAG = "AppLogger"
    }
}
