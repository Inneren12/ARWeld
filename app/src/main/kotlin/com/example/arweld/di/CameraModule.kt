package com.example.arweld.di

import com.example.arweld.camera.CameraXPhotoCaptureService
import com.example.arweld.feature.work.camera.PhotoCaptureService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraModule {

    @Binds
    @Singleton
    abstract fun bindPhotoCaptureService(
        impl: CameraXPhotoCaptureService,
    ): PhotoCaptureService
}
