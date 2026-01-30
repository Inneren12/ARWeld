package com.example.arweld.core.drawing2d.validation

/**
 * Validation violation for Drawing2D (v1).
 */
data class ViolationV1(
    val code: String,
    val severity: SeverityV1,
    val path: String,
    val message: String,
    val hint: String? = null,
    val refs: List<String> = emptyList(),
)
