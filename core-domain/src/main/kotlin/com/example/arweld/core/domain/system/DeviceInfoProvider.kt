package com.example.arweld.core.domain.system

/**
 * Supplies device-specific metadata for event logging.
 */
fun interface DeviceInfoProvider {
    val deviceId: String
}
