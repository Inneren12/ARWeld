package com.example.arweld.feature.work.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds QC checklist selections for the current inspection.
 */
@HiltViewModel
class QcChecklistViewModel @Inject constructor() : ViewModel() {

    private val _checklistResult = MutableStateFlow(
        QcChecklistResult(
            items = defaultChecklistTemplates.map { template ->
                template.toItem(state = ChecklistItemState.NA)
            }
        )
    )
    val checklistResult: StateFlow<QcChecklistResult> = _checklistResult.asStateFlow()

    fun updateItemState(id: String, newState: ChecklistItemState) {
        _checklistResult.value = _checklistResult.value.copy(
            items = _checklistResult.value.items.map { item ->
                if (item.id == id) item.copy(state = newState) else item
            }
        )
    }
}

enum class ChecklistItemState {
    OK,
    NOT_OK,
    NA,
}

data class QcChecklistItem(
    val id: String,
    val title: String,
    val required: Boolean,
    val state: ChecklistItemState,
)

data class QcChecklistResult(
    val items: List<QcChecklistItem>,
)

private data class ChecklistTemplate(
    val id: String,
    val title: String,
    val required: Boolean,
)

private val defaultChecklistTemplates = listOf(
    ChecklistTemplate("weld_penetration", "Weld penetration adequate", required = true),
    ChecklistTemplate("no_cracks", "No visible cracks", required = true),
    ChecklistTemplate("no_porosity", "No porosity or voids", required = true),
    ChecklistTemplate("bead_uniformity", "Bead uniformity acceptable", required = false),
    ChecklistTemplate("cleanup", "Spatter cleaned", required = false),
)

private fun ChecklistTemplate.toItem(state: ChecklistItemState): QcChecklistItem {
    return QcChecklistItem(
        id = id,
        title = title,
        required = required,
        state = state,
    )
}
