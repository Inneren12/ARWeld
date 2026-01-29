package com.example.arweld.di

import android.content.Context
import com.example.arweld.core.ar.api.ArCaptureServiceFactory
import com.example.arweld.core.ar.api.ArSessionManagerFactory
import com.example.arweld.core.ar.api.DriftMonitorFactory
import com.example.arweld.core.ar.api.MarkerDetectorFactory
import com.example.arweld.core.ar.api.MarkerPoseEstimatorFactory
import com.example.arweld.core.ar.api.MultiMarkerPoseRefinerFactory
import com.example.arweld.core.domain.diagnostics.DeviceHealthProvider
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.feature.arview.alignment.AlignmentEventLogger
import com.example.arweld.feature.arview.alignment.RigidTransformSolver
import com.example.arweld.feature.arview.arcore.ARViewController
import com.example.arweld.feature.arview.arcore.ArViewControllerFactory
import com.example.arweld.feature.arview.render.AndroidFilamentModelLoader
import com.example.arweld.feature.arview.zone.ZoneAligner
import com.example.arweld.feature.arview.zone.ZoneRegistry
import javax.inject.Inject

class ArViewControllerFactoryImpl @Inject constructor(
    private val sessionManagerFactory: ArSessionManagerFactory,
    private val markerDetectorFactory: MarkerDetectorFactory,
    private val captureServiceFactory: ArCaptureServiceFactory,
    private val markerPoseEstimatorFactory: MarkerPoseEstimatorFactory,
    private val multiMarkerPoseRefinerFactory: MultiMarkerPoseRefinerFactory,
    private val driftMonitorFactory: DriftMonitorFactory,
) : ArViewControllerFactory {
    override fun create(
        context: Context,
        alignmentEventLogger: AlignmentEventLogger,
        workItemId: String?,
        diagnosticsRecorder: DiagnosticsRecorder?,
        deviceHealthProvider: DeviceHealthProvider?,
    ): ARViewController {
        val appContext = context.applicationContext
        val modelLoader = AndroidFilamentModelLoader(appContext)
        val zoneRegistry = ZoneRegistry.fromAssets(appContext.assets)
        val zoneAligner = ZoneAligner(zoneRegistry)

        return ARViewController(
            context = context,
            alignmentEventLogger = alignmentEventLogger,
            workItemId = workItemId,
            sessionManager = sessionManagerFactory.create(appContext),
            modelLoader = modelLoader,
            markerDetectorFactory = markerDetectorFactory,
            markerPoseEstimator = markerPoseEstimatorFactory.create(),
            multiMarkerPoseRefiner = multiMarkerPoseRefinerFactory.create(),
            zoneRegistry = zoneRegistry,
            zoneAligner = zoneAligner,
            rigidTransformSolver = RigidTransformSolver(),
            driftMonitor = driftMonitorFactory.create(),
            captureServiceFactory = captureServiceFactory,
            diagnosticsRecorder = diagnosticsRecorder,
            deviceHealthProvider = deviceHealthProvider,
        )
    }
}
