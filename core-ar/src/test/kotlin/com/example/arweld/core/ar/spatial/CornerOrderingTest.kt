package com.example.arweld.core.ar.spatial

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for corner ordering algorithm in core-ar spatial module.
 */
class CornerOrderingTest {

    @Test
    fun `orderCornersClockwiseFromTopLeft produces TL-TR-BR-BL order`() {
        // Given: unordered corners representing a quadrilateral
        val unordered = listOf(
            Point2f(50f, 80f),  // bottom-right-ish
            Point2f(10f, 10f),  // top-left
            Point2f(90f, 20f),  // top-right
            Point2f(20f, 70f),  // bottom-left-ish
        )

        // When: ordering clockwise from top-left
        val ordered = orderCornersClockwiseFromTopLeft(unordered)

        // Then: order should be TL -> TR -> BR -> BL (clockwise in image coordinates)
        assertThat(ordered).hasSize(4)
        assertThat(ordered[0]).isEqualTo(Point2f(10f, 10f))   // top-left (smallest y, then smallest x)
        assertThat(ordered[1]).isEqualTo(Point2f(90f, 20f))   // top-right
        assertThat(ordered[2]).isEqualTo(Point2f(50f, 80f))   // bottom-right
        assertThat(ordered[3]).isEqualTo(Point2f(20f, 70f))   // bottom-left
    }

    @Test
    fun `orderCornersClockwiseFromTopLeft is idempotent for already-ordered corners`() {
        // Given: corners already in correct clockwise order from top-left
        val alreadyOrdered = listOf(
            Point2f(0f, 0f),    // TL
            Point2f(100f, 0f),  // TR
            Point2f(100f, 100f),// BR
            Point2f(0f, 100f),  // BL
        )

        // When: ordering again
        val result = orderCornersClockwiseFromTopLeft(alreadyOrdered)

        // Then: order should remain the same (idempotent)
        assertThat(result).isEqualTo(alreadyOrdered)
    }

    @Test
    fun `orderCornersClockwiseFromTopLeft returns input for less than 4 corners`() {
        // Given: fewer than 4 corners
        val threeCorners = listOf(
            Point2f(10f, 10f),
            Point2f(90f, 20f),
            Point2f(50f, 80f),
        )

        // When: attempting to order
        val result = orderCornersClockwiseFromTopLeft(threeCorners)

        // Then: returns input unchanged (algorithm requires 4 corners)
        assertThat(result).isEqualTo(threeCorners)
    }

    @Test
    fun `Point2f ZERO constant is at origin`() {
        // Validates Point2f.ZERO constant
        assertThat(Point2f.ZERO.x).isEqualTo(0f)
        assertThat(Point2f.ZERO.y).isEqualTo(0f)
    }
}
