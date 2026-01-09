package com.example.arweld.di

import android.content.Context
import com.example.arweld.feature.scanner.camera.BarcodeDecoder
import com.example.arweld.feature.scanner.camera.CameraScannerEngine
import com.example.arweld.feature.scanner.camera.MlKitBarcodeDecoder
import com.example.arweld.feature.scanner.camera.ScannerEngine
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ScannerModule {

    @Provides
    fun provideBarcodeDecoder(): BarcodeDecoder = MlKitBarcodeDecoder()

    @Provides
    fun provideScannerEngine(
        @ApplicationContext context: Context,
        barcodeDecoder: BarcodeDecoder,
    ): ScannerEngine = CameraScannerEngine(context, barcodeDecoder)
}
