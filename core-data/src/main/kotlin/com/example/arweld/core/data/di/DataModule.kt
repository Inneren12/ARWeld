package com.example.arweld.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.repository.EventRepositoryImpl
import com.example.arweld.core.data.repository.EvidenceRepositoryImpl
import com.example.arweld.core.data.repository.WorkItemRepository
import com.example.arweld.core.data.repository.WorkItemRepositoryImpl
import com.example.arweld.core.data.work.WorkRepositoryImpl
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.work.WorkRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database and repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "arweld.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWorkItemDao(database: AppDatabase): WorkItemDao {
        return database.workItemDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: AppDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideEvidenceDao(database: AppDatabase): EvidenceDao {
        return database.evidenceDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: AppDatabase): SyncQueueDao {
        return database.syncQueueDao()
    }
}

/**
 * Binds repository interfaces to their implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWorkItemRepository(
        impl: WorkItemRepositoryImpl
    ): WorkItemRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindEvidenceRepository(
        impl: EvidenceRepositoryImpl
    ): EvidenceRepository

    @Binds
    @Singleton
    abstract fun bindWorkRepository(
        impl: WorkRepositoryImpl
    ): WorkRepository
}
