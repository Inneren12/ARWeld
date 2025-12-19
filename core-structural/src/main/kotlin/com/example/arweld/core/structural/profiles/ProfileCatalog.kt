package com.example.arweld.core.structural.profiles

import com.example.arweld.core.structural.profiles.parse.canonicalizePlateDesignation
import com.example.arweld.core.structural.profiles.parse.normalizeDesignation
import com.example.arweld.core.structural.profiles.parse.parsePlateDimensions
import com.example.arweld.core.structural.profiles.parse.parseProfileString
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val DEFAULT_RESOURCE_NAME = "profiles.json"

class ProfileCatalog(
    private val resourceLoader: CatalogResourceLoader = DefaultCatalogResourceLoader(),
    extraProfiles: List<ProfileSpec> = emptyList(),
    private val resourceName: String = DEFAULT_RESOURCE_NAME
) {

    private val index: Lazy<CatalogIndex> = lazy {
        val resourceProfiles = loadProfilesFromResource(resourceName)
            ?: CatalogResourceParser.parseAll(resourceLoader.loadCatalogResources())
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

        val searchStandards = buildSearchPriority(parsed.standardHint, preferredStandard)

        searchStandards.forEach { standard ->
            resolveProfileKey(normalizedDesignation, standard)?.let { key ->
                return index.value.specsByKey[key]
            }
        }

        if (parsed.type == ProfileType.PL) {
            val plateStandard = parsed.standardHint ?: preferredStandard
            return createPlateSpecFromDesignation(normalizedDesignation, plateStandard)
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
        standard: ProfileStandard
    ): ProfileKey? {
        val catalog = index.value

        val directPreferred = ProfileKey(standard, normalizedDesignation)
        if (catalog.specsByKey.containsKey(directPreferred)) {
            return directPreferred
        }

        catalog.designationIndex[normalizedDesignation]
            ?.firstOrNull { it.standard == standard }
            ?.let { return it }

        catalog.aliasIndex[normalizedDesignation]
            ?.firstOrNull { it.standard == standard }
            ?.let { return it }

        return null
    }

    private fun buildIndex(specs: List<ProfileSpec>): CatalogIndex {
        val normalizedSpecs = specs.map { it.normalize() }
        val specsByKey = mutableMapOf<ProfileKey, ProfileSpec>()
        val designationIndex = mutableMapOf<String, MutableList<ProfileKey>>()
        val aliasIndex = mutableMapOf<String, MutableList<ProfileKey>>()

        normalizedSpecs.forEach { spec ->
            val key = ProfileKey(spec.standard, spec.designation)
            val existing = specsByKey[key]
            if (existing != null && existing != spec) {
                error("Profile collision for ${spec.designation} (${spec.standard})")
            }
            specsByKey[key] = existing ?: spec

            designationIndex.getOrPut(spec.designation) { mutableListOf() }
                .add(key)

            spec.aliases.forEach { alias ->
                val aliasMappings = aliasIndex.getOrPut(alias) { mutableListOf() }
                if (aliasMappings.any { specsByKey[it] != spec }) {
                    error("Alias collision for $alias")
                }
                aliasMappings.add(key)
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

    private fun buildSearchPriority(
        standardHint: ProfileStandard?,
        preferredStandard: ProfileStandard
    ): List<ProfileStandard> {
        val ordered = mutableListOf<ProfileStandard>()
        if (standardHint != null) {
            ordered += standardHint
        }
        if (preferredStandard !in ordered) {
            ordered += preferredStandard
        }
        ordered.addAll(index.value.standards.filterNot { it in ordered })
        return ordered
    }

    private fun ProfileSpec.normalize(): ProfileSpec {
        val canonical = normalizeDesignation(type, designation)
        val normalizedAliases = aliases.map { normalizeDesignation(type, it) }
        return when (this) {
            is WShapeSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is ChannelSpec -> copy(
                designation = canonical,
                aliases = normalizedAliases,
                channelSeries = channelSeries
            )
            is HssSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is AngleSpec -> copy(designation = canonical, aliases = normalizedAliases)
            is PlateSpec -> copy(designation = canonical, aliases = normalizedAliases)
        }
    }

    private fun createPlateSpecFromDesignation(
        normalizedDesignation: String,
        preferredStandard: ProfileStandard
    ): ProfileSpec? {
        val canonical = canonicalizePlateDesignation(normalizedDesignation) ?: return null
        val dimensions = parsePlateDimensions(normalizedDesignation) ?: return null
        val areaMm2 = dimensions.thicknessMm * dimensions.widthMm
        val massKgPerM = areaMm2 * 1e-6 * 7850
        return PlateSpec(
            designation = canonical,
            standard = preferredStandard,
            aliases = emptyList(),
            tMm = dimensions.thicknessMm,
            wMm = dimensions.widthMm,
            areaMm2 = areaMm2,
            massKgPerM = massKgPerM
        )
    }

    private fun loadProfilesFromResource(resourceName: String): List<ProfileSpec>? {
        if (resourceName.isBlank()) {
            return null
        }
        val classLoader = ProfileCatalog::class.java.classLoader
        val stream = classLoader.getResourceAsStream(resourceName) ?: return null
        val content = stream.bufferedReader().use { it.readText() }
        return ProfilesJsonParser.parse(content)
    }

    companion object {
        fun loadFromResource(
            resourceName: String,
            extraProfiles: List<ProfileSpec> = emptyList()
        ): ProfileCatalog = ProfileCatalog(extraProfiles = extraProfiles, resourceName = resourceName)
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
        val classLoader = ProfileCatalog::class.java.classLoader
        val indexStream = classLoader.getResourceAsStream("profiles/catalog_index.json")
            ?: return emptyList()

        val indexJson = indexStream.bufferedReader().use { it.readText() }
        val index = CatalogIndexDtoJson.decodeFromString<CatalogResourceIndexDto>(indexJson)

        // TODO: no directory listing; JAR-safe
        return index.files.mapNotNull { name ->
            classLoader.getResourceAsStream("profiles/$name")?.use { stream ->
                CatalogResource(name = name, content = stream.bufferedReader().use { it.readText() })
            }
        }
    }
}

private object CatalogResourceParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseAll(resources: List<CatalogResource>): List<ProfileSpec> {
        val errors = mutableListOf<String>()
        val specs = mutableListOf<ProfileSpec>()

        resources.forEach { resource ->
            val dto = json.decodeFromString<ProfileCatalogResourceDto>(resource.content)
            dto.items.forEach { item ->
                val missing = item.validate(dto.type)
                if (missing.isNotEmpty()) {
                    errors += "${resource.name} :: ${item.designation}: missing ${missing.joinToString()}"
                } else {
                    specs += item.toSpec(dto.standard, dto.type)
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw IllegalStateException("Invalid profile catalog resources:\n" + errors.joinToString("\n"))
        }

        return specs
    }
}

@Serializable
private data class ProfilesJsonDocument(
    val version: String,
    val standard: ProfileStandard,
    val profiles: List<ProfilesJsonProfileDto>
)

@Serializable
private data class ProfilesJsonProfileDto(
    val type: ProfileType,
    val designation: String,
    val aliases: List<String> = emptyList(),
    val massKgPerM: Double? = null,
    val areaMm2: Double? = null,
    val channelSeries: ChannelSeries? = null,
    val dMm: Double? = null,
    val bfMm: Double? = null,
    val twMm: Double? = null,
    val tfMm: Double? = null,
    val kMm: Double? = null,
    val rMm: Double? = null,
    val hMm: Double? = null,
    val bMm: Double? = null,
    val tMm: Double? = null,
    val cornerRadiusMm: Double? = null,
    val leg1Mm: Double? = null,
    val leg2Mm: Double? = null,
    val wMm: Double? = null
)

private object ProfilesJsonParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(content: String): List<ProfileSpec> {
        val document = json.decodeFromString<ProfilesJsonDocument>(content)
        val errors = mutableListOf<String>()
        val specs = mutableListOf<ProfileSpec>()

        document.profiles.forEach { item ->
            val dto = item.toCatalogItemDto()
            val missing = dto.validate(item.type)
            if (missing.isNotEmpty()) {
                errors += "${item.designation}: missing ${missing.joinToString()}"
            } else {
                specs += dto.toSpec(document.standard, item.type)
            }
        }

        if (errors.isNotEmpty()) {
            throw IllegalStateException("Invalid profiles.json:\n" + errors.joinToString("\n"))
        }

        return specs
    }
}

private fun ProfilesJsonProfileDto.toCatalogItemDto(): ProfileCatalogItemDto =
    ProfileCatalogItemDto(
        designation = designation,
        aliases = aliases,
        massKgPerM = massKgPerM,
        areaMm2 = areaMm2,
        channelSeries = channelSeries,
        dMm = dMm,
        bfMm = bfMm,
        twMm = twMm,
        tfMm = tfMm,
        kMm = kMm,
        rMm = rMm,
        hMm = hMm,
        bMm = bMm,
        tMm = tMm,
        cornerRadiusMm = cornerRadiusMm,
        leg1Mm = leg1Mm,
        leg2Mm = leg2Mm,
        wMm = wMm
    )

@Serializable
data class ProfileCatalogResourceDto(
    val standard: ProfileStandard,
    val type: ProfileType,
    val items: List<ProfileCatalogItemDto>
)

@Serializable
data class ProfileCatalogItemDto(
    val designation: String,
    val aliases: List<String> = emptyList(),
    val massKgPerM: Double? = null,
    val areaMm2: Double? = null,
    val channelSeries: ChannelSeries? = null,
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

fun ProfileCatalogItemDto.validate(type: ProfileType): List<String> {
    val missing = mutableListOf<String>()
    when (type) {
        ProfileType.W -> {
            if (dMm == null) missing += "dMm"
            if (bfMm == null) missing += "bfMm"
            if (twMm == null) missing += "twMm"
            if (tfMm == null) missing += "tfMm"
        }

        ProfileType.C -> {
            if (dMm == null) missing += "dMm"
            if (bfMm == null) missing += "bfMm"
            if (twMm == null) missing += "twMm"
            if (tfMm == null) missing += "tfMm"
        }

        ProfileType.HSS -> {
            if (hMm == null) missing += "hMm"
            if (bMm == null) missing += "bMm"
            if (tMm == null) missing += "tMm"
        }

        ProfileType.L -> {
            if (leg1Mm == null) missing += "leg1Mm"
            if (leg2Mm == null) missing += "leg2Mm"
            if (tMm == null) missing += "tMm"
        }

        ProfileType.PL -> {
            if (tMm == null) missing += "tMm"
            if (wMm == null && leg1Mm == null) missing += "wMm"
        }
    }
    return missing
}

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
            dMm = dMm ?: error("dMm is required for channel ($designation)"),
            bfMm = bfMm ?: error("bfMm is required for channel ($designation)"),
            twMm = twMm ?: error("twMm is required for channel ($designation)"),
            tfMm = tfMm ?: error("tfMm is required for channel ($designation)"),
            channelSeries = channelSeries ?: inferChannelSeriesFromDesignation(designation)
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

        ProfileType.PL -> {
            val thickness = tMm ?: error("tMm is required for plate ($designation)")
            val width = wMm ?: leg1Mm ?: error("wMm is required for plate ($designation)")
            val resolvedArea = areaMm2 ?: thickness * width
            val resolvedMass = massKgPerM ?: resolvedArea * 1e-6 * 7850

            PlateSpec(
                designation = designation,
                standard = standard,
                aliases = aliases,
                tMm = thickness,
                wMm = width,
                areaMm2 = resolvedArea,
                massKgPerM = resolvedMass
            )
        }
    }
}

private fun inferChannelSeriesFromDesignation(designation: String): ChannelSeries =
    if (designation.trimStart().startsWith("MC", ignoreCase = true)) ChannelSeries.MC else ChannelSeries.C

@Serializable
private data class CatalogResourceIndexDto(
    val files: List<String>
)

private val CatalogIndexDtoJson = Json { ignoreUnknownKeys = true }
