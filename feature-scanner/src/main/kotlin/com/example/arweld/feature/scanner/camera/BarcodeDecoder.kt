package com.example.arweld.feature.scanner.camera

import com.google.mlkit.vision.common.InputImage

interface BarcodeDecoder {
    fun decode(
        inputImage: InputImage,
        onResult: (List<String>) -> Unit,
        onFailure: (Exception) -> Unit,
        onComplete: () -> Unit,
    )
}
