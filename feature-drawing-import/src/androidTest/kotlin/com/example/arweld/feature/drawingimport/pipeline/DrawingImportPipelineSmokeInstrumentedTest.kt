package com.example.arweld.feature.drawingimport.pipeline

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.arweld.core.drawing2d.Drawing2DJson
import com.example.arweld.core.drawing2d.artifacts.io.v1.FinalizeOutcomeV1
import com.example.arweld.core.drawing2d.artifacts.layout.v1.ProjectLayoutV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.core.drawing2d.artifacts.v1.ManifestV1
import com.example.arweld.feature.drawingimport.artifacts.DrawingImportArtifacts
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawingImportPipelineSmokeInstrumentedTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun assetBasedPipelineRun_producesArtifactsAndManifest() = runBlocking {
        val artifactsRoot = File(context.cacheDir, "drawing-import-smoke/${UUID.randomUUID()}")
        try {
            val runner = TestProjectRunnerV1(context, artifactsRoot)
            val result = runner.run("page_sample.png")
            val projectDir = result.projectDir

            val rawFile = File(projectDir, DrawingImportArtifacts.rawImageRelPath())
            val rectifiedFile = File(projectDir, ProjectLayoutV1.RECTIFIED_IMAGE_PNG)
            val metaFile = File(projectDir, ProjectLayoutV1.CAPTURE_META_JSON)
            val manifestFile = File(projectDir, ProjectLayoutV1.MANIFEST_JSON)
            val checksumsFile = File(projectDir, ProjectLayoutV1.CHECKSUMS_SHA256)

            assertTrue(rawFile.exists())
            assertTrue(rawFile.length() > 0)
            assertTrue(rectifiedFile.exists())
            assertTrue(rectifiedFile.length() > 0)
            assertTrue(metaFile.exists())
            assertTrue(metaFile.length() > 0)
            assertTrue(manifestFile.exists())
            assertTrue(manifestFile.length() > 0)
            assertTrue(checksumsFile.exists())
            assertTrue(checksumsFile.length() > 0)

            val captureMeta = Drawing2DJson.decodeFromString<CaptureMetaV1>(metaFile.readText())
            assertEquals(1, captureMeta.schemaVersion)
            assertEquals(9, captureMeta.homographyH.size)
            assertTrue(captureMeta.quality.decision.isNotBlank())

            val manifest = Drawing2DJson.decodeFromString<ManifestV1>(manifestFile.readText())
            val relPaths = manifest.artifacts.map { it.relPath }.toSet()
            val expectedRelPaths = setOf(
                DrawingImportArtifacts.rawImageRelPath(),
                ProjectLayoutV1.RECTIFIED_IMAGE_PNG,
                ProjectLayoutV1.CAPTURE_META_JSON,
                ProjectLayoutV1.PROJECT_COMPLETE_JSON,
            )
            assertTrue(relPaths.containsAll(expectedRelPaths))

            val finalization = result.finalization
            assertNotNull(finalization)
            when (finalization) {
                is FinalizeOutcomeV1.Success -> Unit
                is FinalizeOutcomeV1.Failure -> {
                    fail("Finalizer verification failed: ${finalization.failure}")
                }
                null -> fail("Finalization output missing")
            }
        } finally {
            artifactsRoot.deleteRecursively()
        }
    }
}
