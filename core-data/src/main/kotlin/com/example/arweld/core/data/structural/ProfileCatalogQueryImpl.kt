package com.example.arweld.core.data.structural

import com.example.arweld.core.domain.structural.ProfileCatalogQuery
import com.example.arweld.core.domain.structural.ProfileItem
import com.example.arweld.core.domain.structural.normalizeProfileRef
import com.example.arweld.core.structural.profiles.AngleSpec
import com.example.arweld.core.structural.profiles.ChannelSpec
import com.example.arweld.core.structural.profiles.HssSpec
import com.example.arweld.core.structural.profiles.PlateSpec
import com.example.arweld.core.structural.profiles.ProfileCatalog
import com.example.arweld.core.structural.profiles.ProfileSpec
import com.example.arweld.core.structural.profiles.ProfileType
import com.example.arweld.core.structural.profiles.WShapeSpec
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class ProfileCatalogQueryImpl @Inject constructor(
    private val catalog: ProfileCatalog,
    private val dispatcher: CoroutineDispatcher
) : ProfileCatalogQuery {

    private val mutex = Mutex()

    @Volatile
    private var cache: CatalogCache? = null

    override suspend fun listAll(): List<ProfileItem> = ensureCache().orderedItems

    override suspend fun search(query: String, limit: Int): List<ProfileItem> {
        if (limit <= 0) return emptyList()
        val normalizedQuery = normalizeSearchQuery(query)
        if (normalizedQuery.isBlank()) {
            return ensureCache().orderedItems.take(limit)
        }
        return ensureCache()
            .entries
            .asSequence()
            .filter { entry -> entry.searchKey.contains(normalizedQuery) }
            .map { it.item }
            .take(limit)
            .toList()
    }

    override suspend fun lookup(profileRef: String): ProfileItem? {
        val spec = catalog.findByDesignation(profileRef) ?: return null
        val cacheSnapshot = ensureCache()
        return cacheSnapshot.byProfileRef[spec.designation] ?: spec.toProfileItem()
    }

    private suspend fun ensureCache(): CatalogCache {
        cache?.let { return it }
        return mutex.withLock {
            cache?.let { return it }
            val built = withContext(dispatcher) { buildCache() }
            cache = built
            built
        }
    }

    private fun buildCache(): CatalogCache {
        val entries = catalog.listAll()
            .map { spec -> spec to spec.toProfileItem() }
            .sortedWith(
                compareBy<Pair<ProfileSpec, ProfileItem>>(
                    { typeOrder[it.second.type] ?: Int.MAX_VALUE },
                    { it.second.profileRef }
                )
            )
            .map { (spec, item) ->
                SearchEntry(
                    item = item,
                    searchKey = buildSearchKey(item, spec.aliases)
                )
            }
        val items = entries.map { it.item }
        return CatalogCache(
            entries = entries,
            orderedItems = items,
            byProfileRef = items.associateBy { it.profileRef }
        )
    }

    private fun buildSearchKey(item: ProfileItem, aliases: List<String>): String {
        val aliasKeys = aliases.map { normalizeSearchKey(it) }
        val keys = listOf(
            normalizeSearchKey(item.profileRef),
            normalizeSearchKey(item.displayName),
        ) + aliasKeys
        return keys.joinToString(separator = " ")
    }

    private fun normalizeSearchQuery(input: String): String {
        val canonical = normalizeProfileRef(input)
        return normalizeSearchKey(canonical ?: input)
    }

    private fun normalizeSearchKey(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""
        val collapsed = trimmed.replace("\\s+".toRegex(), "")
        return collapsed
            .replace('×', 'x')
            .lowercase(Locale.US)
    }

    private fun ProfileSpec.toProfileItem(): ProfileItem {
        return ProfileItem(
            profileRef = designation,
            displayName = designation,
            type = type,
            dimensionsSummary = formatDimensions(this)
        )
    }

    private fun formatDimensions(spec: ProfileSpec): String? {
        val formatter = decimalFormatter
        return when (spec) {
            is WShapeSpec -> formatDimensions(
                formatter,
                spec.dMm,
                spec.bfMm,
                spec.twMm,
                spec.tfMm
            )

            is ChannelSpec -> formatDimensions(
                formatter,
                spec.dMm,
                spec.bfMm,
                spec.twMm,
                spec.tfMm
            )

            is HssSpec -> formatDimensions(
                formatter,
                spec.hMm,
                spec.bMm,
                spec.tMm
            )

            is AngleSpec -> formatDimensions(
                formatter,
                spec.leg1Mm,
                spec.leg2Mm,
                spec.tMm
            )

            is PlateSpec -> formatDimensions(
                formatter,
                spec.tMm,
                spec.wMm
            )
        } + " mm"
    }

    private fun formatDimensions(
        formatter: DecimalFormat,
        vararg values: Double
    ): String = values.joinToString(" x ") { formatter.format(it) }

    private data class SearchEntry(
        val item: ProfileItem,
        val searchKey: String
    )

    private data class CatalogCache(
        val entries: List<SearchEntry>,
        val orderedItems: List<ProfileItem>,
        val byProfileRef: Map<String, ProfileItem>
    )

    private companion object {
        val typeOrder = mapOf(
            ProfileType.W to 0,
            ProfileType.HSS to 1,
            ProfileType.C to 2,
            ProfileType.L to 3,
            ProfileType.PL to 4
        )

        val decimalFormatter = DecimalFormat("0.###", DecimalFormatSymbols(Locale.US))
    }
}
