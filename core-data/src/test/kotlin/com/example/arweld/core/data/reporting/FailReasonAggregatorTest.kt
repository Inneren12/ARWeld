package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.event.Event
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.reporting.FailReasonCount
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.reporting.ReportV1
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FailReasonAggregatorTest {

    @Test
    fun `aggregates single reason in period`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            events = listOf(
                event(
                    id = "evt-1",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T09:00:00Z"),
                    payloadJson = "{\"reasons\":[\"POROSITY\"]}",
                ),
                event(
                    id = "evt-2",
                    type = EventType.QC_PASSED,
                    timestamp = instantMillis("2025-01-01T10:00:00Z"),
                    payloadJson = null,
                    workItemId = "work-2",
                ),
            ),
        )
        val aggregator = FailReasonAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T11:00:00Z"))

        assertEquals(listOf(FailReasonCount(reason = "POROSITY", count = 1)), result)
    }

    @Test
    fun `counts multiple reasons per item with deterministic order`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            events = listOf(
                event(
                    id = "evt-1",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T09:00:00Z"),
                    payloadJson = "{\"reasons\":[\"CRACK\",\"BETA\"]}",
                    workItemId = "work-1",
                ),
                event(
                    id = "evt-2",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T09:30:00Z"),
                    payloadJson = "{\"reasons\":[\"POROSITY\"]}",
                    workItemId = "work-2",
                ),
                event(
                    id = "evt-3",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T10:00:00Z"),
                    payloadJson = "{\"reasons\":[\"POROSITY\"]}",
                    workItemId = "work-3",
                ),
            ),
        )
        val aggregator = FailReasonAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T11:00:00Z"))

        assertEquals(
            listOf(
                FailReasonCount(reason = "POROSITY", count = 2),
                FailReasonCount(reason = "BETA", count = 1),
                FailReasonCount(reason = "CRACK", count = 1),
            ),
            result,
        )
    }

    @Test
    fun `maps empty reasons to unknown`() = runBlocking {
        val report = ReportV1(
            generatedAt = 0L,
            events = listOf(
                event(
                    id = "evt-1",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T09:00:00Z"),
                    payloadJson = null,
                    workItemId = "work-1",
                ),
                event(
                    id = "evt-2",
                    type = EventType.QC_FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T09:30:00Z"),
                    payloadJson = "{\"reasons\":[]}",
                    workItemId = "work-2",
                ),
            ),
        )
        val aggregator = FailReasonAggregator(FakeReportProvider(report))

        val result = aggregator(periodFor("2025-01-01T08:00:00Z", "2025-01-01T11:00:00Z"))

        assertEquals(listOf(FailReasonCount(reason = "UNKNOWN", count = 2)), result)
    }

    private class FakeReportProvider(
        private val report: ReportV1,
    ) : ReportProvider {
        override suspend fun buildReport(period: ReportPeriod): ReportV1 {
            val filteredEvents = report.events.filter { period.contains(it.timestamp) }
            return report.copy(events = filteredEvents)
        }
    }

    private fun event(
        id: String,
        type: EventType,
        timestamp: Long,
        payloadJson: String?,
        workItemId: String = "work-1",
    ): Event {
        return Event(
            id = id,
            workItemId = workItemId,
            type = type,
            timestamp = timestamp,
            actorId = "qc-1",
            actorRole = Role.QC,
            deviceId = "device-1",
            payloadJson = payloadJson,
        )
    }

    private fun instantMillis(value: String): Long {
        return Instant.parse(value).toEpochMilli()
    }

    private fun periodFor(start: String, end: String): ReportPeriod {
        return ReportPeriod(
            startMillis = instantMillis(start),
            endMillis = instantMillis(end),
        )
    }
}
