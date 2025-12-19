package com.example.arweld.core.structural.geom

import kotlin.math.max
import kotlin.math.min

/**
 * Axis-Aligned Bounding Box.
 * Used for fast spatial queries and culling.
 */
data class Aabb(val min: Vec3, val max: Vec3) {

    /**
     * Expand this AABB to include the given point.
     */
    fun include(p: Vec3): Aabb = Aabb(
        min = Vec3(min(min.x, p.x), min(min.y, p.y), min(min.z, p.z)),
        max = Vec3(max(max.x, p.x), max(max.y, p.y), max(max.z, p.z))
    )

    /**
     * Expand this AABB to include another AABB.
     */
    fun include(other: Aabb): Aabb = Aabb(
        min = Vec3(min(min.x, other.min.x), min(min.y, other.min.y), min(min.z, other.min.z)),
        max = Vec3(max(max.x, other.max.x), max(max.y, other.max.y), max(max.z, other.max.z))
    )

    /**
     * Get the size of the AABB in each dimension.
     */
    fun size(): Vec3 = max - min

    /**
     * Get the center of the AABB.
     */
    fun center(): Vec3 = (min + max) * 0.5f

    /**
     * Check if this AABB contains a point.
     */
    fun contains(p: Vec3): Boolean =
        p.x >= min.x && p.x <= max.x &&
        p.y >= min.y && p.y <= max.y &&
        p.z >= min.z && p.z <= max.z

    companion object {
        /**
         * Create an empty AABB (inverted bounds that will expand to include any point).
         */
        fun empty(): Aabb = Aabb(
            min = Vec3(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            max = Vec3(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY)
        )

        /**
         * Create an AABB from a list of points.
         */
        fun fromPoints(points: List<Vec3>): Aabb {
            require(points.isNotEmpty()) { "Cannot create AABB from empty point list" }
            var aabb = empty()
            for (p in points) {
                aabb = aabb.include(p)
            }
            return aabb
        }
    }
}
