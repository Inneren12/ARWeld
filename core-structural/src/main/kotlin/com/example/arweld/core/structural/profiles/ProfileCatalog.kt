package com.example.arweld.core.structural.profiles

import com.example.arweld.core.structural.profiles.parse.normalizeDesignation
import com.example.arweld.core.structural.profiles.parse.parseProfileString
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ProfileCatalog(
    private val resourceLoader: CatalogResourceLoader = DefaultCatalogResourceLoader(),
    extraProfiles: List<ProfileSpec> = emptyList()
) {

    private val index: Lazy<CatalogIndex> = lazy {
        val resourceProfiles = resourceLoader.loadCatalogResources().flatMap { resource ->
            CatalogResourceParser.parse(resource)
        }
        buildIndex(resourceProfiles + extraProfiles)
    }

    /**
     * Finds a profile by designation after normalizing the provided string.
     * Returns null if the profile is unknown or the string cannot be parsed.
     */
    fun findByDesignation(
        rawOrCanonical: String,
        preferredStandard: ProfileStandard = ProfileStandard.CSA
    ): ProfileSpec? {
        val parsed = runCatching { parseProfileString(rawOrCanonical) }.getOrElse { return null }
        val normalizedDesignation = parsed.designation

        val key = resolveProfileKey(normalizedDesignation, preferredStandard)
        if (key != null) {
            return index.value.specsByKey[key]
        }

        if (parsed.type == ProfileType.PL) {
            return createPlateSpecFromDesignation(normalizedDesignation, preferredStandard)
        }

        return null
    }

    fun requireByDesignation(
        rawOrCanonical: String,
        preferredStandard: ProfileStandard = ProfileStandard.CSA
    ): ProfileSpec =
        findByDesignation(rawOrCanonical, preferredStandard)
            ?: error("Profile '$rawOrCanonical' not found (preferred standard: $preferredStandard)")

    fun listStandards(): Set<ProfileStandard> = index.value.standards

    fun listTypes(): Set<ProfileType> = index.value.types

    private fun resolveProfileKey(
        normalizedDesignation: String,
        preferredStandard: ProfileStandard
    ): ProfileKey? {
        val catalog = index.value

        val directPreferred = ProfileKey(preferredStandard, normalizedDesignation)
        if (catalog.specsByKey.containsKey(directPreferred)) {
            return directPreferred
        }

        catalog.designationIndex[normalizedDesignation]
            ?.firstOrNull { it.standard == preferredStandard }
            ?.let { return it }

        catalog.aliasIndex[normalizedDesignation]
            ?.firstOrNull { it.standard == preferredStandard }
            ?.let { return it }

        catalog.designationIndex[normalizedDesignation]?.firstOrNull()?.let { return it }
        catalog.aliasIndex[normalizedDesignation]?.firstOrNull()?.let { return it }

        return null
    }

    private fun buildIndex(specs: List<ProfileSpec>): CatalogIndex {
        val normalizedSpecs = specs.map { it.normalize() }
        val specsByKey = mutableMapOf<ProfileKey, ProfileSpec>()
        val designationIndex = mutableMapOf<String, MutableList<ProfileKey>>()
        val aliasIndex = mutableMapOf<String, MutableList<ProfileKey>>()

        normalizedSpecs.forEach { spec ->
            val key = ProfileKey(spec.standard, spec.designation)
            specsByKey.putIfAbsent(key, spec)

            designationIndex.getOrPut(spec.designation) { mutableListOf() }
                .add(key)

            spec.aliases.forEach { alias ->
                aliasIndex.getOrPut(alias) { mutableListOf() }.add(key)
            }
        }

        return CatalogIndex(
            specsByKey = specsByKey,
            designationIndex = designationIndex,
            aliasIndex = aliasIndex,
            standards = normalizedSpecs.map { it.standard }.toSet(),
            types = normalizedSpecs.map { it.type }.toSet() + ProfileType.PL
        )
    }

    private fun ProfileSpec.normalize(): ProfileSpec {
        val canonical = normalizeDesignation(type, designation)
        val normalizedAliases = aliases.map { normalizeDesignation(type, it) }
        return when (this) {
            is WShapeSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is ChannelSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is HssSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is AngleSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is PlateSpec -> copy(designation = canonical, aliases = normalizedAliases)
        }
    }

    private fun createPlateSpecFromDesignation(
        normalizedDesignation: String,
        preferredStandard: ProfileStandard
    ): ProfileSpec? {
        val body = normalizedDesignation.removePrefix("PL").removePrefix("pl")
        val parts = body.split("x")
        if (parts.size != 2) return null
        val thickness = parts[0].toDoubleOrNull() ?: return null
        val width = parts[1].toDoubleOrNull() ?: return null
        val canonical = normalizeDesignation(ProfileType.PL, normalizedDesignation)
        val areaMm2 = thickness * width
        return PlateSpec(
            designation = canonical,
            standard = preferredStandard,
            aliases = emptyList(),
            tMm = thickness,
            wMm = width,
            areaMm2 = areaMm2,
            massKgPerM = null
        )
    }
}

private data class ProfileKey(
    val standard: ProfileStandard,
    val designation: String
)

private data class CatalogIndex(
    val specsByKey: Map<ProfileKey, ProfileSpec>,
    val designationIndex: Map<String, List<ProfileKey>>,
    val aliasIndex: Map<String, List<ProfileKey>>,
    val standards: Set<ProfileStandard>,
    val types: Set<ProfileType>
)

data class CatalogResource(
    val name: String,
    val content: String
)

interface CatalogResourceLoader {
    fun loadCatalogResources(): List<CatalogResource>
}

class DefaultCatalogResourceLoader : CatalogResourceLoader {
    override fun loadCatalogResources(): List<CatalogResource> {
        val rootUrl = ProfileCatalog::class.java.getResource("/profiles") ?: return emptyList()
        val rootFile = File(rootUrl.toURI())
        if (!rootFile.exists() || !rootFile.isDirectory) return emptyList()

        return rootFile.listFiles { file ->
            file.isFile && file.name.startsWith("catalog_") && file.name.endsWith(".json")
        }?.map { file ->
            CatalogResource(name = file.name, content = file.readText())
        } ?: emptyList()
    }
}

private object CatalogResourceParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(resource: CatalogResource): List<ProfileSpec> {
        val dto = json.decodeFromString<ProfileCatalogResourceDto>(resource.content)
        return dto.items.map { it.toSpec(dto.standard, dto.type) }
    }
}

@Serializable
private data class ProfileCatalogResourceDto(
    val standard: ProfileStandard,
    val type: ProfileType,
    val items: List<ProfileCatalogItemDto>
)

@Serializable
private data class ProfileCatalogItemDto(
    val designation: String,
    val aliases: List<String> = emptyList(),
    val massKgPerM: Double? = null,
    val areaMm2: Double? = null,
    // W/C
    val dMm: Double? = null,
    val bfMm: Double? = null,
    val twMm: Double? = null,
    val tfMm: Double? = null,
    val kMm: Double? = null,
    val rMm: Double? = null,
    // HSS
    val hMm: Double? = null,
    val bMm: Double? = null,
    val tMm: Double? = null,
    val cornerRadiusMm: Double? = null,
    // Angle
    val leg1Mm: Double? = null,
    val leg2Mm: Double? = null,
    val wMm: Double? = null
)

private fun ProfileCatalogItemDto.toSpec(
    standard: ProfileStandard,
    type: ProfileType
): ProfileSpec {
    return when (type) {
        ProfileType.W -> WShapeSpec(
            designation = designation,
            standard = standard,
            aliases = aliases,
            massKgPerM = massKgPerM,
            areaMm2 = areaMm2,
            dMm = dMm ?: error("dMm is required for W shape ($designation)"),
            bfMm = bfMm ?: error("bfMm is required for W shape ($designation)"),
            twMm = twMm ?: error("twMm is required for W shape ($designation)"),
            tfMm = tfMm ?: error("tfMm is required for W shape ($designation)"),
            kMm = kMm,
            rMm = rMm
        )

        ProfileType.C -> ChannelSpec(
            designation = designation,
            standard = standard,
            aliases = aliases,
            massKgPerM = massKgPerM,
            areaMm2 = areaMm2,
            dMm = dMm,
            bfMm = bfMm,
            twMm = twMm,
            tfMm = tfMm
        )

        ProfileType.HSS -> HssSpec(
            designation = designation,
            standard = standard,
            aliases = aliases,
            massKgPerM = massKgPerM,
            areaMm2 = areaMm2,
            hMm = hMm ?: error("hMm is required for HSS ($designation)"),
            bMm = bMm ?: error("bMm is required for HSS ($designation)"),
            tMm = tMm ?: error("tMm is required for HSS ($designation)"),
            cornerRadiusMm = cornerRadiusMm
        )

        ProfileType.L -> AngleSpec(
            designation = designation,
            standard = standard,
            aliases = aliases,
            massKgPerM = massKgPerM,
            areaMm2 = areaMm2,
            leg1Mm = leg1Mm ?: error("leg1Mm is required for angle ($designation)"),
            leg2Mm = leg2Mm ?: error("leg2Mm is required for angle ($designation)"),
            tMm = tMm ?: error("tMm is required for angle ($designation)"),
            cornerRadiusMm = cornerRadiusMm
        )

        ProfileType.PL -> PlateSpec(
            designation = designation,
            standard = standard,
            aliases = aliases,
            massKgPerM = massKgPerM,
            areaMm2 = areaMm2,
            tMm = tMm ?: error("tMm is required for plate ($designation)"),
            wMm = wMm ?: leg1Mm ?: error("wMm is required for plate ($designation)")
        )
    }
}
