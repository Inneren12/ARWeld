package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.state.WorkStatus
import com.example.arweld.core.domain.work.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AssemblerQueueUiState(
    val isLoading: Boolean = false,
    val inProgress: List<WorkItemState> = emptyList(),
    val readyForQc: List<WorkItemState> = emptyList(),
    val reworkRequired: List<WorkItemState> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class AssemblerQueueViewModel @Inject constructor(
    private val workRepository: WorkRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssemblerQueueUiState(isLoading = true))
    val uiState: StateFlow<AssemblerQueueUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching {
                val user = authRepository.currentUser()
                    ?: throw IllegalStateException("User not found")

                if (user.role != Role.ASSEMBLER) {
                    throw IllegalStateException("Assembler queue available only for ASSEMBLER role")
                }

                val queue = workRepository.getMyQueue(user.id)
                _uiState.value = AssemblerQueueUiState(
                    isLoading = false,
                    inProgress = queue.filter { it.status == WorkStatus.IN_PROGRESS },
                    readyForQc = queue.filter { it.status == WorkStatus.READY_FOR_QC },
                    reworkRequired = queue.filter { it.status == WorkStatus.REWORK_REQUIRED },
                )
            }.onFailure { throwable ->
                _uiState.value = AssemblerQueueUiState(
                    isLoading = false,
                    error = throwable.message ?: "Unable to load queue",
                )
            }
        }
    }
}
