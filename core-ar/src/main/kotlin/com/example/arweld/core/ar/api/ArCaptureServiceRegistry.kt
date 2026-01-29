package com.example.arweld.core.ar.api

import java.util.concurrent.atomic.AtomicReference

/**
 * Tracks the active [ArCaptureService] instance so other modules can request AR captures.
 */
object ArCaptureServiceRegistry {
    private val serviceRef = AtomicReference<ArCaptureService?>()

    fun register(service: ArCaptureService) {
        serviceRef.set(service)
    }

    fun unregister(service: ArCaptureService) {
        serviceRef.compareAndSet(service, null)
    }

    fun current(): ArCaptureService? = serviceRef.get()
}
