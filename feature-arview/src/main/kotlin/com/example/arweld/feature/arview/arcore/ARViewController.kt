package com.example.arweld.feature.arview.arcore

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.Surface
import android.view.SurfaceView
import android.view.View
import android.view.MotionEvent
import android.view.WindowManager
import androidx.core.content.getSystemService
import com.example.arweld.core.domain.spatial.CameraIntrinsics
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.core.domain.spatial.Vector3
import com.example.arweld.core.domain.spatial.AlignmentPoint
import com.example.arweld.core.domain.spatial.AlignmentSample
import com.example.arweld.feature.arview.marker.DetectedMarker
import com.example.arweld.feature.arview.marker.MarkerDetector
import com.example.arweld.feature.arview.marker.StubMarkerDetector
import com.example.arweld.feature.arview.alignment.ManualAlignmentState
import com.example.arweld.feature.arview.alignment.RigidTransformSolver
import com.example.arweld.feature.arview.pose.MarkerPoseEstimator
import com.example.arweld.feature.arview.render.AndroidFilamentModelLoader
import com.example.arweld.feature.arview.render.LoadedModel
import com.example.arweld.feature.arview.render.ModelLoader
import com.example.arweld.feature.arview.zone.ZoneAligner
import com.example.arweld.feature.arview.zone.ZoneRegistry
import com.google.ar.core.Frame
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Placeholder controller for AR rendering surface.
 * Handles lifecycle callbacks and exposes a [SurfaceView] for Compose hosting.
 */
class ARViewController(
    context: Context,
) {

    private val surfaceView: SurfaceView = SurfaceView(context).apply {
        setBackgroundColor(Color.BLACK)
    }
    private val sessionManager = ARCoreSessionManager(context)
    private val modelLoader: ModelLoader = AndroidFilamentModelLoader(context)
    private val sceneRenderer = ARSceneRenderer(surfaceView, sessionManager, modelLoader.engine)
    private val markerDetector: MarkerDetector = StubMarkerDetector()
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

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    private val _detectedMarkers = MutableStateFlow<List<DetectedMarker>>(emptyList())
    val detectedMarkers: StateFlow<List<DetectedMarker>> = _detectedMarkers
    private val _markerWorldPoses = MutableStateFlow<Map<Int, Pose3D>>(emptyMap())
    val markerWorldPoses: StateFlow<Map<Int, Pose3D>> = _markerWorldPoses
    private val _manualAlignmentState = MutableStateFlow(ManualAlignmentState())
    val manualAlignmentState: StateFlow<ManualAlignmentState> = _manualAlignmentState

    fun onCreate() {
        Log.d(TAG, "ARViewController onCreate")
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
        testNodeModel?.let {
            modelLoader.destroyModel(it)
            testNodeModel = null
        }
        sceneRenderer.setFrameListener(null)
        sceneRenderer.destroy()
        sessionManager.onDestroy()
        detectorScope.cancel()
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
                }
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

        val worldZonePose = markerWorldPoses.entries.firstNotNullOfOrNull { (markerId, pose) ->
            zoneAligner.computeWorldZoneTransform(
                markerPoseWorld = pose,
                markerId = markerId,
            )
        }

        if (worldZonePose != null && modelAligned.compareAndSet(false, true)) {
            sceneRenderer.setModelRootPose(worldZonePose)
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
        } else {
            manualAlignmentPoints.clear()
            _manualAlignmentState.value = ManualAlignmentState(
                isActive = false,
                sample = AlignmentSample(emptyList()),
                statusMessage = "Failed to solve manual alignment",
            )
        }
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
    }
}
