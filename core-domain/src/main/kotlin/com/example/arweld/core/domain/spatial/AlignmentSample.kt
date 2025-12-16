package com.example.arweld.core.domain.spatial

/**
 * Represents a single correspondence between a world-space observation and a
 * known model-space point.
 */
data class AlignmentPoint(
    val worldPoint: Vector3,
    val modelPoint: Vector3,
)

/**
 * Collection of point correspondences used to solve a rigid transform.
 */
data class AlignmentSample(
    val points: List<AlignmentPoint>,
)
