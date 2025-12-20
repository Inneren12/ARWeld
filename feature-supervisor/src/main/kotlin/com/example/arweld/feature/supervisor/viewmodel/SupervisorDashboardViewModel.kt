package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.feature.supervisor.model.BottleneckItem
import com.example.arweld.feature.supervisor.model.ShopKpis
import com.example.arweld.feature.supervisor.model.UserActivity
import com.example.arweld.feature.supervisor.usecase.CalculateKpisUseCase
import com.example.arweld.feature.supervisor.usecase.GetQcBottleneckUseCase
import com.example.arweld.feature.supervisor.usecase.GetUserActivityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Supervisor Dashboard.
 * Loads KPIs, QC bottleneck, and user activity from the event log.
 */
@HiltViewModel
class SupervisorDashboardViewModel @Inject constructor(
    private val calculateKpisUseCase: CalculateKpisUseCase,
    private val getQcBottleneckUseCase: GetQcBottleneckUseCase,
    private val getUserActivityUseCase: GetUserActivityUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val kpis = calculateKpisUseCase()
                val bottleneck = getQcBottleneckUseCase(_uiState.value.bottleneckThresholdMs)
                val userActivity = getUserActivityUseCase()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        kpis = kpis,
                        bottleneckItems = bottleneck,
                        userActivities = userActivity
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun setBottleneckThreshold(thresholdMs: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(bottleneckThresholdMs = thresholdMs) }

            try {
                val bottleneck = getQcBottleneckUseCase(thresholdMs)
                _uiState.update { it.copy(bottleneckItems = bottleneck) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Error updating bottleneck")
                }
            }
        }
    }
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val kpis: ShopKpis = ShopKpis(
        totalWorkItems = 0,
        inProgress = 0,
        readyForQc = 0,
        qcInProgress = 0,
        approved = 0,
        rework = 0,
        avgQcWaitTimeMs = 0L,
        qcPassRate = 0f
    ),
    val bottleneckItems: List<BottleneckItem> = emptyList(),
    val userActivities: List<UserActivity> = emptyList(),
    val bottleneckThresholdMs: Long = 0L, // Default: show all
    val error: String? = null
)
