package com.example.arweld.feature.arview.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TriangleEstimatorTest {

    private val maxTriangles = 100

    @Test
    fun estimateTriangles_trianglesModeUsesIndices() {
        val triangles = estimateTrianglesForMode(
            mode = 4,
            indexCount = 9,
            vertexCount = null,
            maxTriangles = maxTriangles,
        )

        assertEquals(3, triangles)
    }

    @Test
    fun estimateTriangles_stripModeUsesIndices() {
        val triangles = estimateTrianglesForMode(
            mode = 5,
            indexCount = 5,
            vertexCount = null,
            maxTriangles = maxTriangles,
        )

        assertEquals(3, triangles)
    }

    @Test
    fun estimateTriangles_fanModeUsesIndices() {
        val triangles = estimateTrianglesForMode(
            mode = 6,
            indexCount = 4,
            vertexCount = null,
            maxTriangles = maxTriangles,
        )

        assertEquals(2, triangles)
    }

    @Test
    fun estimateTriangles_unknownModeFailsClosed() {
        val triangles = estimateTrianglesForMode(
            mode = 1,
            indexCount = 10,
            vertexCount = null,
            maxTriangles = maxTriangles,
        )

        assertTrue(triangles > maxTriangles)
    }
}
