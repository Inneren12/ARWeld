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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SupervisorWorkListViewModel @Inject constructor(
    private val getSupervisorWorkListUseCase: GetSupervisorWorkListUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkListUiState())
    val uiState: StateFlow<WorkListUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigationEvents: SharedFlow<String> = _navigationEvents.asSharedFlow()

    private var searchJob: Job? = null
    private var hasAppliedInitialStatus = false

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
                val filterResult = applyFiltersWithValidation(items, filters)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = items,
                        filteredItems = filterResult.items,
                        availableZones = availableZones,
                        availableAssignees = availableAssignees,
                        searchError = filterResult.searchError,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Unable to load work list")
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        updateDraftFilters { it.copy(searchQuery = query) }
        _uiState.update { it.copy(searchError = searchErrorFor(query)) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            applySearchQuery(query)
        }
    }

    fun updateStatus(status: WorkStatus?) = updateDraftFilters { it.copy(status = status) }

    fun updateZone(zoneId: String?) = updateDraftFilters { it.copy(zoneId = zoneId) }

    fun updateAssignee(assigneeId: String?) = updateDraftFilters { it.copy(assigneeId = assigneeId) }

    fun updateDateRange(dateRange: WorkListDateRange) = updateDraftFilters { it.copy(dateRange = dateRange) }

    fun updateSortOrder(sortOrder: WorkListSortOrder) = updateDraftFilters { it.copy(sortOrder = sortOrder) }

    fun applyFilters() {
        _uiState.update { current ->
            val appliedFilters = current.draftFilters
            val filterResult = applyFiltersWithValidation(current.items, appliedFilters)
            current.copy(
                filters = appliedFilters,
                filteredItems = filterResult.items,
                searchError = filterResult.searchError,
            )
        }
    }

    fun resetFilters() {
        _uiState.update { current ->
            val reset = WorkListFilters()
            val filterResult = applyFiltersWithValidation(current.items, reset)
            current.copy(
                filters = reset,
                draftFilters = reset,
                filteredItems = filterResult.items,
                searchError = filterResult.searchError,
            )
        }
    }

    fun applyInitialStatus(status: WorkStatus?) {
        if (status == null || hasAppliedInitialStatus) return
        hasAppliedInitialStatus = true
        _uiState.update { current ->
            val updatedFilters = current.filters.copy(status = status)
            val updatedDraft = current.draftFilters.copy(status = status)
            val filterResult = applyFiltersWithValidation(current.items, updatedFilters)
            current.copy(
                filters = updatedFilters,
                draftFilters = updatedDraft,
                filteredItems = filterResult.items,
                searchError = filterResult.searchError,
            )
        }
    }

    private fun updateDraftFilters(update: (WorkListFilters) -> WorkListFilters) {
        _uiState.update { current ->
            current.copy(draftFilters = update(current.draftFilters))
        }
    }

    private fun applySearchQuery(query: String) {
        _uiState.update { current ->
            val updatedFilters = current.filters.copy(searchQuery = query)
            val filterResult = applyFiltersWithValidation(current.items, updatedFilters)
            current.copy(
                filters = updatedFilters,
                filteredItems = filterResult.items,
                searchError = filterResult.searchError,
            )
        }

        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank() || searchErrorFor(trimmedQuery) != null) return

        val match = _uiState.value.items.firstOrNull { item ->
            item.code.equals(trimmedQuery, ignoreCase = true) ||
                item.workItemId.equals(trimmedQuery, ignoreCase = true)
        }
        match?.let { _navigationEvents.tryEmit(it.workItemId) }
    }

    private fun applyFiltersWithValidation(
        items: List<SupervisorWorkItem>,
        filters: WorkListFilters,
    ): FilterResult {
        val error = searchErrorFor(filters.searchQuery)
        val filteredItems = if (error != null) {
            emptyList()
        } else {
            applyWorkListFilters(items, filters)
        }
        return FilterResult(filteredItems, error)
    }

    private fun searchErrorFor(query: String): String? {
        if (query.isBlank()) return null
        return if (SEARCH_QUERY_REGEX.matches(query.trim())) {
            null
        } else {
            "Enter a valid code or work item ID (letters, numbers, dashes)."
        }
    }
}

private data class FilterResult(
    val items: List<SupervisorWorkItem>,
    val searchError: String?
)

data class WorkListUiState(
    val isLoading: Boolean = false,
    val items: List<SupervisorWorkItem> = emptyList(),
    val filteredItems: List<SupervisorWorkItem> = emptyList(),
    val filters: WorkListFilters = WorkListFilters(),
    val draftFilters: WorkListFilters = WorkListFilters(),
    val availableZones: List<String> = emptyList(),
    val availableAssignees: List<WorkListAssignee> = emptyList(),
    val error: String? = null,
    val searchError: String? = null,
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

            val codeMatch = item.code.lowercase().contains(normalizedQuery)
            val idMatch = item.workItemId.lowercase().contains(normalizedQuery)
            codeMatch || idMatch
        }
        .sortedWith(comparator)
        .toList()
}

private const val SEARCH_DEBOUNCE_MS = 300L
private val SEARCH_QUERY_REGEX = Regex("^[A-Za-z0-9-]+$")
