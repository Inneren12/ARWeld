package com.example.arweld.feature.arview.alignment

import com.example.arweld.core.domain.spatial.AlignmentSample

data class ManualAlignmentState(
    val isActive: Boolean = false,
    val sample: AlignmentSample = AlignmentSample(emptyList()),
    val statusMessage: String? = null,
) {
    val collectedCount: Int get() = sample.points.size
}
