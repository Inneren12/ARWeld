package com.example.arweld.core.data.reporting

import com.example.arweld.core.domain.reporting.QcOutcome
import com.example.arweld.core.domain.reporting.QcResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.reporting.ReportV1
import javax.inject.Inject
import javax.inject.Singleton

fun interface ReportProvider {
    suspend fun buildReport(period: ReportPeriod): ReportV1
}

@Singleton
class ReportExportServiceProvider @Inject constructor(
    private val reportExportService: ReportExportService,
) : ReportProvider {
    override suspend fun buildReport(period: ReportPeriod): ReportV1 {
        return reportExportService.buildReport(period)
    }
}

data class ShiftCounts(
    val totalDone: Int,
    val passCount: Int,
    val failCount: Int,
)

@Singleton
class ShiftCountsCalculator @Inject constructor(
    private val reportProvider: ReportProvider,
) {
    suspend operator fun invoke(period: ReportPeriod): ShiftCounts {
        val report = reportProvider.buildReport(period)
        val terminalResults = report.qcResults
            .groupBy { it.workItemId }
            .mapNotNull { (_, results) ->
                results.maxWithOrNull(compareBy<QcResult> { it.timestamp }.thenBy { it.eventId })
            }
        val passCount = terminalResults.count { it.outcome == QcOutcome.PASSED }
        val failCount = terminalResults.count { it.outcome == QcOutcome.FAILED_REWORK }
        return ShiftCounts(
            totalDone = terminalResults.size,
            passCount = passCount,
            failCount = failCount,
        )
    }
}
