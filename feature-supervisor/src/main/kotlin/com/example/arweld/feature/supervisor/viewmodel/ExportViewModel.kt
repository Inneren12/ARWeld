package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.usecase.ExportOptions
import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import com.example.arweld.feature.supervisor.usecase.ExportReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ExportUiState(
    val isExporting: Boolean = false,
    val error: String? = null,
    val lastExportPath: String? = null,
    val selectedPeriod: ExportPeriod? = null,
    val options: ExportOptions = ExportOptions(),
    val availablePeriods: List<ExportPeriod> = emptyList(),
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val exportReportUseCase: ExportReportUseCase,
    private val timeProvider: TimeProvider,
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

    companion object {
        private const val SHIFT_DURATION_MILLIS = 8 * 60 * 60 * 1000L
    }
}
