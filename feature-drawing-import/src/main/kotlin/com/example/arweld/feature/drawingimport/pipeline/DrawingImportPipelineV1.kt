package com.example.arweld.feature.drawingimport.pipeline

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import androidx.exifinterface.media.ExifInterface
import com.example.arweld.core.drawing2d.artifacts.io.v1.FileArtifactStoreV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactEntryV1
import com.example.arweld.core.drawing2d.artifacts.v1.ArtifactKindV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureCornersV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetaV1
import com.example.arweld.core.drawing2d.artifacts.v1.CaptureMetricsV1
import com.example.arweld.core.drawing2d.artifacts.v1.CornerQuadV1
import com.example.arweld.core.drawing2d.artifacts.v1.RectifiedCaptureV1
import com.example.arweld.core.drawing2d.v1.PointV1
import com.example.arweld.feature.drawingimport.artifacts.CaptureMetaWriterV1
import com.example.arweld.feature.drawingimport.artifacts.RectifiedArtifactWriterV1
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEvent
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportEventLogger
import com.example.arweld.feature.drawingimport.diagnostics.DrawingImportBlurFailureCode
import com.example.arweld.feature.drawingimport.preprocess.ContourV1
import com.example.arweld.feature.drawingimport.preprocess.CornerOrderingV1
import com.example.arweld.feature.drawingimport.preprocess.CornerPointV1
import com.example.arweld.feature.drawingimport.preprocess.CornerRefinerV1
import com.example.arweld.feature.drawingimport.preprocess.EdgeMap
import com.example.arweld.feature.drawingimport.preprocess.OrderedCornersV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectContourExtractor
import com.example.arweld.feature.drawingimport.preprocess.PageDetectEdgeDetector
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureCodeV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFailureV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectFrame
import com.example.arweld.feature.drawingimport.preprocess.PageDetectOutcomeV1
import com.example.arweld.feature.drawingimport.preprocess.PageDetectStageV1
import com.example.arweld.feature.drawingimport.preprocess.PageQuadCandidate
import com.example.arweld.feature.drawingimport.preprocess.PageQuadSelector
import com.example.arweld.feature.drawingimport.preprocess.RectifiedSizeV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizeParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RectifySizePolicyV1
import com.example.arweld.feature.drawingimport.preprocess.RefineParamsV1
import com.example.arweld.feature.drawingimport.preprocess.RefineResultV1
import com.example.arweld.feature.drawingimport.quality.QualityMetricsV1
import com.example.arweld.feature.drawingimport.quality.RectifiedQualityMetricsV1
import com.example.arweld.feature.drawingimport.ui.DrawingImportSession
import com.example.arweld.feature.drawingimport.ui.RectifiedImageInfo
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ensureActive

class DrawingImportPipelineV1(
    private val params: DrawingImportPipelineParamsV1 = DrawingImportPipelineParamsV1(),
    private val stages: DrawingImportPipelineStagesV1 = DrawingImportPipelineStagesV1.Default(params),
    private val eventLogger: DrawingImportEventLogger? = null,
    private val stageListener: (PageDetectStageV1) -> Unit = {},
    private val cancellationContext: CoroutineContext? = null,
) {
    fun run(session: DrawingImportSession): PageDetectOutcomeV1<PipelineResultV1> {
        logEvent(DrawingImportEvent.PIPELINE_START, session.projectId)
        val rawFile = session.rawImageFileOrNull()
            ?: return failure(PageDetectStageV1.LOAD_UPRIGHT, PageDetectFailureCodeV1.DECODE_FAILED, "Missing raw image.")

        val uprightOutcome = runStage(PageDetectStageV1.LOAD_UPRIGHT, session.projectId) {
            stages.loadUpright(rawFile)
        }
        val upright = when (uprightOutcome) {
            is PageDetectOutcomeV1.Success -> uprightOutcome.value
            is PageDetectOutcomeV1.Failure -> return uprightOutcome
        }

        val frameOutcome = runStage(PageDetectStageV1.PREPROCESS, session.projectId) {
            stages.preprocess(upright, params)
        }
        val frame = when (frameOutcome) {
            is PageDetectOutcomeV1.Success -> frameOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return frameOutcome
            }
        }

        val edgeOutcome = runStage(PageDetectStageV1.EDGES, session.projectId) {
            stages.detectEdges(frame)
        }
        val edges = when (edgeOutcome) {
            is PageDetectOutcomeV1.Success -> edgeOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return edgeOutcome
            }
        }

        val contourOutcome = runStage(PageDetectStageV1.CONTOURS, session.projectId) {
            stages.detectContours(edges)
        }
        val contours = when (contourOutcome) {
            is PageDetectOutcomeV1.Success -> contourOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return contourOutcome
            }
        }

        val quadOutcome = runStage(PageDetectStageV1.QUAD_SELECT, session.projectId) {
            stages.selectQuad(contours, frame)
        }
        val quad = when (quadOutcome) {
            is PageDetectOutcomeV1.Success -> quadOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return quadOutcome
            }
        }

        val orderedOutcome = runStage(PageDetectStageV1.ORDER, session.projectId) {
            stages.orderCorners(quad)
        }
        val orderedFrame = when (orderedOutcome) {
            is PageDetectOutcomeV1.Success -> orderedOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return orderedOutcome
            }
        }

        val refinedOutcome = runStage(PageDetectStageV1.REFINE, session.projectId) {
            stages.refineCorners(frame, orderedFrame, params)
        }
        val refinedFrame = when (refinedOutcome) {
            is PageDetectOutcomeV1.Success -> refinedOutcome.value.corners
            is PageDetectOutcomeV1.Failure -> {
                // Best-effort refinement: fall back to ordered corners on failure.
                null
            }
        }

        val scaleX = upright.bitmap.width.toDouble() / frame.width.toDouble()
        val scaleY = upright.bitmap.height.toDouble() / frame.height.toDouble()
        val orderedFull = orderedFrame.scale(scaleX, scaleY)
        val refinedFull = refinedFrame?.scale(scaleX, scaleY)
        val sizeOutcome = runStage(PageDetectStageV1.RECTIFY_SIZE, session.projectId) {
            stages.computeRectifiedSize(refinedFull ?: orderedFull, params)
        }
        val rectifiedSize = when (sizeOutcome) {
            is PageDetectOutcomeV1.Success -> sizeOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return sizeOutcome
            }
        }

        val rectifiedOutcome = runStage(PageDetectStageV1.RECTIFY, session.projectId) {
            stages.rectifyBitmap(upright.bitmap, refinedFull ?: orderedFull, rectifiedSize)
        }
        val rectifiedBitmap = when (rectifiedOutcome) {
            is PageDetectOutcomeV1.Success -> rectifiedOutcome.value
            is PageDetectOutcomeV1.Failure -> {
                upright.recycle()
                return rectifiedOutcome
            }
        }

        cancellationContext?.ensureActive()
        val rectifiedMetrics = computeRectifiedMetrics(rectifiedBitmap, session.projectId)
        cancellationContext?.ensureActive()

        val saveOutcome = runStage(PageDetectStageV1.SAVE, session.projectId) {
            stages.saveArtifacts(session, rectifiedBitmap, rectifiedSize)
        }
        rectifiedBitmap.recycle()
        upright.recycle()
        val updatedSession = when (saveOutcome) {
            is PageDetectOutcomeV1.Success -> saveOutcome.value
            is PageDetectOutcomeV1.Failure -> return saveOutcome
        }
        val captureMetaOutcome = writeCaptureMeta(
            session = updatedSession,
            orderedCorners = orderedFull,
            refinedCorners = refinedFull,
            rectifiedSize = rectifiedSize,
            rectifiedMetrics = rectifiedMetrics,
        )
        val sessionWithMeta = when (captureMetaOutcome) {
            is PageDetectOutcomeV1.Success -> captureMetaOutcome.value
            is PageDetectOutcomeV1.Failure -> return captureMetaOutcome
        }

        logEvent(DrawingImportEvent.PIPELINE_OK, session.projectId)
        return PageDetectOutcomeV1.Success(
            PipelineResultV1(
                orderedCorners = orderedFull,
                refinedCorners = refinedFull,
                rectifiedSize = rectifiedSize,
                artifacts = sessionWithMeta.artifacts,
                rectifiedQualityMetrics = rectifiedMetrics,
            ),
        )
    }

    private fun <T> runStage(
        stage: PageDetectStageV1,
        projectId: String,
        block: () -> PageDetectOutcomeV1<T>,
    ): PageDetectOutcomeV1<T> {
        cancellationContext?.ensureActive()
        stageListener(stage)
        logEvent(DrawingImportEvent.STAGE_START, projectId, mapOf("stage" to stage.name))
        val outcome = try {
            block()
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = stage,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
        when (outcome) {
            is PageDetectOutcomeV1.Success -> {
                logEvent(DrawingImportEvent.STAGE_OK, projectId, mapOf("stage" to stage.name))
            }
            is PageDetectOutcomeV1.Failure -> {
                logEvent(
                    DrawingImportEvent.STAGE_FAIL,
                    projectId,
                    mapOf(
                        "stage" to stage.name,
                        "code" to outcome.failure.code.name,
                    ),
                )
            }
        }
        return outcome
    }

    private fun failure(
        stage: PageDetectStageV1,
        code: PageDetectFailureCodeV1,
        message: String?,
    ): PageDetectOutcomeV1<PipelineResultV1> {
        logEvent(
            DrawingImportEvent.STAGE_FAIL,
            null,
            mapOf("stage" to stage.name, "code" to code.name),
        )
        return PageDetectOutcomeV1.Failure(PageDetectFailureV1(stage, code, message))
    }

    private fun logEvent(
        event: DrawingImportEvent,
        projectId: String?,
        extras: Map<String, String> = emptyMap(),
    ) {
        eventLogger?.logEvent(
            event = event,
            state = "pipeline",
            projectId = projectId,
            extras = extras,
        )
    }

    private fun computeRectifiedMetrics(
        rectifiedBitmap: Bitmap,
        projectId: String,
    ): RectifiedQualityMetricsV1 {
        logEvent(DrawingImportEvent.BLUR_METRIC_START, projectId)
        val blurVariance = try {
            QualityMetricsV1.blurVarianceLaplacian(rectifiedBitmap)
        } catch (error: Throwable) {
            if (error is CancellationException) throw error
            val failureCode = when (error) {
                is OutOfMemoryError -> DrawingImportBlurFailureCode.OUT_OF_MEMORY
                else -> DrawingImportBlurFailureCode.UNKNOWN
            }
            logEvent(
                DrawingImportEvent.BLUR_METRIC_FAIL,
                projectId,
                mapOf("failureCode" to failureCode.name),
            )
            null
        }
        if (blurVariance != null) {
            logEvent(
                DrawingImportEvent.BLUR_METRIC_OK,
                projectId,
                mapOf("blurVariance" to blurVariance.toString()),
            )
        }
        return RectifiedQualityMetricsV1(blurVariance = blurVariance)
    }

    private fun writeCaptureMeta(
        session: DrawingImportSession,
        orderedCorners: OrderedCornersV1,
        refinedCorners: OrderedCornersV1?,
        rectifiedSize: RectifiedSizeV1,
        rectifiedMetrics: RectifiedQualityMetricsV1,
    ): PageDetectOutcomeV1<DrawingImportSession> {
        return try {
            val captureMeta = CaptureMetaV1(
                corners = CaptureCornersV1(
                    ordered = orderedCorners.toCornerQuadV1(),
                    refined = refinedCorners?.toCornerQuadV1(),
                ),
                rectified = RectifiedCaptureV1(
                    widthPx = rectifiedSize.width,
                    heightPx = rectifiedSize.height,
                ),
                metrics = CaptureMetricsV1(
                    blurVariance = rectifiedMetrics.blurVariance,
                ),
            )
            val store = FileArtifactStoreV1(session.projectDir)
            val updatedSession = CaptureMetaWriterV1().write(
                captureMeta = captureMeta,
                projectStore = store,
                session = session,
                rewriteManifest = true,
            )
            PageDetectOutcomeV1.Success(updatedSession)
        } catch (error: Throwable) {
            PageDetectOutcomeV1.Failure(
                PageDetectFailureV1(
                    stage = PageDetectStageV1.SAVE,
                    code = PageDetectFailureCodeV1.UNKNOWN,
                    debugMessage = error.message,
                ),
            )
        }
    }
}

data class PipelineResultV1(
    val orderedCorners: OrderedCornersV1,
    val refinedCorners: OrderedCornersV1?,
    val rectifiedSize: RectifiedSizeV1,
    val artifacts: List<ArtifactEntryV1>,
    val rectifiedQualityMetrics: RectifiedQualityMetricsV1? = null,
)

data class DrawingImportPipelineParamsV1(
    val maxSide: Int = 2048,
    val minSide: Int = 256,
    val enforceEven: Boolean = true,
    val edgeLowThreshold: Int = 60,
    val edgeHighThreshold: Int = 180,
    val refineParams: RefineParamsV1 = RefineParamsV1(
        windowRadiusPx = 6,
        maxIters = 6,
        epsilon = 0.25,
    ),
)

data class UprightBitmapV1(
    val bitmap: Bitmap,
    val rotationAppliedDeg: Int,
) {
    fun recycle() {
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}

interface DrawingImportPipelineStagesV1 {
    fun loadUpright(rawFile: File): PageDetectOutcomeV1<UprightBitmapV1>
    fun preprocess(upright: UprightBitmapV1, params: DrawingImportPipelineParamsV1): PageDetectOutcomeV1<PageDetectFrame>
    fun detectEdges(frame: PageDetectFrame): PageDetectOutcomeV1<EdgeMap>
    fun detectContours(edgeMap: EdgeMap): PageDetectOutcomeV1<List<ContourV1>>
    fun selectQuad(contours: List<ContourV1>, frame: PageDetectFrame): PageDetectOutcomeV1<PageQuadCandidate>
    fun orderCorners(candidate: PageQuadCandidate): PageDetectOutcomeV1<OrderedCornersV1>
    fun refineCorners(
        frame: PageDetectFrame,
        ordered: OrderedCornersV1,
        params: DrawingImportPipelineParamsV1,
    ): PageDetectOutcomeV1<RefineResultV1>

    fun computeRectifiedSize(
        corners: OrderedCornersV1,
        params: DrawingImportPipelineParamsV1,
    ): PageDetectOutcomeV1<RectifiedSizeV1>

    fun rectifyBitmap(
        upright: Bitmap,
        corners: OrderedCornersV1,
        size: RectifiedSizeV1,
    ): PageDetectOutcomeV1<Bitmap>

    fun saveArtifacts(
        session: DrawingImportSession,
        rectified: Bitmap,
        size: RectifiedSizeV1,
    ): PageDetectOutcomeV1<DrawingImportSession>

    class Default(
        private val params: DrawingImportPipelineParamsV1,
        private val contourExtractor: PageDetectContourExtractor = PageDetectContourExtractor(),
        private val quadSelector: PageQuadSelector = PageQuadSelector(),
        private val artifactWriter: RectifiedArtifactWriterV1 = RectifiedArtifactWriterV1(),
    ) : DrawingImportPipelineStagesV1 {
        override fun loadUpright(rawFile: File): PageDetectOutcomeV1<UprightBitmapV1> {
            val options = android.graphics.BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = try {
                android.graphics.BitmapFactory.decodeFile(rawFile.absolutePath, options)
            } catch (error: Throwable) {
                return failure(PageDetectStageV1.LOAD_UPRIGHT, PageDetectFailureCodeV1.DECODE_FAILED, error.message)
            } ?: return failure(
                PageDetectStageV1.LOAD_UPRIGHT,
                PageDetectFailureCodeV1.DECODE_FAILED,
                "Unable to decode image: $rawFile",
            )
            val exifOrientation = try {
                ExifInterface(rawFile).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED,
                )
            } catch (error: Throwable) {
                decoded.recycle()
                return failure(PageDetectStageV1.LOAD_UPRIGHT, PageDetectFailureCodeV1.EXIF_FAILED, error.message)
            }
            val transform = exifTransform(exifOrientation)
            val rotated = try {
                applyTransform(decoded, transform)
            } catch (error: Throwable) {
                decoded.recycle()
                return failure(PageDetectStageV1.LOAD_UPRIGHT, PageDetectFailureCodeV1.UNKNOWN, error.message)
            }
            if (rotated !== decoded) {
                decoded.recycle()
            }
            return PageDetectOutcomeV1.Success(
                UprightBitmapV1(
                    bitmap = rotated,
                    rotationAppliedDeg = transform.rotationDeg,
                ),
            )
        }

        override fun preprocess(
            upright: UprightBitmapV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<PageDetectFrame> {
            if (params.maxSide <= 0) {
                return failure(PageDetectStageV1.PREPROCESS, PageDetectFailureCodeV1.UNKNOWN, "maxSide must be > 0")
            }
            val target = computeTargetSize(upright.bitmap.width, upright.bitmap.height, params.maxSide)
            val scaled = if (target.width == upright.bitmap.width && target.height == upright.bitmap.height) {
                upright.bitmap
            } else {
                Bitmap.createScaledBitmap(upright.bitmap, target.width, target.height, true)
            }
            val gray = try {
                toGrayscale(scaled)
            } catch (error: Throwable) {
                if (scaled !== upright.bitmap) {
                    scaled.recycle()
                }
                return failure(PageDetectStageV1.PREPROCESS, PageDetectFailureCodeV1.UNKNOWN, error.message)
            }
            val frame = PageDetectFrame(
                width = scaled.width,
                height = scaled.height,
                gray = gray,
                originalWidth = upright.bitmap.width,
                originalHeight = upright.bitmap.height,
                downscaleFactor = target.downscaleFactor,
                rotationAppliedDeg = upright.rotationAppliedDeg,
            )
            if (scaled !== upright.bitmap) {
                scaled.recycle()
            }
            return PageDetectOutcomeV1.Success(frame)
        }

        override fun detectEdges(frame: PageDetectFrame): PageDetectOutcomeV1<EdgeMap> {
            val detector = PageDetectEdgeDetector(
                lowThreshold = params.edgeLowThreshold,
                highThreshold = params.edgeHighThreshold,
            )
            return detector.detect(frame)
        }

        override fun detectContours(edgeMap: EdgeMap): PageDetectOutcomeV1<List<ContourV1>> {
            return contourExtractor.extract(edgeMap)
        }

        override fun selectQuad(
            contours: List<ContourV1>,
            frame: PageDetectFrame,
        ): PageDetectOutcomeV1<PageQuadCandidate> {
            return quadSelector.select(contours, frame.width, frame.height)
        }

        override fun orderCorners(candidate: PageQuadCandidate): PageDetectOutcomeV1<OrderedCornersV1> {
            return CornerOrderingV1.order(candidate.points)
        }

        override fun refineCorners(
            frame: PageDetectFrame,
            ordered: OrderedCornersV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<RefineResultV1> {
            return CornerRefinerV1.refine(frame, ordered, params.refineParams)
        }

        override fun computeRectifiedSize(
            corners: OrderedCornersV1,
            params: DrawingImportPipelineParamsV1,
        ): PageDetectOutcomeV1<RectifiedSizeV1> {
            return RectifySizePolicyV1.compute(
                corners,
                RectifySizeParamsV1(
                    maxSide = params.maxSide,
                    minSide = params.minSide,
                    enforceEven = params.enforceEven,
                ),
            )
        }

        override fun rectifyBitmap(
            upright: Bitmap,
            corners: OrderedCornersV1,
            size: RectifiedSizeV1,
        ): PageDetectOutcomeV1<Bitmap> {
            if (size.width <= 0 || size.height <= 0) {
                return failure(PageDetectStageV1.RECTIFY, PageDetectFailureCodeV1.UNKNOWN, "Invalid rectified size")
            }
            val src = floatArrayOf(
                corners.topLeft.x.toFloat(), corners.topLeft.y.toFloat(),
                corners.topRight.x.toFloat(), corners.topRight.y.toFloat(),
                corners.bottomRight.x.toFloat(), corners.bottomRight.y.toFloat(),
                corners.bottomLeft.x.toFloat(), corners.bottomLeft.y.toFloat(),
            )
            val dst = floatArrayOf(
                0f, 0f,
                size.width.toFloat(), 0f,
                size.width.toFloat(), size.height.toFloat(),
                0f, size.height.toFloat(),
            )
            val matrix = Matrix()
            val success = matrix.setPolyToPoly(src, 0, dst, 0, 4)
            if (!success) {
                return failure(PageDetectStageV1.RECTIFY, PageDetectFailureCodeV1.UNKNOWN, "Unable to compute rectification matrix")
            }
            val output = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                isFilterBitmap = true
            }
            canvas.drawBitmap(upright, matrix, paint)
            return PageDetectOutcomeV1.Success(output)
        }

        override fun saveArtifacts(
            session: DrawingImportSession,
            rectified: Bitmap,
            size: RectifiedSizeV1,
        ): PageDetectOutcomeV1<DrawingImportSession> {
            return try {
                val projectStore = FileArtifactStoreV1(session.projectDir)
                val updated = artifactWriter.write(
                    rectifiedBitmap = rectified,
                    projectStore = projectStore,
                    session = session,
                    rewriteManifest = true,
                )
                PageDetectOutcomeV1.Success(
                    updated.copy(
                        rectifiedImageInfo = RectifiedImageInfo(size.width, size.height),
                    ),
                )
            } catch (error: Throwable) {
                PageDetectOutcomeV1.Failure(
                    PageDetectFailureV1(
                        stage = PageDetectStageV1.SAVE,
                        code = PageDetectFailureCodeV1.UNKNOWN,
                        debugMessage = error.message,
                    ),
                )
            }
        }

        private fun failure(
            stage: PageDetectStageV1,
            code: PageDetectFailureCodeV1,
            message: String?,
        ): PageDetectOutcomeV1<Nothing> {
            return PageDetectOutcomeV1.Failure(PageDetectFailureV1(stage, code, message))
        }

        private fun computeTargetSize(
            originalWidth: Int,
            originalHeight: Int,
            maxSide: Int,
        ): TargetSize {
            require(originalWidth > 0 && originalHeight > 0) { "Invalid source size." }
            require(maxSide > 0) { "maxSide must be > 0." }
            val originalMax = kotlin.math.max(originalWidth, originalHeight)
            if (originalMax <= maxSide) {
                return TargetSize(originalWidth, originalHeight, 1.0)
            }
            val ratio = maxSide.toDouble() / originalMax.toDouble()
            val targetWidth = kotlin.math.floor(originalWidth * ratio).toInt().coerceAtLeast(1)
            val targetHeight = kotlin.math.floor(originalHeight * ratio).toInt().coerceAtLeast(1)
            val targetMax = kotlin.math.max(targetWidth, targetHeight)
            val downscaleFactor = originalMax.toDouble() / targetMax.toDouble()
            return TargetSize(targetWidth, targetHeight, downscaleFactor)
        }

        private fun toGrayscale(bitmap: Bitmap): ByteArray {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val gray = ByteArray(width * height)
            for (i in pixels.indices) {
                val pixel = pixels[i]
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                val luma = (77 * r + 150 * g + 29 * b) shr 8
                gray[i] = luma.toByte()
            }
            return gray
        }

        private fun applyTransform(source: Bitmap, transform: ExifTransform): Bitmap {
            if (transform.rotationDeg == 0 && !transform.flipHorizontal) {
                return source
            }
            val matrix = Matrix().apply {
                if (transform.rotationDeg != 0) {
                    postRotate(transform.rotationDeg.toFloat())
                }
                if (transform.flipHorizontal) {
                    postScale(-1f, 1f)
                }
            }
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }

        private fun exifTransform(orientation: Int): ExifTransform {
            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> ExifTransform(rotationDeg = 90, flipHorizontal = false)
                ExifInterface.ORIENTATION_ROTATE_180 -> ExifTransform(rotationDeg = 180, flipHorizontal = false)
                ExifInterface.ORIENTATION_ROTATE_270 -> ExifTransform(rotationDeg = 270, flipHorizontal = false)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> ExifTransform(rotationDeg = 0, flipHorizontal = true)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> ExifTransform(rotationDeg = 180, flipHorizontal = true)
                ExifInterface.ORIENTATION_TRANSPOSE -> ExifTransform(rotationDeg = 90, flipHorizontal = true)
                ExifInterface.ORIENTATION_TRANSVERSE -> ExifTransform(rotationDeg = 270, flipHorizontal = true)
                else -> ExifTransform(rotationDeg = 0, flipHorizontal = false)
            }
        }

        private data class TargetSize(
            val width: Int,
            val height: Int,
            val downscaleFactor: Double,
        )

        private data class ExifTransform(
            val rotationDeg: Int,
            val flipHorizontal: Boolean,
        )
    }
}

private fun OrderedCornersV1.scale(scaleX: Double, scaleY: Double): OrderedCornersV1 {
    return OrderedCornersV1(
        topLeft = CornerPointV1(
            x = topLeft.x * scaleX,
            y = topLeft.y * scaleY,
        ),
        topRight = CornerPointV1(
            x = topRight.x * scaleX,
            y = topRight.y * scaleY,
        ),
        bottomRight = CornerPointV1(
            x = bottomRight.x * scaleX,
            y = bottomRight.y * scaleY,
        ),
        bottomLeft = CornerPointV1(
            x = bottomLeft.x * scaleX,
            y = bottomLeft.y * scaleY,
        ),
    )
}

private fun OrderedCornersV1.toCornerQuadV1(): CornerQuadV1 {
    return CornerQuadV1(
        topLeft = PointV1(topLeft.x, topLeft.y),
        topRight = PointV1(topRight.x, topRight.y),
        bottomRight = PointV1(bottomRight.x, bottomRight.y),
        bottomLeft = PointV1(bottomLeft.x, bottomLeft.y),
    )
}

private fun DrawingImportSession.rawImageFileOrNull(): File? {
    val entry = artifacts.firstOrNull { it.kind == ArtifactKindV1.RAW_IMAGE } ?: return null
    return File(projectDir, entry.relPath)
}
