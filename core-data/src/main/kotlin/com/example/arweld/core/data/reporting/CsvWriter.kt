package com.example.arweld.core.data.reporting

import javax.inject.Inject

class CsvWriter @Inject constructor() {
    fun write(header: List<String>, rows: List<List<String>>): String {
        return buildString {
            appendLine(header.joinToString(",") { escape(it) })
            rows.forEach { row ->
                appendLine(row.joinToString(",") { escape(it) })
            }
        }
    }

    private fun escape(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        if (!needsQuotes) return value
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
