package com.example.arweld.core.data.reporting

import android.net.Uri
import com.example.arweld.core.domain.reporting.ExportResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.reporting.ReportV1
import com.example.arweld.core.domain.reporting.ReportV1Json
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportExportService @Inject constructor(
    private val reportV1Builder: ReportV1Builder,
    private val reportJsonWriter: ReportJsonWriter,
    private val csvSummaryBuilder: CsvSummaryBuilder,
    private val csvWriter: CsvWriter,
    private val reportCsvWriter: ReportCsvWriter,
) {
    suspend fun buildReport(period: ReportPeriod): ReportV1 = withContext(Dispatchers.IO) {
        val report = reportV1Builder.build()
        val filteredEvents = report.events.filter { period.contains(it.timestamp) }
        val includedWorkItemIds = filteredEvents.map { it.workItemId }.toSet()
        report.copy(
            workItems = report.workItems.filter { includedWorkItemIds.contains(it.id) },
            events = filteredEvents,
            qcResults = report.qcResults.filter { period.contains(it.timestamp) },
        )
    }

    suspend fun writeReportJson(uri: Uri, report: ReportV1): ExportResult = withContext(Dispatchers.IO) {
        try {
            val json = ReportV1Json.encode(report)
            val bytes = reportJsonWriter.writeJson(uri, json)
            ExportResult.Success(bytesWritten = bytes)
        } catch (exception: Exception) {
            ExportResult.Failure(
                message = exception.message ?: "Unable to write report JSON",
                throwable = exception,
            )
        }
    }

    suspend fun writeSummaryCsv(uri: Uri, report: ReportV1): ExportResult = withContext(Dispatchers.IO) {
        try {
            val summary = csvSummaryBuilder.build(report)
            val csv = csvWriter.write(summary.header, summary.rows)
            val bytes = reportCsvWriter.writeCsv(uri, csv)
            ExportResult.Success(bytesWritten = bytes)
        } catch (exception: Exception) {
            ExportResult.Failure(
                message = exception.message ?: "Unable to write summary CSV",
                throwable = exception,
            )
        }
    }
}
