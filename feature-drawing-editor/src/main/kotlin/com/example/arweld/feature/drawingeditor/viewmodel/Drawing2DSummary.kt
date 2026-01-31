package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Drawing2D
import com.example.arweld.core.drawing2d.editor.v1.missingNodeReferences

fun Drawing2D.toSummary(): Drawing2DSummary {
    val missingRefs = missingNodeReferences()
    return Drawing2DSummary(
        nodeCount = nodes.size,
        memberCount = members.size,
        missingNodeRefs = missingRefs.size,
        hasScale = scale != null,
    )
}
