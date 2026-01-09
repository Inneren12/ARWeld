package com.example.arweld.feature.work.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistItem
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.feature.work.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Holds QC checklist selections for the current inspection.
 */
@HiltViewModel
class QcChecklistViewModel @Inject constructor(
    private val itemsProvider: QcChecklistItemsProvider,
    private val qcEvidencePolicy: QcEvidencePolicy,
    private val evidenceRepository: EvidenceRepository,
) : ViewModel() {

    private val defaultItems = itemsProvider.defaultItems()
    private val _uiState = MutableStateFlow(
        QcChecklistUiState(
            checklist = QcChecklistResult(defaultItems),
            missingEvidence = REQUIRED_KINDS,
        ),
    )
    val uiState: StateFlow<QcChecklistUiState> = _uiState.asStateFlow()

    private var initializedForWorkItemId: String? = null

    fun initialize(workItemId: String?) {
        if (workItemId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Work item not found",
                policySatisfied = false,
                missingEvidence = REQUIRED_KINDS,
            )
            return
        }
        if (initializedForWorkItemId == workItemId) {
            refreshPolicy(workItemId)
            return
        }
        initializedForWorkItemId = workItemId
        refreshPolicy(workItemId)
    }

    fun updateItemState(id: String, newState: QcCheckState) {
        _uiState.update { state ->
            val updatedItems = state.checklist.items.map { item ->
                if (item.id == id) {
                    item.copy(state = newState)
                } else {
                    item
                }
            }

            state.copy(checklist = state.checklist.copy(items = updatedItems))
        }
    }

    fun resetChecklist() {
        _uiState.value = _uiState.value.copy(checklist = QcChecklistResult(defaultItems))
    }

    private fun refreshPolicy(workItemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvidence = true)
            runCatching {
                val counts = evidenceRepository.countsByKindForWorkItem(workItemId)
                val policy = qcEvidencePolicy.evaluate(workItemId)
                counts to policy
            }.onSuccess { (counts, policy) ->
                _uiState.value = _uiState.value.copy(
                    isLoadingEvidence = false,
                    evidenceCounts = counts,
                    policySatisfied = policy.satisfied,
                    missingEvidence = policy.missing,
                    errorMessage = null,
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoadingEvidence = false,
                    policySatisfied = false,
                    missingEvidence = REQUIRED_KINDS,
                    errorMessage = throwable.message
                        ?: "Не удалось проверить требования доказательств",
                )
            }
        }
    }
}

data class QcChecklistUiState(
    val checklist: QcChecklistResult,
    val evidenceCounts: Map<EvidenceKind, Int> = emptyMap(),
    val policySatisfied: Boolean = false,
    val missingEvidence: Set<EvidenceKind> = emptySet(),
    val isLoadingEvidence: Boolean = false,
    val errorMessage: String? = null,
)

interface QcChecklistItemsProvider {
    fun defaultItems(): List<QcChecklistItem>
}

class ResourceQcChecklistItemsProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : QcChecklistItemsProvider {
    override fun defaultItems(): List<QcChecklistItem> {
        return listOf(
            QcChecklistItem(
                id = "geometry",
                title = context.getString(R.string.qc_checklist_title_geometry),
                description = null,
                state = QcCheckState.NA,
            ),
            QcChecklistItem(
                id = "completeness",
                title = context.getString(R.string.qc_checklist_title_completeness),
                description = null,
                state = QcCheckState.NA,
            ),
            QcChecklistItem(
                id = "fasteners",
                title = context.getString(R.string.qc_checklist_title_fasteners),
                description = null,
                state = QcCheckState.NA,
            ),
            QcChecklistItem(
                id = "marking",
                title = context.getString(R.string.qc_checklist_title_marking),
                description = null,
                state = QcCheckState.NA,
            ),
            QcChecklistItem(
                id = "cleanliness",
                title = context.getString(R.string.qc_checklist_title_cleanliness),
                description = null,
                state = QcCheckState.NA,
            ),
        )
    }
}

private val REQUIRED_KINDS = setOf(EvidenceKind.PHOTO, EvidenceKind.AR_SCREENSHOT)
