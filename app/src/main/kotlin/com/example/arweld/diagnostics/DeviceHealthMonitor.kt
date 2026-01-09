package com.example.arweld.diagnostics

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import com.example.arweld.core.domain.diagnostics.DeviceHealthSnapshot
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceHealthMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diagnosticsRecorder: DiagnosticsRecorder,
) {
    private val powerManager: PowerManager? = context.getSystemService(PowerManager::class.java)
    private var lastThermalStatus: Int = PowerManager.THERMAL_STATUS_NONE
    private var lastMemoryTrimLevel: Int? = null
    private var lastMemoryTrimReason: String? = null

    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            registerThermalListener()
        }
        updateSnapshot()
    }

    fun onTrimMemory(level: Int) {
        lastMemoryTrimLevel = level
        lastMemoryTrimReason = trimReason(level)
        diagnosticsRecorder.recordEvent(
            name = "memory_trim",
            attributes = mapOf(
                "level" to level.toString(),
                "reason" to (lastMemoryTrimReason ?: "unknown"),
            ),
        )
        updateSnapshot()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun registerThermalListener() {
        val manager = powerManager ?: return
        manager.addThermalStatusListener(context.mainExecutor) { status ->
            lastThermalStatus = status
            diagnosticsRecorder.recordEvent(
                name = "thermal_status",
                attributes = mapOf(
                    "status" to thermalStatusLabel(status),
                    "level" to status.toString(),
                ),
            )
            updateSnapshot()
        }
    }

    private fun updateSnapshot() {
        diagnosticsRecorder.updateDeviceHealth(
            DeviceHealthSnapshot(
                timestampMillis = System.currentTimeMillis(),
                thermalStatus = thermalStatusLabel(lastThermalStatus),
                thermalStatusLevel = lastThermalStatus,
                isDeviceHot = lastThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE,
                memoryTrimLevel = lastMemoryTrimLevel,
                memoryTrimReason = lastMemoryTrimReason,
            ),
        )
    }

    private fun thermalStatusLabel(status: Int): String = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "none"
        PowerManager.THERMAL_STATUS_LIGHT -> "light"
        PowerManager.THERMAL_STATUS_MODERATE -> "moderate"
        PowerManager.THERMAL_STATUS_SEVERE -> "severe"
        PowerManager.THERMAL_STATUS_CRITICAL -> "critical"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "emergency"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "shutdown"
        else -> "unknown"
    }

    private fun trimReason(level: Int): String = when (level) {
        android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> "running_moderate"
        android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> "running_low"
        android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> "running_critical"
        android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> "background"
        android.content.ComponentCallbacks2.TRIM_MEMORY_MODERATE -> "moderate"
        android.content.ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> "complete"
        else -> "unknown"
    }
}
