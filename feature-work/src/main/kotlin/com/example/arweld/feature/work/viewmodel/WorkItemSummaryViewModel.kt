package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.model.WorkItem
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
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val workItem: WorkItem? = null,
    val workState: WorkItemState? = null,
    val currentUser: User? = null,
    val error: String? = null,
)

@HiltViewModel
class WorkItemSummaryViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val workRepository: WorkRepository,
    private val claimWorkUseCase: ClaimWorkUseCase,
    private val startWorkUseCase: StartWorkUseCase,
    private val markReadyForQcUseCase: MarkReadyForQcUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkItemSummaryUiState(isLoading = true))
    val uiState: StateFlow<WorkItemSummaryUiState> = _uiState.asStateFlow()

    private var currentWorkItemId: String? = null

    fun onMissingWorkItem() {
        currentWorkItemId = null
        _uiState.value = WorkItemSummaryUiState(
            isLoading = false,
            error = "Work item not found",
        )
    }

    fun load(workItemId: String) {
        currentWorkItemId = workItemId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                val workItem = workRepository.getWorkItemById(workItemId)
                    ?: workRepository.getWorkItemByCode(workItemId)
                    ?: error("Work item not found")
                val workState = workRepository.getWorkItemState(workItemId)
                val currentUser = authRepository.currentUser()

                LoadedPayload(
                    workItem = workItem,
                    workState = workState,
                    currentUser = currentUser,
                )
            }.onSuccess { payload ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionInProgress = false,
                    workItem = payload.workItem,
                    workState = payload.workState,
                    currentUser = payload.currentUser,
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

    fun onClaimWork() {
        performAction { workItemId -> claimWorkUseCase(workItemId) }
    }

    fun onStartWork() {
        performAction { workItemId -> startWorkUseCase(workItemId) }
    }

    fun onMarkReadyForQc() {
        performAction { workItemId -> markReadyForQcUseCase(workItemId) }
    }

    fun refresh() {
        currentWorkItemId?.let { load(it) }
    }

    private fun performAction(block: suspend (String) -> Unit) {
        val workItemId = currentWorkItemId ?: _uiState.value.workItem?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, error = null)
            runCatching {
                block(workItemId)
            }.onSuccess {
                load(workItemId)
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    actionInProgress = false,
                    error = throwable.message ?: "Unable to perform action",
                )
            }
        }
    }

    private data class LoadedPayload(
        val workItem: WorkItem?,
        val workState: WorkItemState,
        val currentUser: User?,
    )
}
