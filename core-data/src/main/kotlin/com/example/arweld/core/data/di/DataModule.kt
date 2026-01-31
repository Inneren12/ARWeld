package com.example.arweld.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.arweld.core.data.db.AppDatabase
import com.example.arweld.core.data.db.MIGRATION_1_2
import com.example.arweld.core.data.db.MIGRATION_2_3
import com.example.arweld.core.data.db.MIGRATION_3_4
import com.example.arweld.core.data.db.MIGRATION_4_5
import com.example.arweld.core.data.db.MIGRATION_5_6
import com.example.arweld.core.data.db.dao.EventDao
import com.example.arweld.core.data.db.dao.EvidenceDao
import com.example.arweld.core.data.db.dao.SyncQueueDao
import com.example.arweld.core.data.db.dao.UserDao
import com.example.arweld.core.data.db.dao.WorkItemDao
import com.example.arweld.core.data.auth.AuthRepositoryImpl
import com.example.arweld.core.data.drawing2d.CurrentDrawingRepositoryImpl
import com.example.arweld.core.data.system.AndroidDeviceInfoProvider
import com.example.arweld.core.data.system.DefaultTimeProvider
import com.example.arweld.core.data.work.ClaimWorkUseCaseImpl
import com.example.arweld.core.data.work.MarkReadyForQcUseCaseImpl
import com.example.arweld.core.data.work.ResolveWorkItemByCodeUseCaseImpl
import com.example.arweld.core.data.work.StartWorkUseCaseImpl
import com.example.arweld.core.data.work.WorkRepositoryImpl
import com.example.arweld.core.data.repository.EventRepositoryImpl
import com.example.arweld.core.data.repository.EvidenceRepositoryImpl
import com.example.arweld.core.data.repository.WorkItemRepository
import com.example.arweld.core.data.repository.WorkItemRepositoryImpl
import com.example.arweld.core.data.sync.SyncQueueRepositoryImpl
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.drawing2d.Drawing2DRepository
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.sync.SyncQueueRepository
import com.example.arweld.core.domain.system.DeviceInfoProvider
import com.example.arweld.core.domain.system.TimeProvider
import com.example.arweld.core.domain.work.ResolveWorkItemByCodeUseCase
import com.example.arweld.core.domain.work.WorkRepository
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase
import com.example.arweld.core.domain.work.usecase.MarkReadyForQcUseCase
import com.example.arweld.core.domain.work.usecase.FailQcUseCase
import com.example.arweld.core.domain.work.usecase.PassQcUseCase
import com.example.arweld.core.domain.work.usecase.StartQcInspectionUseCase
import com.example.arweld.core.domain.work.usecase.StartWorkUseCase
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
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()
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

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = DefaultTimeProvider()

    @Provides
    @Singleton
    fun provideDeviceInfoProvider(): DeviceInfoProvider = AndroidDeviceInfoProvider()

    @Provides
    @Singleton
    fun provideQcEvidencePolicy(
        evidenceRepository: EvidenceRepository,
    ): QcEvidencePolicy = QcEvidencePolicy(evidenceRepository)

    @Provides
    @Singleton
    fun provideStartQcInspectionUseCase(
        eventRepository: EventRepository,
        authRepository: AuthRepository,
        timeProvider: TimeProvider,
        deviceInfoProvider: DeviceInfoProvider,
    ): StartQcInspectionUseCase {
        return StartQcInspectionUseCase(
            eventRepository = eventRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
        )
    }

    @Provides
    @Singleton
    fun providePassQcUseCase(
        eventRepository: EventRepository,
        evidenceRepository: EvidenceRepository,
        authRepository: AuthRepository,
        timeProvider: TimeProvider,
        deviceInfoProvider: DeviceInfoProvider,
        qcEvidencePolicy: QcEvidencePolicy,
    ): PassQcUseCase {
        return PassQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )
    }

    @Provides
    @Singleton
    fun provideFailQcUseCase(
        eventRepository: EventRepository,
        evidenceRepository: EvidenceRepository,
        authRepository: AuthRepository,
        timeProvider: TimeProvider,
        deviceInfoProvider: DeviceInfoProvider,
        qcEvidencePolicy: QcEvidencePolicy,
    ): FailQcUseCase {
        return FailQcUseCase(
            eventRepository = eventRepository,
            evidenceRepository = evidenceRepository,
            authRepository = authRepository,
            timeProvider = timeProvider,
            deviceInfoProvider = deviceInfoProvider,
            qcEvidencePolicy = qcEvidencePolicy,
        )
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
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncQueueRepository(
        impl: SyncQueueRepositoryImpl
    ): SyncQueueRepository

    @Binds
    @Singleton
    abstract fun bindWorkRepository(
        impl: WorkRepositoryImpl
    ): WorkRepository

    @Binds
    @Singleton
    abstract fun bindDrawing2DRepository(
        impl: CurrentDrawingRepositoryImpl
    ): Drawing2DRepository

    @Binds
    @Singleton
    abstract fun bindResolveWorkItemByCodeUseCase(
        impl: ResolveWorkItemByCodeUseCaseImpl
    ): ResolveWorkItemByCodeUseCase

    @Binds
    @Singleton
    abstract fun bindReportProvider(
        impl: com.example.arweld.core.data.reporting.ReportExportServiceProvider
    ): com.example.arweld.core.data.reporting.ReportProvider

    @Binds
    @Singleton
    abstract fun bindClaimWorkUseCase(
        impl: ClaimWorkUseCaseImpl
    ): ClaimWorkUseCase

    @Binds
    @Singleton
    abstract fun bindStartWorkUseCase(
        impl: StartWorkUseCaseImpl
    ): StartWorkUseCase

    @Binds
    @Singleton
    abstract fun bindMarkReadyForQcUseCase(
        impl: MarkReadyForQcUseCaseImpl
    ): MarkReadyForQcUseCase
}
