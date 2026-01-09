package com.example.arweld.diagnostics

import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthProvider
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsEvent
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DiagnosticsRepository @Inject constructor() : DiagnosticsRecorder, DeviceHealthProvider {

    private val eventBuffer = ArrayDeque<DiagnosticsEvent>()
    private val eventLock = Any()

    private val _arTelemetry = MutableStateFlow<ArTelemetrySnapshot?>(null)
    private val _deviceHealth = MutableStateFlow(
        DeviceHealthSnapshot(
            timestampMillis = System.currentTimeMillis(),
            thermalStatus = "unknown",
            thermalStatusLevel = -1,
            isDeviceHot = false,
            memoryTrimLevel = null,
            memoryTrimReason = null,
        ),
    )

    override val deviceHealth: StateFlow<DeviceHealthSnapshot> = _deviceHealth.asStateFlow()

    override fun recordEvent(name: String, attributes: Map<String, String>) {
        val event = DiagnosticsEvent(
            timestampMillis = System.currentTimeMillis(),
            name = name,
            attributes = attributes,
        )
        synchronized(eventLock) {
            if (eventBuffer.size >= MAX_EVENTS) {
                repeat(eventBuffer.size - MAX_EVENTS + 1) { eventBuffer.removeFirstOrNull() }
            }
            eventBuffer.addLast(event)
        }
    }

    override fun updateArTelemetry(snapshot: ArTelemetrySnapshot) {
        _arTelemetry.value = snapshot
    }

    override fun updateDeviceHealth(snapshot: DeviceHealthSnapshot) {
        _deviceHealth.value = snapshot
    }

    override fun snapshot(maxEvents: Int): DiagnosticsSnapshot {
        val events = synchronized(eventLock) {
            if (eventBuffer.isEmpty()) {
                emptyList()
            } else {
                eventBuffer.toList().takeLast(maxEvents)
            }
        }
        return DiagnosticsSnapshot(
            arTelemetry = _arTelemetry.value,
            deviceHealth = _deviceHealth.value,
            recentEvents = events,
        )
    }

    private companion object {
        const val MAX_EVENTS = 200
    }
}
