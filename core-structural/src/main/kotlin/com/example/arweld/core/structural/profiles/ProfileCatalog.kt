package com.example.arweld.core.structural.profiles

import kotlinx.serialization.decodeFromString

class ProfileCatalog(
    profiles: List<ProfileSpec> = loadSeedProfiles()
) {
    private val profilesByDesignation: Map<String, ProfileSpec> =
        profiles.flatMap { spec ->
            val canonicalId = normalizeDesignation(spec.type, spec.designation)
            val aliasIds = spec.aliases.map { normalizeDesignation(spec.type, it) }
            (listOf(canonicalId) + aliasIds).map { normalized -> normalized to spec }
        }.toMap()

    /**
     * Finds a profile by designation after normalizing the provided string.
     * Returns null if the profile is unknown or the string cannot be parsed.
     */
    fun findByDesignation(designation: String): ProfileSpec? {
        val parsed = runCatching { parseProfileString(designation) }.getOrElse { return null }
        return profilesByDesignation[parsed.designation]
    }

    fun allProfiles(): List<ProfileSpec> = profilesByDesignation.values.toSet().toList()

    companion object {
        private val json = kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
            classDiscriminator = "specKind"
        }

        /**
         * Loads the bundled seed profiles from resources.
         */
        fun loadSeedProfiles(): List<ProfileSpec> {
            val resource =
                ProfileCatalog::class.java.getResource("/profiles_seed.json")
                    ?: throw IllegalStateException("profiles_seed.json not found in resources")
            val text = resource.readText()
            val decoded = json.decodeFromString<List<ProfileSpec>>(text)
            return decoded.map { spec ->
                val normalizedDesignation = normalizeDesignation(spec.type, spec.designation)
                val normalizedAliases = spec.aliases.map { alias ->
                    normalizeDesignation(spec.type, alias)
                }
                when (spec) {
                    is WShapeSpec -> spec.copy(
                        designation = normalizedDesignation,
                        aliases = normalizedAliases
                    )

                    is ChannelSpec -> spec.copy(
                        designation = normalizedDesignation,
                        aliases = normalizedAliases
                    )

                    is HssSpec -> spec.copy(
                        designation = normalizedDesignation,
                        aliases = normalizedAliases
                    )

                    is AngleSpec -> spec.copy(
                        designation = normalizedDesignation,
                        aliases = normalizedAliases
                    )

                    is PlateSpec -> spec.copy(
                        designation = normalizedDesignation,
                        aliases = normalizedAliases
                    )
                }
            }
        }

        internal fun normalizeDesignation(type: ProfileType, rawDesignation: String): String {
            val compactBody = rawDesignation.trim()
                .replace("\\s+".toRegex(), "")
                .replace("X", "x")
            val uppercaseBody = compactBody.uppercase()
            val bodyWithoutPrefix = when (type) {
                ProfileType.HSS, ProfileType.PL -> if (uppercaseBody.startsWith(type.name)) {
                    compactBody.drop(type.name.length)
                } else {
                    compactBody
                }

                ProfileType.C -> when {
                    uppercaseBody.startsWith("MC") -> compactBody.drop(2)
                    uppercaseBody.startsWith(type.name) -> compactBody.drop(type.name.length)
                    else -> compactBody
                }

                else -> if (uppercaseBody.startsWith(type.name)) {
                    compactBody.drop(type.name.length)
                } else {
                    compactBody
                }
            }
            return when (type) {
                ProfileType.HSS, ProfileType.PL -> "${type.name} $bodyWithoutPrefix"
                else -> "${type.name}$bodyWithoutPrefix"
            }
        }
    }
}
