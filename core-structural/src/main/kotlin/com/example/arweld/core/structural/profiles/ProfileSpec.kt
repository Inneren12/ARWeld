package com.example.arweld.core.structural.profiles

import kotlinx.serialization.Serializable

/**
 * Catalog entry describing a structural steel profile.
 *
 * All geometry values are expressed in millimeters (mm). Mass properties are
 * expressed as kilograms per meter (kg/m).
 */
@Serializable
data class ProfileSpec(
    val type: ProfileType,
    val designation: String,       // e.g., "W310x39"
    val depthMm: Double,           // overall depth, mm
    val widthMm: Double?,          // flange/leg/plate width, mm (null if not applicable)
    val thicknessWebMm: Double?,   // web thickness, mm
    val thicknessFlangeMm: Double?,// flange/leg/plate thickness, mm
    val areaMm2: Double?,          // cross-sectional area, mm^2
    val weightPerMeterKg: Double?  // linear mass, kg/m
)

@Serializable
enum class ProfileType {
    W,
    HSS,
    C,
    L,
    PL
}
