package com.example.arweld.feature.drawingimport.pipeline

import android.graphics.Bitmap
import com.example.arweld.core.drawing2d.artifacts.io.v1.ArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.feature.drawingimport.preprocess.BboxV1
import com.example.arweld.feature.drawingimport.preprocess.ContourV1
import com.example.arweld.feature.drawingimport.preprocess.CornerPointV1
import com.example.arweld.feature.drawingimport.preprocess.EdgeMap
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureCodeV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFrame
import com.example.arweld.feature.drawingimport.preprocess.PageDetectOutcomeV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectStageV1
import com.example.arweld.feature.drawingimport.preprocess.PageQuadCandidate
import com.example.arweld.feature.drawingimport.preprocess.PointV1
import com.example.arweld.feature.drawingimport.preprocess.RectifiedSizeV1
import com.example.arweld.feature.drawingimport.preprocess.RefineResultV1
import com.example.arweld.feature.drawingimport.preprocess.RefineStatusV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DrawingImportPipelineV1Test {
    @Test
    fun `run short-circuits on failure stage`() {
        val session = testSession()
        val outcomes = defaultOutcomes(session)
        val failure = PageDetectFailureV1(
            stage = PageDetectStageV1.CONTOURS,
            code = PageDetectFailureCodeV1.CONTOURS_EMPTY,
            debugMessage = "No contours",
        )
        outcomes[PageDetectStageV1.CONTOURS] = PageDetectOutcomeV1.Failure(failure)
        val stages = FakeStages(outcomes)
        val pipeline = DrawingImportPipelineV1(stages = stages)

        val result = runBlocking { pipeline.run(session) }

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failureResult = result as PageDetectOutcomeV1.Failure
        assertEquals(PageDetectStageV1.CONTOURS, failureResult.failure.stage)
        assertEquals(PageDetectFailureCodeV1.CONTOURS_EMPTY, failureResult.failure.code)
        assertEquals(
            listOf(
                PageDetectStageV1.LOAD_UPRIGHT,
                PageDetectStageV1.PREPROCESS,
                PageDetectStageV1.EDGES,
                PageDetectStageV1.CONTOURS,
            ),
            stages.calls,
        )
    }

    @Test
    fun `run propagates failure code`() {
        val session = testSession()
        val outcomes = defaultOutcomes(session)
        val failure = PageDetectFailureV1(
            stage = PageDetectStageV1.RECTIFY_SIZE,
            code = PageDetectFailureCodeV1.ORDER_DEGENERATE,
            debugMessage = "Degenerate",
        )
        outcomes[PageDetectStageV1.RECTIFY_SIZE] = PageDetectOutcomeV1.Failure(failure)
        val stages = FakeStages(outcomes)
        val pipeline = DrawingImportPipelineV1(stages = stages)

        val result = runBlocking { pipeline.run(session) }

        assertTrue(result is PageDetectOutcomeV1.Failure)
        val failureResult = result as PageDetectOutcomeV1.Failure
        assertEquals(PageDetectStageV1.RECTIFY_SIZE, failureResult.failure.stage)
        assertEquals(PageDetectFailureCodeV1.ORDER_DEGENERATE, failureResult.failure.code)
    }

    @Test
    fun `run computes rectified blur variance`() {
        val session = testSession()
        val outcomes = defaultOutcomes(session)
        val stages = FakeStages(outcomes)
        val pipeline = DrawingImportPipelineV1(stages = stages)

        val result = runBlocking { pipeline.run(session) }

        assertTrue(result is PageDetectOutcomeV1.Success)
        val success = result as PageDetectOutcomeV1.Success
        assertTrue(success.value.rectifiedQualityMetrics?.blurVariance != null)
    }

    private fun testSession(): DrawingImportSession {
        val rootDir = createTempDir(prefix = "pipeline-artifacts")
        val projectDir = File(rootDir, "project-1")
        val rawFile = File(projectDir, "raw/raw.jpg")
        rawFile.parentFile?.mkdirs()
        rawFile.writeBytes(ByteArray(1))
        val rawEntry = ArtifactEntryV1(
            kind = ArtifactKindV1.RAW_IMAGE,
            relPath = "raw/raw.jpg",
            sha256 = "deadbeef",
            byteSize = rawFile.length(),
            mime = "image/jpeg",
        )
        return DrawingImportSession(
            projectId = "project-1",
            projectDir = projectDir,
            artifacts = listOf(rawEntry),
        )
    }

    private fun defaultOutcomes(session: DrawingImportSession): MutableMap<PageDetectStageV1, PageDetectOutcomeV1<*>> {
        val upright = UprightBitmapV1(Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888), 0)
        val frame = PageDetectFrame(
            width = 4,
            height = 4,
            gray = ByteArray(16),
            originalWidth = 4,
            originalHeight = 4,
            downscaleFactor = 1.0,
            rotationAppliedDeg = 0,
        )
        val edgeMap = EdgeMap(width = 4, height = 4, edges = ByteArray(16))
        val contour = ContourV1(
            points = listOf(PointV1(0, 0), PointV1(3, 0), PointV1(3, 3), PointV1(0, 3)),
            area = 9.0,
            perimeter = 12.0,
            bbox = BboxV1(0, 0, 4, 4),
        )
        val candidate = PageQuadCandidate(
            points = contour.points,
            contourArea = contour.area,
            score = 1.0,
        )
        val ordered = OrderedCornersV1(
            topLeft = CornerPointV1(0.0, 0.0),
            topRight = CornerPointV1(3.0, 0.0),
            bottomRight = CornerPointV1(3.0, 3.0),
            bottomLeft = CornerPointV1(0.0, 3.0),
        )
        val refine = RefineResultV1(
            corners = ordered,
            deltasPx = listOf(0.0, 0.0, 0.0, 0.0),
            status = RefineStatusV1.REFINED,
        )
        val rectifiedSize = RectifiedSizeV1(100, 100)
        val rectifiedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val updatedSession = session.copy(artifacts = session.artifacts)
        return mutableMapOf(
            PageDetectStageV1.LOAD_UPRIGHT to PageDetectOutcomeV1.Success(upright),
            PageDetectStageV1.PREPROCESS to PageDetectOutcomeV1.Success(frame),
            PageDetectStageV1.EDGES to PageDetectOutcomeV1.Success(edgeMap),
            PageDetectStageV1.CONTOURS to PageDetectOutcomeV1.Success(listOf(contour)),
            PageDetectStageV1.QUAD_SELECT to PageDetectOutcomeV1.Success(candidate),
            PageDetectStageV1.ORDER to PageDetectOutcomeV1.Success(ordered),
            PageDetectStageV1.REFINE to PageDetectOutcomeV1.Success(refine),
            PageDetectStageV1.RECTIFY_SIZE to PageDetectOutcomeV1.Success(rectifiedSize),
            PageDetectStageV1.RECTIFY to PageDetectOutcomeV1.Success(rectifiedBitmap),
            PageDetectStageV1.SAVE to PageDetectOutcomeV1.Success(updatedSession),
        )
    }

    private class FakeStages(
        private val outcomes: Map<PageDetectStageV1, PageDetectOutcomeV1<*>>,
    ) : DrawingImportPipelineStagesV1 {
        val calls = mutableListOf<PageDetectStageV1>()

        override fun loadUpright(rawFile: File): PageDetectOutcomeV1<UprightBitmapV1> {
            return next(PageDetectStageV1.LOAD_UPRIGHT)
        }

        override fun preprocess(
            upright: UprightBitmapV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<PageDetectFrame> {
            return next(PageDetectStageV1.PREPROCESS)
        }

        override fun detectEdges(frame: PageDetectFrame): PageDetectOutcomeV1<EdgeMap> {
            return next(PageDetectStageV1.EDGES)
        }

        override fun detectContours(edgeMap: EdgeMap): PageDetectOutcomeV1<List<ContourV1>> {
            return next(PageDetectStageV1.CONTOURS)
        }

        override fun selectQuad(
            contours: List<ContourV1>,
            frame: PageDetectFrame,
        ): PageDetectOutcomeV1<PageQuadCandidate> {
            return next(PageDetectStageV1.QUAD_SELECT)
        }

        override fun orderCorners(candidate: PageQuadCandidate): PageDetectOutcomeV1<OrderedCornersV1> {
            return next(PageDetectStageV1.ORDER)
        }

        override fun refineCorners(
            frame: PageDetectFrame,
            ordered: OrderedCornersV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<RefineResultV1> {
            return next(PageDetectStageV1.REFINE)
        }

        override fun computeRectifiedSize(
            corners: OrderedCornersV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<RectifiedSizeV1> {
            return next(PageDetectStageV1.RECTIFY_SIZE)
        }

        override fun rectifyBitmap(
            upright: Bitmap,
            corners: OrderedCornersV1,
            size: RectifiedSizeV1,
        ): PageDetectOutcomeV1<Bitmap> {
            return next(PageDetectStageV1.RECTIFY)
        }

        override fun saveArtifacts(
            session: DrawingImportSession,
            projectStore: ArtifactStoreV1,
            rectified: Bitmap,
            size: RectifiedSizeV1,
        ): PageDetectOutcomeV1<DrawingImportSession> {
            return next(PageDetectStageV1.SAVE)
        }

        @Suppress("UNCHECKED_CAST")
        private fun <T> next(stage: PageDetectStageV1): PageDetectOutcomeV1<T> {
            calls.add(stage)
            return outcomes.getValue(stage) as PageDetectOutcomeV1<T>
        }
    }
}
