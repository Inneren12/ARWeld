package com.example.arweld.diagnostics

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.arweld.BuildConfig
import com.example.arweld.core.domain.diagnostics.DiagnosticsExportResult
import com.example.arweld.core.domain.diagnostics.DiagnosticsExportService
import com.example.arweld.core.domain.diagnostics.DiagnosticsMetadata
import com.example.arweld.core.domain.diagnostics.DiagnosticsReport
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class DiagnosticsExportServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val diagnosticsRecorder: DiagnosticsRecorder,
) : DiagnosticsExportService {

    override suspend fun exportDiagnostics(outputRoot: File): DiagnosticsExportResult = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val exportDir = File(outputRoot, "diagnostics/diag_$timestamp")
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }

        val snapshot = diagnosticsRecorder.snapshot()
        val metadata = buildMetadata(timestamp)
        val settings = buildSettings(snapshot)
        val report = DiagnosticsReport(
            metadata = metadata,
            settings = settings,
            arTelemetry = snapshot.arTelemetry,
            deviceHealth = snapshot.deviceHealth,
            recentEvents = snapshot.recentEvents,
        )

        val json = Json {
            prettyPrint = true
            encodeDefaults = true
        }

        val reportFile = File(exportDir, "diagnostics.json").apply {
            writeText(json.encodeToString(report))
        }
        val eventsFile = File(exportDir, "recent_events.json").apply {
            writeText(json.encodeToString(snapshot.recentEvents))
        }
        val telemetryFile = File(exportDir, "ar_telemetry.json").apply {
            val content = snapshot.arTelemetry?.let { json.encodeToString(it) } ?: "{}"
            writeText(content)
        }
        val deviceHealthFile = File(exportDir, "device_health.json").apply {
            val content = snapshot.deviceHealth?.let { json.encodeToString(it) } ?: "{}"
            writeText(content)
        }
        val settingsFile = File(exportDir, "settings.json").apply {
            writeText(json.encodeToString(settings))
        }
        val logsFile = File(exportDir, "logs.txt").apply {
            writeText(buildLogLines(snapshot))
        }

        val zipFile = File(exportDir, "diagnostics.zip")
        ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
            listOf(reportFile, eventsFile, telemetryFile, deviceHealthFile, settingsFile, logsFile).forEach { file ->
                zip.putNextEntry(ZipEntry(file.name))
                file.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
        }

        DiagnosticsExportResult(
            outputDir = exportDir,
            zipFile = zipFile,
        )
    }

    private fun buildLogLines(snapshot: com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot): String {
        return snapshot.recentEvents.joinToString(separator = "\n") { event ->
            val attributes = if (event.attributes.isNotEmpty()) {
                event.attributes.entries.joinToString(separator = ",", prefix = " [", postfix = "]") { entry ->
                    "${entry.key}=${entry.value}"
                }
            } else {
                ""
            }
            "${event.timestampMillis} ${event.name}$attributes"
        }
    }

    private fun buildSettings(snapshot: com.example.arweld.core.domain.diagnostics.DiagnosticsSnapshot): Map<String, String> {
        val settings = mutableMapOf<String, String>()
        snapshot.arTelemetry?.performanceMode?.let { settings["arPerformanceMode"] = it }
        snapshot.deviceHealth?.memoryTrimLevel?.let { settings["lastMemoryTrimLevel"] = it.toString() }
        snapshot.deviceHealth?.thermalStatus?.let { settings["thermalStatus"] = it }
        return settings
    }

    private fun buildMetadata(timestamp: Long): DiagnosticsMetadata {
        val packageInfo = context.packageManager.getPackageInfoCompat(context.packageName)
        return DiagnosticsMetadata(
            generatedAtMillis = timestamp,
            appVersionName = packageInfo.versionName ?: "unknown",
            appVersionCode = packageInfo.longVersionCodeCompat(),
            buildType = BuildConfig.BUILD_TYPE,
            deviceModel = Build.MODEL,
            deviceManufacturer = Build.MANUFACTURER,
            sdkInt = Build.VERSION.SDK_INT,
            isDebuggable = BuildConfig.DEBUG,
        )
    }

    private fun PackageManager.getPackageInfoCompat(packageName: String): android.content.pm.PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getPackageInfo(packageName, 0)
        }
    }

    private fun android.content.pm.PackageInfo.longVersionCodeCompat(): Long {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            longVersionCode
        } else {
            @Suppress("DEPRECATION")
            versionCode.toLong()
        }
    }
}
