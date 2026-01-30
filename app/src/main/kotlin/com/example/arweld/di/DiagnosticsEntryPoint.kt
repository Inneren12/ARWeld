package com.example.arweld.di

import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DiagnosticsEntryPoint {
    fun diagnosticsRecorder(): DiagnosticsRecorder
}
