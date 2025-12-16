package com.example.arweld.core.domain.system

/**
 * Provides the current time in milliseconds since epoch.
 */
fun interface TimeProvider {
    fun nowMillis(): Long
}
