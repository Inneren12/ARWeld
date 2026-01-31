package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CornerOrderingV1Test {
    @Test
    fun `ordering is stable across all permutations`() {
        val points = listOf(
            PointV1(10, 10),
            PointV1(110, 10),
            PointV1(110, 60),
            PointV1(10, 60),
        )
        val expected = OrderedCornersV1(
            tl = PointV1(10, 10),
            tr = PointV1(110, 10),
            br = PointV1(110, 60),
            bl = PointV1(10, 60),
        )

        permutations(points).forEach { permuted ->
            val result = CornerOrderingV1.order(permuted)
            assertTrue(result is OrderResult.Success)
            val ordered = (result as OrderResult.Success).ordered
            assertEquals(expected, ordered)
        }
    }

    @Test
    fun `ordering handles rotated rectangle`() {
        val rotated = listOf(
            PointV1(1, 3),
            PointV1(3, -1),
            PointV1(1, -1),
            PointV1(3, 3),
        )
        val expected = OrderedCornersV1(
            tl = PointV1(1, -1),
            tr = PointV1(3, -1),
            br = PointV1(3, 3),
            bl = PointV1(1, 3),
        )

        val result = CornerOrderingV1.order(rotated)

        assertTrue(result is OrderResult.Success)
        assertEquals(expected, (result as OrderResult.Success).ordered)
    }

    @Test
    fun `ordering rejects degenerate and duplicate inputs`() {
        val notFour = CornerOrderingV1.order(
            listOf(PointV1(0, 0), PointV1(1, 0), PointV1(1, 1)),
        )
        assertTrue(notFour is OrderResult.Failure)
        assertEquals(OrderFailureV1.NOT_FOUR_POINTS, (notFour as OrderResult.Failure).code)

        val duplicate = CornerOrderingV1.order(
            listOf(
                PointV1(0, 0),
                PointV1(1, 0),
                PointV1(1, 1),
                PointV1(1, 1),
            ),
        )
        assertTrue(duplicate is OrderResult.Failure)
        assertEquals(OrderFailureV1.DUPLICATE_POINTS, (duplicate as OrderResult.Failure).code)

        val collinear = CornerOrderingV1.order(
            listOf(
                PointV1(0, 0),
                PointV1(1, 0),
                PointV1(2, 0),
                PointV1(3, 0),
            ),
        )
        assertTrue(collinear is OrderResult.Failure)
        assertEquals(OrderFailureV1.DEGENERATE_QUAD, (collinear as OrderResult.Failure).code)
    }

    @Test
    fun `ordering is stable for randomized rectangles`() {
        val random = Random(42)
        repeat(10) {
            val x = random.nextInt(0, 100)
            val y = random.nextInt(0, 100)
            val width = random.nextInt(10, 80)
            val height = random.nextInt(10, 80)
            val rect = listOf(
                PointV1(x, y),
                PointV1(x + width, y),
                PointV1(x + width, y + height),
                PointV1(x, y + height),
            )
            val expected = OrderedCornersV1(
                tl = PointV1(x, y),
                tr = PointV1(x + width, y),
                br = PointV1(x + width, y + height),
                bl = PointV1(x, y + height),
            )

            permutations(rect).shuffled(random).take(6).forEach { permuted ->
                val result = CornerOrderingV1.order(permuted)
                assertTrue(result is OrderResult.Success)
                val ordered = (result as OrderResult.Success).ordered
                assertEquals(expected, ordered)
            }
        }
    }

    private fun <T> permutations(items: List<T>): List<List<T>> {
        if (items.isEmpty()) return listOf(emptyList())
        return items.indices.flatMap { index ->
            val item = items[index]
            val rest = items.toMutableList().also { it.removeAt(index) }
            permutations(rest).map { listOf(item) + it }
        }
    }
}
