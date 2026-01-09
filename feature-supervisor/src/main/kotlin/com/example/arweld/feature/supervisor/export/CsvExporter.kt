package com.example.arweld.feature.supervisor.export

import java.io.File
import javax.inject.Inject

class CsvExporter @Inject constructor() {
    fun exportToString(report: ExportReport): String {
        val header = listOf(
            "work_item_id",
            "code",
            "description",
            "status",
            "zone",
            "node_id",
            "event_count",
            "evidence_count",
        )

        val rows = report.workItems.map { item ->
            listOf(
                item.id,
                item.code,
                item.description,
                item.status,
                item.zone.orEmpty(),
                item.nodeId.orEmpty(),
                item.events.size.toString(),
                item.evidence.size.toString(),
            )
        }

        return buildString {
            appendLine(header.joinToString(","))
            rows.forEach { row ->
                appendLine(row.joinToString(",") { escapeCsv(it) })
            }
        }
    }

    fun exportToFile(report: ExportReport, file: File) {
        file.writeText(exportToString(report))
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
