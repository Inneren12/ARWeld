package com.example.arweld.core.structural.profiles.parse

data class PlateDimensions(
    val thicknessToken: String,
    val widthToken: String,
    val thicknessMm: Double,
    val widthMm: Double
)

private val platePattern =
    "^PL\\s*(\\d+(?:\\.\\d+)?)(?:\\s*x\\s*|\\s+)(\\d+(?:\\.\\d+)?)(?:\\s*x\\s*\\d+(?:\\.\\d+)?(?:.*)?)?$"
        .toRegex(setOf(RegexOption.IGNORE_CASE))

internal fun parsePlateDimensions(raw: String): PlateDimensions? {
    val normalized = normalizeRawProfileInput(raw)
    val match = platePattern.find(normalized) ?: return null
    val thicknessToken = match.groupValues[1]
    val widthToken = match.groupValues[2]
    val thickness = thicknessToken.toDoubleOrNull() ?: return null
    val width = widthToken.toDoubleOrNull() ?: return null
    return PlateDimensions(
        thicknessToken = thicknessToken,
        widthToken = widthToken,
        thicknessMm = thickness,
        widthMm = width
    )
}

internal fun canonicalizePlateDesignation(raw: String): String? {
    val dimensions = parsePlateDimensions(raw) ?: return null
    return "PL ${dimensions.thicknessToken}x${dimensions.widthToken}"
}
