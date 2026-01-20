package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.diagnostics.DiagnosticsExportService
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.reporting.ExportResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.core.data.reporting.ReportExportService
import com.example.arweld.feature.supervisor.usecase.ExportOptions
import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import com.example.arweld.feature.supervisor.usecase.ExportReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.IllegalStateException
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri

data class ExportUiState(
    val isExporting: Boolean = false,
    val isDiagnosticsExporting: Boolean = false,
    val isReportJsonExporting: Boolean = false,
    val isReportCsvExporting: Boolean = false,
    val error: String? = null,
    val diagnosticsError: String? = null,
    val reportJsonError: String? = null,
    val reportCsvError: String? = null,
    val lastExportPath: String? = null,
    val lastDiagnosticsPath: String? = null,
    val lastReportJsonMessage: String? = null,
    val lastReportCsvMessage: String? = null,
    val selectedPeriod: ExportPeriod? = null,
    val options: ExportOptions = ExportOptions(),
    val availablePeriods: List<ExportPeriod> = emptyList(),
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportReportUseCase: ExportReportUseCase,
    private val diagnosticsExportService: DiagnosticsExportService,
    private val reportExportService: ReportExportService,
    private val timeProvider: TimeProvider,
    private val appLogger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        refreshPeriods()
    }

    fun refreshPeriods() {
        val now = timeProvider.nowMillis()
        val todayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val shiftStart = now - SHIFT_DURATION_MILLIS
        val periods = listOf(
            ExportPeriod(startMillis = todayStart, endMillis = now, label = "Today"),
            ExportPeriod(startMillis = shiftStart, endMillis = now, label = "Last 8 hours"),
        )
        _uiState.value = _uiState.value.copy(
            availablePeriods = periods,
            selectedPeriod = periods.firstOrNull(),
        )
    }

    fun selectPeriod(period: ExportPeriod) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
    }

    fun toggleCsv(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(includeCsv = enabled))
    }

    fun toggleZip(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(includeZip = enabled))
    }

    fun toggleManifest(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(options = _uiState.value.options.copy(includeManifest = enabled))
    }

    fun export(outputRoot: File) {
        val period = _uiState.value.selectedPeriod ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true, error = null, lastExportPath = null)
            try {
                val result = exportReportUseCase(period, _uiState.value.options, outputRoot)
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    lastExportPath = result.exportDir.absolutePath,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = e.message ?: "Export failed",
                )
            }
        }
    }

    fun exportDiagnostics(outputRoot: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiagnosticsExporting = true,
                diagnosticsError = null,
                lastDiagnosticsPath = null,
            )
            try {
                val result = diagnosticsExportService.exportDiagnostics(outputRoot)
                _uiState.value = _uiState.value.copy(
                    isDiagnosticsExporting = false,
                    lastDiagnosticsPath = result.zipFile.absolutePath,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDiagnosticsExporting = false,
                    diagnosticsError = e.message ?: "Diagnostics export failed",
                )
            }
        }
    }

    fun exportReportJson(uri: Uri?) {
        val period = _uiState.value.selectedPeriod
        val reportPeriod = period?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())

        if (uri == null) {
            val exception = IllegalStateException("No destination Uri selected for report export")
            appLogger.logRepositoryError(REPORT_JSON_OPERATION, exception)
            _uiState.value = _uiState.value.copy(reportJsonError = "Export canceled. No file selected.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isReportJsonExporting = true,
                reportJsonError = null,
                lastReportJsonMessage = null,
            )
            try {
                val report = reportExportService.buildReport(reportPeriod)
                when (val result = reportExportService.writeReportJson(uri, report)) {
                    is ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isReportJsonExporting = false,
                            lastReportJsonMessage = "Report JSON saved (${result.bytesWritten} bytes).",
                        )
                    }
                    is ExportResult.Failure -> {
                        appLogger.logRepositoryError(REPORT_JSON_OPERATION, result.throwable ?: Exception(result.message))
                        _uiState.value = _uiState.value.copy(
                            isReportJsonExporting = false,
                            reportJsonError = buildReportJsonError(result.throwable),
                        )
                    }
                }
            } catch (e: Exception) {
                appLogger.logRepositoryError(REPORT_JSON_OPERATION, e)
                _uiState.value = _uiState.value.copy(
                    isReportJsonExporting = false,
                    reportJsonError = buildReportJsonError(e),
                )
            }
        }
    }

    fun exportReportCsv(uri: Uri?) {
        val period = _uiState.value.selectedPeriod
        val reportPeriod = period?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())

        if (uri == null) {
            val exception = IllegalStateException("No destination Uri selected for summary CSV export")
            appLogger.logRepositoryError(REPORT_CSV_OPERATION, exception)
            _uiState.value = _uiState.value.copy(reportCsvError = "Export canceled. No file selected.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isReportCsvExporting = true,
                reportCsvError = null,
                lastReportCsvMessage = null,
            )
            try {
                val report = reportExportService.buildReport(reportPeriod)
                when (val result = reportExportService.writeSummaryCsv(uri, report)) {
                    is ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isReportCsvExporting = false,
                            lastReportCsvMessage = "Summary CSV saved (${result.bytesWritten} bytes).",
                        )
                    }
                    is ExportResult.Failure -> {
                        appLogger.logRepositoryError(REPORT_CSV_OPERATION, result.throwable ?: Exception(result.message))
                        _uiState.value = _uiState.value.copy(
                            isReportCsvExporting = false,
                            reportCsvError = buildReportCsvError(result.throwable),
                        )
                    }
                }
            } catch (e: Exception) {
                appLogger.logRepositoryError(REPORT_CSV_OPERATION, e)
                _uiState.value = _uiState.value.copy(
                    isReportCsvExporting = false,
                    reportCsvError = buildReportCsvError(e),
                )
            }
        }
    }

    fun suggestedReportFileName(): String {
        val period = _uiState.value.selectedPeriod?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return "arweld-report-v1-${REPORT_DATE_FORMAT.format(date)}.json"
    }

    fun suggestedSummaryCsvFileName(): String {
        val period = _uiState.value.selectedPeriod?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
        return "arweld-summary-v1-${REPORT_DATE_FORMAT.format(date)}.csv"
    }

    private fun ExportPeriod.toReportPeriod(): ReportPeriod {
        return ReportPeriod(startMillis = startMillis, endMillis = endMillis)
    }

    private fun buildReportJsonError(throwable: Throwable?): String {
        return when (throwable) {
            is SecurityException -> "Permission denied. Choose a different location and try again."
            else -> "Unable to export report JSON. Please try again."
        }
    }

    private fun buildReportCsvError(throwable: Throwable?): String {
        return when (throwable) {
            is SecurityException -> "Permission denied. Choose a different location and try again."
            else -> "Unable to export summary CSV. Please try again."
        }
    }

    companion object {
        private const val SHIFT_DURATION_MILLIS = 8 * 60 * 60 * 1000L
        private const val REPORT_JSON_OPERATION = "report_json_export"
        private const val REPORT_CSV_OPERATION = "report_csv_export"
        private val REPORT_DATE_FORMAT = DateTimeFormatter.ISO_DATE.withZone(ZoneId.of("UTC"))
    }
}
