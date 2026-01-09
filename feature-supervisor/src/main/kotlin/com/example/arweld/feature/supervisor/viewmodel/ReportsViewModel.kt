package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.feature.supervisor.model.FailReasonSummary
import com.example.arweld.feature.supervisor.model.NodeIssueSummary
import com.example.arweld.feature.supervisor.model.ShiftReportSummary
import com.example.arweld.feature.supervisor.usecase.GetReportsUseCase
import com.example.arweld.feature.supervisor.usecase.ReportsPeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneOffset
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
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val getReportsUseCase: GetReportsUseCase,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState(isLoading = true))
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val now = timeProvider.nowMillis()
                val start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
                val snapshot = getReportsUseCase(ReportsPeriod(startMillis = start, endMillis = now))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    shiftCounts = snapshot.shiftCounts,
                    topFailReasons = snapshot.topFailReasons,
                    problematicNodes = snapshot.problematicNodes,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reports",
                )
            }
        }
    }
}
