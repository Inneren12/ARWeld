package com.example.arweld.feature.drawingimport.preprocess

import java.io.File

data class PageDetectInput(
    val rawImageFile: File,
    val params: PageDetectParamsV1 = PageDetectParamsV1(),
)
