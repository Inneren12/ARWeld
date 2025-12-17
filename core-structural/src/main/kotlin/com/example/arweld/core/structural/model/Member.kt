package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

enum class MemberKind { BEAM, COLUMN, BRACE, OTHER }

/**
 * Additional orientation metadata applied to the physical member.
 *
 * - rollAngleDeg: optional rotation about the member axis, degrees.
 * - camberMm: optional camber magnitude along the member, millimeters.
 */
@Serializable
data class OrientationMeta(
    val rollAngleDeg: Double? = null,
    val camberMm: Double? = null
)

/**
 * Member connecting two nodes. The profileDesignation is resolved via
 * ProfileCatalog during model loading.
 */
@Serializable
data class Member(
    val id: String,
    val kind: MemberKind,
    val profileDesignation: String,
    val nodeStartId: String,
    val nodeEndId: String,
    val orientation: OrientationMeta? = null
)
