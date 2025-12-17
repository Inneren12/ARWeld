package com.example.arweld.core.structural.profiles

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ProfileCatalog(
    profiles: List<ProfileSpec> = loadSeedProfiles()
) {
    private val profilesByDesignation: Map<String, ProfileSpec> =
        profiles.associateBy { normalizeDesignation(it.type, it.designation) }

    /**
     * Finds a profile by designation after normalizing the provided string.
     * Returns null if the profile is unknown or the string cannot be parsed.
     */
    fun findByDesignation(designation: String): ProfileSpec? {
        val parsed = runCatching { parseProfileString(designation) }.getOrElse { return null }
        return profilesByDesignation[parsed.designation]
    }

    fun allProfiles(): List<ProfileSpec> = profilesByDesignation.values.toList()

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * Loads the bundled seed profiles from resources.
         */
        fun loadSeedProfiles(): List<ProfileSpec> {
            val resource =
                ProfileCatalog::class.java.getResource("/profiles_seed.json")
                    ?: throw IllegalStateException("profiles_seed.json not found in resources")
            val text = resource.readText()
            val decoded = json.decodeFromString<List<ProfileSpec>>(text)
            return decoded.map { it.copy(designation = normalizeDesignation(it.type, it.designation)) }
        }

        internal fun normalizeDesignation(type: ProfileType, rawDesignation: String): String {
            val compactBody = rawDesignation.trim()
                .replace("\\s+".toRegex(), "")
                .replace("X", "x")
            val bodyWithoutPrefix = if (compactBody.uppercase().startsWith(type.name)) {
                compactBody.substring(type.name.length)
            } else {
                compactBody
            }
            return when (type) {
                ProfileType.HSS, ProfileType.PL -> "${type.name} $bodyWithoutPrefix"
                else -> "${type.name}$bodyWithoutPrefix"
            }
        }
    }
}
