package com.example.arweld.core.structural.profiles

data class ParsedProfileId(
    val type: ProfileType,
    val designation: String // normalized ID for lookup
)

/**
 * Parses raw profile strings such as "W310x39", "hss 6x6x3/8", "PL 10x250"
 * into normalized identifiers suitable for catalog lookup.
 */
fun parseProfileString(raw: String): ParsedProfileId {
    val trimmed = raw.trim()
    require(trimmed.isNotEmpty()) { "Profile string is empty" }

    val patterns = listOf(
        ProfileType.W to Regex("^(?i)\\s*W\\s*([0-9A-Za-z./\\-xX\\s]+)\\s*$"),
        ProfileType.HSS to Regex("^(?i)\\s*HSS\\s*([0-9A-Za-z./\\-xX\\s]+)\\s*$"),
        ProfileType.C to Regex("^(?i)\\s*(?:MC|C)\\s*([0-9A-Za-z./\\-xX\\s]+)\\s*$"),
        ProfileType.L to Regex("^(?i)\\s*L\\s*([0-9A-Za-z./\\-xX\\s]+)\\s*$"),
        ProfileType.PL to Regex("^(?i)\\s*PL\\s*([0-9A-Za-z./\\-xX\\s]+)\\s*$")
    )

    for (pattern in patterns) {
        val match = pattern.second.find(trimmed)
        if (match != null) {
            val normalizedDesignation =
                ProfileCatalog.normalizeDesignation(pattern.first, match.groupValues[1])
            return ParsedProfileId(pattern.first, normalizedDesignation)
        }
    }

    throw IllegalArgumentException("Unsupported profile format: '$raw'")
}
