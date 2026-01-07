package com.example.arweld.core.data.di;

import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.event.EventRepository;
import com.example.arweld.core.domain.evidence.EvidenceRepository;
import com.example.arweld.core.domain.policy.QcEvidencePolicy;
import com.example.arweld.core.domain.system.DeviceInfoProvider;
import com.example.arweld.core.domain.system.TimeProvider;
import com.example.arweld.core.domain.work.usecase.FailQcUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideFailQcUseCaseFactory implements Factory<FailQcUseCase> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<EvidenceRepository> evidenceRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<TimeProvider> timeProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  private final Provider<QcEvidencePolicy> qcEvidencePolicyProvider;

  public DataModule_ProvideFailQcUseCaseFactory(Provider<EventRepository> eventRepositoryProvider,
      Provider<EvidenceRepository> evidenceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<TimeProvider> timeProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.evidenceRepositoryProvider = evidenceRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.timeProvider = timeProvider;
    this.deviceInfoProvider = deviceInfoProvider;
    this.qcEvidencePolicyProvider = qcEvidencePolicyProvider;
  }

  @Override
  public FailQcUseCase get() {
    return provideFailQcUseCase(eventRepositoryProvider.get(), evidenceRepositoryProvider.get(), authRepositoryProvider.get(), timeProvider.get(), deviceInfoProvider.get(), qcEvidencePolicyProvider.get());
  }

  public static DataModule_ProvideFailQcUseCaseFactory create(
      javax.inject.Provider<EventRepository> eventRepositoryProvider,
      javax.inject.Provider<EvidenceRepository> evidenceRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<TimeProvider> timeProvider,
      javax.inject.Provider<DeviceInfoProvider> deviceInfoProvider,
      javax.inject.Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new DataModule_ProvideFailQcUseCaseFactory(Providers.asDaggerProvider(eventRepositoryProvider), Providers.asDaggerProvider(evidenceRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(timeProvider), Providers.asDaggerProvider(deviceInfoProvider), Providers.asDaggerProvider(qcEvidencePolicyProvider));
  }

  public static DataModule_ProvideFailQcUseCaseFactory create(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<EvidenceRepository> evidenceRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<TimeProvider> timeProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new DataModule_ProvideFailQcUseCaseFactory(eventRepositoryProvider, evidenceRepositoryProvider, authRepositoryProvider, timeProvider, deviceInfoProvider, qcEvidencePolicyProvider);
  }

  public static FailQcUseCase provideFailQcUseCase(EventRepository eventRepository,
      EvidenceRepository evidenceRepository, AuthRepository authRepository,
      TimeProvider timeProvider, DeviceInfoProvider deviceInfoProvider,
      QcEvidencePolicy qcEvidencePolicy) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideFailQcUseCase(eventRepository, evidenceRepository, authRepository, timeProvider, deviceInfoProvider, qcEvidencePolicy));
  }
}
