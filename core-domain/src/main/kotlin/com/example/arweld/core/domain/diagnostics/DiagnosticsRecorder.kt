package com.example.arweld.core.domain.diagnostics

interface DiagnosticsRecorder {
    fun recordEvent(name: String, attributes: Map<String, String> = emptyMap())
    fun updateArTelemetry(snapshot: ArTelemetrySnapshot)
    fun updateDeviceHealth(snapshot: DeviceHealthSnapshot)
    fun snapshot(maxEvents: Int = DEFAULT_EVENT_LIMIT): DiagnosticsSnapshot

    companion object {
        const val DEFAULT_EVENT_LIMIT = 50
    }
}
