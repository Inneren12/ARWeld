package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.state.WorkItemState
import com.example.arweld.core.domain.work.WorkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class QcQueueSort {
    BY_AGE,
    BY_ZONE,
}

data class QcQueueItemUiModel(
    val id: String,
    val code: String,
    val zone: String?,
    val readyForQcSince: Long?,
    val waitingTimeMinutes: Long? = null,
)

data class QcQueueUiState(
    val isLoading: Boolean = false,
    val items: List<QcQueueItemUiModel> = emptyList(),
    val errorMessage: String? = null,
    val sortMode: QcQueueSort = QcQueueSort.BY_AGE,
)

@HiltViewModel
class QcQueueViewModel @Inject constructor(
    private val workRepository: WorkRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcQueueUiState(isLoading = true))
    val uiState: StateFlow<QcQueueUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch { loadQcQueueInternal() }
    }

    fun updateSortMode(sortMode: QcQueueSort) {
        _uiState.value = _uiState.value.copy(sortMode = sortMode)
    }

    private suspend fun loadQcQueueInternal() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        runCatching {
            val queue: List<WorkItemState> = workRepository.getQcQueue()
            val mappedItems: List<QcQueueItemUiModel> = queue.mapNotNull { state ->
                val workItemId = state.lastEvent?.workItemId ?: return@mapNotNull null
                val workItem = workRepository.getWorkItemById(workItemId) ?: return@mapNotNull null
                val readyForQcSince = state.readyForQcSince
                val waitingTimeMinutes = readyForQcSince?.let { readySince ->
                    val deltaMillis = System.currentTimeMillis() - readySince
                    (deltaMillis.coerceAtLeast(0) / 60_000)
                }

                QcQueueItemUiModel(
                    id = workItem.id,
                    code = workItem.code,
                    zone = workItem.zone,
                    readyForQcSince = readyForQcSince,
                    waitingTimeMinutes = waitingTimeMinutes,
                )
            }
            val comparator: Comparator<QcQueueItemUiModel> = when (_uiState.value.sortMode) {
                QcQueueSort.BY_AGE -> compareBy<QcQueueItemUiModel> { it.readyForQcSince ?: Long.MAX_VALUE }
                    .thenBy { it.code }
                QcQueueSort.BY_ZONE -> compareBy<QcQueueItemUiModel> { it.zone ?: "" }
                    .thenBy { it.readyForQcSince ?: Long.MAX_VALUE }
            }
            mappedItems.sortedWith(comparator)
        }.onSuccess { items ->
            _uiState.value = QcQueueUiState(
                isLoading = false,
                items = items,
                errorMessage = null,
                sortMode = _uiState.value.sortMode,
            )
        }.onFailure { throwable ->
            _uiState.value = QcQueueUiState(
                isLoading = false,
                items = emptyList(),
                errorMessage = throwable.message ?: "Unable to load QC queue",
                sortMode = _uiState.value.sortMode,
            )
        }
    }
}
