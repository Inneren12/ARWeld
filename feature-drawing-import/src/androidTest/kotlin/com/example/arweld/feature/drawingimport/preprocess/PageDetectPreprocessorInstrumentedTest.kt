package com.example.arweld.feature.drawingimport.preprocess

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.FileOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageDetectPreprocessorInstrumentedTest {
    @Test
    fun preprocessesAssetToDeterministicFrame() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val assetInput = context.assets.open("test_drawing.png")
        val tempFile = File(context.cacheDir, "test_drawing.png")
        FileOutputStream(tempFile).use { output ->
            assetInput.use { input ->
                input.copyTo(output)
            }
        }

        val preprocessor = PageDetectPreprocessor()
        val outcome = preprocessor.preprocess(
            PageDetectInput(
                rawImageFile = tempFile,
                maxSide = 64,
            ),
        )
        assertTrue(outcome is PageDetectOutcomeV1.Success)
        val frame = (outcome as PageDetectOutcomeV1.Success).value

        assertTrue(frame.width <= 64)
        assertTrue(frame.height <= 64)
        assertEquals(frame.width * frame.height, frame.gray.size)
        assertTrue(frame.rotationAppliedDeg in listOf(0, 90, 180, 270))
    }
}
