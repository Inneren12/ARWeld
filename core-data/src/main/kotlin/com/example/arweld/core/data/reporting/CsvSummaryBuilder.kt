package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportV1
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import java.time.Instant
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val startEventTypes = setOf(
    EventType.WORK_CLAIMED,
    EventType.WORK_STARTED,
    EventType.REWORK_STARTED,
)

private val completionEventTypes = setOf(
    EventType.QC_PASSED,
    EventType.QC_FAILED_REWORK,
)

data class CsvSummary(
    val header: List<String>,
    val rows: List<List<String>>,
)

class CsvSummaryBuilder @Inject constructor() {
    private val json = Json { ignoreUnknownKeys = true }

    fun build(report: ReportV1): CsvSummary {
        val header = listOf(
            "work_item_id",
            "zone_id",
            "status",
            "qc_pass",
            "fail_count",
            "top_fail_reason",
            "started_at",
            "completed_at",
            "duration_sec",
        )

        val eventsByWorkItem = report.events.groupBy { it.workItemId }
        val qcResultsByWorkItem = report.qcResults.groupBy { it.workItemId }

        val rows = report.workItems
            .sortedBy { it.id }
            .map { workItem ->
                val events = eventsByWorkItem[workItem.id].orEmpty()
                val qcResults = qcResultsByWorkItem[workItem.id].orEmpty()
                val startedAt = findStartTimestamp(events)
                val completedAt = findCompletionTimestamp(events)
                val durationSeconds = calculateDurationSeconds(startedAt, completedAt)
                listOf(
                    workItem.id,
                    workItem.zone.orEmpty(),
                    mapStatus(events),
                    mapQcPass(qcResults),
                    qcResults.count { it.outcome == QcOutcome.FAILED_REWORK }.toString(),
                    topFailReason(events),
                    formatInstant(startedAt),
                    formatInstant(completedAt),
                    durationSeconds,
                )
            }

        return CsvSummary(header = header, rows = rows)
    }

    private fun mapStatus(events: List<Event>): String {
        val status = reduce(events).status
        return when (status) {
            WorkStatus.APPROVED -> "DONE_PASS"
            WorkStatus.REWORK_REQUIRED -> "DONE_FAIL"
            WorkStatus.IN_PROGRESS,
            WorkStatus.READY_FOR_QC,
            WorkStatus.QC_IN_PROGRESS -> "IN_PROGRESS"
            WorkStatus.NEW -> "UNKNOWN"
        }
    }

    private fun mapQcPass(results: List<QcResult>): String {
        val latest = results.maxWithOrNull(compareBy<QcResult> { it.timestamp }.thenBy { it.eventId })
        return when (latest?.outcome) {
            QcOutcome.PASSED -> "true"
            QcOutcome.FAILED_REWORK -> "false"
            null -> ""
        }
    }

    private fun topFailReason(events: List<Event>): String {
        val reasons = events
            .filter { it.type == EventType.QC_FAILED_REWORK }
            .flatMap { parseFailReasons(it.payloadJson) }

        if (reasons.isEmpty()) return ""

        return reasons
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
            ?.key
            .orEmpty()
    }

    private fun parseFailReasons(payloadJson: String?): List<String> {
        if (payloadJson.isNullOrBlank()) return emptyList()
        return runCatching {
            val element = json.decodeFromString<JsonElement>(payloadJson)
            element.jsonObject["reasons"]
                ?.jsonArray
                ?.mapNotNull { it.jsonPrimitive.contentOrNull }
                .orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun findStartTimestamp(events: List<Event>): Long? {
        return events
            .filter { it.type in startEventTypes }
            .minWithOrNull(compareBy<Event> { it.timestamp }.thenBy { it.id })
            ?.timestamp
    }

    private fun findCompletionTimestamp(events: List<Event>): Long? {
        return events
            .filter { it.type in completionEventTypes }
            .maxWithOrNull(compareBy<Event> { it.timestamp }.thenBy { it.id })
            ?.timestamp
    }

    private fun calculateDurationSeconds(startedAt: Long?, completedAt: Long?): String {
        if (startedAt == null || completedAt == null || completedAt < startedAt) return ""
        return ((completedAt - startedAt) / 1000L).toString()
    }

    private fun formatInstant(timestamp: Long?): String {
        return timestamp?.let { Instant.ofEpochMilli(it).toString() }.orEmpty()
    }
}
