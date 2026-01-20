package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import com.example.arweld.feature.supervisor.usecase.ExportPeriod
import com.example.arweld.feature.supervisor.usecase.GetReportsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val shiftCounts: List<ShiftReportSummary> = emptyList(),
    val topFailReasons: List<FailReasonSummary> = emptyList(),
    val problematicNodes: List<NodeIssueSummary> = emptyList(),
    val availableZones: List<String> = emptyList(),
    val selectedZoneId: String? = null,
    val selectedPeriod: ExportPeriod? = null,
    val periodType: ExportPeriodType = ExportPeriodType.SHIFT,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedShift: ShiftSelection = ShiftSelection.CURRENT,
    val currentShiftLabel: String = "",
    val previousShiftLabel: String = "",
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState(isLoading = true))
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        refreshPeriod()
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

    fun selectZone(zoneId: String?) {
        _uiState.value = _uiState.value.copy(selectedZoneId = zoneId)
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            val state = _uiState.value
            val period = state.selectedPeriod ?: buildDefaultPeriod(state)
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, selectedPeriod = period)
            try {
                val snapshot = getReportsUseCase(period, state.selectedZoneId)
                val selectedZone = state.selectedZoneId?.takeIf { it in snapshot.availableZones }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shiftCounts = snapshot.shiftCounts,
                    topFailReasons = snapshot.topFailReasons.take(MAX_LIST_ENTRIES),
                    problematicNodes = snapshot.problematicNodes.take(MAX_LIST_ENTRIES),
                    availableZones = snapshot.availableZones,
                    selectedZoneId = selectedZone,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reports",
                )
            }
        }
    }

    private fun refreshPeriod() {
        val zoneId = ZoneId.systemDefault()
        val now = timeProvider.nowMillis()
        val today = LocalDate.now(zoneId)
        val shiftLabels = PeriodSelectionHelper.buildShiftLabels(now, zoneId)
        val period = PeriodSelectionHelper.buildPeriod(
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
            selectedPeriod = period,
        )
        loadReports()
    }

    private fun updatePeriodSelection(
        periodType: ExportPeriodType = _uiState.value.periodType,
        selectedDate: LocalDate = _uiState.value.selectedDate,
        shiftSelection: ShiftSelection = _uiState.value.selectedShift,
    ) {
        val zoneId = ZoneId.systemDefault()
        val now = timeProvider.nowMillis()
        val shiftLabels = PeriodSelectionHelper.buildShiftLabels(now, zoneId)
        val period = PeriodSelectionHelper.buildPeriod(
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
        loadReports()
    }

    private fun buildDefaultPeriod(state: ReportsUiState): ExportPeriod {
        val zoneId = ZoneId.systemDefault()
        val now = timeProvider.nowMillis()
        return PeriodSelectionHelper.buildPeriod(
            periodType = state.periodType,
            selectedDate = state.selectedDate,
            shiftSelection = state.selectedShift,
            nowMillis = now,
            zoneId = zoneId,
        )
    }

    private companion object {
        private const val MAX_LIST_ENTRIES = 5
    }
}
