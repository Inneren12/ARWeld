package com.example.arweld.feature.drawingimport.preprocess

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PageDetectPreprocessorTest {
    private val preprocessor = PageDetectPreprocessor()

    @Test
    fun `computeTargetSize keeps original when below max side`() {
        val target = preprocessor.computeTargetSize(
            originalWidth = 640,
            originalHeight = 480,
            maxSide = 1024,
        )

        assertEquals(640, target.width)
        assertEquals(480, target.height)
        assertEquals(1.0, target.downscaleFactor, 0.0)
    }

    @Test
    fun `computeTargetSize scales to max side deterministically`() {
        val target = preprocessor.computeTargetSize(
            originalWidth = 4000,
            originalHeight = 3000,
            maxSide = 1024,
        )

        assertTrue(target.width <= 1024)
        assertTrue(target.height <= 1024)
        assertEquals(1024, maxOf(target.width, target.height))
        assertTrue(target.downscaleFactor > 1.0)
    }

    @Test
    fun `preprocess returns decode failed for missing file`() {
        val missing = File("does_not_exist.jpg")

        val outcome = preprocessor.preprocess(PageDetectInput(rawImageFile = missing))

        assertTrue(outcome is PageDetectOutcomeV1.Failure)
        val failure = (outcome as PageDetectOutcomeV1.Failure).failure
        assertEquals(PageDetectStageV1.PREPROCESS, failure.stage)
        assertEquals(PageDetectFailureCodeV1.DECODE_FAILED, failure.code)
    }
}
