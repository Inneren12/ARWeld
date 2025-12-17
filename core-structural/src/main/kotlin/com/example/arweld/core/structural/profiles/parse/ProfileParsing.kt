package com.example.arweld.core.structural.profiles.parse

import com.example.arweld.core.structural.profiles.ProfileStandard
import com.example.arweld.core.structural.profiles.ProfileType

data class ParsedProfileId(
    val type: ProfileType,
    val designation: String, // canonical
    val standardHint: ProfileStandard? = ProfileStandard.CSA
)

/**
 * Parses raw profile strings such as "C200X17", "PL 10×190", or "L51x38x6,4"
 * into normalized identifiers suitable for catalog lookup.
 */
fun parseProfileString(raw: String): ParsedProfileId {
    val normalizedInput = normalizeRawProfileInput(raw)
    require(normalizedInput.isNotBlank()) { "Profile string is empty" }

    val upper = normalizedInput.uppercase()
    val type = when {
        upper.startsWith("PL") -> ProfileType.PL
        upper.startsWith("C") || upper.startsWith("MC") -> ProfileType.C
        upper.startsWith("L") -> ProfileType.L
        upper.startsWith("W") -> ProfileType.W
        upper.startsWith("HSS") -> ProfileType.HSS
        else -> throw IllegalArgumentException("Unsupported profile format: '$raw'")
    }

    val designation = normalizeDesignation(type, normalizedInput)
    return ParsedProfileId(type = type, designation = designation, standardHint = ProfileStandard.CSA)
}
