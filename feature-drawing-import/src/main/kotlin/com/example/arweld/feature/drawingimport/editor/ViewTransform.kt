package com.example.arweld.feature.drawingimport.editor

data class ViewTransform(
    val scale: Float,
    val translation: Vec2,
) {
    init {
        require(scale != 0f) { "scale must be non-zero" }
    }

    fun screenToWorld(screen: Vec2): Vec2 = (screen - translation) / scale

    fun worldToScreen(world: Vec2): Vec2 = world * scale + translation

    companion object {
        fun identity(): ViewTransform = ViewTransform(scale = 1f, translation = Vec2(0f, 0f))
    }
}
