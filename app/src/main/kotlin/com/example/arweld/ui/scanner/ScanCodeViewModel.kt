package com.example.arweld.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.arweld.feature.scanner.ui.ScanCodeResolutionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ScanCodeViewModel @Inject constructor(
    private val resolveWorkItemByCode: ResolveWorkItemByCodeUseCase,
    private val appLogger: AppLogger,
) : ViewModel() {

    private val _resolutionState = MutableStateFlow<ScanCodeResolutionState>(
        ScanCodeResolutionState.Idle
    )
    val resolutionState: StateFlow<ScanCodeResolutionState> = _resolutionState

    fun resetResolution() {
        _resolutionState.value = ScanCodeResolutionState.Idle
    }

    fun resolveCode(
        code: String,
        onFound: (String) -> Unit,
    ) {
        if (_resolutionState.value is ScanCodeResolutionState.Resolving) {
            return
        }
        viewModelScope.launch {
            _resolutionState.value = ScanCodeResolutionState.Resolving
            runCatching { resolveWorkItemByCode(code) }
                .onSuccess { workItem ->
                    if (workItem != null) {
                        _resolutionState.value = ScanCodeResolutionState.Idle
                        onFound(workItem.id)
                    } else {
                        _resolutionState.value = ScanCodeResolutionState.NotFound(code)
                    }
                }
                .onFailure { throwable ->
                    _resolutionState.value = ScanCodeResolutionState.Error("Failed to resolve code")
                    appLogger.logRepositoryError("resolveWorkItemByCode", throwable)
                }
        }
    }
}
