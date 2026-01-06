package com.example.arweld.di

import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.logging.CrashReporter
import com.example.arweld.logging.NoOpCrashReporter
import com.example.arweld.logging.TimberAppLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggingModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(noOpCrashReporter: NoOpCrashReporter): CrashReporter

    companion object {
        @Provides
        @Singleton
        fun provideAppLogger(crashReporter: CrashReporter): AppLogger = TimberAppLogger(crashReporter)
    }
}
