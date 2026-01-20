package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.reporting.FailReasonAggregation
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject

data class ReportWorkItemStatus(
    val nodeId: String?,
    val status: WorkStatus,
)

data class ReportAnalyticsSnapshot(
    val shiftCounts: List<ShiftReportSummary>,
    val topFailReasons: List<FailReasonSummary>,
    val problematicNodes: List<NodeIssueSummary>,
)

class ReportAnalyticsCalculator @Inject constructor() {
    fun calculate(
        eventsInPeriod: List<Event>,
        workItemStatuses: List<ReportWorkItemStatus>,
    ): ReportAnalyticsSnapshot {
        val qcEvents = eventsInPeriod.filter { it.type == EventType.QC_PASSED || it.type == EventType.QC_FAILED_REWORK }
        val shiftCounts = qcEvents.groupBy { shiftLabel(it.timestamp) }
            .map { (label, shiftEvents) ->
                val passed = shiftEvents.count { it.type == EventType.QC_PASSED }
                val failed = shiftEvents.count { it.type == EventType.QC_FAILED_REWORK }
                ShiftReportSummary(
                    label = label,
                    total = shiftEvents.size,
                    passed = passed,
                    failed = failed,
                )
            }
            .sortedBy { it.label }

        val topFailReasons = FailReasonAggregation.aggregateFromEvents(eventsInPeriod)
            .map { FailReasonSummary(reason = it.reason, count = it.count) }

        val nodeIdCounts = workItemStatuses.mapNotNull { it.nodeId }.groupingBy { it }.eachCount()
        val nodeFailures = workItemStatuses
            .filter { it.status == WorkStatus.REWORK_REQUIRED }
            .mapNotNull { it.nodeId }
            .groupingBy { it }
            .eachCount()

        val problematicNodes = nodeFailures.entries.map { (nodeId, failures) ->
            NodeIssueSummary(
                nodeId = nodeId,
                failures = failures,
                totalItems = nodeIdCounts[nodeId] ?: failures,
            )
        }.sortedWith(compareByDescending<NodeIssueSummary> { it.failures }.thenBy { it.nodeId })

        return ReportAnalyticsSnapshot(
            shiftCounts = shiftCounts,
            topFailReasons = topFailReasons,
            problematicNodes = problematicNodes,
        )
    }

    private fun shiftLabel(timestamp: Long): String {
        val hour = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).hour
        return when (hour) {
            in 6 until 14 -> "Shift 06:00-14:00"
            in 14 until 22 -> "Shift 14:00-22:00"
            else -> "Shift 22:00-06:00"
        }
    }
}
