package com.example.arweld.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.arweld.core.data.db.ArWeldDatabase
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.repository.EventRepository
import com.example.arweld.core.data.repository.EventRepositoryImpl
import com.example.arweld.core.data.repository.EvidenceRepository
import com.example.arweld.core.data.repository.EvidenceRepositoryImpl
import com.example.arweld.core.data.repository.WorkItemRepository
import com.example.arweld.core.data.repository.WorkItemRepositoryImpl
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
    fun provideArWeldDatabase(
        @ApplicationContext context: Context
    ): ArWeldDatabase {
        return Room.databaseBuilder(
            context,
            ArWeldDatabase::class.java,
            "arweld.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWorkItemDao(database: ArWeldDatabase): WorkItemDao {
        return database.workItemDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: ArWeldDatabase): EventDao {
        return database.eventDao()
    }

    @Provides
    @Singleton
    fun provideEvidenceDao(database: ArWeldDatabase): EvidenceDao {
        return database.evidenceDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: ArWeldDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideSyncQueueDao(database: ArWeldDatabase): SyncQueueDao {
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
}
