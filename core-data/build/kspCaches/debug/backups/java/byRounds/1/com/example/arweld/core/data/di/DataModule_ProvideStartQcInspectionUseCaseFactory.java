package com.example.arweld.core.data.di;

import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.event.EventRepository;
import com.example.arweld.core.domain.system.DeviceInfoProvider;
import com.example.arweld.core.domain.system.TimeProvider;
import com.example.arweld.core.domain.work.usecase.StartQcInspectionUseCase;
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
public final class DataModule_ProvideStartQcInspectionUseCaseFactory implements Factory<StartQcInspectionUseCase> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<TimeProvider> timeProvider;

  private final Provider<DeviceInfoProvider> deviceInfoProvider;

  public DataModule_ProvideStartQcInspectionUseCaseFactory(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<TimeProvider> timeProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.timeProvider = timeProvider;
    this.deviceInfoProvider = deviceInfoProvider;
  }

  @Override
  public StartQcInspectionUseCase get() {
    return provideStartQcInspectionUseCase(eventRepositoryProvider.get(), authRepositoryProvider.get(), timeProvider.get(), deviceInfoProvider.get());
  }

  public static DataModule_ProvideStartQcInspectionUseCaseFactory create(
      javax.inject.Provider<EventRepository> eventRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<TimeProvider> timeProvider,
      javax.inject.Provider<DeviceInfoProvider> deviceInfoProvider) {
    return new DataModule_ProvideStartQcInspectionUseCaseFactory(Providers.asDaggerProvider(eventRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(timeProvider), Providers.asDaggerProvider(deviceInfoProvider));
  }

  public static DataModule_ProvideStartQcInspectionUseCaseFactory create(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<TimeProvider> timeProvider,
      Provider<DeviceInfoProvider> deviceInfoProvider) {
    return new DataModule_ProvideStartQcInspectionUseCaseFactory(eventRepositoryProvider, authRepositoryProvider, timeProvider, deviceInfoProvider);
  }

  public static StartQcInspectionUseCase provideStartQcInspectionUseCase(
      EventRepository eventRepository, AuthRepository authRepository, TimeProvider timeProvider,
      DeviceInfoProvider deviceInfoProvider) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideStartQcInspectionUseCase(eventRepository, authRepository, timeProvider, deviceInfoProvider));
  }
}
