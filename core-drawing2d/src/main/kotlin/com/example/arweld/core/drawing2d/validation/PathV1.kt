package com.example.arweld.core.drawing2d.validation

/**
 * Minimal JSONPath-like helpers for Drawing2D validation paths.
 */
object PathV1 {
    const val root: String = "$"

    fun field(parent: String, name: String): String = "$parent.$name"

    fun index(parent: String, name: String, i: Int): String = "$parent.$name[$i]"

    fun idSelector(parent: String, name: String, id: String): String {
        val escaped = escapeSelectorValue(id)
        return "$parent.$name[id=$escaped]"
    }

    private fun escapeSelectorValue(value: String): String {
        return value
            .replace("\\", "\\\\")
            .replace("]", "\\]")
            .replace("\"", "\\\"")
    }
}
