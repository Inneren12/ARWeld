package com.example.arweld.ui.ar

import com.example.arweld.core.ar.alignment.DefaultDriftMonitorFactory
import com.example.arweld.core.ar.api.ArCaptureServiceFactory
import com.example.arweld.core.ar.api.ArSessionManager
import com.example.arweld.core.ar.api.ArSessionManagerFactory
import com.example.arweld.core.ar.api.DriftMonitorFactory
import com.example.arweld.core.ar.api.MarkerDetectorFactory
import com.example.arweld.core.ar.api.MarkerPoseEstimatorFactory
import com.example.arweld.core.ar.api.MultiMarkerPoseRefinerFactory
import com.example.arweld.core.ar.capture.SurfaceViewArCaptureServiceFactory
import com.example.arweld.core.ar.pose.DefaultMarkerPoseEstimatorFactory
import com.example.arweld.core.ar.pose.DefaultMultiMarkerPoseRefinerFactory
import com.example.arweld.di.ArCoreModule
import com.example.arweld.feature.arview.arcore.ArViewControllerFactory
import com.example.arweld.feature.arview.marker.SimulatedMarkerDetector
import com.example.arweld.di.ArViewControllerFactoryImpl
import com.google.ar.core.Session
import dagger.Module
import dagger.Provides
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ArCoreModule::class],
)
object ArCoreTestModule {

    private const val ARCORE_DISABLED_MESSAGE = "ARCore disabled for instrumentation tests"

    @Provides
    @Singleton
    fun provideSessionManagerFactory(): ArSessionManagerFactory = ArSessionManagerFactory { _ ->
        FakeArSessionManager()
    }

    @Provides
    @Singleton
    fun provideMarkerDetectorFactory(): MarkerDetectorFactory = MarkerDetectorFactory { _ ->
        SimulatedMarkerDetector()
    }

    @Provides
    @Singleton
    fun provideCaptureServiceFactory(): ArCaptureServiceFactory = SurfaceViewArCaptureServiceFactory()

    @Provides
    @Singleton
    fun provideMarkerPoseEstimatorFactory(): MarkerPoseEstimatorFactory = DefaultMarkerPoseEstimatorFactory()

    @Provides
    @Singleton
    fun provideMultiMarkerPoseRefinerFactory(): MultiMarkerPoseRefinerFactory = DefaultMultiMarkerPoseRefinerFactory()

    @Provides
    @Singleton
    fun provideDriftMonitorFactory(): DriftMonitorFactory = DefaultDriftMonitorFactory()

    @Provides
    @Singleton
    fun provideArViewControllerFactory(
        impl: ArViewControllerFactoryImpl,
    ): ArViewControllerFactory = impl

    private class FakeArSessionManager : ArSessionManager {
        override val session: Session? = null

        override fun onResume(displayRotation: Int, viewportWidth: Int, viewportHeight: Int): String? {
            return ARCORE_DISABLED_MESSAGE
        }

        override fun onPause() = Unit

        override fun onDestroy() = Unit
    }
}
