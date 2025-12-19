package com.example.arweld.core.structural.geom

import kotlin.math.abs

/**
 * Simple ear-clipping triangulation for 2D polygons.
 * Assumes counter-clockwise winding and no self-intersections.
 */
object Triangulation {

    /**
     * Triangulate a 2D polygon using ear clipping.
     * @param points List of 2D points in counter-clockwise order
     * @return List of triangle indices (each 3 consecutive values form a triangle)
     */
    fun triangulate2D(points: List<Pair<Float, Float>>): List<Int> {
        if (points.size < 3) return emptyList()
        if (points.size == 3) return listOf(0, 1, 2)

        val indices = mutableListOf<Int>()
        val remaining = points.indices.toMutableList()

        while (remaining.size > 3) {
            var earFound = false

            for (i in remaining.indices) {
                val prev = remaining[(i - 1 + remaining.size) % remaining.size]
                val curr = remaining[i]
                val next = remaining[(i + 1) % remaining.size]

                if (isEar(points, remaining, prev, curr, next)) {
                    indices.add(prev)
                    indices.add(curr)
                    indices.add(next)
                    remaining.removeAt(i)
                    earFound = true
                    break
                }
            }

            if (!earFound) {
                // Fallback: if no ear found, just emit remaining as fan
                if (remaining.size >= 3) {
                    val base = remaining[0]
                    for (j in 1 until remaining.size - 1) {
                        indices.add(base)
                        indices.add(remaining[j])
                        indices.add(remaining[j + 1])
                    }
                }
                break
            }
        }

        // Add final triangle
        if (remaining.size == 3) {
            indices.add(remaining[0])
            indices.add(remaining[1])
            indices.add(remaining[2])
        }

        return indices
    }

    private fun isEar(
        points: List<Pair<Float, Float>>,
        remaining: List<Int>,
        prev: Int,
        curr: Int,
        next: Int
    ): Boolean {
        val p1 = points[prev]
        val p2 = points[curr]
        val p3 = points[next]

        // Check if triangle is oriented correctly (CCW)
        val cross = (p2.first - p1.first) * (p3.second - p1.second) -
                    (p2.second - p1.second) * (p3.first - p1.first)
        if (cross <= 0) return false

        // Check if any other point is inside the triangle
        for (idx in remaining) {
            if (idx == prev || idx == curr || idx == next) continue
            if (pointInTriangle(points[idx], p1, p2, p3)) {
                return false
            }
        }

        return true
    }

    private fun pointInTriangle(
        p: Pair<Float, Float>,
        a: Pair<Float, Float>,
        b: Pair<Float, Float>,
        c: Pair<Float, Float>
    ): Boolean {
        val sign = { p1: Pair<Float, Float>, p2: Pair<Float, Float>, p3: Pair<Float, Float> ->
            (p1.first - p3.first) * (p2.second - p3.second) -
            (p2.first - p3.first) * (p1.second - p3.second)
        }

        val d1 = sign(p, a, b)
        val d2 = sign(p, b, c)
        val d3 = sign(p, c, a)

        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)

        return !(hasNeg && hasPos)
    }
}
