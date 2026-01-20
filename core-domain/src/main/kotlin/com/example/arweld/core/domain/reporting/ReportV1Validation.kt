package com.example.arweld.core.domain.reporting

data class ReportV1ValidationResult(
    val errors: List<String>,
) {
    val isValid: Boolean
        get() = errors.isEmpty()
}

fun ReportV1.validate(): ReportV1ValidationResult {
    val errors = mutableListOf<String>()
    if (reportVersion != 1) {
        errors.add("reportVersion must be 1")
    }
    if (generatedAt < 0) {
        errors.add("generatedAt must be non-negative")
    }
    return ReportV1ValidationResult(errors)
}
