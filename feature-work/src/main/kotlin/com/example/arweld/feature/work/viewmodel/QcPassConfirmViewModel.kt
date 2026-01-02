package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.core.domain.work.usecase.PassQcInput
import com.example.arweld.core.domain.work.usecase.PassQcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class QcPassConfirmViewModel @Inject constructor(
    private val passQcUseCase: PassQcUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcPassConfirmUiState())
    val uiState: StateFlow<QcPassConfirmUiState> = _uiState.asStateFlow()

    fun initialize(workItemId: String?, code: String?, checklist: QcChecklistResult?) {
        if (_uiState.value.initialized) return
        if (workItemId == null || checklist == null) {
            _uiState.value = QcPassConfirmUiState(
                initialized = true,
                errorMessage = "Отсутствуют данные для подтверждения QC",
            )
            return
        }

        _uiState.value = QcPassConfirmUiState(
            initialized = true,
            workItemId = workItemId,
            code = code,
            checklist = checklist,
        )
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun submit() {
        val workItemId = _uiState.value.workItemId ?: return
        val checklist = _uiState.value.checklist ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSubmitting = true,
                errorMessage = null,
                completed = false,
            )

            runCatching {
                passQcUseCase(
                    PassQcInput(
                        workItemId = workItemId,
                        checklist = checklist,
                        comment = _uiState.value.comment.takeIf { it.isNotBlank() },
                    ),
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    completed = true,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = throwable.message ?: "Не удалось завершить QC",
                )
            }
        }
    }
}

data class QcPassConfirmUiState(
    val initialized: Boolean = false,
    val workItemId: String? = null,
    val code: String? = null,
    val checklist: QcChecklistResult? = null,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false,
)
