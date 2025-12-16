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
import com.google.ar.core.TrackingState

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
    private val camera = engine.createCamera()
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

    private var rendering = false
    private val choreographer = Choreographer.getInstance()
    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            renderFrame()
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

    fun onResume() {
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
        swapChain = engine.createSwapChain(holder.surface)
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

    private fun renderFrame() {
        val swapChain = swapChain ?: return
        val session = sessionManager.session ?: return
        try {
            val frame = session.update()
            notifyFrameListeners(frame)
            ensureAnchor(frame)
            updateCamera(frame)
            updateModelTransform()
            renderer.render(swapChain, view)
        } catch (error: Exception) {
            Log.w(TAG, "Failed to render AR frame", error)
        }
    }

    private fun notifyFrameListeners(frame: Frame) {
        try {
            frameListener?.invoke(frame)
        } catch (error: Exception) {
            Log.w(TAG, "Frame listener failed", error)
        }
    }

    private fun ensureAnchor(frame: Frame) {
        if (anchor != null || testModel == null) return
        if (frame.camera.trackingState != TrackingState.TRACKING) return
        val forwardPose = frame.camera.pose.compose(Pose.makeTranslation(0f, 0f, -DEFAULT_Z_OFFSET_METERS))
        anchor = frame.session.createAnchor(forwardPose)
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

        camera.setCustomProjection(projection, NEAR_PLANE.toDouble(), FAR_PLANE.toDouble())
        camera.setModelMatrix(cameraModelMatrix)
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

    companion object {
        private const val TAG = "ARSceneRenderer"
        private const val DEFAULT_Z_OFFSET_METERS = 1.5f
        private const val MODEL_SCALE = 0.25f
        private const val NEAR_PLANE = 0.1f
        private const val FAR_PLANE = 100f
    }
}
