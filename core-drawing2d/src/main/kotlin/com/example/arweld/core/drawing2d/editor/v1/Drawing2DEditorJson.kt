package com.example.arweld.core.drawing2d.editor.v1

import com.example.arweld.core.drawing2d.Drawing2DJson

/**
 * JSON helpers for the manual 2D editor schema (v1).
 */
object Drawing2DEditorJson {
    val json = Drawing2DJson.json

    fun encodeToString(drawing: Drawing2D): String {
        return Drawing2DJson.encodeToString(drawing)
    }

    fun decodeFromString(string: String): Drawing2D {
        return Drawing2DJson.decodeFromString(string)
    }
}
