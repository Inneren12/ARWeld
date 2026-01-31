package com.example.arweld.feature.drawingimport.preprocess

import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class PageDetectContourExtractor(
    private val minPoints: Int = 20,
) {
    /**
     * Extracts connected edge components (8-connected) and summarizes each component
     * using a convex hull contour with area/perimeter and bounding box stats.
     */
    fun extract(edgeMap: EdgeMap): PageDetectOutcomeV1<List<ContourV1>> {
        val width = edgeMap.width
        val height = edgeMap.height
        val size = width * height
        if (edgeMap.edges.size != size) {
            return PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.CONTOURS,
                    code = PageDetectFailureCodeV1.CONTOURS_EMPTY,
                    debugMessage = "Edge map size mismatch.",
                ),
            )
        }
        return try {
            val visited = BooleanArray(size)
            val contours = mutableListOf<ContourV1>()
            val queue = IntArray(size)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    if (edgeMap.edges[idx].toInt() == 0 || visited[idx]) continue
                    val points = mutableListOf<PointV1>()
                    var minX = x
                    var maxX = x
                    var minY = y
                    var maxY = y
                    var queueStart = 0
                    var queueEnd = 0
                    queue[queueEnd++] = idx
                    visited[idx] = true
                    while (queueStart < queueEnd) {
                        val current = queue[queueStart++]
                        val cx = current % width
                        val cy = current / width
                        points.add(PointV1(cx, cy))
                        minX = min(minX, cx)
                        maxX = max(maxX, cx)
                        minY = min(minY, cy)
                        maxY = max(maxY, cy)
                        for (ny in cy - 1..cy + 1) {
                            if (ny < 0 || ny >= height) continue
                            val rowBase = ny * width
                            for (nx in cx - 1..cx + 1) {
                                if (nx < 0 || nx >= width) continue
                                val nIdx = rowBase + nx
                                if (visited[nIdx] || edgeMap.edges[nIdx].toInt() == 0) continue
                                visited[nIdx] = true
                                queue[queueEnd++] = nIdx
                            }
                        }
                    }
                    if (points.size < minPoints) continue
                    val bbox = BboxV1(
                        x = minX,
                        y = minY,
                        width = maxX - minX + 1,
                        height = maxY - minY + 1,
                    )
                    val hull = convexHull(points)
                    val area = polygonArea(hull)
                    val perimeter = polygonPerimeter(hull)
                    contours.add(
                        ContourV1(
                            points = if (hull.isNotEmpty()) hull else points,
                            area = area,
                            perimeter = perimeter,
                            bbox = bbox,
                        )
                    )
                }
            }
            if (contours.isEmpty()) {
                PageDetectOutcomeV1.Failure(
                    PageDetectFailureV1(
                        stage = PageDetectStageV1.CONTOURS,
                        code = PageDetectFailureCodeV1.CONTOURS_EMPTY,
                        debugMessage = "No contours above minPoints=$minPoints.",
                    ),
                )
            } else {
                PageDetectOutcomeV1.Success(contours)
            }
        } catch (error: Throwable) {
            PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.CONTOURS,
                    code = PageDetectFailureCodeV1.CONTOURS_EMPTY,
                    debugMessage = error.message,
                ),
            )
        }
    }

    private fun convexHull(points: List<PointV1>): List<PointV1> {
        if (points.size <= 1) return points
        val sorted = points.distinct().sortedWith(compareBy<PointV1> { it.x }.thenBy { it.y })
        if (sorted.size <= 2) return sorted
        val lower = mutableListOf<PointV1>()
        for (point in sorted) {
            while (lower.size >= 2 && cross(lower[lower.size - 2], lower[lower.size - 1], point) <= 0) {
                lower.removeAt(lower.size - 1)
            }
            lower.add(point)
        }
        val upper = mutableListOf<PointV1>()
        for (point in sorted.asReversed()) {
            while (upper.size >= 2 && cross(upper[upper.size - 2], upper[upper.size - 1], point) <= 0) {
                upper.removeAt(upper.size - 1)
            }
            upper.add(point)
        }
        lower.removeAt(lower.size - 1)
        upper.removeAt(upper.size - 1)
        return lower + upper
    }

    private fun cross(a: PointV1, b: PointV1, c: PointV1): Int {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
    }

    private fun polygonArea(points: List<PointV1>): Double {
        if (points.size < 3) return 0.0
        var sum = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            sum += points[i].x * points[j].y - points[j].x * points[i].y
        }
        return abs(sum) / 2.0
    }

    private fun polygonPerimeter(points: List<PointV1>): Double {
        if (points.size < 2) return 0.0
        if (points.size == 2) {
            val dx = (points[0].x - points[1].x).toDouble()
            val dy = (points[0].y - points[1].y).toDouble()
            return 2.0 * hypot(dx, dy)
        }
        var total = 0.0
        for (i in points.indices) {
            val j = (i + 1) % points.size
            val dx = (points[i].x - points[j].x).toDouble()
            val dy = (points[i].y - points[j].y).toDouble()
            total += hypot(dx, dy)
        }
        return total
    }
}
