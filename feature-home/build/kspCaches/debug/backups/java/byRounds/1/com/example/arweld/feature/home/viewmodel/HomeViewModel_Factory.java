package com.example.arweld.feature.home.viewmodel;

import com.example.arweld.core.data.repository.WorkItemRepository;
import com.example.arweld.core.domain.auth.AuthRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<WorkItemRepository> workItemRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  public HomeViewModel_Factory(Provider<WorkItemRepository> workItemRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    this.workItemRepositoryProvider = workItemRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(workItemRepositoryProvider.get(), authRepositoryProvider.get());
  }

  public static HomeViewModel_Factory create(
      javax.inject.Provider<WorkItemRepository> workItemRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider) {
    return new HomeViewModel_Factory(Providers.asDaggerProvider(workItemRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider));
  }

  public static HomeViewModel_Factory create(
      Provider<WorkItemRepository> workItemRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider) {
    return new HomeViewModel_Factory(workItemRepositoryProvider, authRepositoryProvider);
  }

  public static HomeViewModel newInstance(WorkItemRepository workItemRepository,
      AuthRepository authRepository) {
    return new HomeViewModel(workItemRepository, authRepository);
  }
}
