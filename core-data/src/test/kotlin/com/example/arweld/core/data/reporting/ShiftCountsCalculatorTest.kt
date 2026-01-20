package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.reporting.ReportV1
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftCountsCalculatorTest {

    @Test
    fun `returns zeros for empty period`() {
        val report = ReportV1(
            generatedAt = 0L,
            qcResults = listOf(
                qcResult(
                    eventId = "evt-1",
                    workItemId = "WI-1",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-01T05:59:00Z"),
                ),
            ),
        )
        val provider = FakeReportProvider(report)
        val calculator = ShiftCountsCalculator(provider)

        val counts = runBlockingCalculator(calculator, ReportPeriod(
            startMillis = instantMillis("2025-01-01T06:00:00Z"),
            endMillis = instantMillis("2025-01-01T17:59:59Z"),
        ))

        assertEquals(0, counts.totalDone)
        assertEquals(0, counts.passCount)
        assertEquals(0, counts.failCount)
    }

    @Test
    fun `counts mixed pass and fail results without double counting`() {
        val report = ReportV1(
            generatedAt = 0L,
            qcResults = listOf(
                qcResult(
                    eventId = "evt-1",
                    workItemId = "WI-1",
                    outcome = QcOutcome.FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T08:00:00Z"),
                ),
                qcResult(
                    eventId = "evt-2",
                    workItemId = "WI-1",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-01T09:00:00Z"),
                ),
                qcResult(
                    eventId = "evt-3",
                    workItemId = "WI-2",
                    outcome = QcOutcome.FAILED_REWORK,
                    timestamp = instantMillis("2025-01-01T10:00:00Z"),
                ),
                qcResult(
                    eventId = "evt-4",
                    workItemId = "WI-3",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-01T11:00:00Z"),
                ),
            ),
        )
        val provider = FakeReportProvider(report)
        val calculator = ShiftCountsCalculator(provider)

        val counts = runBlockingCalculator(calculator, ReportPeriod(
            startMillis = instantMillis("2025-01-01T06:00:00Z"),
            endMillis = instantMillis("2025-01-01T17:59:59Z"),
        ))

        assertEquals(3, counts.totalDone)
        assertEquals(2, counts.passCount)
        assertEquals(1, counts.failCount)
    }

    @Test
    fun `counts qc results across a cross-midnight shift window`() {
        val report = ReportV1(
            generatedAt = 0L,
            qcResults = listOf(
                qcResult(
                    eventId = "evt-1",
                    workItemId = "WI-early",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-01T17:59:00Z"),
                ),
                qcResult(
                    eventId = "evt-2",
                    workItemId = "WI-2",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-01T18:00:00Z"),
                ),
                qcResult(
                    eventId = "evt-3",
                    workItemId = "WI-3",
                    outcome = QcOutcome.FAILED_REWORK,
                    timestamp = instantMillis("2025-01-02T02:30:00Z"),
                ),
                qcResult(
                    eventId = "evt-4",
                    workItemId = "WI-late",
                    outcome = QcOutcome.PASSED,
                    timestamp = instantMillis("2025-01-02T06:00:00Z"),
                ),
            ),
        )
        val provider = FakeReportProvider(report)
        val calculator = ShiftCountsCalculator(provider)

        val counts = runBlockingCalculator(calculator, ReportPeriod(
            startMillis = instantMillis("2025-01-01T18:00:00Z"),
            endMillis = instantMillis("2025-01-02T05:59:59Z"),
        ))

        assertEquals(2, counts.totalDone)
        assertEquals(1, counts.passCount)
        assertEquals(1, counts.failCount)
    }

    private class FakeReportProvider(
        private val report: ReportV1,
    ) : ReportProvider {
        override suspend fun buildReport(period: ReportPeriod): ReportV1 {
            return report.copy(qcResults = report.qcResults.filter { period.contains(it.timestamp) })
        }
    }

    private fun qcResult(
        eventId: String,
        workItemId: String,
        outcome: QcOutcome,
        timestamp: Long,
    ): QcResult {
        return QcResult(
            eventId = eventId,
            workItemId = workItemId,
            outcome = outcome,
            timestamp = timestamp,
            inspectorId = "qc",
            inspectorRole = Role.QC,
            deviceId = "device",
            payloadJson = null,
        )
    }

    private fun instantMillis(value: String): Long {
        return Instant.parse(value).toEpochMilli()
    }

    private fun runBlockingCalculator(
        calculator: ShiftCountsCalculator,
        period: ReportPeriod,
    ): ShiftCounts {
        return kotlinx.coroutines.runBlocking {
            calculator(period)
        }
    }
}
