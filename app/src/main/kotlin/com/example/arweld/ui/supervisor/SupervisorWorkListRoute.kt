package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.feature.supervisor.ui.SupervisorWorkListScreen
import com.example.arweld.feature.supervisor.viewmodel.SupervisorWorkListViewModel

@Composable
fun SupervisorWorkListRoute(
    onNavigateBack: () -> Unit,
    onWorkItemClick: (String) -> Unit,
    viewModel: SupervisorWorkListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SupervisorWorkListScreen(
        state = uiState,
        onSearchChange = viewModel::updateSearchQuery,
        onStatusChange = viewModel::updateStatus,
        onZoneChange = viewModel::updateZone,
        onAssigneeChange = viewModel::updateAssignee,
        onDateRangeChange = viewModel::updateDateRange,
        onClearFilters = viewModel::clearFilters,
        onWorkItemClick = onWorkItemClick,
        onRefresh = viewModel::loadWorkList,
        onNavigateBack = onNavigateBack,
    )
}
