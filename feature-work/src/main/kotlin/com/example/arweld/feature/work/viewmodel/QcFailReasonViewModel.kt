package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.core.domain.work.usecase.FailQcInput
import com.example.arweld.core.domain.work.usecase.FailQcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class QcFailReasonViewModel @Inject constructor(
    private val failQcUseCase: FailQcUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcFailReasonUiState())
    val uiState: StateFlow<QcFailReasonUiState> = _uiState.asStateFlow()

    fun initialize(workItemId: String?, code: String?, checklist: QcChecklistResult?) {
        if (_uiState.value.initialized) return
        if (workItemId == null || checklist == null) {
            _uiState.value = QcFailReasonUiState(
                initialized = true,
                errorMessage = "Отсутствуют данные для отклонения QC",
            )
            return
        }

        _uiState.value = QcFailReasonUiState(
            initialized = true,
            workItemId = workItemId,
            code = code,
            checklist = checklist,
        )
    }

    fun updateReasonsInput(value: String) {
        _uiState.update { it.copy(reasonsInput = value) }
    }

    fun updateComment(value: String) {
        _uiState.update { it.copy(comment = value) }
    }

    fun updatePriority(value: Int) {
        _uiState.update { it.copy(priority = value) }
    }

    fun submit() {
        val workItemId = _uiState.value.workItemId ?: return
        val checklist = _uiState.value.checklist ?: return
        val reasons = _uiState.value.reasonsInput
            .split('\n', ',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (reasons.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "Укажите хотя бы одну причину отклонения")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    errorMessage = null,
                    completed = false,
                )
            }

            runCatching {
                failQcUseCase(
                    FailQcInput(
                        workItemId = workItemId,
                        checklist = checklist,
                        reasons = reasons,
                        priority = _uiState.value.priority,
                        comment = _uiState.value.comment.takeIf { comment -> comment.isNotBlank() },
                    ),
                )
            }.onSuccess {
                _uiState.update { it.copy(isSubmitting = false, completed = true) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.message ?: "Не удалось отклонить QC",
                    )
                }
            }
        }
    }
}

data class QcFailReasonUiState(
    val initialized: Boolean = false,
    val workItemId: String? = null,
    val code: String? = null,
    val checklist: QcChecklistResult? = null,
    val reasonsInput: String = "",
    val priority: Int = 1,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false,
)
