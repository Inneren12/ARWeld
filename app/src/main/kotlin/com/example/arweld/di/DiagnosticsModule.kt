package com.example.arweld.di

import com.example.arweld.core.domain.diagnostics.DeviceHealthProvider
import com.example.arweld.core.domain.diagnostics.DiagnosticsExportService
import com.example.arweld.core.domain.diagnostics.DiagnosticsRecorder
import com.example.arweld.diagnostics.DiagnosticsExportServiceImpl
import com.example.arweld.diagnostics.DiagnosticsRepository
import com.example.arweld.feature.drawingeditor.diagnostics.EditorDiagnosticsLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiagnosticsModule {

    @Binds
    @Singleton
    abstract fun bindDiagnosticsRecorder(repository: DiagnosticsRepository): DiagnosticsRecorder

    @Binds
    @Singleton
    abstract fun bindDeviceHealthProvider(repository: DiagnosticsRepository): DeviceHealthProvider

    @Binds
    @Singleton
    abstract fun bindDiagnosticsExportService(
        impl: DiagnosticsExportServiceImpl,
    ): DiagnosticsExportService

    companion object {
        @Provides
        @Singleton
        fun provideEditorDiagnosticsLogger(
            diagnosticsRecorder: DiagnosticsRecorder,
        ): EditorDiagnosticsLogger {
            return EditorDiagnosticsLogger(diagnosticsRecorder)
        }
    }
}
