package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.work.WorkRepository
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase
import com.example.arweld.core.domain.work.usecase.MarkReadyForQcUseCase
import com.example.arweld.core.domain.work.usecase.StartWorkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkItemSummaryUiState(
    val workItemId: String? = null,
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val workItemState: WorkItemState? = null,
    val error: String? = null,
)

@HiltViewModel
class WorkItemSummaryViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val claimWorkUseCase: ClaimWorkUseCase,
    private val startWorkUseCase: StartWorkUseCase,
    private val markReadyForQcUseCase: MarkReadyForQcUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkItemSummaryUiState(isLoading = true))
    val uiState: StateFlow<WorkItemSummaryUiState> = _uiState.asStateFlow()

    fun initialize(workItemId: String?) {
        if (workItemId == null) {
            _uiState.value = WorkItemSummaryUiState(
                isLoading = false,
                error = "Work item not found",
            )
            return
        }

        if (_uiState.value.workItemId == workItemId && !_uiState.value.isLoading) {
            return
        }

        _uiState.value = _uiState.value.copy(
            workItemId = workItemId,
            isLoading = true,
            error = null,
        )
        refresh()
    }

    fun refresh() {
        val workItemId = _uiState.value.workItemId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                workRepository.getWorkItemState(workItemId)
            }.onSuccess { state ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionInProgress = false,
                    workItemState = state,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionInProgress = false,
                    error = throwable.message ?: "Failed to load work item",
                )
            }
        }
    }

    fun claimWork() {
        performAction { workItemId -> claimWorkUseCase(workItemId) }
    }

    fun startWork() {
        performAction { workItemId -> startWorkUseCase(workItemId) }
    }

    fun markReadyForQc() {
        performAction { workItemId -> markReadyForQcUseCase(workItemId) }
    }

    private fun performAction(block: suspend (String) -> Unit) {
        val workItemId = _uiState.value.workItemId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, error = null)
            runCatching {
                block(workItemId)
            }.onSuccess {
                refresh()
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = throwable.message ?: "Unable to perform action",
                )
            }
        }
    }
}
