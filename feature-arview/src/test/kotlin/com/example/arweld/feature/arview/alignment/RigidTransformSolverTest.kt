package com.example.arweld.feature.arview.alignment

import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.angularDistance
import kotlin.math.sqrt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RigidTransformSolverTest {

    private val solver = RigidTransformSolver()

    @Test
    fun solveRigidTransform_recoversKnownTransform() {
        val modelPoints = listOf(
            Vector3(0.0, 0.0, 0.0),
            Vector3(1.0, 0.0, 0.0),
            Vector3(0.0, 1.0, 0.0),
            Vector3(1.0, 1.0, 0.0),
        )

        val rotation = Quaternion(0.0, 0.0, sqrt(0.5), sqrt(0.5))
        val translation = Vector3(1.5, -0.25, 2.0)

        val worldPoints = modelPoints.map { rotation.rotate(it) + translation }

        val solved = solver.solveRigidTransform(modelPoints, worldPoints)

        assertNotNull(solved)
        solved!!
        assertVectorAlmostEquals(translation, solved.position)
        val angleError = rotation.angularDistance(solved.rotation)
        assertEquals(0.0, angleError, 1e-6)
    }

    @Test
    fun solveRigidTransform_requiresThreePoints() {
        val modelPoints = listOf(Vector3(0.0, 0.0, 0.0), Vector3(1.0, 0.0, 0.0))
        val worldPoints = modelPoints
        val solved = solver.solveRigidTransform(modelPoints, worldPoints)
        assertNull(solved)
    }

    private fun assertVectorAlmostEquals(expected: Vector3, actual: Vector3, tolerance: Double = 1e-6) {
        assertEquals(expected.x, actual.x, tolerance)
        assertEquals(expected.y, actual.y, tolerance)
        assertEquals(expected.z, actual.z, tolerance)
    }
}
