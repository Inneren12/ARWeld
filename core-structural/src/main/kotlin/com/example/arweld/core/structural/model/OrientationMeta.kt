package com.example.arweld.core.structural.model

import kotlinx.serialization.Serializable

/**
 * Additional orientation metadata applied to the physical member.
 *
 * - rollAngleDeg: optional rotation about the member axis, degrees.
 * - camberMm: optional camber magnitude along the member, millimeters.
 */
@Serializable
data class OrientationMeta(
    val rollAngleDeg: Double?,
    val camberMm: Double?
)
