package com.example.arweld.core.domain.structural

import com.example.arweld.core.structural.profiles.ProfileType
import com.example.arweld.core.structural.profiles.parse.parseProfileString

data class ProfileItem(
    val profileRef: String,
    val displayName: String,
    val type: ProfileType,
    val dimensionsSummary: String? = null
)

interface ProfileCatalogQuery {
    suspend fun listAll(): List<ProfileItem>
    suspend fun search(query: String, limit: Int): List<ProfileItem>
    suspend fun lookup(profileRef: String): ProfileItem?
}

fun normalizeProfileRef(input: String): String? =
    runCatching { parseProfileString(input).designation }.getOrNull()
