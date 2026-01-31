package com.example.arweld.feature.drawingeditor.viewmodel

import com.example.arweld.core.drawing2d.editor.v1.Node2D
import java.util.Locale

private const val NODE_COORDINATE_DECIMALS = 3
private val STRICT_SIGNED_NUMBER_REGEX = Regex("^-?\\d+(\\.\\d+)?$")

fun parseStrictSignedNumber(text: String): Double? {
    val trimmed = text.trim()
    if (!STRICT_SIGNED_NUMBER_REGEX.matches(trimmed)) {
        return null
    }
    val value = trimmed.toDoubleOrNull() ?: return null
    if (!value.isFinite()) {
        return null
    }
    return value
}

fun formatNodeCoordinate(value: Double): String {
    return String.format(Locale.US, "%.${NODE_COORDINATE_DECIMALS}f", value)
}

fun validateNodeCoordinateText(text: String, axisLabel: String): String? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) {
        return "Enter $axisLabel coordinate."
    }
    val parsed = parseStrictSignedNumber(trimmed)
    return if (parsed == null) {
        "Enter a valid $axisLabel coordinate (dot decimal, e.g. -12.5)."
    } else {
        null
    }
}

fun buildNodeEditDraft(node: Node2D): NodeEditDraft {
    return NodeEditDraft(
        nodeId = node.id,
        xText = formatNodeCoordinate(node.x),
        yText = formatNodeCoordinate(node.y),
        xError = null,
        yError = null,
        applyError = null,
    )
}
