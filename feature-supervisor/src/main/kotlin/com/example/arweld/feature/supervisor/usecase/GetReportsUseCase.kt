package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.data.reporting.FailReasonAggregator
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import java.time.Instant
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.first

data class ReportsPeriod(
    val startMillis: Long,
    val endMillis: Long,
)

data class ReportsSnapshot(
    val shiftCounts: List<ShiftReportSummary>,
    val topFailReasons: List<FailReasonSummary>,
    val problematicNodes: List<NodeIssueSummary>,
)

class GetReportsUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val failReasonAggregator: FailReasonAggregator,
) {
    suspend operator fun invoke(period: ReportsPeriod): ReportsSnapshot {
        val workItems = workItemDao.observeAll().first()
        val workItemIds = workItems.map { it.id }
        val eventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk -> eventDao.getByWorkItemIds(chunk) }
        }
        val events = eventEntities.map { it.toDomain() }
        val eventsInPeriod = events.filter { it.timestamp in period.startMillis..period.endMillis }

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

        val reportPeriod = ReportPeriod(
            startMillis = period.startMillis,
            endMillis = period.endMillis,
        )
        val topFailReasons = failReasonAggregator(reportPeriod)
            .map { FailReasonSummary(reason = it.reason, count = it.count) }

        val eventsByWorkItem = events.groupBy { it.workItemId }
        val nodeIdCounts = workItems.mapNotNull { it.nodeId }.groupingBy { it }.eachCount()
        val workItemStates = workItems.map { workItem ->
            val state = reduce(eventsByWorkItem[workItem.id].orEmpty())
            workItem.id to state.status
        }.toMap()
        val nodeFailures = workItems.mapNotNull { item ->
            val status = workItemStates[item.id] ?: WorkStatus.NEW
            if (status == WorkStatus.REWORK_REQUIRED) item.nodeId else null
        }.groupingBy { it }.eachCount()

        val problematicNodes = nodeFailures.entries.map { (nodeId, failures) ->
            NodeIssueSummary(
                nodeId = nodeId,
                failures = failures,
                totalItems = nodeIdCounts[nodeId] ?: failures,
            )
        }.sortedWith(compareByDescending<NodeIssueSummary> { it.failures }.thenBy { it.nodeId })

        return ReportsSnapshot(
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
