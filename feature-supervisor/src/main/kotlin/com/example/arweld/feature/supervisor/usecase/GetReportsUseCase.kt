package com.example.arweld.feature.supervisor.usecase

import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.event.toDomain
import com.example.arweld.core.domain.state.reduce
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ReportsSnapshot(
    val shiftCounts: List<ShiftReportSummary>,
    val topFailReasons: List<FailReasonSummary>,
    val problematicNodes: List<NodeIssueSummary>,
    val availableZones: List<String>,
)

class GetReportsUseCase @Inject constructor(
    private val workItemDao: WorkItemDao,
    private val eventDao: EventDao,
    private val reportAnalyticsCalculator: ReportAnalyticsCalculator,
) {
    suspend operator fun invoke(period: ExportPeriod, zoneId: String?): ReportsSnapshot = withContext(Dispatchers.IO) {
        val workItems = workItemDao.observeAll().first()
        val availableZones = workItems.mapNotNull { it.zoneId }.distinct().sorted()
        val filteredWorkItems = zoneId?.let { id -> workItems.filter { it.zoneId == id } } ?: workItems
        val workItemIds = filteredWorkItems.map { it.id }
        val eventEntities = if (workItemIds.isEmpty()) {
            emptyList()
        } else {
            workItemIds.chunked(900).flatMap { chunk -> eventDao.getByWorkItemIds(chunk) }
        }
        val events = eventEntities.map { it.toDomain() }
        val eventsInPeriod = events.filter { it.timestamp in period.startMillis..period.endMillis }
        val eventsByWorkItem = events.groupBy { it.workItemId }
        val includedWorkItems = filteredWorkItems.filter { workItem ->
            val hasEvents = eventsByWorkItem[workItem.id].orEmpty().any {
                it.timestamp in period.startMillis..period.endMillis
            }
            val createdAt = workItem.createdAt ?: 0L
            hasEvents || createdAt in period.startMillis..period.endMillis
        }
        val workItemStatuses = includedWorkItems.map { workItem ->
            val stateEvents = eventsByWorkItem[workItem.id].orEmpty()
                .filter { it.timestamp <= period.endMillis }
            val status = reduce(stateEvents).status
            ReportWorkItemStatus(nodeId = workItem.nodeId, status = status)
        }

        val analytics = reportAnalyticsCalculator.calculate(eventsInPeriod, workItemStatuses)

        ReportsSnapshot(
            shiftCounts = analytics.shiftCounts,
            topFailReasons = analytics.topFailReasons,
            problematicNodes = analytics.problematicNodes,
            availableZones = availableZones,
        )
    }
}
