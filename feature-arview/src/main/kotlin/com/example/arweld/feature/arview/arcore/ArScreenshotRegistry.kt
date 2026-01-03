package com.example.arweld.feature.arview.arcore

import java.util.concurrent.atomic.AtomicReference

/**
 * Tracks the active [ArScreenshotService] instance so other modules can capture AR screenshots.
 */
object ArScreenshotRegistry {
    private val serviceRef = AtomicReference<ArScreenshotService?>()

    fun register(service: ArScreenshotService) {
        serviceRef.set(service)
    }

    fun unregister(service: ArScreenshotService) {
        serviceRef.compareAndSet(service, null)
    }

    fun current(): ArScreenshotService? = serviceRef.get()
}
