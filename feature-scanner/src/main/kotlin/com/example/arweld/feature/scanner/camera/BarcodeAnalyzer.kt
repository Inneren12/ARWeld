package com.example.arweld.feature.scanner.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onCodeDetected: (String) -> Unit,
    private val decoder: BarcodeDecoder = MlKitBarcodeDecoder(),
    private val deduplicationIntervalMillis: Long = 1500L,
    timeProvider: ElapsedRealtimeProvider = SystemElapsedRealtimeProvider,
) : ImageAnalysis.Analyzer {

    private val codeDeduper = CodeDeduper(
        timeProvider = timeProvider,
        deduplicationIntervalMillis = deduplicationIntervalMillis,
    )

    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        decoder.decode(
            inputImage = inputImage,
            onResult = { values ->
                val firstValue = values.firstOrNull { it.isNotBlank() }
                if (!firstValue.isNullOrBlank() && codeDeduper.shouldEmit(firstValue)) {
                    onCodeDetected(firstValue)
                }
            },
            onFailure = { _ -> },
            onComplete = { image.close() },
        )
    }
}
