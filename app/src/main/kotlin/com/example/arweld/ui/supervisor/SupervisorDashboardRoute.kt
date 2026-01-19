package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.ui.SupervisorDashboardScreen
import com.example.arweld.feature.supervisor.viewmodel.SupervisorDashboardViewModel

@Composable
fun SupervisorDashboardRoute(
    onWorkItemClick: (String) -> Unit,
    onKpiClick: (WorkStatus?) -> Unit,
    onWorkListClick: () -> Unit,
    viewModel: SupervisorDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SupervisorDashboardScreen(
        kpis = uiState.kpis,
        bottleneckItems = uiState.bottleneckItems,
        userActivities = uiState.userActivities,
        bottleneckThresholdMs = uiState.bottleneckThresholdMs,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onRefresh = { viewModel.loadDashboard() },
        onBottleneckThresholdChange = { threshold ->
            viewModel.setBottleneckThreshold(threshold)
        },
        onWorkItemClick = onWorkItemClick,
        onKpiClick = onKpiClick,
        onWorkListClick = onWorkListClick
    )
}
