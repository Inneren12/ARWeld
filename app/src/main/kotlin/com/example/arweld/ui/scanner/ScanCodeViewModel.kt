package com.example.arweld.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScanCodeViewModel @Inject constructor(
    private val resolveWorkItemByCode: ResolveWorkItemByCodeUseCase,
    private val appLogger: AppLogger,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

    fun resolveCode(
        code: String,
        onFound: (String) -> Unit,
    ) {
        viewModelScope.launch {
            runCatching { resolveWorkItemByCode(code) }
                .onSuccess { workItem ->
                    if (workItem != null) {
                        _errorMessage.value = null
                        onFound(workItem.id)
                    } else {
                        _errorMessage.value = "Work item not found"
                    }
                }
                .onFailure { throwable ->
                    _errorMessage.value = "Failed to resolve code"
                    appLogger.logRepositoryError("resolveWorkItemByCode", throwable)
                }
        }
    }
}
