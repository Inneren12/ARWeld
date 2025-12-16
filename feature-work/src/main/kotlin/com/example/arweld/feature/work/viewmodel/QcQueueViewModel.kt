package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.work.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QcQueueItemUiModel(
    val id: String,
    val code: String,
    val zone: String?,
    val waitingTimeMinutes: Long? = null,
)

data class QcQueueUiState(
    val isLoading: Boolean = false,
    val items: List<QcQueueItemUiModel> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class QcQueueViewModel @Inject constructor(
    private val workRepository: WorkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcQueueUiState(isLoading = true))
    val uiState: StateFlow<QcQueueUiState> = _uiState.asStateFlow()

    suspend fun loadQcQueue() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            val queue = workRepository.getQcQueue()
            queue.mapNotNull { state ->
                val workItemId = state.lastEvent?.workItemId ?: return@mapNotNull null
                val workItem = workRepository.getWorkItemById(workItemId) ?: return@mapNotNull null

                QcQueueItemUiModel(
                    id = workItem.id,
                    code = workItem.code,
                    zone = workItem.zone,
                    waitingTimeMinutes = null,
                )
            }
        }.onSuccess { items ->
            _uiState.value = QcQueueUiState(
                isLoading = false,
                items = items,
                errorMessage = null,
            )
        }.onFailure { throwable ->
            _uiState.value = QcQueueUiState(
                isLoading = false,
                items = emptyList(),
                errorMessage = throwable.message ?: "Unable to load QC queue",
            )
        }
    }

    fun refresh() {
        viewModelScope.launch { loadQcQueue() }
    }
}
