package com.example.arweld.feature.drawingimport.preprocess

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageDetectEdgesContoursInstrumentedTest {
    @Test
    fun preprocessesEdgesAndContoursFromAsset() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetInput = context.assets.open("test_drawing.png")
        val tempFile = File(context.cacheDir, "test_drawing.png")
        FileOutputStream(tempFile).use { output ->
            assetInput.use { input ->
                input.copyTo(output)
            }
        }

        val preprocessor = PageDetectPreprocessor()
        val preprocessOutcome = preprocessor.preprocess(
            PageDetectInput(
                rawImageFile = tempFile,
                maxSide = 256,
            ),
        )
        assertTrue(preprocessOutcome is PageDetectOutcomeV1.Success)
        val frame = (preprocessOutcome as PageDetectOutcomeV1.Success).value

        val edgeDetector = PageDetectEdgeDetector()
        val edgeOutcome = edgeDetector.detect(frame)
        assertTrue(edgeOutcome is PageDetectOutcomeV1.Success)
        val edgeMap = (edgeOutcome as PageDetectOutcomeV1.Success).value
        val edgeCount = edgeMap.edges.count { it.toInt() != 0 }
        assertTrue(edgeCount > 0)

        val contourExtractor = PageDetectContourExtractor()
        val contourOutcome = contourExtractor.extract(edgeMap)
        assertTrue(contourOutcome is PageDetectOutcomeV1.Success)
        val contours = (contourOutcome as PageDetectOutcomeV1.Success).value
        assertTrue(contours.isNotEmpty())

        val maxArea = contours.maxOf { it.area }
        val frameArea = frame.width.toDouble() * frame.height.toDouble()
        assertTrue(maxArea >= frameArea * 0.05)
    }
}
