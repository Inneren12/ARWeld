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
import com.example.arweld.feature.arview.marker.DetectedMarker
import com.example.arweld.feature.arview.marker.MarkerDetector
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
) : ArScreenshotService {

    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        setBackgroundColor(Color.BLACK)
    }
    private val sessionManager = ARCoreSessionManager(context)
    private val modelLoader: ModelLoader = AndroidFilamentModelLoader(context)
    private val sceneRenderer = ARSceneRenderer(surfaceView, sessionManager, modelLoader.engine)
    private val markerDetector: MarkerDetector = SimulatedMarkerDetector()
    private val markerPoseEstimator = MarkerPoseEstimator()
    private val zoneRegistry = ZoneRegistry()
    private val zoneAligner = ZoneAligner(zoneRegistry)
    private val rigidTransformSolver = RigidTransformSolver()
    private val detectorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val isDetectingMarkers = AtomicBoolean(false)
    private val modelAligned = AtomicBoolean(false)
    private var testNodeModel: LoadedModel? = null
    private val cachedIntrinsics = AtomicReference<CameraIntrinsics?>()
    private val manualAlignmentPoints = mutableListOf<AlignmentPoint>()
    private val lastMarkerTimestampNs = AtomicReference<Long>(0L)
    private var lastQualityChangeNs: Long = 0L

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _detectedMarkers = MutableStateFlow<List<DetectedMarker>>(emptyList())
    val detectedMarkers: StateFlow<List<DetectedMarker>> = _detectedMarkers
    private val _markerWorldPoses = MutableStateFlow<Map<Int, Pose3D>>(emptyMap())
    val markerWorldPoses: StateFlow<Map<Int, Pose3D>> = _markerWorldPoses
    private val _manualAlignmentState = MutableStateFlow(ManualAlignmentState())
    val manualAlignmentState: StateFlow<ManualAlignmentState> = _manualAlignmentState
    private val _trackingStatus = MutableStateFlow(
        TrackingStatus(
            quality = TrackingQuality.POOR,
            reason = "Initializing tracking",
        ),
    )
    val trackingStatus: StateFlow<TrackingStatus> = _trackingStatus
    private val mainHandler = Handler(Looper.getMainLooper())

    fun onCreate() {
        Log.d(TAG, "ARViewController onCreate")
        ArScreenshotRegistry.register(this)
        sceneRenderer.setFrameListener(::onFrame)
        sceneRenderer.setHitTestResultListener(::onHitTestResult)
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
        sceneRenderer.destroy()
        sessionManager.onDestroy()
        detectorScope.cancel()
    }

    fun triggerDebugMarkerDetection() {
        (markerDetector as? SimulatedMarkerDetector)?.triggerSimulatedDetection()
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
        val cameraIntrinsics = cachedIntrinsics.get() ?: frame.camera.toCameraIntrinsics()?.also {
            cachedIntrinsics.set(it)
        }
        val featurePointCount = computeFeaturePointCount(frame)
        val trackingState = frame.camera.trackingState
        val frameTimestampNs = frame.timestamp
        val cameraPoseWorld = frame.camera.pose.toPose3D()
        detectorScope.launch {
            try {
                val markers = markerDetector.detectMarkers(frame)
                _detectedMarkers.value = markers
                val intrinsics = cameraIntrinsics
                if (intrinsics != null) {
                    val worldPoses = markers.mapNotNull { marker ->
                        markerPoseEstimator.estimateMarkerPose(
                            intrinsics = intrinsics,
                            marker = marker,
                            markerSizeMeters = DEFAULT_MARKER_SIZE_METERS,
                            cameraPoseWorld = cameraPoseWorld,
                        )?.let { pose ->
                            marker.id to pose
                        }
                    }.toMap()
                    _markerWorldPoses.value = worldPoses
                    maybeAlignModel(worldPoses)
                    updateMarkerTimestampIfNeeded(worldPoses.isNotEmpty(), frameTimestampNs)
                }
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
        _manualAlignmentState.value = ManualAlignmentState(
            isActive = true,
            sample = AlignmentSample(emptyList()),
            statusMessage = "Tap 3 reference points to align manually",
        )
    }

    private fun maybeAlignModel(markerWorldPoses: Map<Int, Pose3D>) {
        if (markerAligned()) return

        val alignmentResult = markerWorldPoses.entries.firstNotNullOfOrNull { (markerId, pose) ->
            zoneAligner.computeWorldZoneTransform(
                markerPoseWorld = pose,
                markerId = markerId,
            )?.let { markerId to it }
        }

        if (alignmentResult != null && modelAligned.compareAndSet(false, true)) {
            val (markerId, worldZonePose) = alignmentResult
            sceneRenderer.setModelRootPose(worldZonePose)
            detectorScope.launch {
                alignmentEventLogger.logMarkerAlignment(workItemId, markerId, worldZonePose)
            }
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
            manualAlignmentPoints.clear()
            _manualAlignmentState.value = ManualAlignmentState(
                isActive = false,
                sample = AlignmentSample(emptyList()),
                statusMessage = "Manual alignment applied",
            )
            detectorScope.launch {
                alignmentEventLogger.logManualAlignment(
                    workItemId = workItemId,
                    numPoints = sample.points.size,
                    transform = solvedPose,
                )
            }
        } else {
            manualAlignmentPoints.clear()
            _manualAlignmentState.value = ManualAlignmentState(
                isActive = false,
                sample = AlignmentSample(emptyList()),
                statusMessage = "Failed to solve manual alignment",
            )
        }
    }

    override suspend fun captureArScreenshotToFile(workItemId: String): Uri {
        val bitmap = copySurfaceBitmap()
        val outputFile = saveBitmap(bitmap, workItemId)
        return outputFile.toUri()
    }

    override fun currentScreenshotMeta(): ArScreenshotMeta {
        val markerIds = _markerWorldPoses.value.keys.toList()
        val trackingQuality = _trackingStatus.value.quality.name
        val alignmentScore = if (modelAligned.get()) 1f else 0f

        return ArScreenshotMeta(
            markerIds = markerIds,
            trackingState = trackingQuality,
            alignmentQualityScore = alignmentScore,
            distanceToMarker = null,
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
        private const val DEFAULT_MARKER_SIZE_METERS = 0.12f
        private const val REQUIRED_POINTS = 3
        private val MODEL_REFERENCE_POINTS = listOf(
            Vector3(0.0, 0.0, 0.0),
            Vector3(0.2, 0.0, 0.0),
            Vector3(0.0, 0.2, 0.0),
        )
        private val QUALITY_HOLD_DURATION_NS = TimeUnit.MILLISECONDS.toNanos(500)
        private val RECENT_MARKER_TIMEOUT_NS = TimeUnit.SECONDS.toNanos(2)
        private const val MIN_FEATURE_POINTS_WARNING = 40
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

        val desiredStatus = when {
            trackingState != TrackingState.TRACKING -> TrackingStatus(
                quality = TrackingQuality.POOR,
                reason = "Camera tracking ${trackingState.name.lowercase()}",
            )
            recentMarkerSeen -> TrackingStatus(
                quality = TrackingQuality.GOOD,
                reason = "Marker lock stable",
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
