package com.example.arweld.core.domain.work.model

import kotlinx.serialization.Serializable

/**
 * Immutable QC checklist models used when persisting QC outcomes.
 */
@Serializable
enum class QcCheckState {
    OK,
    NOT_OK,
    NA,
}

@Serializable
data class QcChecklistItem(
    val id: String,
    val title: String? = null,
    val required: Boolean = false,
    val description: String? = null,
    val state: QcCheckState = QcCheckState.NA,
)

@Serializable
data class QcChecklistResult(
    val items: List<QcChecklistItem>,
)
