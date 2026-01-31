package com.example.arweld.feature.drawingimport.preprocess

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageDetectCornerRefineInstrumentedTest {
    @Test
    fun refinesSyntheticQuadWithinTolerance() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val bitmap = Bitmap.createBitmap(200, 150, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)
        val paint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val quad = listOf(
            Pair(30f, 20f),
            Pair(170f, 30f),
            Pair(160f, 120f),
            Pair(40f, 110f),
        )
        val path = Path().apply {
            moveTo(quad[0].first, quad[0].second)
            lineTo(quad[1].first, quad[1].second)
            lineTo(quad[2].first, quad[2].second)
            lineTo(quad[3].first, quad[3].second)
            close()
        }
        canvas.drawPath(path, paint)
        val file = File(context.cacheDir, "synthetic_quad.png")
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        bitmap.recycle()

        val preprocessOutcome = PageDetectPreprocessor().preprocess(
            PageDetectInput(
                rawImageFile = file,
                maxSide = 200,
            ),
        )
        assertTrue(preprocessOutcome is PageDetectOutcomeV1.Success)
        val frame = (preprocessOutcome as PageDetectOutcomeV1.Success).value

        val edgesOutcome = PageDetectEdgeDetector().detect(frame)
        assertTrue(edgesOutcome is PageDetectOutcomeV1.Success)
        val edges = (edgesOutcome as PageDetectOutcomeV1.Success).value

        val contoursOutcome = PageDetectContourExtractor().extract(edges)
        assertTrue(contoursOutcome is PageDetectOutcomeV1.Success)
        val contours = (contoursOutcome as PageDetectOutcomeV1.Success).value

        val quadResult = PageQuadSelector(
            PageQuadSelectionConfig(
                minAreaFraction = 0.1,
                approxEpsilonRatio = 0.02,
                maxAspectRatio = 4.0,
            ),
        ).select(contours, frame.width, frame.height)
        assertTrue(quadResult is PageDetectOutcomeV1.Success)
        val candidate = (quadResult as PageDetectOutcomeV1.Success).value
        assertEquals(4, candidate.points.size)

        val orderedOutcome = CornerOrderingV1.order(candidate.points)
        assertTrue(orderedOutcome is PageDetectOutcomeV1.Success)
        val ordered = (orderedOutcome as PageDetectOutcomeV1.Success).value

        val refineOutcome = CornerRefinerV1.refine(
            frame,
            ordered,
            RefineParamsV1(windowRadiusPx = 6, maxIters = 6, epsilon = 0.25),
        )
        assertTrue(refineOutcome is PageDetectOutcomeV1.Success)
        val refine = (refineOutcome as PageDetectOutcomeV1.Success).value
        assertTrue(refine.deltasPx.all { it.isFinite() })
        assertTrue(refine.status == RefineStatusV1.REFINED)

        val expected = ordered.toList()
        val refined = refine.corners.toList()
        for (i in expected.indices) {
            val dx = refined[i].x - expected[i].x
            val dy = refined[i].y - expected[i].y
            val distance = kotlin.math.hypot(dx, dy)
            assertTrue(distance <= 5.0)
        }
    }
}
