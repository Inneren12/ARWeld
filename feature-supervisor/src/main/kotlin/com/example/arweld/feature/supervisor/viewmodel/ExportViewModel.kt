package com.example.arweld.feature.supervisor.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.data.reporting.ExportedFileReference
import com.example.arweld.core.data.reporting.ManifestBuilder
import com.example.arweld.core.data.reporting.ManifestJsonWriter
import com.example.arweld.core.data.reporting.ReportExportService
import com.example.arweld.core.domain.diagnostics.DiagnosticsExportService
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.reporting.ExportManifestV1Json
import com.example.arweld.core.domain.reporting.ExportResult
import com.example.arweld.core.domain.reporting.ReportPeriod
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.usecase.EvidenceZipExportUseCase
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import java.lang.IllegalStateException
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportPreviewCounts(
    val workItems: Int,
    val events: Int,
    val evidence: Int,
)

enum class ExportPeriodType {
    SHIFT,
    DAY,
}

enum class ShiftSelection {
    CURRENT,
    PREVIOUS,
}

enum class BannerType {
    SUCCESS,
    ERROR,
}

data class ExportUiState(
    val isPreviewLoading: Boolean = false,
    val isDiagnosticsExporting: Boolean = false,
    val isReportJsonExporting: Boolean = false,
    val isReportCsvExporting: Boolean = false,
    val isEvidenceZipExporting: Boolean = false,
    val isManifestExporting: Boolean = false,
    val includeEvidenceZip: Boolean = true,
    val error: String? = null,
    val diagnosticsError: String? = null,
    val reportJsonError: String? = null,
    val reportCsvError: String? = null,
    val evidenceZipError: String? = null,
    val manifestError: String? = null,
    val previewError: String? = null,
    val bannerMessage: String? = null,
    val bannerType: BannerType = BannerType.SUCCESS,
    val lastExportPath: String? = null,
    val lastDiagnosticsPath: String? = null,
    val lastReportJsonMessage: String? = null,
    val lastReportCsvMessage: String? = null,
    val lastEvidenceZipPath: String? = null,
    val lastManifestMessage: String? = null,
    val evidenceZipMissingCount: Int? = null,
    val selectedPeriod: ExportPeriod? = null,
    val previewCounts: ExportPreviewCounts? = null,
    val periodType: ExportPeriodType = ExportPeriodType.SHIFT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedShift: ShiftSelection = ShiftSelection.CURRENT,
    val currentShiftLabel: String = "",
    val previousShiftLabel: String = "",
    val availablePeriods: List<ExportPeriod> = emptyList(),
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val evidenceZipExportUseCase: EvidenceZipExportUseCase,
    private val diagnosticsExportService: DiagnosticsExportService,
    private val reportExportService: ReportExportService,
    private val manifestBuilder: ManifestBuilder,
    private val manifestJsonWriter: ManifestJsonWriter,
    private val timeProvider: TimeProvider,
    private val appLogger: AppLogger,
    private val evidenceDao: EvidenceDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()
    private var lastReportJsonReference: ExportedFileReference? = null
    private var lastReportCsvReference: ExportedFileReference? = null
    private var lastEvidenceZipReference: ExportedFileReference? = null

    init {
        refreshPeriods()
    }

    fun refreshPeriods() {
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now(zoneId)
        val now = timeProvider.nowMillis()
        val shiftLabels = buildShiftLabels(now, zoneId)
        val selectedPeriod = buildPeriod(
            periodType = _uiState.value.periodType,
            selectedDate = today,
            shiftSelection = _uiState.value.selectedShift,
            nowMillis = now,
            zoneId = zoneId,
        )
        _uiState.value = _uiState.value.copy(
            selectedDate = today,
            currentShiftLabel = shiftLabels.current,
            previousShiftLabel = shiftLabels.previous,
            selectedPeriod = selectedPeriod,
        )
        loadPreviewCounts(selectedPeriod)
    }

    fun selectPeriodType(type: ExportPeriodType) {
        updatePeriodSelection(periodType = type)
    }

    fun selectShift(selection: ShiftSelection) {
        updatePeriodSelection(shiftSelection = selection)
    }

    fun selectDate(date: LocalDate) {
        updatePeriodSelection(selectedDate = date)
    }

    fun toggleEvidenceZip(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(includeEvidenceZip = enabled)
    }

    fun exportDiagnostics(outputRoot: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDiagnosticsExporting = true,
                diagnosticsError = null,
                lastDiagnosticsPath = null,
                bannerMessage = null,
            )
            try {
                val result = diagnosticsExportService.exportDiagnostics(outputRoot)
                _uiState.value = _uiState.value.copy(
                    isDiagnosticsExporting = false,
                    lastDiagnosticsPath = result.zipFile.absolutePath,
                    bannerMessage = "Diagnostics export complete.",
                    bannerType = BannerType.SUCCESS,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDiagnosticsExporting = false,
                    diagnosticsError = e.message ?: "Diagnostics export failed",
                    bannerMessage = e.message ?: "Diagnostics export failed",
                    bannerType = BannerType.ERROR,
                )
            }
        }
    }

    fun exportEvidenceZip(outputRoot: File) {
        val period = _uiState.value.selectedPeriod
        if (period == null) {
            _uiState.value = _uiState.value.copy(evidenceZipError = "Select a period first.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isEvidenceZipExporting = true,
                evidenceZipError = null,
                lastEvidenceZipPath = null,
                evidenceZipMissingCount = null,
                bannerMessage = null,
            )
            try {
                val result = evidenceZipExportUseCase(period, outputRoot)
                _uiState.value = _uiState.value.copy(
                    isEvidenceZipExporting = false,
                    lastEvidenceZipPath = result.zipFile.absolutePath,
                    evidenceZipMissingCount = result.missingFiles.size,
                    bannerMessage = "Evidence ZIP export complete.",
                    bannerType = BannerType.SUCCESS,
                )
                lastEvidenceZipReference = ExportedFileReference(file = result.zipFile)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isEvidenceZipExporting = false,
                    evidenceZipError = e.message ?: "Evidence zip export failed",
                    bannerMessage = e.message ?: "Evidence zip export failed",
                    bannerType = BannerType.ERROR,
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
                bannerMessage = null,
            )
            try {
                val report = reportExportService.buildReport(reportPeriod)
                when (val result = reportExportService.writeReportJson(uri, report)) {
                    is ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isReportJsonExporting = false,
                            lastReportJsonMessage = "Report JSON saved (${result.bytesWritten} bytes).",
                            bannerMessage = "Report JSON exported.",
                            bannerType = BannerType.SUCCESS,
                        )
                        lastReportJsonReference = ExportedFileReference(uri = uri)
                    }
                    is ExportResult.Failure -> {
                        appLogger.logRepositoryError(REPORT_JSON_OPERATION, result.throwable ?: Exception(result.message))
                        _uiState.value = _uiState.value.copy(
                            isReportJsonExporting = false,
                            reportJsonError = buildReportJsonError(result.throwable),
                            bannerMessage = buildReportJsonError(result.throwable),
                            bannerType = BannerType.ERROR,
                        )
                    }
                }
            } catch (e: Exception) {
                appLogger.logRepositoryError(REPORT_JSON_OPERATION, e)
                _uiState.value = _uiState.value.copy(
                    isReportJsonExporting = false,
                    reportJsonError = buildReportJsonError(e),
                    bannerMessage = buildReportJsonError(e),
                    bannerType = BannerType.ERROR,
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
                bannerMessage = null,
            )
            try {
                val report = reportExportService.buildReport(reportPeriod)
                when (val result = reportExportService.writeSummaryCsv(uri, report)) {
                    is ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isReportCsvExporting = false,
                            lastReportCsvMessage = "Summary CSV saved (${result.bytesWritten} bytes).",
                            bannerMessage = "Summary CSV exported.",
                            bannerType = BannerType.SUCCESS,
                        )
                        lastReportCsvReference = ExportedFileReference(uri = uri)
                    }
                    is ExportResult.Failure -> {
                        appLogger.logRepositoryError(REPORT_CSV_OPERATION, result.throwable ?: Exception(result.message))
                        _uiState.value = _uiState.value.copy(
                            isReportCsvExporting = false,
                            reportCsvError = buildReportCsvError(result.throwable),
                            bannerMessage = buildReportCsvError(result.throwable),
                            bannerType = BannerType.ERROR,
                        )
                    }
                }
            } catch (e: Exception) {
                appLogger.logRepositoryError(REPORT_CSV_OPERATION, e)
                _uiState.value = _uiState.value.copy(
                    isReportCsvExporting = false,
                    reportCsvError = buildReportCsvError(e),
                    bannerMessage = buildReportCsvError(e),
                    bannerType = BannerType.ERROR,
                )
            }
        }
    }

    fun exportManifest(uri: Uri?) {
        val period = _uiState.value.selectedPeriod
        val reportPeriod = period?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())

        if (uri == null) {
            val exception = IllegalStateException("No destination Uri selected for manifest export")
            appLogger.logRepositoryError(MANIFEST_OPERATION, exception)
            _uiState.value = _uiState.value.copy(manifestError = "Export canceled. No file selected.")
            return
        }

        val exportedFiles = listOfNotNull(
            lastReportJsonReference,
            lastReportCsvReference,
            lastEvidenceZipReference,
        )

        if (exportedFiles.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                manifestError = "No exported files available. Export JSON, CSV, or evidence zip first.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isManifestExporting = true,
                manifestError = null,
                lastManifestMessage = null,
                bannerMessage = null,
            )
            try {
                val warnings = buildManifestWarnings()
                val manifest = manifestBuilder.build(reportPeriod, exportedFiles, warnings)
                val json = ExportManifestV1Json.encode(manifest)
                val bytes = manifestJsonWriter.writeJson(uri, json)
                _uiState.value = _uiState.value.copy(
                    isManifestExporting = false,
                    lastManifestMessage = "Manifest saved (${bytes} bytes).",
                    bannerMessage = "Manifest exported.",
                    bannerType = BannerType.SUCCESS,
                )
            } catch (e: Exception) {
                appLogger.logRepositoryError(MANIFEST_OPERATION, e)
                _uiState.value = _uiState.value.copy(
                    isManifestExporting = false,
                    manifestError = buildManifestError(e),
                    bannerMessage = buildManifestError(e),
                    bannerType = BannerType.ERROR,
                )
            }
        }
    }

    fun suggestedReportFileName(): String {
        val period = _uiState.value.selectedPeriod?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return "arweld-report-v1-${REPORT_DATE_FORMAT.format(date)}.json"
    }

    fun suggestedSummaryCsvFileName(): String {
        val period = _uiState.value.selectedPeriod?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return "arweld-summary-v1-${REPORT_DATE_FORMAT.format(date)}.csv"
    }

    fun suggestedManifestFileName(): String {
        val period = _uiState.value.selectedPeriod?.toReportPeriod()
            ?: ReportPeriod(startMillis = timeProvider.nowMillis(), endMillis = timeProvider.nowMillis())
        val date = Instant.ofEpochMilli(period.endMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return "arweld-manifest-v1-${REPORT_DATE_FORMAT.format(date)}.json"
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

    private fun buildManifestError(throwable: Throwable?): String {
        return when (throwable) {
            is SecurityException -> "Permission denied. Choose a different location and try again."
            else -> "Unable to export manifest. Please try again."
        }
    }

    private fun buildManifestWarnings(): List<String> {
        val warnings = mutableListOf<String>()
        if (lastReportJsonReference == null) {
            warnings.add("Report JSON not exported.")
        }
        if (lastReportCsvReference == null) {
            warnings.add("Summary CSV not exported.")
        }
        if (lastEvidenceZipReference == null) {
            warnings.add("Evidence zip not exported.")
        }
        return warnings
    }

    private fun updatePeriodSelection(
        periodType: ExportPeriodType = _uiState.value.periodType,
        selectedDate: LocalDate = _uiState.value.selectedDate,
        shiftSelection: ShiftSelection = _uiState.value.selectedShift,
    ) {
        val zoneId = ZoneId.systemDefault()
        val now = timeProvider.nowMillis()
        val shiftLabels = buildShiftLabels(now, zoneId)
        val period = buildPeriod(
            periodType = periodType,
            selectedDate = selectedDate,
            shiftSelection = shiftSelection,
            nowMillis = now,
            zoneId = zoneId,
        )
        _uiState.value = _uiState.value.copy(
            periodType = periodType,
            selectedDate = selectedDate,
            selectedShift = shiftSelection,
            currentShiftLabel = shiftLabels.current,
            previousShiftLabel = shiftLabels.previous,
            selectedPeriod = period,
        )
        loadPreviewCounts(period)
    }

    private fun buildPeriod(
        periodType: ExportPeriodType,
        selectedDate: LocalDate,
        shiftSelection: ShiftSelection,
        nowMillis: Long,
        zoneId: ZoneId,
    ): ExportPeriod {
        return when (periodType) {
            ExportPeriodType.DAY -> buildDayPeriod(selectedDate, zoneId)
            ExportPeriodType.SHIFT -> buildShiftPeriod(nowMillis, shiftSelection, zoneId)
        }
    }

    private fun buildDayPeriod(date: LocalDate, zoneId: ZoneId): ExportPeriod {
        val start = date.atStartOfDay(zoneId).toInstant()
        val end = date.plusDays(1).atStartOfDay(zoneId).toInstant().minusMillis(1)
        val label = "Day · ${DAY_LABEL_FORMAT.format(date)}"
        return ExportPeriod(
            startMillis = start.toEpochMilli(),
            endMillis = end.toEpochMilli(),
            label = label,
        )
    }

    private fun buildShiftPeriod(nowMillis: Long, selection: ShiftSelection, zoneId: ZoneId): ExportPeriod {
        val nowZoned = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val shift = resolveCurrentShift(nowZoned)
        val shiftStart = when (selection) {
            ShiftSelection.CURRENT -> shift.start
            ShiftSelection.PREVIOUS -> shift.start.minusHours(SHIFT_DURATION_HOURS)
        }
        val shiftName = when (selection) {
            ShiftSelection.CURRENT -> shift.name
            ShiftSelection.PREVIOUS -> if (shift.name == SHIFT_A_NAME) SHIFT_B_NAME else SHIFT_A_NAME
        }
        val shiftEnd = shiftStart.plusHours(SHIFT_DURATION_HOURS).minusMillis(1)
        val label = "${shiftName} · ${SHIFT_TIME_FORMAT.format(shiftStart)}–${SHIFT_TIME_FORMAT.format(shiftEnd)}"
        return ExportPeriod(
            startMillis = shiftStart.toInstant().toEpochMilli(),
            endMillis = shiftEnd.toInstant().toEpochMilli(),
            label = label,
        )
    }

    private fun resolveCurrentShift(now: java.time.ZonedDateTime): ShiftWindow {
        val localTime = now.toLocalTime()
        return if (localTime >= SHIFT_A_START && localTime < SHIFT_A_END) {
            val start = now.toLocalDate().atTime(SHIFT_A_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_A_NAME, start = start)
        } else if (localTime >= SHIFT_B_START) {
            val start = now.toLocalDate().atTime(SHIFT_B_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_B_NAME, start = start)
        } else {
            val start = now.toLocalDate().minusDays(1).atTime(SHIFT_B_START).atZone(now.zone)
            ShiftWindow(name = SHIFT_B_NAME, start = start)
        }
    }

    private fun buildShiftLabels(nowMillis: Long, zoneId: ZoneId): ShiftLabels {
        val now = Instant.ofEpochMilli(nowMillis).atZone(zoneId)
        val shift = resolveCurrentShift(now)
        val currentLabel = "Current ${shift.name} · ${SHIFT_TIME_FORMAT.format(shift.start)}–" +
            SHIFT_TIME_FORMAT.format(shift.start.plusHours(SHIFT_DURATION_HOURS).minusMillis(1))
        val previousStart = shift.start.minusHours(SHIFT_DURATION_HOURS)
        val previousName = if (shift.name == SHIFT_A_NAME) SHIFT_B_NAME else SHIFT_A_NAME
        val previousLabel = "Previous ${previousName} · ${SHIFT_TIME_FORMAT.format(previousStart)}–" +
            SHIFT_TIME_FORMAT.format(previousStart.plusHours(SHIFT_DURATION_HOURS).minusMillis(1))
        return ShiftLabels(current = currentLabel, previous = previousLabel)
    }

    private fun loadPreviewCounts(period: ExportPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPreviewLoading = true, previewError = null)
            try {
                val report = reportExportService.buildReport(period.toReportPeriod())
                val eventIds = report.events.map { it.id }
                val evidenceCount = if (eventIds.isEmpty()) {
                    0
                } else {
                    eventIds.chunked(EVIDENCE_CHUNK_SIZE).sumOf { chunk -> evidenceDao.listByEvents(chunk).size }
                }
                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    previewCounts = ExportPreviewCounts(
                        workItems = report.workItems.size,
                        events = report.events.size,
                        evidence = evidenceCount,
                    ),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPreviewLoading = false,
                    previewError = "Unable to load preview counts.",
                )
            }
        }
    }

    companion object {
        private const val SHIFT_DURATION_HOURS = 12L
        private val SHIFT_A_START: LocalTime = LocalTime.of(6, 0)
        private val SHIFT_A_END: LocalTime = LocalTime.of(18, 0)
        private val SHIFT_B_START: LocalTime = LocalTime.of(18, 0)
        private const val SHIFT_A_NAME = "Shift A"
        private const val SHIFT_B_NAME = "Shift B"
        private const val EVIDENCE_CHUNK_SIZE = 900
        private const val REPORT_JSON_OPERATION = "report_json_export"
        private const val REPORT_CSV_OPERATION = "report_csv_export"
        private const val MANIFEST_OPERATION = "manifest_export"
        private val REPORT_DATE_FORMAT = DateTimeFormatter.ISO_DATE
        private val DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy")
        private val SHIFT_TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d HH:mm")
    }
}

private data class ShiftWindow(
    val name: String,
    val start: java.time.ZonedDateTime,
)

private data class ShiftLabels(
    val current: String,
    val previous: String,
)
