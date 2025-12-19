package com.example.arweld.core.structural.geom

/**
 * Builds 3D meshes by extruding 2D cross-sections along the Z axis.
 *
 * The cross-section is defined in the XY plane (at z=0), and is extruded to z=length.
 * Supports outer contours and optional inner holes.
 */
class ExtrusionBuilder {

    private val vertices = mutableListOf<Float>()
    private val indices = mutableListOf<Int>()

    /**
     * Extrude a 2D cross-section along the Z axis.
     *
     * @param outerLoop List of 2D points (x, y) forming the outer boundary (CCW)
     * @param innerLoops Optional list of inner holes, each defined by 2D points (CW for holes)
     * @param length Length of extrusion along Z axis (in mm)
     * @return Pair of (vertices, indices) where vertices = [x,y,z, x,y,z, ...] and indices = [i0,i1,i2, ...]
     */
    fun extrude(
        outerLoop: List<Pair<Float, Float>>,
        innerLoops: List<List<Pair<Float, Float>>> = emptyList(),
        length: Float
    ): Pair<FloatArray, IntArray> {
        require(outerLoop.size >= 3) { "Outer loop must have at least 3 points" }
        require(length > 1e-3f) { "Extrusion length must be positive (> 1e-3 mm)" }

        vertices.clear()
        indices.clear()

        // Create vertices: bottom and top for each point
        val allLoops = listOf(outerLoop) + innerLoops
        val loopStarts = mutableListOf<Int>()

        for (loop in allLoops) {
            loopStarts.add(vertices.size / 3)
            // Bottom vertices (z = 0)
            for ((x, y) in loop) {
                vertices.add(x)
                vertices.add(y)
                vertices.add(0f)
            }
            // Top vertices (z = length)
            for ((x, y) in loop) {
                vertices.add(x)
                vertices.add(y)
                vertices.add(length)
            }
        }

        // Build side faces for each loop
        for ((loopIdx, loop) in allLoops.withIndex()) {
            val startIdx = loopStarts[loopIdx]
            val n = loop.size

            for (i in 0 until n) {
                val next = (i + 1) % n
                val i0 = startIdx + i          // bottom current
                val i1 = startIdx + next       // bottom next
                val i2 = startIdx + n + next   // top next
                val i3 = startIdx + n + i      // top current

                // Two triangles for quad (CCW from outside)
                if (loopIdx == 0) {
                    // Outer loop: CCW from outside
                    indices.add(i0)
                    indices.add(i1)
                    indices.add(i2)

                    indices.add(i0)
                    indices.add(i2)
                    indices.add(i3)
                } else {
                    // Inner loops (holes): CW from outside (reversed winding)
                    indices.add(i0)
                    indices.add(i2)
                    indices.add(i1)

                    indices.add(i0)
                    indices.add(i3)
                    indices.add(i2)
                }
            }
        }

        // Build end caps using triangulation
        // Bottom cap (z = 0)
        addEndCap(outerLoop, innerLoops, loopStarts, isTop = false)

        // Top cap (z = length)
        addEndCap(outerLoop, innerLoops, loopStarts, isTop = true)

        return Pair(vertices.toFloatArray(), indices.toIntArray())
    }

    private fun addEndCap(
        outerLoop: List<Pair<Float, Float>>,
        innerLoops: List<List<Pair<Float, Float>>>,
        loopStarts: List<Int>,
        isTop: Boolean
    ) {
        val outerSize = outerLoop.size
        val baseOffset = if (isTop) outerSize else 0

        if (innerLoops.isEmpty()) {
            // Simple case: no holes, triangulate outer loop
            val triIndices = Triangulation.triangulate2D(outerLoop)

            // Triangulation produces CCW triangles (normal pointing +Z when viewed from +Z)
            // For top cap (z=length, facing +Z): use as-is
            // For bottom cap (z=0, facing -Z): reverse each triangle's winding
            if (isTop) {
                // Top cap: normal should point outward (+Z)
                for (idx in triIndices) {
                    val vertexIdx = loopStarts[0] + baseOffset + idx
                    indices.add(vertexIdx)
                }
            } else {
                // Bottom cap: normal should point outward (-Z), so reverse winding
                var i = 0
                while (i < triIndices.size) {
                    val i0 = loopStarts[0] + baseOffset + triIndices[i]
                    val i1 = loopStarts[0] + baseOffset + triIndices[i + 1]
                    val i2 = loopStarts[0] + baseOffset + triIndices[i + 2]
                    // Reverse the triangle: instead of (i0, i1, i2), emit (i0, i2, i1)
                    indices.add(i0)
                    indices.add(i2)
                    indices.add(i1)
                    i += 3
                }
            }
        } else {
            // Complex case with holes: special handling for rectangular tubes (HSS)
            // Check if we have a 4-sided outer and 4-sided inner (rectangular tube)
            if (outerLoop.size == 4 && innerLoops.size == 1 && innerLoops[0].size == 4) {
                // HSS case: generate ring caps connecting outer and inner rectangles
                addRectangularRingCap(outerLoop, innerLoops[0], loopStarts, isTop)
            } else {
                // Fallback: simple fan from first vertex (works for convex regions)
                // Note: This doesn't properly handle holes - for production, use proper triangulation
                val center = loopStarts[0] + baseOffset
                for (i in 1 until outerSize - 1) {
                    if (isTop) {
                        // Top: reverse winding
                        indices.add(loopStarts[0] + baseOffset + i + 1)
                        indices.add(loopStarts[0] + baseOffset + i)
                        indices.add(center)
                    } else {
                        // Bottom: normal winding
                        indices.add(center)
                        indices.add(loopStarts[0] + baseOffset + i)
                        indices.add(loopStarts[0] + baseOffset + i + 1)
                    }
                }
            }
        }
    }

    /**
     * Generate ring cap for rectangular tube (HSS) by creating quads between outer and inner edges.
     *
     * For a 4-sided outer and 4-sided inner rectangle:
     * - Outer loop is CCW: [0,1,2,3]
     * - Inner loop is CW: [0,1,2,3]
     * - Generate 4 quads (8 triangles) connecting corresponding edges
     */
    private fun addRectangularRingCap(
        outerLoop: List<Pair<Float, Float>>,
        innerLoop: List<Pair<Float, Float>>,
        loopStarts: List<Int>,
        isTop: Boolean
    ) {
        val outerSize = outerLoop.size
        val baseOffset = if (isTop) outerSize else 0

        val outerStart = loopStarts[0] + baseOffset
        val innerStart = loopStarts[1] + baseOffset

        // For each of the 4 sides, create a quad connecting outer edge to inner edge
        // Outer loop (CCW): 0 -> 1 -> 2 -> 3 -> 0
        // Inner loop (CW):  0 -> 1 -> 2 -> 3 -> 0
        // For proper correspondence, we need to reverse inner indexing
        for (i in 0 until 4) {
            val outerCurr = outerStart + i
            val outerNext = outerStart + (i + 1) % 4

            // Inner loop is CW, so to match outer edge, we go in reverse
            val innerCurr = innerStart + (4 - i) % 4
            val innerNext = innerStart + (4 - i - 1 + 4) % 4

            // Create quad: outerCurr -> outerNext -> innerNext -> innerCurr
            // Split into two triangles with correct winding
            if (isTop) {
                // Top cap: normal points +Z (outward)
                // Triangle 1: outerCurr, outerNext, innerNext
                indices.add(outerCurr)
                indices.add(outerNext)
                indices.add(innerNext)

                // Triangle 2: outerCurr, innerNext, innerCurr
                indices.add(outerCurr)
                indices.add(innerNext)
                indices.add(innerCurr)
            } else {
                // Bottom cap: normal points -Z (outward)
                // Reverse winding: Triangle 1: outerCurr, innerNext, outerNext
                indices.add(outerCurr)
                indices.add(innerNext)
                indices.add(outerNext)

                // Triangle 2: outerCurr, innerCurr, innerNext
                indices.add(outerCurr)
                indices.add(innerCurr)
                indices.add(innerNext)
            }
        }
    }
}
