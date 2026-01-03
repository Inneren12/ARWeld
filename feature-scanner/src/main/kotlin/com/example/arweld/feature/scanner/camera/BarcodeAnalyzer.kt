package com.example.arweld.feature.scanner.camera

import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val onCodeDetected: (String) -> Unit,
    private val deduplicationIntervalMillis: Long = 1500L,
) : ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    private var lastCode: String? = null
    private var lastTimestamp: Long = 0L

    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
        scanner
            .process(inputImage)
            .addOnSuccessListener { barcodes ->
                val firstValue = barcodes
                    .firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.rawValue

                if (!firstValue.isNullOrBlank() && shouldEmit(firstValue)) {
                    lastCode = firstValue
                    lastTimestamp = SystemClock.elapsedRealtime()
                    onCodeDetected(firstValue)
                }
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    private fun shouldEmit(value: String): Boolean {
        val now = SystemClock.elapsedRealtime()
        return value != lastCode || now - lastTimestamp > deduplicationIntervalMillis
    }
}
