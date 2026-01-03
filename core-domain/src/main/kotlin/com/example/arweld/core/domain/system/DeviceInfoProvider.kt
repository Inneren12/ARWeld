package com.example.arweld.core.domain.system

/**
 * Supplies device-specific metadata for event logging.
 */
fun interface DeviceInfoProvider {
    /**
     * Single abstract method (SAM) for lambda-friendly implementations.
     * Must NOT be named getDeviceId(), because property 'deviceId' generates JVM getter getDeviceId().
     */
    fun provideDeviceId(): String

    /**
     * Kotlin-friendly property access without JVM signature clashes.
     */
    val deviceId: String
        get() = provideDeviceId()
}
