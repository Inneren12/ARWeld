package com.example.arweld.core.structural.model

import com.example.arweld.core.structural.profiles.ProfileSpec
import kotlinx.serialization.Serializable

/**
 * Member connecting two nodes.
 */
@Serializable
data class Member(
    val id: String,
    val kind: MemberKind,
    val profile: ProfileSpec,
    val nodeStartId: String,
    val nodeEndId: String,
    val orientationMeta: OrientationMeta? = null
)
