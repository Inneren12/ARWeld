package com.example.arweld.feature.supervisor.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.model.SupervisorWorkItem
import com.example.arweld.feature.supervisor.model.WorkListAssignee
import com.example.arweld.feature.supervisor.viewmodel.WorkListDateRange
import com.example.arweld.feature.supervisor.viewmodel.WorkListFilters
import com.example.arweld.feature.supervisor.viewmodel.WorkListSortOrder
import com.example.arweld.feature.supervisor.viewmodel.WorkListUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorWorkListScreen(
    state: WorkListUiState,
    onSearchChange: (String) -> Unit,
    onStatusChange: (WorkStatus?) -> Unit,
    onZoneChange: (String?) -> Unit,
    onAssigneeChange: (String?) -> Unit,
    onDateRangeChange: (WorkListDateRange) -> Unit,
    onSortOrderChange: (WorkListSortOrder) -> Unit,
    onApplyFilters: () -> Unit,
    onResetFilters: () -> Unit,
    onWorkItemClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supervisor Work List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (state.error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = state.error,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = onRefresh) {
                    Text("Retry")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                FilterHeader(
                    draftFilters = state.draftFilters,
                    appliedFilters = state.filters,
                    availableZones = state.availableZones,
                    availableAssignees = state.availableAssignees,
                    searchError = state.searchError,
                    onSearchChange = onSearchChange,
                    onStatusChange = onStatusChange,
                    onZoneChange = onZoneChange,
                    onAssigneeChange = onAssigneeChange,
                    onDateRangeChange = onDateRangeChange,
                    onSortOrderChange = onSortOrderChange,
                    onApplyFilters = onApplyFilters,
                    onResetFilters = onResetFilters,
                )
            }

            if (state.filteredItems.isEmpty()) {
                item {
                    val emptyMessage = when {
                        state.isLoading -> "Loading work list..."
                        state.searchError != null -> state.searchError
                        state.filters.searchQuery.isNotBlank() ->
                            "No work items found for that code or ID."
                        else -> "No work items match the filters."
                    }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = emptyMessage,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            } else {
                items(state.filteredItems) { item ->
                    WorkListItemCard(item = item, onWorkItemClick = onWorkItemClick)
                }
            }
        }
    }
}

@Composable
private fun FilterHeader(
    draftFilters: WorkListFilters,
    appliedFilters: WorkListFilters,
    availableZones: List<String>,
    availableAssignees: List<WorkListAssignee>,
    searchError: String?,
    onSearchChange: (String) -> Unit,
    onStatusChange: (WorkStatus?) -> Unit,
    onZoneChange: (String?) -> Unit,
    onAssigneeChange: (String?) -> Unit,
    onDateRangeChange: (WorkListDateRange) -> Unit,
    onSortOrderChange: (WorkListSortOrder) -> Unit,
    onApplyFilters: () -> Unit,
    onResetFilters: () -> Unit,
) {
    val hasPendingChanges = draftFilters != appliedFilters

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = draftFilters.searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search by code or work item ID") },
            supportingText = {
                Text(searchError ?: "Use codes like ARWELD-W-001 or internal IDs.")
            },
            singleLine = true,
            isError = searchError != null,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMenu(
                label = "Status",
                current = draftFilters.status?.displayLabel() ?: "All",
                options = listOf("All") + WorkStatus.values().map { it.displayLabel() },
                onSelected = { selected ->
                    val status = WorkStatus.values().firstOrNull { it.displayLabel() == selected }
                    onStatusChange(status)
                },
                modifier = Modifier.weight(1f)
            )

            FilterMenu(
                label = "Zone",
                current = draftFilters.zoneId ?: "All",
                options = listOf("All") + availableZones,
                onSelected = { selected ->
                    onZoneChange(selected.takeIf { it != "All" })
                },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterMenu(
                label = "Assignee",
                current = draftFilters.assigneeId?.let { id ->
                    availableAssignees.firstOrNull { it.id == id }?.name ?: id
                } ?: "All",
                options = listOf("All") + availableAssignees.map { it.name },
                onSelected = { selected ->
                    val assignee = availableAssignees.firstOrNull { it.name == selected }
                    onAssigneeChange(assignee?.id)
                },
                modifier = Modifier.weight(1f)
            )

            FilterMenu(
                label = "Date",
                current = draftFilters.dateRange.label,
                options = WorkListDateRange.values().map { it.label },
                onSelected = { selected ->
                    val range = WorkListDateRange.values().first { it.label == selected }
                    onDateRangeChange(range)
                },
                modifier = Modifier.weight(1f)
            )
        }

        FilterMenu(
            label = "Sort",
            current = draftFilters.sortOrder.label,
            options = WorkListSortOrder.values().map { it.label },
            onSelected = { selected ->
                val order = WorkListSortOrder.values().first { it.label == selected }
                onSortOrderChange(order)
            },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Applied: ${appliedFilters.sortOrder.label}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onResetFilters) {
                    Text("Reset")
                }
                Button(
                    onClick = onApplyFilters,
                    enabled = hasPendingChanges
                ) {
                    Text(if (hasPendingChanges) "Apply" else "Applied")
                }
            }
        }
    }
}

@Composable
private fun WorkListItemCard(
    item: SupervisorWorkItem,
    onWorkItemClick: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWorkItemClick(item.workItemId) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.code.ifBlank { item.workItemId },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = item.status.displayLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Zone: ${item.zoneId ?: "-"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Assignee: ${item.assigneeName ?: "Unassigned"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last change: ${formatTimestamp(item.lastChangedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

private fun WorkStatus.displayLabel(): String {
    return name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercaseChar() }
}
