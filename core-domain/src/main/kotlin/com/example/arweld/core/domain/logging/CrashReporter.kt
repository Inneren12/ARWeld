package com.example.arweld.core.domain.logging

/**
 * Abstraction over crash reporting so the app can run safely without external services
 * (e.g., in release builds without secret keys).
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, attributes: Map<String, String> = emptyMap())

    fun setUserId(userId: String?)
}
