package com.example.arweld.feature.scanner.camera

import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MlKitBarcodeDecoder : BarcodeDecoder {

    private val scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    )

    override fun decode(
        inputImage: InputImage,
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onComplete: () -> Unit,
    ) {
        scanner
            .process(inputImage)
            .addOnSuccessListener { barcodes ->
                val values = barcodes.mapNotNull { barcode ->
                    barcode.rawValue?.takeIf { it.isNotBlank() }
                }
                onResult(values)
            }
            .addOnFailureListener { throwable ->
                onFailure(Exception(throwable))
            }
            .addOnCompleteListener {
                onComplete()
            }
    }
}
