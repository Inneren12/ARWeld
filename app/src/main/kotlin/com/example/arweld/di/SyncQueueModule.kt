package com.example.arweld.di

import com.example.arweld.core.domain.sync.SyncQueueProcessor
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.sync.SyncQueueWorkHandler
import com.example.arweld.core.domain.sync.SyncQueueWriter
import com.example.arweld.sync.NoOpSyncQueueWorkHandler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncQueueModule {

    @Binds
    @Singleton
    abstract fun bindSyncQueueWorkHandler(
        impl: NoOpSyncQueueWorkHandler,
    ): SyncQueueWorkHandler

    companion object {
        @Provides
        @Singleton
        fun provideSyncQueueProcessor(
            repository: SyncQueueRepository,
            handler: SyncQueueWorkHandler,
        ): SyncQueueProcessor {
            return SyncQueueProcessor(repository, handler)
        }

        @Provides
        @Singleton
        fun provideSyncQueueWriter(
            repository: SyncQueueRepository,
        ): SyncQueueWriter {
            return SyncQueueWriter(repository)
        }
    }
}
