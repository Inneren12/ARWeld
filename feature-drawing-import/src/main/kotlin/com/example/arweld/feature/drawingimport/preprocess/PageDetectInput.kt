package com.example.arweld.feature.drawingimport.preprocess

import java.io.File

data class PageDetectInput(
    val rawImageFile: File,
    val maxSide: Int = 1024,
    val maxPixels: Int = DrawingImportGuardrailsV1.MAX_DECODE_PIXELS,
)
