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
            for (idx in triIndices) {
                val vertexIdx = loopStarts[0] + baseOffset + idx
                indices.add(vertexIdx)
            }
        } else {
            // Complex case with holes: use simplified approach
            // For v0.1, we'll use a simple fan from first vertex (works for convex regions)
            // More robust: Constrained Delaunay or polygon with holes library
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

            // Note: This simplified approach doesn't properly handle holes
            // For production, use a proper polygon triangulation library
        }
    }
}
