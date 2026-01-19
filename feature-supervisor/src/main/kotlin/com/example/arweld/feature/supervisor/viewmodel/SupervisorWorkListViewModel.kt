package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.feature.supervisor.model.SupervisorWorkItem
import com.example.arweld.feature.supervisor.model.WorkListAssignee
import com.example.arweld.feature.supervisor.usecase.GetSupervisorWorkListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SupervisorWorkListViewModel @Inject constructor(
    private val getSupervisorWorkListUseCase: GetSupervisorWorkListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkListUiState())
    val uiState: StateFlow<WorkListUiState> = _uiState.asStateFlow()

    init {
        loadWorkList()
    }

    fun loadWorkList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val items = getSupervisorWorkListUseCase()
                val availableZones = items.mapNotNull { it.zoneId }.distinct().sorted()
                val availableAssignees = items
                    .filter { it.assigneeId != null }
                    .distinctBy { it.assigneeId }
                    .mapNotNull { item ->
                        val assigneeId = item.assigneeId ?: return@mapNotNull null
                        WorkListAssignee(assigneeId, item.assigneeName ?: assigneeId)
                    }
                    .sortedBy { it.name }

                val filters = _uiState.value.filters
                val filteredItems = applyWorkListFilters(items, filters)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = items,
                        filteredItems = filteredItems,
                        availableZones = availableZones,
                        availableAssignees = availableAssignees,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unable to load work list")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) = updateDraftFilters { it.copy(searchQuery = query) }

    fun updateStatus(status: WorkStatus?) = updateDraftFilters { it.copy(status = status) }

    fun updateZone(zoneId: String?) = updateDraftFilters { it.copy(zoneId = zoneId) }

    fun updateAssignee(assigneeId: String?) = updateDraftFilters { it.copy(assigneeId = assigneeId) }

    fun updateDateRange(dateRange: WorkListDateRange) = updateDraftFilters { it.copy(dateRange = dateRange) }

    fun updateSortOrder(sortOrder: WorkListSortOrder) = updateDraftFilters { it.copy(sortOrder = sortOrder) }

    fun applyFilters() {
        _uiState.update { current ->
            val appliedFilters = current.draftFilters
            current.copy(
                filters = appliedFilters,
                filteredItems = applyWorkListFilters(current.items, appliedFilters),
            )
        }
    }

    fun resetFilters() {
        _uiState.update { current ->
            val reset = WorkListFilters()
            current.copy(
                filters = reset,
                draftFilters = reset,
                filteredItems = applyWorkListFilters(current.items, reset),
            )
        }
    }

    private fun updateDraftFilters(update: (WorkListFilters) -> WorkListFilters) {
        _uiState.update { current ->
            current.copy(draftFilters = update(current.draftFilters))
        }
    }
}

data class WorkListUiState(
    val isLoading: Boolean = false,
    val items: List<SupervisorWorkItem> = emptyList(),
    val filteredItems: List<SupervisorWorkItem> = emptyList(),
    val filters: WorkListFilters = WorkListFilters(),
    val draftFilters: WorkListFilters = WorkListFilters(),
    val availableZones: List<String> = emptyList(),
    val availableAssignees: List<WorkListAssignee> = emptyList(),
    val error: String? = null,
)

data class WorkListFilters(
    val searchQuery: String = "",
    val status: WorkStatus? = null,
    val zoneId: String? = null,
    val assigneeId: String? = null,
    val dateRange: WorkListDateRange = WorkListDateRange.ALL,
    val sortOrder: WorkListSortOrder = WorkListSortOrder.LAST_CHANGED_DESC,
)

enum class WorkListDateRange(val label: String, val durationMs: Long?) {
    ALL("All", null),
    LAST_24_HOURS("Last 24h", TimeUnit.HOURS.toMillis(24)),
    LAST_7_DAYS("Last 7d", TimeUnit.DAYS.toMillis(7)),
    LAST_30_DAYS("Last 30d", TimeUnit.DAYS.toMillis(30)),
}

enum class WorkListSortOrder(val label: String) {
    LAST_CHANGED_DESC("Last change (newest)"),
    LAST_CHANGED_ASC("Last change (oldest)"),
}

internal fun applyWorkListFilters(
    items: List<SupervisorWorkItem>,
    filters: WorkListFilters,
    nowMs: Long = System.currentTimeMillis(),
): List<SupervisorWorkItem> {
    val normalizedQuery = filters.searchQuery.trim().lowercase()
    val threshold = filters.dateRange.durationMs?.let { nowMs - it }

    val comparator = when (filters.sortOrder) {
        WorkListSortOrder.LAST_CHANGED_DESC -> compareByDescending<SupervisorWorkItem> { it.lastChangedAt }
        WorkListSortOrder.LAST_CHANGED_ASC -> compareBy<SupervisorWorkItem> { it.lastChangedAt }
    }.thenBy { it.code }.thenBy { it.workItemId }

    return items.asSequence()
        .filter { item ->
            if (filters.status != null && item.status != filters.status) return@filter false
            if (filters.zoneId != null && item.zoneId != filters.zoneId) return@filter false
            if (filters.assigneeId != null && item.assigneeId != filters.assigneeId) return@filter false
            if (threshold != null && item.lastChangedAt < threshold) return@filter false

            if (normalizedQuery.isBlank()) return@filter true

            val haystack = listOf(
                item.code,
                item.description,
                item.workItemId,
                item.assigneeName ?: "",
                item.zoneId ?: "",
            ).joinToString(" ").lowercase()

            haystack.contains(normalizedQuery)
        }
        .sortedWith(comparator)
        .toList()
}
