package com.example.arweld.feature.supervisor.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.feature.supervisor.model.TimelineEntry
import com.example.arweld.feature.supervisor.model.WorkItemDetail
import com.example.arweld.feature.supervisor.usecase.GetWorkItemDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Work Item Detail screen.
 * Loads detailed information, timeline, and evidence for a single work item.
 */
@HiltViewModel
class WorkItemDetailViewModel @Inject constructor(
    private val getWorkItemDetailUseCase: GetWorkItemDetailUseCase,
    private val evidenceDao: EvidenceDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workItemId: String = checkNotNull(savedStateHandle["workItemId"]) {
        "workItemId is required"
    }

    private val _uiState = MutableStateFlow(WorkItemDetailUiState())
    val uiState: StateFlow<WorkItemDetailUiState> = _uiState.asStateFlow()

    init {
        loadWorkItemDetail()
    }

    private fun loadWorkItemDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val detail = getWorkItemDetailUseCase(workItemId)
                val timeline = getWorkItemDetailUseCase.getTimeline(workItemId)

                // Batch load all evidence for this work item (eliminates N+1 query)
                val evidenceEntities = evidenceDao.getByWorkItemId(workItemId)
                val allEvidence = evidenceEntities.map { evidenceEntity ->
                    Evidence(
                        id = evidenceEntity.id,
                        eventId = evidenceEntity.eventId,
                        kind = EvidenceKind.valueOf(evidenceEntity.kind),
                        uri = evidenceEntity.uri,
                        sha256 = evidenceEntity.sha256,
                        metaJson = evidenceEntity.metaJson,
                        createdAt = evidenceEntity.createdAt
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        timeline = timeline,
                        evidence = allEvidence
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun refresh() {
        loadWorkItemDetail()
    }
}

data class WorkItemDetailUiState(
    val isLoading: Boolean = false,
    val detail: WorkItemDetail? = null,
    val timeline: List<TimelineEntry> = emptyList(),
    val evidence: List<Evidence> = emptyList(),
    val error: String? = null
)
