package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ChannelSeries
import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.ProfileType

data class ParsedProfileId(
    val type: ProfileType,
    val designation: String, // canonical
    val standardHint: ProfileStandard?,
    val channelSeries: ChannelSeries? = null,
    val raw: String
)

/**
 * Parses raw profile strings such as "C200X17", "PL 10×190", or "L51x38x6,4"
 * into normalized identifiers suitable for catalog lookup.
 */
fun parseProfileString(raw: String): ParsedProfileId {
    val normalizedInput = normalizeRawProfileInput(raw)
    require(normalizedInput.isNotBlank()) { "Profile string is empty" }

    val rule = profileParseRules.firstOrNull { it.regex.containsMatchIn(normalizedInput) }
        ?: throw IllegalArgumentException("Unsupported profile format: '$raw'")

    validateFormat(rule.type, normalizedInput, raw)

    val designation = normalizeDesignation(rule.type, normalizedInput, rule.channelSeries)
    val standardHint = determineStandardHint(normalizedInput)

    return ParsedProfileId(
        type = rule.type,
        designation = designation,
        standardHint = standardHint,
        channelSeries = rule.channelSeries,
        raw = raw
    )
}

private val fractionRegex = "\\d+/\\d+".toRegex()

private fun determineStandardHint(input: String): ProfileStandard? =
    if (fractionRegex.containsMatchIn(input) || input.contains('"') || input.contains('\'')) {
        ProfileStandard.AISC
    } else {
        null
    }

private data class ProfileParseRule(
    val regex: Regex,
    val type: ProfileType,
    val channelSeries: ChannelSeries? = null
)

private val profileParseRules = listOf(
    ProfileParseRule("^\\s*PL".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.PL),
    ProfileParseRule("^\\s*HSS".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.HSS),
    ProfileParseRule("^\\s*MC".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.C, ChannelSeries.MC),
    ProfileParseRule("^\\s*C".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.C, ChannelSeries.C),
    ProfileParseRule("^\\s*L".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.L),
    ProfileParseRule("^\\s*W".toRegex(setOf(RegexOption.IGNORE_CASE)), ProfileType.W)
)

private val wPattern =
    "^\\s*W\\s*\\d+(?:\\.\\d+)?\\s*x\\s*\\d+(?:\\.\\d+)?\\s*$".toRegex(RegexOption.IGNORE_CASE)
private val hssPattern =
    "^\\s*HSS\\s*\\d+(?:\\.\\d+)?\\s*x\\s*\\d+(?:\\.\\d+)?\\s*x\\s*(?:\\d+(?:\\.\\d+)?|\\d+/\\d+)\\s*$"
        .toRegex(RegexOption.IGNORE_CASE)
private val channelPattern =
    "^\\s*(?:MC|C)\\s*\\d+(?:\\.\\d+)?\\s*x\\s*\\d+(?:\\.\\d+)?\\s*$"
        .toRegex(RegexOption.IGNORE_CASE)
private val anglePattern =
    "^\\s*L\\s*\\d+(?:\\.\\d+)?\\s*x\\s*\\d+(?:\\.\\d+)?\\s*x\\s*(?:\\d+(?:\\.\\d+)?|\\d+/\\d+)\\s*$"
        .toRegex(RegexOption.IGNORE_CASE)

private fun validateFormat(type: ProfileType, normalizedInput: String, raw: String) {
    val isValid = when (type) {
        ProfileType.W -> wPattern.matches(normalizedInput)
        ProfileType.HSS -> hssPattern.matches(normalizedInput)
        ProfileType.C -> channelPattern.matches(normalizedInput)
        ProfileType.L -> anglePattern.matches(normalizedInput)
        ProfileType.PL -> parsePlateDimensions(normalizedInput) != null
    }

    if (!isValid) {
        throw IllegalArgumentException("Unsupported profile format: '$raw'")
    }
}
