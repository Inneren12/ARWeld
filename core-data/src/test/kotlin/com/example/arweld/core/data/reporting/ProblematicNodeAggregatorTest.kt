package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.model.WorkItemType
import com.example.arweld.core.domain.reporting.NodeFailStats
import com.example.arweld.core.domain.reporting.ProblematicNodeAggregation
import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.reporting.ReportV1
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ProblematicNodeAggregatorTest {

    @Test
    fun `counts repeated failures on a single node`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            workItems = listOf(
                workItem(id = "work-1", nodeId = "NODE-1"),
                workItem(id = "work-2", nodeId = "NODE-1"),
                workItem(id = "work-3", nodeId = "NODE-1"),
            ),
            qcResults = listOf(
                qcResult("evt-1", "work-1", QcOutcome.FAILED_REWORK, "2025-01-01T09:00:00Z"),
                qcResult("evt-2", "work-2", QcOutcome.FAILED_REWORK, "2025-01-01T09:10:00Z"),
                qcResult("evt-3", "work-3", QcOutcome.PASSED, "2025-01-01T09:20:00Z"),
            ),
        )
        val aggregator = ProblematicNodeAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T10:00:00Z"))

        assertEquals(
            listOf(
                NodeFailStats(
                    nodeId = "NODE-1",
                    failCount = 2,
                    workItemIds = listOf("work-1", "work-2"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `sorts by failure count then node id`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            workItems = listOf(
                workItem(id = "work-1", nodeId = "NODE-B"),
                workItem(id = "work-2", nodeId = "NODE-A"),
                workItem(id = "work-3", nodeId = "NODE-B"),
            ),
            qcResults = listOf(
                qcResult("evt-1", "work-1", QcOutcome.FAILED_REWORK, "2025-01-01T09:00:00Z"),
                qcResult("evt-2", "work-2", QcOutcome.FAILED_REWORK, "2025-01-01T09:05:00Z"),
                qcResult("evt-3", "work-3", QcOutcome.FAILED_REWORK, "2025-01-01T09:10:00Z"),
            ),
        )
        val aggregator = ProblematicNodeAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T10:00:00Z"))

        assertEquals(
            listOf(
                NodeFailStats(
                    nodeId = "NODE-B",
                    failCount = 2,
                    workItemIds = listOf("work-1", "work-3"),
                ),
                NodeFailStats(
                    nodeId = "NODE-A",
                    failCount = 1,
                    workItemIds = listOf("work-2"),
                ),
            ),
            result,
        )
    }

    @Test
    fun `buckets missing node ids under unknown`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            workItems = listOf(
                workItem(id = "work-1", nodeId = null),
                workItem(id = "work-2", nodeId = "NODE-1"),
            ),
            qcResults = listOf(
                qcResult("evt-1", "work-1", QcOutcome.FAILED_REWORK, "2025-01-01T09:00:00Z"),
                qcResult("evt-2", "work-2", QcOutcome.FAILED_REWORK, "2025-01-01T09:10:00Z"),
            ),
        )
        val aggregator = ProblematicNodeAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T10:00:00Z"))

        assertEquals(
            listOf(
                NodeFailStats(
                    nodeId = "NODE-1",
                    failCount = 1,
                    workItemIds = listOf("work-2"),
                ),
                NodeFailStats(
                    nodeId = ProblematicNodeAggregation.UNKNOWN_NODE,
                    failCount = 1,
                    workItemIds = listOf("work-1"),
                ),
            ),
            result,
        )
    }

    private class FakeReportProvider(
        private val report: ReportV1,
    ) : ReportProvider {
        override suspend fun buildReport(period: ReportPeriod): ReportV1 {
            return report.copy(
                qcResults = report.qcResults.filter { period.contains(it.timestamp) },
            )
        }
    }

    private fun workItem(id: String, nodeId: String?): WorkItem {
        return WorkItem(
            id = id,
            code = "CODE-$id",
            type = WorkItemType.NODE,
            description = "desc-$id",
            nodeId = nodeId,
            createdAt = 0L,
        )
    }

    private fun qcResult(
        eventId: String,
        workItemId: String,
        outcome: QcOutcome,
        timestamp: String,
    ): QcResult {
        return QcResult(
            eventId = eventId,
            workItemId = workItemId,
            outcome = outcome,
            timestamp = Instant.parse(timestamp).toEpochMilli(),
            inspectorId = "qc-1",
            inspectorRole = Role.QC,
            deviceId = "device-1",
        )
    }

    private fun periodFor(start: String, end: String): ReportPeriod {
        return ReportPeriod(
            startMillis = Instant.parse(start).toEpochMilli(),
            endMillis = Instant.parse(end).toEpochMilli(),
        )
    }
}
