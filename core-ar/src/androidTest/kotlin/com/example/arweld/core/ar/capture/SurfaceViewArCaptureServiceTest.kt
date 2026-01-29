package com.example.arweld.core.ar.capture

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.arweld.core.ar.api.ArCaptureRequest
import com.example.arweld.core.ar.api.createSurfaceViewCaptureService
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class SurfaceViewArCaptureServiceTest {
    @Test
    fun capturesSurfaceViewScreenshotToFile() {
        ActivityScenario.launch(SurfaceViewCaptureTestActivity::class.java).use { scenario ->
            lateinit var activity: SurfaceViewCaptureTestActivity
            scenario.onActivity { activity = it }

            assertThat(activity.surfaceReady.await(3, TimeUnit.SECONDS)).isTrue()

            scenario.onActivity { it.drawSolidColor() }

            val service = createSurfaceViewCaptureService(activity.surfaceView)
            val result = runBlocking {
                service.captureScreenshot(ArCaptureRequest(workItemId = "work-item-1"))
            }

            assertThat(result.fileUri.toString()).isNotEmpty()
            assertThat(result.width).isGreaterThan(0)
            assertThat(result.height).isGreaterThan(0)

            val outputFile = File(requireNotNull(result.fileUri.path))
            assertThat(outputFile.exists()).isTrue()
            outputFile.delete()
        }
    }
}
