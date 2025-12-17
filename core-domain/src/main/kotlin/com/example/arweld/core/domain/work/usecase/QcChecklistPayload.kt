package com.example.arweld.core.domain.work.usecase

import com.example.arweld.core.domain.work.model.QcCheckState
import com.example.arweld.core.domain.work.model.QcChecklistResult
import kotlinx.serialization.Serializable

internal fun buildChecklistSummary(checklist: QcChecklistResult): ChecklistSummary {
    val items = checklist.items
    val totals = ChecklistTotals(
        ok = items.count { it.state == QcCheckState.OK },
        notOk = items.count { it.state == QcCheckState.NOT_OK },
        na = items.count { it.state == QcCheckState.NA },
    )

    return ChecklistSummary(
        totals = totals,
        items = items.map { item ->
            ChecklistItemState(
                id = item.id,
                state = item.state,
            )
        },
    )
}

@Serializable
internal data class ChecklistSummary(
    val totals: ChecklistTotals,
    val items: List<ChecklistItemState>,
)

@Serializable
internal data class ChecklistTotals(
    val ok: Int,
    val notOk: Int,
    val na: Int,
)

@Serializable
internal data class ChecklistItemState(
    val id: String,
    val state: QcCheckState,
)
