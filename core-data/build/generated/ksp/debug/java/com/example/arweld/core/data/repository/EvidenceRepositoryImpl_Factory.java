package com.example.arweld.core.data.repository;

import com.example.arweld.core.data.db.dao.EvidenceDao;
import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.event.EventRepository;
import com.example.arweld.core.domain.system.DeviceInfoProvider;
import com.example.arweld.core.domain.system.TimeProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class EvidenceRepositoryImpl_Factory implements Factory<EvidenceRepositoryImpl> {
  private final Provider<EvidenceDao> evidenceDaoProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<TimeProvider> timeProvider;

  public EvidenceRepositoryImpl_Factory(Provider<EvidenceDao> evidenceDaoProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider, Provider<TimeProvider> timeProvider) {
    this.evidenceDaoProvider = evidenceDaoProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.timeProvider = timeProvider;
  }

  @Override
  public EvidenceRepositoryImpl get() {
    return newInstance(evidenceDaoProvider.get(), eventRepositoryProvider.get(), authRepositoryProvider.get(), deviceInfoProvider.get(), timeProvider.get());
  }

  public static EvidenceRepositoryImpl_Factory create(
      javax.inject.Provider<EvidenceDao> evidenceDaoProvider,
      javax.inject.Provider<EventRepository> eventRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<DeviceInfoProvider> deviceInfoProvider,
      javax.inject.Provider<TimeProvider> timeProvider) {
    return new EvidenceRepositoryImpl_Factory(Providers.asDaggerProvider(evidenceDaoProvider), Providers.asDaggerProvider(eventRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(deviceInfoProvider), Providers.asDaggerProvider(timeProvider));
  }

  public static EvidenceRepositoryImpl_Factory create(Provider<EvidenceDao> evidenceDaoProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider, Provider<TimeProvider> timeProvider) {
    return new EvidenceRepositoryImpl_Factory(evidenceDaoProvider, eventRepositoryProvider, authRepositoryProvider, deviceInfoProvider, timeProvider);
  }

  public static EvidenceRepositoryImpl newInstance(EvidenceDao evidenceDao,
      EventRepository eventRepository, AuthRepository authRepository,
      DeviceInfoProvider deviceInfoProvider, TimeProvider timeProvider) {
    return new EvidenceRepositoryImpl(evidenceDao, eventRepository, authRepository, deviceInfoProvider, timeProvider);
  }
}
