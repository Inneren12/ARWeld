package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.work.WorkRepository
import com.example.arweld.core.domain.work.usecase.StartQcInspectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Starts a QC inspection and surfaces the minimal WorkItem details required for the entry screen.
 */
@HiltViewModel
class QcStartViewModel @Inject constructor(
    private val startQcInspectionUseCase: StartQcInspectionUseCase,
    private val workRepository: WorkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcStartUiState(isLoading = true))
    val uiState: StateFlow<QcStartUiState> = _uiState.asStateFlow()

    private var initializedForWorkItemId: String? = null

    fun onMissingWorkItem() {
        initializedForWorkItemId = null
        _uiState.value = QcStartUiState(
            isLoading = false,
            errorMessage = "Work item not found",
        )
    }

    fun start(workItemId: String) {
        if (initializedForWorkItemId == workItemId) return
        initializedForWorkItemId = workItemId

        viewModelScope.launch {
            _uiState.value = QcStartUiState(isLoading = true)
            runCatching {
                startQcInspectionUseCase(workItemId)
                workRepository.getWorkItemById(workItemId)
                    ?: error("Work item not found")
            }.onSuccess { workItem ->
                _uiState.value = QcStartUiState(
                    isLoading = false,
                    workItemId = workItem.id,
                    code = workItem.code,
                    zone = workItem.zone,
                    errorMessage = null,
                )
            }.onFailure { throwable ->
                _uiState.value = QcStartUiState(
                    isLoading = false,
                    workItemId = workItemId,
                    errorMessage = throwable.message ?: "Failed to start QC",
                )
            }
        }
    }
}

data class QcStartUiState(
    val isLoading: Boolean = false,
    val workItemId: String? = null,
    val code: String? = null,
    val zone: String? = null,
    val errorMessage: String? = null,
)
