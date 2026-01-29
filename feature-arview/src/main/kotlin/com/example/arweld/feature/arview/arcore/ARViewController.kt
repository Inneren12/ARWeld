package com.example.arweld.feature.arview.arcore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.example.arweld.core.domain.diagnostics.ArTelemetrySnapshot
import com.example.arweld.core.domain.diagnostics.DeviceHealthProvider
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.feature.arview.BuildConfig
import com.example.arweld.feature.arview.R
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.spatial.AlignmentPoint
import com.example.arweld.core.domain.spatial.AlignmentSample
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Quaternion
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.angularDistance
import com.example.arweld.core.ar.arcore.ARCoreSessionManager
import com.example.arweld.core.ar.marker.DetectedMarker
import com.example.arweld.core.ar.marker.MarkerDetector
import com.example.arweld.core.ar.marker.RealMarkerDetector
import com.example.arweld.feature.arview.marker.SimulatedMarkerDetector
import com.example.arweld.feature.arview.alignment.ManualAlignmentState
import com.example.arweld.feature.arview.alignment.RigidTransformSolver
import com.example.arweld.feature.arview.alignment.AlignmentEventLogger
import com.example.arweld.feature.arview.alignment.DriftMonitor
import com.example.arweld.feature.arview.arcore.ArScreenshotRegistry
import com.example.arweld.feature.arview.pose.MarkerPoseEstimator
import com.example.arweld.feature.arview.pose.MarkerPoseEstimateResult
import com.example.arweld.feature.arview.pose.MultiMarkerPoseRefiner
import com.example.arweld.feature.arview.render.AndroidFilamentModelLoader
import com.example.arweld.feature.arview.render.LoadedModel
import com.example.arweld.feature.arview.render.ModelLoader
import com.example.arweld.feature.arview.render.ModelTooComplexException
import com.example.arweld.feature.arview.tracking.PointCloudStatus
import com.example.arweld.feature.arview.tracking.PerformanceMode
import com.example.arweld.feature.arview.tracking.TrackingQuality
import com.example.arweld.feature.arview.tracking.TrackingStatus
import com.example.arweld.feature.arview.zone.ZoneAligner
import com.example.arweld.feature.arview.zone.ZoneRegistry
import com.google.ar.core.Frame
import com.google.ar.core.TrackingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

/**
 * Placeholder controller for AR rendering surface.
 * Handles lifecycle callbacks and exposes a [SurfaceView] for Compose hosting.
 */
class ARViewController(
    context: Context,
    private val alignmentEventLogger: AlignmentEventLogger,
    private val workItemId: String?,
    markerDetector: MarkerDetector? = null,
    private val diagnosticsRecorder: DiagnosticsRecorder? = null,
    private val deviceHealthProvider: DeviceHealthProvider? = null,
) : ArScreenshotService {

    private val appContext: Context = context.applicationContext
    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        setBackgroundColor(Color.BLACK)
    }
    private val sessionManager = ARCoreSessionManager(appContext)
    private val modelLoader: ModelLoader = AndroidFilamentModelLoader(appContext)
    private val sceneRenderer = ARSceneRenderer(surfaceView, sessionManager, modelLoader.engine)
    private val markerDetector: MarkerDetector = markerDetector ?: RealMarkerDetector(::currentRotation)
    private val markerPoseEstimator = MarkerPoseEstimator()
    private val multiMarkerPoseRefiner = MultiMarkerPoseRefiner()
    private val zoneRegistry = ZoneRegistry.fromAssets(appContext.assets)
    private val zoneAligner = ZoneAligner(zoneRegistry)
    private val rigidTransformSolver = RigidTransformSolver()
    private val detectorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val isDetectingMarkers = AtomicBoolean(false)
    private val modelAligned = AtomicBoolean(false)
    private var testNodeModel: LoadedModel? = null
    private val cachedIntrinsics = AtomicReference<CameraIntrinsics?>()
    private val manualAlignmentPoints = mutableListOf<AlignmentPoint>()
    private val lastMarkerTimestampNs = AtomicReference<Long>(0L)
    private val lastAlignmentSetNs = AtomicLong(0L)
    private val lastAlignmentEventNs = AtomicLong(0L)
    private val lastAlignmentEventPose = AtomicReference<Pose3D?>()
    private val lastAppliedPose = AtomicReference<Pose3D?>()
    private var smoothedPose: Pose3D? = null
    private var hadMarkerPoseLastFrame = false
    private var lastQualityChangeNs: Long = 0L
    private var lastManualResetNs: Long = 0L
    private val lastCvRunNs = AtomicLong(0L)
    private val cvThrottleNs = AtomicLong(DEFAULT_CV_THROTTLE_NS)
    private val lastCvCameraPose = AtomicReference<Pose3D?>()
    private val lastMarkerRois = AtomicReference<List<RectF>>(emptyList())
    private val lastMarkerRoiTimestampNs = AtomicLong(0L)
    private var lastDriftEstimateMm: Double = 0.0
    private var lastResidualMm: Double = 0.0
    private val driftMonitor = DriftMonitor()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _intrinsicsAvailable = MutableStateFlow(false)
    val intrinsicsAvailable: StateFlow<Boolean> = _intrinsicsAvailable
    private val _detectedMarkers = MutableStateFlow<List<DetectedMarker>>(emptyList())
    val detectedMarkers: StateFlow<List<DetectedMarker>> = _detectedMarkers
    private val _markerWorldPoses = MutableStateFlow<Map<String, Pose3D>>(emptyMap())
    val markerWorldPoses: StateFlow<Map<String, Pose3D>> = _markerWorldPoses
    private val _alignmentScore = MutableStateFlow(0f)
    val alignmentScore: StateFlow<Float> = _alignmentScore
    private val _alignmentDriftMm = MutableStateFlow(0.0)
    val alignmentDriftMm: StateFlow<Double> = _alignmentDriftMm
    private val _alignmentDegraded = MutableStateFlow(false)
    val alignmentDegraded: StateFlow<Boolean> = _alignmentDegraded
    private val _manualAlignmentState = MutableStateFlow(ManualAlignmentState())
    val manualAlignmentState: StateFlow<ManualAlignmentState> = _manualAlignmentState
    private val _pointCloudStatus = MutableStateFlow(PointCloudStatusReport())
    val pointCloudStatus: StateFlow<PointCloudStatusReport> = _pointCloudStatus
    private val _trackingStatus = MutableStateFlow(
        TrackingStatus(
            quality = TrackingQuality.POOR,
            reason = "Initializing tracking",
        ),
    )
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus
    private val _renderFps = MutableStateFlow(0.0)
    val renderFps: StateFlow<Double> = _renderFps
    private val _performanceMode = MutableStateFlow(PerformanceMode.NORMAL)
    val performanceMode: StateFlow<PerformanceMode> = _performanceMode
    private val _arTelemetry = MutableStateFlow(
        ArTelemetrySnapshot(
            timestampMillis = System.currentTimeMillis(),
            fps = 0.0,
            frameTimeP95Ms = 0.0,
            cvLatencyP95Ms = 0.0,
            cvFps = 0.0,
            cvSkippedFrames = 0,
            performanceMode = PerformanceMode.NORMAL.name.lowercase(),
        ),
    )
    val arTelemetry: StateFlow<ArTelemetrySnapshot> = _arTelemetry
    private val mainHandler = Handler(Looper.getMainLooper())
    private val lastPointCloudLogMs = AtomicLong(0L)
    private val telemetryTracker = ArTelemetryTracker()
    private val forceLowPowerMode = AtomicBoolean(false)
    private val lastThermalOrMemoryReason = AtomicReference<String?>(null)

    fun onCreate() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ARViewController onCreate")
        }
        diagnosticsRecorder?.recordEvent("ar_view_created")
        ArScreenshotRegistry.register(this)
        sceneRenderer.setFrameListener(::onFrame)
        sceneRenderer.setHitTestResultListener(::onHitTestResult)
        sceneRenderer.setRenderRateListener { fps ->
            _renderFps.value = fps
            telemetryTracker.recordRenderFps(fps)
            updateTelemetrySnapshot()
            updatePerformanceMode(fps)
        }
        sceneRenderer.setFrameIntervalListener { intervalNs ->
            telemetryTracker.recordFrameIntervalNs(intervalNs)
            updateTelemetrySnapshot()
        }
        surfaceView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                sceneRenderer.queueHitTest(event.x, event.y)
                true
            } else {
                false
            }
        }
        deviceHealthProvider?.let { provider ->
            detectorScope.launch {
                provider.deviceHealth.collect { health ->
                    val trimLevel = health.memoryTrimLevel
                    val shouldForceLow = health.isDeviceHot ||
                        (trimLevel != null &&
                            trimLevel >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)
                    val reason = when {
                        health.isDeviceHot -> "thermal"
                        trimLevel != null -> "memory"
                        else -> null
                    }
                    if (forceLowPowerMode.getAndSet(shouldForceLow) != shouldForceLow) {
                        lastThermalOrMemoryReason.set(reason)
                        diagnosticsRecorder?.recordEvent(
                            name = "performance_throttle",
                            attributes = mapOf("source" to (reason ?: "none")),
                        )
                        updatePerformanceMode(_renderFps.value)
                    }
                }
            }
        }
    }

    fun onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ARViewController onResume")
        }
        diagnosticsRecorder?.recordEvent("ar_view_resume")
        val rotation = currentRotation()
        val error = sessionManager.onResume(
            displayRotation = rotation,
            viewportWidth = surfaceView.width,
            viewportHeight = surfaceView.height,
        )
        _errorMessage.value = error
        sceneRenderer.onResume()
    }

    fun onPause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ARViewController onPause")
        }
        diagnosticsRecorder?.recordEvent("ar_view_pause")
        sessionManager.onPause()
        sceneRenderer.onPause()
    }

    fun onDestroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "ARViewController onDestroy")
        }
        diagnosticsRecorder?.recordEvent("ar_view_destroy")
        ArScreenshotRegistry.unregister(this)
        testNodeModel?.let {
            modelLoader.destroyModel(it)
            testNodeModel = null
        }
        sceneRenderer.setFrameListener(null)
        sceneRenderer.setRenderRateListener(null)
        sceneRenderer.setFrameIntervalListener(null)
        sceneRenderer.destroy()
        sessionManager.onDestroy()
        detectorScope.cancel()
    }

    fun triggerDebugMarkerDetection() {
        if (markerDetector is SimulatedMarkerDetector) {
            (markerDetector as SimulatedMarkerDetector).triggerSimulatedDetection()
        } else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Simulated detector not active; real detector running")
            }
        }
    }

    fun getView(): View = surfaceView

    fun retryIntrinsics() {
        cachedIntrinsics.set(null)
        _intrinsicsAvailable.value = false
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Retrying intrinsics capture")
        }
    }

    fun restartSession() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Restarting ARCore session")
        }
        sceneRenderer.onPause()
        sessionManager.onPause()
        sessionManager.onDestroy()
        val error = sessionManager.onResume(
            displayRotation = currentRotation(),
            viewportWidth = surfaceView.width,
            viewportHeight = surfaceView.height,
        )
        _errorMessage.value = error
        sceneRenderer.onResume()
    }

    /**
     * Loads the sprint test node model from assets so the renderer can attach it to the scene.
     */
    suspend fun loadTestNodeModel(): LoadedModel? {
        return try {
            val loadedModel = modelLoader.loadGlbFromAssets(TEST_NODE_ASSET_PATH)
            testNodeModel = loadedModel
            sceneRenderer.setTestModel(loadedModel)
            loadedModel
        } catch (error: ModelTooComplexException) {
            Log.w(TAG, "Model rejected for AR rendering: ${error.assetPath}", error)
            _errorMessage.value = appContext.getString(R.string.model_too_complex)
            null
        } catch (error: Exception) {
            Log.e(TAG, "Failed to load test node model", error)
            _errorMessage.value = "Failed to load test node model"
            null
        }
    }

    private fun currentRotation(): Int {
        val windowManager = surfaceView.context.getSystemService<android.view.WindowManager>()
        val defaultRotation = windowManager?.defaultDisplay?.rotation
        val contextRotation = surfaceView.display?.rotation
        return (contextRotation ?: defaultRotation ?: Surface.ROTATION_0)
    }

    private fun onFrame(frame: Frame) {
        if (!isDetectingMarkers.compareAndSet(false, true)) return
        val rotationDegrees = rotationDegreesFromSurface(currentRotation())
        val freshIntrinsics = frame.camera.toCameraIntrinsics(rotationDegrees)?.also { intrinsics ->
            cachedIntrinsics.set(intrinsics)
        }
        val cameraIntrinsics = freshIntrinsics ?: cachedIntrinsics.get()
        _intrinsicsAvailable.value = cameraIntrinsics != null
        val pointCloudReport = computePointCloudStatus(frame)
        _pointCloudStatus.value = pointCloudReport
        val trackingState = frame.camera.trackingState
        val frameTimestampNs = frame.timestamp
        val cameraPoseWorld = frame.camera.pose.toPose3D()
        if (!shouldRunCv(frameTimestampNs, cameraPoseWorld)) {
            telemetryTracker.recordCvFrameSkipped()
            val markers = _detectedMarkers.value
            updateTrackingStatus(
                trackingState = trackingState,
                featurePoints = pointCloudReport.pointCount,
                markerCount = markers.size,
                frameTimestampNs = frameTimestampNs,
                driftEstimateMm = lastDriftEstimateMm,
            )
            isDetectingMarkers.set(false)
            return
        }
        telemetryTracker.recordCvFrameTimestampNs(frameTimestampNs)
        detectorScope.launch {
            try {
                val cvStartNs = System.nanoTime()
                val markers = markerDetector.detectMarkers(frame)
                telemetryTracker.recordCvLatencyNs(System.nanoTime() - cvStartNs)
                updateTelemetrySnapshot()
                _detectedMarkers.value = markers
                if (markers.isNotEmpty()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "marker detected: ${markers.joinToString { it.id }}")
                    }
                }
                val intrinsics = cameraIntrinsics
                if (intrinsics == null) {
                    if (_errorMessage.value != INTRINSICS_ERROR_MESSAGE) {
                        _errorMessage.value = INTRINSICS_ERROR_MESSAGE
                    }
                } else if (_errorMessage.value == INTRINSICS_ERROR_MESSAGE) {
                    _errorMessage.value = null
                }

                val observations = mutableListOf<MultiMarkerPoseRefiner.MarkerObservation>()
                val worldPoses = if (intrinsics != null) {
                    markers.mapNotNull { marker ->
                        val zoneTransform = zoneRegistry.get(marker.id)
                        if (zoneTransform == null) {
                            Log.w(TAG, "No zone alignment configured for markerId=${marker.id}")
                            return@mapNotNull null
                        }
                        when (
                            val result = markerPoseEstimator.estimateMarkerPoseWithDiagnostics(
                                intrinsics = intrinsics,
                                marker = marker,
                                markerSizeMeters = zoneTransform.markerSizeMeters,
                                cameraPoseWorld = cameraPoseWorld,
                            )
                        ) {
                            is MarkerPoseEstimateResult.Success -> {
                                observations.add(
                                    MultiMarkerPoseRefiner.MarkerObservation(
                                        markerId = marker.id,
                                        markerPoseCamera = result.cameraPose,
                                        markerSizeMeters = zoneTransform.markerSizeMeters,
                                        tMarkerZone = zoneTransform.tMarkerZone,
                                    ),
                                )
                                marker.id to result.worldPose
                            }
                            is MarkerPoseEstimateResult.Failure -> {
                                if (BuildConfig.DEBUG) {
                                    Log.d(
                                        TAG,
                                        "Pose estimation failed for marker ${marker.id}: ${result.reason}",
                                    )
                                }
                                null
                            }
                        }
                    }.toMap()
                } else {
                    emptyMap()
                }

                _markerWorldPoses.value = worldPoses
                updateMarkerRois(markers, intrinsics, cameraPoseWorld, frameTimestampNs)
                val hasMarkerPose = worldPoses.isNotEmpty()
                val markerPoseReacquired = hasMarkerPose && !hadMarkerPoseLastFrame
                hadMarkerPoseLastFrame = hasMarkerPose
                if (hasMarkerPose) {
                    _errorMessage.value = null
                    lastDriftEstimateMm = maybeAlignModel(
                        markerWorldPoses = worldPoses,
                        observations = observations,
                        cameraPoseWorld = cameraPoseWorld,
                        markerPoseReacquired = markerPoseReacquired,
                    )
                } else {
                    if (markers.isNotEmpty()) {
                        _errorMessage.value = "Failed to estimate marker pose"
                    } else {
                        _errorMessage.value = null
                    }
                    updateAlignmentScoreForStaleFrame()
                    lastDriftEstimateMm = 0.0
                }
                _alignmentDriftMm.value = lastDriftEstimateMm
                updateMarkerTimestampIfNeeded(markers.isNotEmpty(), frameTimestampNs)
                updateTrackingStatus(
                    trackingState = trackingState,
                    featurePoints = pointCloudReport.pointCount,
                    markerCount = markers.size,
                    frameTimestampNs = frameTimestampNs,
                    driftEstimateMm = lastDriftEstimateMm,
                )
            } catch (error: Exception) {
                Log.w(TAG, "Marker detection failed", error)
            } finally {
                isDetectingMarkers.set(false)
            }
        }
    }

    fun startManualAlignment() {
        manualAlignmentPoints.clear()
        modelAligned.set(false)
        updateAlignmentScore(0f)
        smoothedPose = null
        lastAppliedPose.set(null)
        lastManualResetNs = System.nanoTime()
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = true,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Tap 3 reference points to align manually",
        )
    }

    fun resetManualAlignment() {
        manualAlignmentPoints.clear()
        modelAligned.set(false)
        updateAlignmentScore(0f)
        smoothedPose = null
        lastAppliedPose.set(null)
        lastManualResetNs = System.nanoTime()
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = true,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Manual alignment reset. Tap 3 reference points again",
        )
    }

    fun cancelManualAlignment() {
        manualAlignmentPoints.clear()
        resetDriftMonitor()
        lastManualResetNs = 0L
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = false,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Manual alignment cancelled",
        )
    }

    private fun maybeAlignModel(
        markerWorldPoses: Map<String, Pose3D>,
        observations: List<MultiMarkerPoseRefiner.MarkerObservation>,
        cameraPoseWorld: Pose3D,
        markerPoseReacquired: Boolean,
    ): Double {
        val alignmentCandidates = markerWorldPoses.mapNotNull { (markerId, pose) ->
            zoneAligner.computeWorldZoneTransform(
                markerPoseWorld = pose,
                markerId = markerId,
            )?.let { markerId to it }
        }

        if (alignmentCandidates.isEmpty()) {
            updateAlignmentScoreForStaleFrame()
            return 0.0
        }

        val primaryMarkerId = alignmentCandidates.first().first
        val markerCount = alignmentCandidates.size
        val refined = if (observations.size >= MULTI_MARKER_THRESHOLD) {
            multiMarkerPoseRefiner.refinePose(
                cameraPoseWorld = cameraPoseWorld,
                observations = observations,
                initialPose = lastAppliedPose.get() ?: alignmentCandidates.first().second,
            )
        } else {
            null
        }

        val resolvedPose = refined?.worldZonePose ?: alignmentCandidates.first().second
        val driftMm = refined?.residualErrorMm ?: 0.0
        lastResidualMm = driftMm
        val resetSmoothing = markerPoseReacquired || !modelAligned.get()
        val smoothed = smoothPose(resolvedPose, markerCount, resetSmoothing)
        val previousPose = lastAppliedPose.get()
        val poseChanged = isPoseSignificantlyDifferent(previousPose, smoothed)

        if (poseChanged || !modelAligned.get()) {
            sceneRenderer.setModelRootPose(smoothed)
            modelAligned.set(true)
            lastAlignmentSetNs.set(System.nanoTime())
            lastAppliedPose.set(smoothed)
            updateAlignmentScore(computeAlignmentScore(markerCount))
            logMarkerAlignmentIfNeeded(
                markerId = primaryMarkerId,
                transform = smoothed,
                alignmentScore = _alignmentScore.value.toDouble(),
            )
        } else if (modelAligned.get()) {
            lastAlignmentSetNs.set(System.nanoTime())
            updateAlignmentScore(computeAlignmentScore(markerCount))
        }

        logAlignmentDiagnostics(markerCount, driftMm)
        return driftMm
    }

    private fun markerAligned(): Boolean = modelAligned.get()

    private fun onHitTestResult(worldPose: Pose3D) {
        if (!_manualAlignmentState.value.isActive) return
        val nextIndex = manualAlignmentPoints.size
        if (nextIndex >= MODEL_REFERENCE_POINTS.size) return
        val alignmentPoint = AlignmentPoint(
            worldPoint = worldPose.position,
            modelPoint = MODEL_REFERENCE_POINTS[nextIndex],
        )
        manualAlignmentPoints.add(alignmentPoint)
        val sample = AlignmentSample(points = manualAlignmentPoints.toList())
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = true,
            sample = sample,
            statusMessage = "Collected ${sample.points.size}/${MODEL_REFERENCE_POINTS.size} reference points",
        )

        if (sample.points.size >= REQUIRED_POINTS) {
            solveManualAlignment(sample)
        }
    }

    private fun solveManualAlignment(sample: AlignmentSample) {
        val modelPoints = sample.points.map { it.modelPoint }
        val worldPoints = sample.points.map { it.worldPoint }
        val solvedPose = rigidTransformSolver.solveRigidTransform(modelPoints, worldPoints)
        if (solvedPose != null) {
            sceneRenderer.setModelRootPose(solvedPose)
            modelAligned.set(true)
            lastAlignmentSetNs.set(System.nanoTime())
            lastAppliedPose.set(solvedPose)
            smoothedPose = solvedPose
            lastResidualMm = 0.0
            updateAlignmentScore(1f)
            manualAlignmentPoints.clear()
            _manualAlignmentState.value = ManualAlignmentState(
                isActive = false,
                sample = AlignmentSample(emptyList()),
                statusMessage = "Manual alignment applied",
            )
            logManualAlignmentIfNeeded(
                numPoints = sample.points.size,
                transform = solvedPose,
                alignmentScore = _alignmentScore.value.toDouble(),
            )
        } else {
            manualAlignmentPoints.clear()
            _manualAlignmentState.value = ManualAlignmentState(
                isActive = false,
                sample = AlignmentSample(emptyList()),
                statusMessage = "Failed to solve manual alignment",
            )
        }
    }

    private fun computeAlignmentScore(markerCount: Int): Float {
        if (!modelAligned.get()) return 0f
        val recencyScore = computeRecencyScore()
        val markerScore = (markerCount.toFloat() / MAX_MARKER_COUNT_FOR_SCORE).coerceIn(0f, 1f)
        val residualScore = computeResidualScore(lastResidualMm)
        return (
            markerScore * MARKER_SCORE_WEIGHT +
                recencyScore * RECENCY_SCORE_WEIGHT +
                residualScore * RESIDUAL_SCORE_WEIGHT
            ).coerceIn(0f, 1f)
    }

    private fun computeRecencyScore(): Float {
        val lastAlignmentNs = lastAlignmentSetNs.get()
        if (lastAlignmentNs == 0L) return 0f
        val elapsed = System.nanoTime() - lastAlignmentNs
        val normalized = 1f - (elapsed.toDouble() / RECENT_ALIGNMENT_HORIZON_NS.toDouble()).toFloat()
        return normalized.coerceIn(0f, 1f)
    }

    private fun computeResidualScore(residualMm: Double): Float {
        if (residualMm <= RESIDUAL_SCORE_MIN_MM) return 1f
        val normalized = 1f - (residualMm / RESIDUAL_SCORE_MAX_MM).toFloat()
        return normalized.coerceIn(0f, 1f)
    }

    private fun logMarkerAlignmentIfNeeded(markerId: String, transform: Pose3D, alignmentScore: Double?) {
        if (!shouldEmitAlignmentEvent(transform)) return
        markAlignmentEvent(transform)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "AR_ALIGNMENT_SET emitted for marker=$markerId score=$alignmentScore")
        }
        detectorScope.launch {
            alignmentEventLogger.logMarkerAlignment(
                workItemId = workItemId,
                markerId = markerId,
                transform = transform,
                alignmentScore = alignmentScore,
            )
        }
    }

    private fun logManualAlignmentIfNeeded(numPoints: Int, transform: Pose3D, alignmentScore: Double?) {
        if (!shouldEmitAlignmentEvent(transform)) return
        markAlignmentEvent(transform)
        detectorScope.launch {
            alignmentEventLogger.logManualAlignment(
                workItemId = workItemId,
                numPoints = numPoints,
                transform = transform,
                alignmentScore = alignmentScore,
            )
        }
    }

    private fun shouldEmitAlignmentEvent(pose: Pose3D): Boolean {
        val lastPose = lastAlignmentEventPose.get()
        val poseChanged = isPoseSignificantlyDifferent(lastPose, pose)
        val intervalElapsed = System.nanoTime() - lastAlignmentEventNs.get()
        val intervalOk = lastAlignmentEventNs.get() == 0L || intervalElapsed >= ALIGNMENT_EVENT_MIN_INTERVAL_NS
        return (lastPose == null || poseChanged) && intervalOk
    }

    private fun markAlignmentEvent(pose: Pose3D) {
        lastAlignmentEventPose.set(pose)
        lastAlignmentEventNs.set(System.nanoTime())
    }

    private fun updateAlignmentScoreForStaleFrame() {
        updateAlignmentScore(computeAlignmentScore(0))
    }

    private fun updateAlignmentScore(score: Float) {
        _alignmentScore.value = score
        if (!modelAligned.get()) {
            resetDriftMonitor()
            return
        }
        val state = driftMonitor.update(score)
        _alignmentDegraded.value = state.isDegraded
        if (state.changed) {
            diagnosticsRecorder?.recordEvent(
                name = "alignment_drift",
                attributes = mapOf(
                    "state" to if (state.isDegraded) "degraded" else "recovered",
                    "score" to "%.3f".format(state.averageScore),
                    "timestamp" to System.currentTimeMillis().toString(),
                ),
            )
        }
    }

    private fun resetDriftMonitor() {
        driftMonitor.reset()
        _alignmentDegraded.value = false
    }

    private fun isPoseSignificantlyDifferent(previousPose: Pose3D?, newPose: Pose3D): Boolean {
        previousPose ?: return true
        val positionDelta = newPose.position - previousPose.position
        val positionDistance = positionDelta.norm()
        val angularDistance = previousPose.rotation.angularDistance(newPose.rotation)
        return positionDistance > POSITION_EPS_METERS || angularDistance > ANGLE_EPS_RADIANS
    }

    override suspend fun captureArScreenshotToFile(workItemId: String): Uri {
        val bitmap = copySurfaceBitmap()
        val outputFile = saveBitmap(bitmap, workItemId)
        return outputFile.toUri()
    }

    override fun currentScreenshotMeta(): ArScreenshotMeta {
        val markerIds = _markerWorldPoses.value.keys.toList()
        val trackingQuality = _trackingStatus.value.quality.name
        val alignmentScore = _alignmentScore.value

        return ArScreenshotMeta(
            markerIds = markerIds,
            trackingState = trackingQuality,
            alignmentQualityScore = alignmentScore,
            distanceToMarker = null,
            timestamp = System.currentTimeMillis(),
        )
    }

    private suspend fun copySurfaceBitmap(): Bitmap {
        val width = surfaceView.width
        val height = surfaceView.height
        check(width > 0 && height > 0) { "SurfaceView not ready for screenshot" }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                PixelCopy.request(surfaceView, bitmap, { result ->
                    if (!continuation.isActive) return@request
                    if (result == PixelCopy.SUCCESS) {
                        continuation.resume(bitmap)
                    } else {
                        continuation.resumeWithException(
                            IllegalStateException("PixelCopy failed with code $result"),
                        )
                    }
                }, mainHandler)
            }
        }
    }

    private suspend fun saveBitmap(bitmap: Bitmap, workItemId: String): File = withContext(Dispatchers.IO) {
        val directory = File(surfaceView.context.filesDir, "evidence")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "${workItemId}_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        file
    }

    companion object {
        private const val TAG = "ARViewController"
        private const val TEST_NODE_ASSET_PATH = "models/test_node.glb"
        private const val INTRINSICS_ERROR_MESSAGE = "Unable to read camera intrinsics"
        private const val REQUIRED_POINTS = 3
        // Reference points chosen from test_node.glb: origin and two 20cm offsets along X/Y on the base plane
        private val MODEL_REFERENCE_POINTS = listOf(
            Vector3(0.0, 0.0, 0.0),
            Vector3(0.2, 0.0, 0.0),
            Vector3(0.0, 0.2, 0.0),
        )
        private val QUALITY_HOLD_DURATION_NS = TimeUnit.MILLISECONDS.toNanos(500)
        private val RECENT_MARKER_TIMEOUT_NS = TimeUnit.SECONDS.toNanos(2)
        private const val MIN_FEATURE_POINTS_WARNING = 40
        private val RECENT_ALIGNMENT_HORIZON_NS = TimeUnit.SECONDS.toNanos(3)
        private val ALIGNMENT_EVENT_MIN_INTERVAL_NS = TimeUnit.SECONDS.toNanos(1)
        private const val MAX_MARKER_COUNT_FOR_SCORE = 3f
        private const val MARKER_SCORE_WEIGHT = 0.2f
        private const val RECENCY_SCORE_WEIGHT = 0.3f
        private const val RESIDUAL_SCORE_WEIGHT = 0.5f
        private const val RESIDUAL_SCORE_MIN_MM = 1.0
        private const val RESIDUAL_SCORE_MAX_MM = 15.0
        private const val POSITION_EPS_METERS = 0.01
        private const val ANGLE_EPS_RADIANS = 0.05
        private const val POINT_CLOUD_LOG_INTERVAL_MS = 3000L
        private const val DRIFT_WARNING_MM = 8.0
        private const val DRIFT_CRITICAL_MM = 15.0
        private const val MULTI_MARKER_THRESHOLD = 2
        private const val SINGLE_MARKER_POSITION_SMOOTHING_ALPHA = 0.35
        private const val MULTI_MARKER_POSITION_SMOOTHING_ALPHA = 0.55
        private const val SINGLE_MARKER_ROTATION_SMOOTHING_ALPHA = 0.25
        private const val MULTI_MARKER_ROTATION_SMOOTHING_ALPHA = 0.45
        private val DEFAULT_CV_THROTTLE_NS = TimeUnit.MILLISECONDS.toNanos(80)
        private val LOW_FPS_CV_THROTTLE_NS = TimeUnit.MILLISECONDS.toNanos(100)
        private const val LOW_FPS_THRESHOLD = 20.0
        private val MAX_ROI_AGE_NS = TimeUnit.MILLISECONDS.toNanos(500)
        private const val ROI_TRANSLATION_EPS_METERS = 0.005
        private const val ROI_ROTATION_EPS_RADIANS = 0.03
    }

    private fun computePointCloudStatus(frame: Frame): PointCloudStatusReport {
        return try {
            val pointCloud = frame.acquirePointCloud()
            val count = pointCloud.points.limit() / 4
            pointCloud.release()
            val status = if (count > 0) PointCloudStatus.OK else PointCloudStatus.EMPTY
            PointCloudStatusReport(status = status, pointCount = count)
        } catch (error: Exception) {
            maybeLogPointCloudFailure(error)
            PointCloudStatusReport(status = PointCloudStatus.FAILED, pointCount = 0)
        }
    }

    private fun maybeLogPointCloudFailure(error: Exception) {
        if (!BuildConfig.DEBUG) return
        val nowMs = System.nanoTime() / 1_000_000
        val last = lastPointCloudLogMs.get()
        if (nowMs - last < POINT_CLOUD_LOG_INTERVAL_MS) return
        if (!lastPointCloudLogMs.compareAndSet(last, nowMs)) return
        Log.w(TAG, "Failed to read point cloud", error)
    }

    private fun rotationDegreesFromSurface(rotation: Int): Int = when (rotation) {
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

    private fun updateMarkerTimestampIfNeeded(hasMarker: Boolean, frameTimestampNs: Long) {
        if (hasMarker) {
            lastMarkerTimestampNs.set(frameTimestampNs)
        }
    }

    private fun shouldRunCv(frameTimestampNs: Long, cameraPoseWorld: Pose3D): Boolean {
        val lastRun = lastCvRunNs.get()
        val throttle = cvThrottleNs.get()
        if (frameTimestampNs - lastRun < throttle) return false
        if (shouldSkipForStableRoi(frameTimestampNs, cameraPoseWorld)) return false
        return lastCvRunNs.compareAndSet(lastRun, frameTimestampNs)
    }

    private fun shouldSkipForStableRoi(frameTimestampNs: Long, cameraPoseWorld: Pose3D): Boolean {
        val roiTimestamp = lastMarkerRoiTimestampNs.get()
        if (roiTimestamp == 0L) return false
        if (frameTimestampNs - roiTimestamp > MAX_ROI_AGE_NS) return false
        val rois = lastMarkerRois.get()
        if (rois.isEmpty()) return false
        val lastPose = lastCvCameraPose.get() ?: return false
        val translationDelta = (cameraPoseWorld.position - lastPose.position).norm()
        val rotationDelta = lastPose.rotation.angularDistance(cameraPoseWorld.rotation)
        return translationDelta <= ROI_TRANSLATION_EPS_METERS && rotationDelta <= ROI_ROTATION_EPS_RADIANS
    }

    private fun updateMarkerRois(
        markers: List<DetectedMarker>,
        intrinsics: CameraIntrinsics?,
        cameraPoseWorld: Pose3D,
        frameTimestampNs: Long,
    ) {
        if (markers.isEmpty() || intrinsics == null) {
            lastMarkerRois.set(emptyList())
            lastCvCameraPose.set(null)
            lastMarkerRoiTimestampNs.set(0L)
            return
        }
        val rois = markers.mapNotNull { marker ->
            roiFromCorners(marker.corners, intrinsics)
        }
        lastMarkerRois.set(rois)
        lastCvCameraPose.set(cameraPoseWorld)
        lastMarkerRoiTimestampNs.set(frameTimestampNs)
    }

    private fun roiFromCorners(corners: List<android.graphics.PointF>, intrinsics: CameraIntrinsics): RectF? {
        if (corners.isEmpty()) return null
        val width = intrinsics.width.toFloat()
        val height = intrinsics.height.toFloat()
        if (width <= 0f || height <= 0f) return null
        val xs = corners.map { (it.x / width).coerceIn(0f, 1f) }
        val ys = corners.map { (it.y / height).coerceIn(0f, 1f) }
        val minX = xs.minOrNull() ?: return null
        val maxX = xs.maxOrNull() ?: return null
        val minY = ys.minOrNull() ?: return null
        val maxY = ys.maxOrNull() ?: return null
        return RectF(minX, minY, maxX, maxY)
    }

    private fun updatePerformanceMode(fps: Double) {
        val nextMode = if (fps > 0 && fps < LOW_FPS_THRESHOLD) {
            PerformanceMode.LOW
        } else {
            PerformanceMode.NORMAL
        }
        val forcedLow = forceLowPowerMode.get()
        val resolvedMode = if (forcedLow) PerformanceMode.LOW else nextMode
        if (resolvedMode == _performanceMode.value) return
        _performanceMode.value = resolvedMode
        cvThrottleNs.set(if (resolvedMode == PerformanceMode.LOW) LOW_FPS_CV_THROTTLE_NS else DEFAULT_CV_THROTTLE_NS)
        sceneRenderer.setPerformanceMode(resolvedMode)
        diagnosticsRecorder?.recordEvent(
            name = "performance_mode",
            attributes = mapOf(
                "mode" to resolvedMode.name.lowercase(),
                "reason" to (lastThermalOrMemoryReason.get() ?: if (fps > 0 && fps < LOW_FPS_THRESHOLD) "low_fps" else "normal"),
            ),
        )
        updateTelemetrySnapshot()
    }

    private fun updateTelemetrySnapshot() {
        val snapshot = telemetryTracker.snapshot(_performanceMode.value)
        diagnosticsRecorder?.updateArTelemetry(snapshot)
        if (_arTelemetry.value != snapshot) {
            _arTelemetry.value = snapshot
        }
    }

    private fun logAlignmentDiagnostics(markerCount: Int, residualMm: Double) {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Alignment updated with $markerCount marker(s), residual=${"%.2f".format(residualMm)}mm",
            )
        }
    }

    private fun smoothPose(target: Pose3D, markerCount: Int, reset: Boolean): Pose3D {
        val previous = smoothedPose
        val positionAlpha = if (markerCount >= MULTI_MARKER_THRESHOLD) {
            MULTI_MARKER_POSITION_SMOOTHING_ALPHA
        } else {
            SINGLE_MARKER_POSITION_SMOOTHING_ALPHA
        }
        val rotationAlpha = if (markerCount >= MULTI_MARKER_THRESHOLD) {
            MULTI_MARKER_ROTATION_SMOOTHING_ALPHA
        } else {
            SINGLE_MARKER_ROTATION_SMOOTHING_ALPHA
        }
        if (reset || previous == null) {
            smoothedPose = target
            return target
        }
        val blendedPosition = previous.position * (1.0 - positionAlpha) + target.position * positionAlpha
        val blendedRotation = nlerp(previous.rotation, target.rotation, rotationAlpha)
        val smoothed = Pose3D(blendedPosition, blendedRotation)
        logPoseSmoothingIfNeeded(
            previous = previous,
            raw = target,
            smoothed = smoothed,
            markerCount = markerCount,
            positionAlpha = positionAlpha,
            rotationAlpha = rotationAlpha,
        )
        smoothedPose = smoothed
        return smoothed
    }

    private fun logPoseSmoothingIfNeeded(
        previous: Pose3D,
        raw: Pose3D,
        smoothed: Pose3D,
        markerCount: Int,
        positionAlpha: Double,
        rotationAlpha: Double,
    ) {
        if (!BuildConfig.DEBUG) return
        val rawPositionDeltaMm = (raw.position - previous.position).norm() * 1000.0
        val smoothedPositionDeltaMm = (smoothed.position - previous.position).norm() * 1000.0
        val rawAngleDeltaDeg = Math.toDegrees(previous.rotation.angularDistance(raw.rotation))
        val smoothedAngleDeltaDeg = Math.toDegrees(previous.rotation.angularDistance(smoothed.rotation))
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "Pose smoothing (markers=$markerCount) posΔ raw=${"%.2f".format(rawPositionDeltaMm)}mm " +
                    "smooth=${"%.2f".format(smoothedPositionDeltaMm)}mm " +
                    "rotΔ raw=${"%.2f".format(rawAngleDeltaDeg)}° " +
                    "smooth=${"%.2f".format(smoothedAngleDeltaDeg)}° " +
                    "alpha(pos=${"%.2f".format(positionAlpha)}, rot=${"%.2f".format(rotationAlpha)})",
            )
        }
    }

    private fun nlerp(from: Quaternion, to: Quaternion, alpha: Double): Quaternion {
        val aligned = if (dot(from, to) < 0.0) {
            Quaternion(-to.x, -to.y, -to.z, -to.w)
        } else {
            to
        }
        val x = from.x + (aligned.x - from.x) * alpha
        val y = from.y + (aligned.y - from.y) * alpha
        val z = from.z + (aligned.z - from.z) * alpha
        val w = from.w + (aligned.w - from.w) * alpha
        return Quaternion(x, y, z, w).normalized()
    }

    private fun dot(a: Quaternion, b: Quaternion): Double = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w

    private fun updateTrackingStatus(
        trackingState: TrackingState,
        featurePoints: Int,
        markerCount: Int,
        frameTimestampNs: Long,
        driftEstimateMm: Double,
    ) {
        val hasMarker = markerCount > 0
        val recentMarkerSeen = hasMarker ||
            frameTimestampNs - lastMarkerTimestampNs.get() <= RECENT_MARKER_TIMEOUT_NS

        val now = System.nanoTime()
        val recentAlignmentSet = now - lastAlignmentSetNs.get() <= RECENT_ALIGNMENT_HORIZON_NS
        val alignmentIsFresh = recentAlignmentSet && _alignmentScore.value >= 0.8f

        val manualModeActive = _manualAlignmentState.value.isActive
        val resetRecently = manualModeActive && now - lastManualResetNs <= RECENT_ALIGNMENT_HORIZON_NS

        val driftWarning = driftEstimateMm >= DRIFT_WARNING_MM
        val driftCritical = driftEstimateMm >= DRIFT_CRITICAL_MM
        val performanceMode = _performanceMode.value
        val performanceHint = when (performanceMode) {
            PerformanceMode.LOW -> "Performance low: reducing visuals"
            PerformanceMode.NORMAL -> null
        }

        val desiredStatus = when {
            trackingState != TrackingState.TRACKING -> TrackingStatus(
                quality = TrackingQuality.POOR,
                reason = "Camera tracking ${trackingState.name.lowercase()}",
            )
            driftCritical -> TrackingStatus(
                quality = TrackingQuality.POOR,
                reason = "Alignment drift ~${driftEstimateMm.roundToInt()} mm. Tap to re-align.",
            )
            driftWarning -> TrackingStatus(
                quality = TrackingQuality.WARNING,
                reason = "Alignment drifting (~${driftEstimateMm.roundToInt()} mm). Tap to re-align.",
            )
            resetRecently -> TrackingStatus(
                quality = TrackingQuality.WARNING,
                reason = "Collecting manual alignment points",
            )
            recentMarkerSeen -> TrackingStatus(
                quality = TrackingQuality.GOOD,
                reason = "Marker lock stable",
            )
            alignmentIsFresh && featurePoints >= MIN_FEATURE_POINTS_WARNING -> TrackingStatus(
                quality = TrackingQuality.GOOD,
                reason = "Manual alignment locked",
            )
            recentAlignmentSet -> TrackingStatus(
                quality = TrackingQuality.WARNING,
                reason = "Alignment set, waiting for more features",
            )
            featurePoints >= MIN_FEATURE_POINTS_WARNING -> TrackingStatus(
                quality = TrackingQuality.WARNING,
                reason = "Tracking but marker not visible",
            )
            else -> TrackingStatus(
                quality = TrackingQuality.POOR,
                reason = "Insufficient features for stable tracking",
            )
        }.withDiagnostics(
            markerCount = markerCount,
            driftEstimateMm = driftEstimateMm,
            performanceMode = performanceMode,
            performanceHint = performanceHint,
        )

        val currentStatus = _trackingStatus.value
        if (desiredStatus.quality == currentStatus.quality) {
            if (desiredStatus.reason != currentStatus.reason) {
                _trackingStatus.value = desiredStatus
            }
            return
        }

        val qualityCheckNow = System.nanoTime()
        if (qualityCheckNow - lastQualityChangeNs < QUALITY_HOLD_DURATION_NS) {
            return
        }

        lastQualityChangeNs = qualityCheckNow
        _trackingStatus.value = desiredStatus
    }

    private fun TrackingStatus.withDiagnostics(
        markerCount: Int,
        driftEstimateMm: Double,
        performanceMode: PerformanceMode,
        performanceHint: String?,
    ): TrackingStatus {
        val combinedReason = listOfNotNull(reason, performanceHint).joinToString(" • ").ifBlank { null }
        return copy(
            reason = combinedReason,
            markerVisibility = markerCount,
            driftEstimateMm = driftEstimateMm,
            performanceMode = performanceMode,
        )
    }
}

data class PointCloudStatusReport(
    val status: PointCloudStatus = PointCloudStatus.UNKNOWN,
    val pointCount: Int = 0,
)
