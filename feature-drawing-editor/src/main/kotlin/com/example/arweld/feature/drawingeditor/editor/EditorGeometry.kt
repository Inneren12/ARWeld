package com.example.arweld.feature.drawingeditor.editor

data class EditorVec2(
    val x: Float,
    val y: Float,
) {
    operator fun plus(other: EditorVec2): EditorVec2 = EditorVec2(x + other.x, y + other.y)

    operator fun minus(other: EditorVec2): EditorVec2 = EditorVec2(x - other.x, y - other.y)

    operator fun times(scale: Float): EditorVec2 = EditorVec2(x * scale, y * scale)

    operator fun div(scale: Float): EditorVec2 = EditorVec2(x / scale, y / scale)

    fun distanceTo(other: EditorVec2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}

data class ViewTransform(
    val scale: Float,
    val translation: EditorVec2,
) {
    init {
        require(scale != 0f) { "scale must be non-zero" }
    }

    fun screenToWorld(screen: EditorVec2): EditorVec2 = (screen - translation) / scale

    fun worldToScreen(world: EditorVec2): EditorVec2 = world * scale + translation

    companion object {
        fun identity(): ViewTransform = ViewTransform(scale = 1f, translation = EditorVec2(0f, 0f))
    }
}
