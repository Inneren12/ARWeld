package com.example.arweld.di

import com.example.arweld.feature.scanner.camera.ScannerEngine
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScannerEntryPoint {
    fun scannerEngine(): ScannerEngine
}
