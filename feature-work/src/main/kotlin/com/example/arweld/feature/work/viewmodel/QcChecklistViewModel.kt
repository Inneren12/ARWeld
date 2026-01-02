package com.example.arweld.feature.work.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
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

/**
 * Holds QC checklist selections for the current inspection.
 */
@HiltViewModel
class QcChecklistViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val defaultItems = listOf(
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

    private val _checklist = MutableStateFlow(QcChecklistResult(defaultItems))
    val checklist: StateFlow<QcChecklistResult> = _checklist.asStateFlow()

    fun updateItemState(id: String, newState: QcCheckState) {
        _checklist.update { result ->
            val updatedItems = result.items.map { item ->
                if (item.id == id) {
                    item.copy(state = newState)
                } else {
                    item
                }
            }

            result.copy(items = updatedItems)
        }
    }

    fun resetChecklist() {
        _checklist.value = QcChecklistResult(defaultItems)
    }
}
