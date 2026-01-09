package com.example.arweld.core.domain.diagnostics

import java.io.File

interface DiagnosticsExportService {
    suspend fun exportDiagnostics(outputRoot: File): DiagnosticsExportResult
}

data class DiagnosticsExportResult(
    val outputDir: File,
    val zipFile: File,
)
