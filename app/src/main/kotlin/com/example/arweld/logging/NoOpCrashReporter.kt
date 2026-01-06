package com.example.arweld.logging

import com.example.arweld.core.domain.logging.CrashReporter
import javax.inject.Inject

/**
 * Release-safe crash reporter that performs no operations. Replace with a
 * concrete implementation (e.g., Firebase Crashlytics) when secrets are
 * available.
 */
class NoOpCrashReporter @Inject constructor() : CrashReporter {
    override fun recordException(throwable: Throwable, attributes: Map<String, String>) {
        // No-op by design
    }

    override fun setUserId(userId: String?) {
        // No-op by design
    }
}
