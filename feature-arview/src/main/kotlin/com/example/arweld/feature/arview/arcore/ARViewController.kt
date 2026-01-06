package com.example.arweld.feature.arview.arcore

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.core.domain.spatial.AlignmentPoint
import com.example.arweld.core.domain.spatial.AlignmentSample
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.angularDistance
import com.example.arweld.feature.arview.marker.DetectedMarker
import com.example.arweld.feature.arview.marker.MarkerDetector
import com.example.arweld.feature.arview.marker.RealMarkerDetector
import com.example.arweld.feature.arview.marker.SimulatedMarkerDetector
import com.example.arweld.feature.arview.alignment.ManualAlignmentState
import com.example.arweld.feature.arview.alignment.RigidTransformSolver
import com.example.arweld.feature.arview.alignment.AlignmentEventLogger
import com.example.arweld.feature.arview.arcore.ArScreenshotRegistry
import com.example.arweld.feature.arview.pose.MarkerPoseEstimator
import com.example.arweld.feature.arview.render.AndroidFilamentModelLoader
import com.example.arweld.feature.arview.render.LoadedModel
import com.example.arweld.feature.arview.render.ModelLoader
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

/**
 * Placeholder controller for AR rendering surface.
 * Handles lifecycle callbacks and exposes a [SurfaceView] for Compose hosting.
 */
class ARViewController(
    context: Context,
    private val alignmentEventLogger: AlignmentEventLogger,
    private val workItemId: String?,
    markerDetector: MarkerDetector? = null,
) : ArScreenshotService {

    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        setBackgroundColor(Color.BLACK)
    }
    private val sessionManager = ARCoreSessionManager(context)
    private val modelLoader: ModelLoader = AndroidFilamentModelLoader(context)
    private val sceneRenderer = ARSceneRenderer(surfaceView, sessionManager, modelLoader.engine)
    private val markerDetector: MarkerDetector = markerDetector ?: RealMarkerDetector(::currentRotation)
    private val markerPoseEstimator = MarkerPoseEstimator()
    private val zoneRegistry = ZoneRegistry.fromAssets(context.assets)
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
    private var lastQualityChangeNs: Long = 0L
    private var lastManualResetNs: Long = 0L

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
    private val _manualAlignmentState = MutableStateFlow(ManualAlignmentState())
    val manualAlignmentState: StateFlow<ManualAlignmentState> = _manualAlignmentState
    private val _trackingStatus = MutableStateFlow(
        TrackingStatus(
            quality = TrackingQuality.POOR,
            reason = "Initializing tracking",
        ),
    )
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus
    private val _renderFps = MutableStateFlow(0.0)
    val renderFps: StateFlow<Double> = _renderFps
    private val mainHandler = Handler(Looper.getMainLooper())

    fun onCreate() {
        Log.d(TAG, "ARViewController onCreate")
        ArScreenshotRegistry.register(this)
        sceneRenderer.setFrameListener(::onFrame)
        sceneRenderer.setHitTestResultListener(::onHitTestResult)
        sceneRenderer.setRenderRateListener { fps ->
            _renderFps.value = fps
        }
        surfaceView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                sceneRenderer.queueHitTest(event.x, event.y)
                true
            } else {
                false
            }
        }
    }

    fun onResume() {
        Log.d(TAG, "ARViewController onResume")
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
        Log.d(TAG, "ARViewController onPause")
        sessionManager.onPause()
        sceneRenderer.onPause()
    }

    fun onDestroy() {
        Log.d(TAG, "ARViewController onDestroy")
        ArScreenshotRegistry.unregister(this)
        testNodeModel?.let {
            modelLoader.destroyModel(it)
            testNodeModel = null
        }
        sceneRenderer.setFrameListener(null)
        sceneRenderer.setRenderRateListener(null)
        sceneRenderer.destroy()
        sessionManager.onDestroy()
        detectorScope.cancel()
    }

    fun triggerDebugMarkerDetection() {
        if (markerDetector is SimulatedMarkerDetector) {
            (markerDetector as SimulatedMarkerDetector).triggerSimulatedDetection()
        } else {
            Log.d(TAG, "Simulated detector not active; real detector running")
        }
    }

    fun getView(): View = surfaceView

    /**
     * Loads the sprint test node model from assets so the renderer can attach it to the scene.
     */
    suspend fun loadTestNodeModel(): LoadedModel? {
        return try {
            val loadedModel = modelLoader.loadGlbFromAssets(TEST_NODE_ASSET_PATH)
            testNodeModel = loadedModel
            sceneRenderer.setTestModel(loadedModel)
            loadedModel
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
        val featurePointCount = computeFeaturePointCount(frame)
        val trackingState = frame.camera.trackingState
        val frameTimestampNs = frame.timestamp
        val cameraPoseWorld = frame.camera.pose.toPose3D()
        detectorScope.launch {
            try {
                val markers = markerDetector.detectMarkers(frame)
                _detectedMarkers.value = markers
                if (markers.isNotEmpty()) {
                    Log.d(TAG, "marker detected: ${markers.joinToString { it.id }}")
                }
                val intrinsics = cameraIntrinsics
                if (intrinsics == null) {
                    _errorMessage.value = "Unable to read camera intrinsics"
                }

                val worldPoses = if (intrinsics != null) {
                    markers.mapNotNull { marker ->
                        val zoneTransform = zoneRegistry.get(marker.id)
                        if (zoneTransform == null) {
                            Log.d(TAG, "Marker ${marker.id} not found in zone registry")
                            return@mapNotNull null
                        }
                        markerPoseEstimator.estimateMarkerPose(
                            intrinsics = intrinsics,
                            marker = marker,
                            markerSizeMeters = zoneTransform.markerSizeMeters,
                            cameraPoseWorld = cameraPoseWorld,
                        )?.let { pose ->
                            marker.id to pose
                        } ?: run {
                            Log.d(TAG, "Pose estimation returned null for marker ${marker.id}")
                            null
                        }
                    }.toMap()
                } else {
                    emptyMap()
                }

                _markerWorldPoses.value = worldPoses
                if (worldPoses.isNotEmpty()) {
                    _errorMessage.value = null
                    maybeAlignModel(worldPoses)
                } else {
                    if (markers.isNotEmpty()) {
                        _errorMessage.value = "Failed to estimate marker pose"
                    } else {
                        _errorMessage.value = null
                    }
                    updateAlignmentScoreForStaleFrame()
                }
                updateMarkerTimestampIfNeeded(markers.isNotEmpty(), frameTimestampNs)
                updateTrackingStatus(
                    trackingState = trackingState,
                    featurePoints = featurePointCount,
                    hasMarker = markers.isNotEmpty(),
                    frameTimestampNs = frameTimestampNs,
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
        _alignmentScore.value = 0f
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
        _alignmentScore.value = 0f
        lastManualResetNs = System.nanoTime()
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = true,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Manual alignment reset. Tap 3 reference points again",
        )
    }

    fun cancelManualAlignment() {
        manualAlignmentPoints.clear()
        lastManualResetNs = 0L
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = false,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Manual alignment cancelled",
        )
    }

    private fun maybeAlignModel(markerWorldPoses: Map<String, Pose3D>) {
        val alignmentResult = markerWorldPoses.entries.firstNotNullOfOrNull { (markerId, pose) ->
            zoneAligner.computeWorldZoneTransform(
                markerPoseWorld = pose,
                markerId = markerId,
            )?.let { markerId to it }
        } ?: run {
            updateAlignmentScoreForStaleFrame()
            return
        }

        val (markerId, worldZonePose) = alignmentResult
        val previousPose = zoneAligner.lastAlignedPose()
        val poseChanged = isPoseSignificantlyDifferent(previousPose, worldZonePose)

        if (poseChanged || !modelAligned.get()) {
            sceneRenderer.setModelRootPose(worldZonePose)
            modelAligned.set(true)
            lastAlignmentSetNs.set(System.nanoTime())
            _alignmentScore.value = computeAlignmentScore(markerWorldPoses.size)
            logMarkerAlignmentIfNeeded(
                markerId = markerId,
                transform = worldZonePose,
                alignmentScore = _alignmentScore.value.toDouble(),
            )
        } else if (modelAligned.get()) {
            lastAlignmentSetNs.set(System.nanoTime())
            _alignmentScore.value = computeAlignmentScore(markerWorldPoses.size)
        }
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
            _alignmentScore.value = 1f
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
        return (markerScore * MARKER_SCORE_WEIGHT + recencyScore * RECENCY_SCORE_WEIGHT).coerceIn(0f, 1f)
    }

    private fun computeRecencyScore(): Float {
        val lastAlignmentNs = lastAlignmentSetNs.get()
        if (lastAlignmentNs == 0L) return 0f
        val elapsed = System.nanoTime() - lastAlignmentNs
        val normalized = 1f - (elapsed.toDouble() / RECENT_ALIGNMENT_HORIZON_NS.toDouble()).toFloat()
        return normalized.coerceIn(0f, 1f)
    }

    private fun logMarkerAlignmentIfNeeded(markerId: String, transform: Pose3D, alignmentScore: Double?) {
        if (!shouldEmitAlignmentEvent(transform)) return
        markAlignmentEvent(transform)
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
        _alignmentScore.value = computeAlignmentScore(0)
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
        private const val MARKER_SCORE_WEIGHT = 0.4f
        private const val RECENCY_SCORE_WEIGHT = 0.6f
        private const val POSITION_EPS_METERS = 0.01
        private const val ANGLE_EPS_RADIANS = 0.05
    }

    private fun computeFeaturePointCount(frame: Frame): Int {
        return try {
            val pointCloud = frame.acquirePointCloud()
            val count = pointCloud.points.limit() / 4
            pointCloud.release()
            count
        } catch (error: Exception) {
            Log.w(TAG, "Failed to read point cloud", error)
            0
        }
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

    private fun updateTrackingStatus(
        trackingState: TrackingState,
        featurePoints: Int,
        hasMarker: Boolean,
        frameTimestampNs: Long,
    ) {
        val recentMarkerSeen = hasMarker ||
            frameTimestampNs - lastMarkerTimestampNs.get() <= RECENT_MARKER_TIMEOUT_NS

        val now = System.nanoTime()
        val recentAlignmentSet = now - lastAlignmentSetNs.get() <= RECENT_ALIGNMENT_HORIZON_NS
        val alignmentIsFresh = recentAlignmentSet && _alignmentScore.value >= 0.8f

        val manualModeActive = _manualAlignmentState.value.isActive
        val resetRecently = manualModeActive && now - lastManualResetNs <= RECENT_ALIGNMENT_HORIZON_NS

        val desiredStatus = when {
            trackingState != TrackingState.TRACKING -> TrackingStatus(
                quality = TrackingQuality.POOR,
                reason = "Camera tracking ${trackingState.name.lowercase()}",
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
        }

        val currentStatus = _trackingStatus.value
        if (desiredStatus.quality == currentStatus.quality) {
            if (desiredStatus.reason != currentStatus.reason) {
                _trackingStatus.value = desiredStatus
            }
            return
        }

        val now = System.nanoTime()
        if (now - lastQualityChangeNs < QUALITY_HOLD_DURATION_NS) {
            return
        }

        lastQualityChangeNs = now
        _trackingStatus.value = desiredStatus
    }
}
