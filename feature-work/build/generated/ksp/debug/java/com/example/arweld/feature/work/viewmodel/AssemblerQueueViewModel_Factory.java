package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.work.WorkRepository;
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase;
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
public final class AssemblerQueueViewModel_Factory implements Factory<AssemblerQueueViewModel> {
  private final Provider<WorkRepository> workRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ClaimWorkUseCase> claimWorkUseCaseProvider;

  public AssemblerQueueViewModel_Factory(Provider<WorkRepository> workRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ClaimWorkUseCase> claimWorkUseCaseProvider) {
    this.workRepositoryProvider = workRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.claimWorkUseCaseProvider = claimWorkUseCaseProvider;
  }

  @Override
  public AssemblerQueueViewModel get() {
    return newInstance(workRepositoryProvider.get(), authRepositoryProvider.get(), claimWorkUseCaseProvider.get());
  }

  public static AssemblerQueueViewModel_Factory create(
      javax.inject.Provider<WorkRepository> workRepositoryProvider,
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<ClaimWorkUseCase> claimWorkUseCaseProvider) {
    return new AssemblerQueueViewModel_Factory(Providers.asDaggerProvider(workRepositoryProvider), Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(claimWorkUseCaseProvider));
  }

  public static AssemblerQueueViewModel_Factory create(
      Provider<WorkRepository> workRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ClaimWorkUseCase> claimWorkUseCaseProvider) {
    return new AssemblerQueueViewModel_Factory(workRepositoryProvider, authRepositoryProvider, claimWorkUseCaseProvider);
  }

  public static AssemblerQueueViewModel newInstance(WorkRepository workRepository,
      AuthRepository authRepository, ClaimWorkUseCase claimWorkUseCase) {
    return new AssemblerQueueViewModel(workRepository, authRepository, claimWorkUseCase);
  }
}
