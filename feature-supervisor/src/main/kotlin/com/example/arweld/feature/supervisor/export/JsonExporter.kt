package com.example.arweld.feature.supervisor.export

import java.io.File
import javax.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class JsonExporter @Inject constructor() {
    private val json: Json = defaultExportJson()
    fun exportToString(report: ExportReport): String = json.encodeToString(report)

    fun exportToFile(report: ExportReport, file: File) {
        file.writeText(exportToString(report))
    }
}

fun defaultExportJson(): Json = Json {
    prettyPrint = true
    prettyPrintIndent = "  "
    encodeDefaults = true
    explicitNulls = false
}
