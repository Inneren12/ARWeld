package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.sync.SyncRetryHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OfflineQueueUiState(
    val pendingCount: Int = 0,
    val errorCount: Int = 0,
    val lastEnqueuedAt: Long? = null,
)

@HiltViewModel
class OfflineQueueViewModel @Inject constructor(
    private val repository: SyncQueueRepository,
    private val retryHandler: SyncRetryHandler,
) : ViewModel() {

    val uiState: StateFlow<OfflineQueueUiState> = combine(
        repository.observePendingCount(),
        repository.observeErrorCount(),
        repository.observeLastEnqueuedAt(),
    ) { pendingCount, errorCount, lastEnqueuedAt ->
        OfflineQueueUiState(
            pendingCount = pendingCount,
            errorCount = errorCount,
            lastEnqueuedAt = lastEnqueuedAt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = OfflineQueueUiState(),
    )

    fun retryAllPending() {
        viewModelScope.launch {
            retryHandler.retryAllPending()
        }
    }

    fun retrySingle(id: String) {
        viewModelScope.launch {
            retryHandler.retrySingle(id)
        }
    }
}
