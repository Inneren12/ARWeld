package com.example.arweld.di

import com.example.arweld.core.ar.alignment.DefaultDriftMonitorFactory
import com.example.arweld.core.ar.api.ArCaptureServiceFactory
import com.example.arweld.core.ar.api.ArSessionManagerFactory
import com.example.arweld.core.ar.api.DriftMonitorFactory
import com.example.arweld.core.ar.api.MarkerDetectorFactory
import com.example.arweld.core.ar.api.MarkerPoseEstimatorFactory
import com.example.arweld.core.ar.api.MultiMarkerPoseRefinerFactory
import com.example.arweld.core.ar.arcore.DefaultArSessionManagerFactory
import com.example.arweld.core.ar.capture.SurfaceViewArCaptureServiceFactory
import com.example.arweld.core.ar.marker.RealMarkerDetectorFactory
import com.example.arweld.core.ar.pose.DefaultMarkerPoseEstimatorFactory
import com.example.arweld.core.ar.pose.DefaultMultiMarkerPoseRefinerFactory
import com.example.arweld.feature.arview.arcore.ArViewControllerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ArCoreModule {

    @Provides
    @Singleton
    fun provideSessionManagerFactory(): ArSessionManagerFactory = DefaultArSessionManagerFactory()

    @Provides
    @Singleton
    fun provideMarkerDetectorFactory(): MarkerDetectorFactory = RealMarkerDetectorFactory()

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
}
