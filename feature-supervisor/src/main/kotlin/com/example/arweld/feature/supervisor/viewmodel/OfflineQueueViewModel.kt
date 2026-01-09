package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.sync.SyncQueueItem
import com.example.arweld.core.domain.sync.SyncQueueProcessor
import com.example.arweld.core.domain.sync.SyncQueueWriter
import com.example.arweld.core.domain.sync.SyncQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class OfflineQueueUiState(
    val isLoading: Boolean = false,
    val pendingItems: List<SyncQueueItem> = emptyList(),
    val errorItems: List<SyncQueueItem> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class OfflineQueueViewModel @Inject constructor(
    private val repository: SyncQueueRepository,
    private val processor: SyncQueueProcessor,
    private val writer: SyncQueueWriter,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfflineQueueUiState(isLoading = true))
    val uiState: StateFlow<OfflineQueueUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val pending = repository.listPending()
                val errors = repository.listErrors()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pendingItems = pending,
                    errorItems = errors,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load queue",
                )
            }
        }
    }

    fun processPending() {
        viewModelScope.launch {
            try {
                processor.processPending()
            } finally {
                refresh()
            }
        }
    }

    fun enqueueSample() {
        viewModelScope.launch {
            val payload = Json.encodeToString(mapOf("type" to "sample", "detail" to "offline-queue"))
            writer.enqueue(payload)
            refresh()
        }
    }
}
