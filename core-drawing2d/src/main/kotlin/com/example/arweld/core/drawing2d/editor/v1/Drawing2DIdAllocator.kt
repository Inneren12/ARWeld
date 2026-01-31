package com.example.arweld.core.drawing2d.editor.v1

data class Drawing2DIdAllocation(
    val drawing: Drawing2D,
    val id: String
)

object Drawing2DIdAllocator {
    private const val NODE_PREFIX = "N"
    private const val MEMBER_PREFIX = "M"
    private const val PAD_WIDTH = 6

    fun allocateNodeId(drawing: Drawing2D): Drawing2DIdAllocation {
        val meta = drawing.meta ?: Drawing2DEditorMetaV1()
        val id = formatNodeId(meta.nextNodeId)
        val updatedMeta = meta.copy(nextNodeId = meta.nextNodeId + 1)
        return Drawing2DIdAllocation(drawing.copy(meta = updatedMeta), id)
    }

    fun allocateMemberId(drawing: Drawing2D): Drawing2DIdAllocation {
        val meta = drawing.meta ?: Drawing2DEditorMetaV1()
        val id = formatMemberId(meta.nextMemberId)
        val updatedMeta = meta.copy(nextMemberId = meta.nextMemberId + 1)
        return Drawing2DIdAllocation(drawing.copy(meta = updatedMeta), id)
    }

    fun formatNodeId(counter: Long): String {
        return formatId(NODE_PREFIX, counter)
    }

    fun formatMemberId(counter: Long): String {
        return formatId(MEMBER_PREFIX, counter)
    }

    private fun formatId(prefix: String, counter: Long): String {
        require(counter > 0) { "ID counter must be positive." }
        return prefix + counter.toString().padStart(PAD_WIDTH, '0')
    }
}
