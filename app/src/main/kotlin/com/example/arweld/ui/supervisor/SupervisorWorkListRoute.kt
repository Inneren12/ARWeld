package com.example.arweld.ui.supervisor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.ui.SupervisorWorkListScreen
import com.example.arweld.feature.supervisor.viewmodel.SupervisorWorkListViewModel
import kotlinx.coroutines.flow.collect

@Composable
fun SupervisorWorkListRoute(
    onNavigateBack: () -> Unit,
    onWorkItemClick: (String) -> Unit,
    initialStatus: WorkStatus? = null,
    viewModel: SupervisorWorkListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialStatus) {
        viewModel.applyInitialStatus(initialStatus)
    }

    LaunchedEffect(viewModel) {
        viewModel.navigationEvents.collect { workItemId ->
            onWorkItemClick(workItemId)
        }
    }

    SupervisorWorkListScreen(
        state = uiState,
        onSearchChange = viewModel::updateSearchQuery,
        onStatusChange = viewModel::updateStatus,
        onZoneChange = viewModel::updateZone,
        onAssigneeChange = viewModel::updateAssignee,
        onDateRangeChange = viewModel::updateDateRange,
        onSortOrderChange = viewModel::updateSortOrder,
        onApplyFilters = viewModel::applyFilters,
        onResetFilters = viewModel::resetFilters,
        onWorkItemClick = onWorkItemClick,
        onRefresh = viewModel::loadWorkList,
        onNavigateBack = onNavigateBack,
    )
}
