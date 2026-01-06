package com.example.arweld.feature.arview.arcore

import android.opengl.Matrix
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.arweld.core.domain.spatial.Pose3D
import com.example.arweld.feature.arview.render.LoadedModel
import com.example.arweld.feature.arview.arcore.toArCorePose
import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.Camera
import com.google.android.filament.LightManager
import com.google.android.filament.Renderer
import com.google.android.filament.Scene
import com.google.android.filament.SwapChain
import com.google.android.filament.View
import com.google.android.filament.Viewport
import com.google.android.filament.utils.Utils
import com.google.ar.core.Anchor
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import java.util.concurrent.TimeUnit
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Bridges ARCore session updates with a Filament renderer to draw models at
 * stable poses in the AR scene.
 */
class ARSceneRenderer(
    private val surfaceView: SurfaceView,
    private val sessionManager: ARCoreSessionManager,
    private val engine: Engine,
) : SurfaceHolder.Callback {

    private val renderer: Renderer = engine.createRenderer()
    private val scene: Scene = engine.createScene()
    private val cameraEntity = EntityManager.get().create()
    private val camera = engine.createCamera(cameraEntity)
    private val view: View = engine.createView().apply {
        scene = this@ARSceneRenderer.scene
        camera = this@ARSceneRenderer.camera
    }
    private var swapChain: SwapChain? = null

    private var testModel: LoadedModel? = null
    private var testModelAttached = false
    private var anchor: Anchor? = null
    private var modelRootPose: Pose3D? = null
    private var frameListener: ((Frame) -> Unit)? = null
    private var hitTestResultListener: ((Pose3D) -> Unit)? = null
    private var renderRateListener: ((Double) -> Unit)? = null
    private val tapQueue = ConcurrentLinkedQueue<Pair<Float, Float>>()

    private var rendering = false
    private var lastRenderTimeNs: Long = 0L
    private var smoothedFrameIntervalNs: Double? = null
    private var lastFpsLogTimestampNs: Long = 0L
    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (shouldRenderFrame(frameTimeNanos)) {
                renderFrame(frameTimeNanos)
            }
            if (rendering) {
                choreographer.postFrameCallback(this)
            }
        }
    }

    init {
        Utils.init()
        surfaceView.holder.addCallback(this)
        addDirectionalLight()
    }

    fun setFrameListener(listener: ((Frame) -> Unit)?) {
        frameListener = listener
    }

    fun setHitTestResultListener(listener: ((Pose3D) -> Unit)?) {
        hitTestResultListener = listener
    }

    fun setRenderRateListener(listener: ((Double) -> Unit)?) {
        renderRateListener = listener
    }

    fun onResume() {
        lastRenderTimeNs = 0L
        smoothedFrameIntervalNs = null
        rendering = true
        choreographer.postFrameCallback(frameCallback)
    }

    fun onPause() {
        rendering = false
        choreographer.removeFrameCallback(frameCallback)
    }

    fun destroy() {
        onPause()
        anchor?.detach()
        anchor = null
        swapChain?.let { chain ->
            engine.destroySwapChain(chain)
            swapChain = null
        }
        engine.destroyRenderer(renderer)
        engine.destroyView(view)
        engine.destroyCameraComponent(camera.entity)
        EntityManager.get().destroy(cameraEntity)
    }

    fun setTestModel(model: LoadedModel) {
        testModel = model
        if (!testModelAttached) {
            scene.addEntities(model.entities)
            testModelAttached = true
        }
    }

    fun setModelRootPose(pose: Pose3D) {
        modelRootPose = pose
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        swapChain = engine.createSwapChain(holder.surface, 0L)
        view.viewport = Viewport(0, 0, surfaceView.width, surfaceView.height)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        view.viewport = Viewport(0, 0, width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        onPause()
        swapChain?.let { chain ->
            engine.destroySwapChain(chain)
            swapChain = null
        }
    }

    private fun renderFrame(frameTimeNanos: Long) {
        val swapChain = swapChain ?: return
        val session = sessionManager.session ?: return
        try {
            val frame = session.update()
            notifyFrameListeners(frame)
            ensureAnchor(frame, session)
            processQueuedHitTests(frame)
            updateCamera(frame)
            updateModelTransform()
            if (renderer.beginFrame(swapChain, frameTimeNanos)) {
                renderer.render(view)
                renderer.endFrame()
                recordFrameTiming(frameTimeNanos)
            }
        } catch (error: Exception) {
            Log.w(TAG, "Failed to render AR frame", error)
        }
    }

    fun queueHitTest(x: Float, y: Float) {
        tapQueue.add(x to y)
    }

    private fun notifyFrameListeners(frame: Frame) {
        try {
            frameListener?.invoke(frame)
        } catch (error: Exception) {
            Log.w(TAG, "Frame listener failed", error)
        }
    }

    private fun processQueuedHitTests(frame: Frame) {
        val listener = hitTestResultListener ?: return
        var tap = tapQueue.poll()
        while (tap != null) {
            try {
                val pose = frame.hitTest(tap.first, tap.second).firstOrNull()?.hitPose
                if (pose != null) {
                    listener(pose.toPose3D())
                }
            } catch (error: Exception) {
                Log.w(TAG, "HitTest failed", error)
            }
            tap = tapQueue.poll()
        }
    }

    private fun ensureAnchor(frame: Frame, session: Session) {
        if (anchor != null || testModel == null) return
        if (frame.camera.trackingState != TrackingState.TRACKING) return
        val forwardPose = frame.camera.pose.compose(Pose.makeTranslation(0f, 0f, -DEFAULT_Z_OFFSET_METERS))
        anchor = session.createAnchor(forwardPose)
    }

    private fun updateCamera(frame: Frame) {
        val arCamera = frame.camera
        if (arCamera.trackingState != TrackingState.TRACKING) return
        val projection = FloatArray(16)
        val viewMatrix = FloatArray(16)
        arCamera.getProjectionMatrix(projection, 0, NEAR_PLANE, FAR_PLANE)
        arCamera.getViewMatrix(viewMatrix, 0)

        val cameraModelMatrix = FloatArray(16)
        Matrix.invertM(cameraModelMatrix, 0, viewMatrix, 0)

        val projectionInv = FloatArray(16)
        if (!Matrix.invertM(projectionInv, 0, projection, 0)) {
            Matrix.setIdentityM(projectionInv, 0)
        }

        val projectionD = DoubleArray(16) { i -> projection[i].toDouble() }
        val projectionInvD = DoubleArray(16) { i -> projectionInv[i].toDouble() }
        val cameraModelD = DoubleArray(16) { i -> cameraModelMatrix[i].toDouble() }

        camera.setCustomProjectionCompat(
            projection = projectionD,
            projectionInverse = projectionInvD,
            near = NEAR_PLANE.toDouble(),
            far = FAR_PLANE.toDouble(),
            viewportWidth = surfaceView.width,
            viewportHeight = surfaceView.height,
        )
        camera.setModelMatrixCompat(cameraModelD)
    }

    private fun updateModelTransform() {
        val model = testModel ?: return
        val transformManager = model.engine.transformManager
        val instance = transformManager.getInstance(model.asset.root)
        if (instance == 0) return

        val modelMatrix = FloatArray(16)
        val rootPose = modelRootPose
        if (rootPose != null) {
            rootPose.toArCorePose().toMatrix(modelMatrix, 0)
        } else {
            val targetAnchor = anchor ?: return
            targetAnchor.pose.toMatrix(modelMatrix, 0)
        }
        Matrix.scaleM(modelMatrix, 0, MODEL_SCALE, MODEL_SCALE, MODEL_SCALE)
        transformManager.setTransform(instance, modelMatrix)
    }

    private fun addDirectionalLight() {
        val sunlight = com.google.android.filament.EntityManager.get().create()
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(1.0f, 1.0f, 1.0f)
            .intensity(50000.0f)
            .direction(0.0f, -1.0f, -0.2f)
            .castShadows(true)
            .build(engine, sunlight)
        scene.addEntity(sunlight)
    }

    private fun shouldRenderFrame(frameTimeNanos: Long): Boolean {
        val lastRender = lastRenderTimeNs
        if (lastRender == 0L) return true
        val delta = frameTimeNanos - lastRender
        return delta + FRAME_INTERVAL_TOLERANCE_NS >= TARGET_FRAME_INTERVAL_NS
    }

    private fun recordFrameTiming(frameTimeNanos: Long) {
        val lastRender = lastRenderTimeNs
        if (lastRender > 0L) {
            val interval = frameTimeNanos - lastRender
            smoothedFrameIntervalNs = smoothedFrameIntervalNs?.let { previous ->
                previous + FRAME_INTERVAL_SMOOTHING * (interval - previous)
            } ?: interval.toDouble()
            val fps = smoothedFrameIntervalNs?.let { intervalNs -> SECONDS_IN_NANOS / intervalNs }
            if (fps != null) {
                renderRateListener?.invoke(fps)
                maybeLogFps(fps)
            }
        }
        lastRenderTimeNs = frameTimeNanos
    }

    private fun maybeLogFps(fps: Double) {
        val now = System.nanoTime()
        if (now - lastFpsLogTimestampNs < FPS_LOG_INTERVAL_NS) return
        lastFpsLogTimestampNs = now
        Log.d(TAG, "Render FPS (smoothed): ${"%.1f".format(fps)}")
    }

    companion object {
        private const val TAG = "ARSceneRenderer"
        private const val DEFAULT_Z_OFFSET_METERS = 1.5f
        private const val MODEL_SCALE = 0.25f
        private const val NEAR_PLANE = 0.1f
        private const val FAR_PLANE = 100f
        private const val TARGET_FPS = 30
        private val TARGET_FRAME_INTERVAL_NS = TimeUnit.SECONDS.toNanos(1) / TARGET_FPS
        private val FRAME_INTERVAL_TOLERANCE_NS = TimeUnit.MILLISECONDS.toNanos(2)
        private const val FRAME_INTERVAL_SMOOTHING = 0.1
        private const val SECONDS_IN_NANOS = 1_000_000_000.0
        private val FPS_LOG_INTERVAL_NS = TimeUnit.SECONDS.toNanos(1)
    }
}

// --- Filament API compatibility (different versions expose different signatures) ---
private fun Camera.setCustomProjectionCompat(
    projection: DoubleArray,
    projectionInverse: DoubleArray,
    near: Double,
    far: Double,
    viewportWidth: Int,
    viewportHeight: Int,
) {
    val intType = Int::class.javaPrimitiveType
    val dblArrType = DoubleArray::class.java

    for (m in javaClass.methods) {
        if (m.name != "setCustomProjection") continue
        val p = m.parameterTypes

        val ok = runCatching {
            when (p.size) {
                // (double[] proj, int offset, double near, double far)
                4 -> when {
                    p[0] == dblArrType && p[1] == intType -> {
                        m.invoke(this, projection, 0, near, far); true
                    }
                    // (double[] proj, double[] invProj, double near, double far)
                    p[0] == dblArrType && p[1] == dblArrType -> {
                        m.invoke(this, projection, projectionInverse, near, far); true
                    }
                    else -> false
                }

                // (double[] proj, double[] invProj, int offset, double near, double far)
                5 -> if (p[0] == dblArrType && p[1] == dblArrType && p[2] == intType) {
                    m.invoke(this, projection, projectionInverse, 0, near, far); true
                } else false

                // (double[] proj, int offset, int w, int h, double near, double far)
                6 -> when {
                    p[0] == dblArrType && p[1] == intType && p[2] == intType && p[3] == intType -> {
                        m.invoke(this, projection, 0, viewportWidth, viewportHeight, near, far); true
                    }
                    // (double[] proj, double[] invProj, int w, int h, double near, double far)
                    p[0] == dblArrType && p[1] == dblArrType && p[2] == intType && p[3] == intType -> {
                        m.invoke(this, projection, projectionInverse, viewportWidth, viewportHeight, near, far); true
                    }
                    else -> false
                }

                // (double[] proj, double[] invProj, int offset, int w, int h, double near, double far)
                7 -> if (
                    p[0] == dblArrType &&
                    p[1] == dblArrType &&
                    p[2] == intType &&
                    p[3] == intType &&
                    p[4] == intType
                ) {
                    m.invoke(this, projection, projectionInverse, 0, viewportWidth, viewportHeight, near, far); true
                } else false

                else -> false
            }
        }.getOrDefault(false)

        if (ok) return
    }
}

private fun Camera.setModelMatrixCompat(modelMatrix: DoubleArray) {
    val intType = Int::class.javaPrimitiveType
    val dblArrType = DoubleArray::class.java

    for (m in javaClass.methods) {
        if (m.name != "setModelMatrix") continue
        val p = m.parameterTypes

        val ok = runCatching {
            when (p.size) {
                // setModelMatrix(double[] m)
                1 -> if (p[0] == dblArrType) { m.invoke(this, modelMatrix); true } else false
                // setModelMatrix(double[] m, int offset)
                2 -> if (p[0] == dblArrType && p[1] == intType) { m.invoke(this, modelMatrix, 0); true } else false
                else -> false
            }
        }.getOrDefault(false)

        if (ok) return
    }
}