package com.example.arweld.core.structural.profiles.parse

private val ocrNoiseChars = setOf('|', ';', ',', '‚', '`', '´')

internal fun normalizeRawProfileInput(raw: String): String {
    var normalized = raw.trim()
    normalized = normalized.replace('×', 'x')
    normalized = normalized.replace('X', 'x')
    normalized = normalizeDecimalSeparator(normalized)
    normalized = collapseSpaces(normalized)
    normalized = trimOcrNoise(normalized)
    return normalized
}

internal fun collapseSpaces(input: String): String =
    input.trim().replace("\\s+".toRegex(), " ")

internal fun trimOcrNoise(input: String): String =
    input.trim { it.isWhitespace() || it in ocrNoiseChars }

internal fun normalizeDecimalSeparator(input: String): String =
    input.replace("(?<=\\d),(?=\\d)".toRegex(), ".")

internal fun removePrefixIgnoreCase(input: String, prefix: String): String =
    if (input.startsWith(prefix, ignoreCase = true)) input.drop(prefix.length) else input
