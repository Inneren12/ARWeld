package com.example.arweld.core.data.work;

import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.event.EventRepository;
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
public final class StartWorkUseCaseImpl_Factory implements Factory<StartWorkUseCaseImpl> {
  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public StartWorkUseCaseImpl_Factory(Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public StartWorkUseCaseImpl get() {
    return newInstance(eventRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static StartWorkUseCaseImpl_Factory create(
      javax.inject.Provider<EventRepository> eventRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new StartWorkUseCaseImpl_Factory(Providers.asDaggerProvider(eventRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static StartWorkUseCaseImpl_Factory create(
      Provider<EventRepository> eventRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new StartWorkUseCaseImpl_Factory(eventRepositoryProvider, authRepositoryProvider);
  }

  public static StartWorkUseCaseImpl newInstance(EventRepository eventRepository,
      AuthRepository authRepository) {
    return new StartWorkUseCaseImpl(eventRepository, authRepository);
  }
}
