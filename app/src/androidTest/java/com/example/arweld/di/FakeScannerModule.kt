package com.example.arweld.di

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.arweld.feature.scanner.camera.BarcodeDecoder
import com.example.arweld.feature.scanner.camera.ScannerEngine
import com.google.mlkit.vision.common.InputImage
import dagger.Module
import dagger.Provides
import dagger.hilt.android.testing.TestInstallIn
import dagger.hilt.components.SingletonComponent

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ScannerModule::class],
)
object FakeScannerModule {

    @Provides
    fun provideBarcodeDecoder(): BarcodeDecoder = FakeBarcodeDecoder()

    @Provides
    fun provideScannerEngine(
        barcodeDecoder: BarcodeDecoder,
    ): ScannerEngine = FakeScannerEngine(barcodeDecoder)
}

private class FakeBarcodeDecoder : BarcodeDecoder {
    override fun decode(
        inputImage: InputImage,
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onComplete: () -> Unit,
    ) {
        onResult(listOf("CODE-123"))
        onComplete()
    }
}

private class FakeScannerEngine(
    private val barcodeDecoder: BarcodeDecoder,
) : ScannerEngine {
    override fun bind(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCodeDetected: (String) -> Unit,
    ) {
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        barcodeDecoder.decode(
            inputImage = inputImage,
            onResult = { values ->
                values.firstOrNull()?.let(onCodeDetected)
            },
            onFailure = { _ -> },
            onComplete = { },
        )
    }

    override fun shutdown() = Unit
}
