package com.example.arweld.core.domain.diagnostics

import kotlinx.serialization.Serializable

@Serializable
data class DiagnosticsEvent(
    val timestampMillis: Long,
    val name: String,
    val attributes: Map<String, String> = emptyMap(),
)

@Serializable
data class ArTelemetrySnapshot(
    val timestampMillis: Long,
    val fps: Double,
    val frameTimeP95Ms: Double,
    val cvLatencyP95Ms: Double,
    val performanceMode: String? = null,
)

@Serializable
data class DeviceHealthSnapshot(
    val timestampMillis: Long,
    val thermalStatus: String,
    val thermalStatusLevel: Int,
    val isDeviceHot: Boolean,
    val memoryTrimLevel: Int? = null,
    val memoryTrimReason: String? = null,
)

@Serializable
data class DiagnosticsReport(
    val metadata: DiagnosticsMetadata,
    val settings: Map<String, String> = emptyMap(),
    val arTelemetry: ArTelemetrySnapshot? = null,
    val deviceHealth: DeviceHealthSnapshot? = null,
    val recentEvents: List<DiagnosticsEvent> = emptyList(),
)

@Serializable
data class DiagnosticsMetadata(
    val generatedAtMillis: Long,
    val appVersionName: String,
    val appVersionCode: Long,
    val buildType: String,
    val deviceModel: String,
    val deviceManufacturer: String,
    val sdkInt: Int,
    val isDebuggable: Boolean,
)

data class DiagnosticsSnapshot(
    val arTelemetry: ArTelemetrySnapshot?,
    val deviceHealth: DeviceHealthSnapshot?,
    val recentEvents: List<DiagnosticsEvent>,
)
