package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.auth.AuthRepository;
import com.example.arweld.core.domain.work.WorkRepository;
import com.example.arweld.core.domain.work.usecase.ClaimWorkUseCase;
import com.example.arweld.core.domain.work.usecase.MarkReadyForQcUseCase;
import com.example.arweld.core.domain.work.usecase.StartWorkUseCase;
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
public final class WorkItemSummaryViewModel_Factory implements Factory<WorkItemSummaryViewModel> {
  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<WorkRepository> workRepositoryProvider;

  private final Provider<ClaimWorkUseCase> claimWorkUseCaseProvider;

  private final Provider<StartWorkUseCase> startWorkUseCaseProvider;

  private final Provider<MarkReadyForQcUseCase> markReadyForQcUseCaseProvider;

  public WorkItemSummaryViewModel_Factory(Provider<AuthRepository> authRepositoryProvider,
      Provider<WorkRepository> workRepositoryProvider,
      Provider<ClaimWorkUseCase> claimWorkUseCaseProvider,
      Provider<StartWorkUseCase> startWorkUseCaseProvider,
      Provider<MarkReadyForQcUseCase> markReadyForQcUseCaseProvider) {
    this.authRepositoryProvider = authRepositoryProvider;
    this.workRepositoryProvider = workRepositoryProvider;
    this.claimWorkUseCaseProvider = claimWorkUseCaseProvider;
    this.startWorkUseCaseProvider = startWorkUseCaseProvider;
    this.markReadyForQcUseCaseProvider = markReadyForQcUseCaseProvider;
  }

  @Override
  public WorkItemSummaryViewModel get() {
    return newInstance(authRepositoryProvider.get(), workRepositoryProvider.get(), claimWorkUseCaseProvider.get(), startWorkUseCaseProvider.get(), markReadyForQcUseCaseProvider.get());
  }

  public static WorkItemSummaryViewModel_Factory create(
      javax.inject.Provider<AuthRepository> authRepositoryProvider,
      javax.inject.Provider<WorkRepository> workRepositoryProvider,
      javax.inject.Provider<ClaimWorkUseCase> claimWorkUseCaseProvider,
      javax.inject.Provider<StartWorkUseCase> startWorkUseCaseProvider,
      javax.inject.Provider<MarkReadyForQcUseCase> markReadyForQcUseCaseProvider) {
    return new WorkItemSummaryViewModel_Factory(Providers.asDaggerProvider(authRepositoryProvider), Providers.asDaggerProvider(workRepositoryProvider), Providers.asDaggerProvider(claimWorkUseCaseProvider), Providers.asDaggerProvider(startWorkUseCaseProvider), Providers.asDaggerProvider(markReadyForQcUseCaseProvider));
  }

  public static WorkItemSummaryViewModel_Factory create(
      Provider<AuthRepository> authRepositoryProvider,
      Provider<WorkRepository> workRepositoryProvider,
      Provider<ClaimWorkUseCase> claimWorkUseCaseProvider,
      Provider<StartWorkUseCase> startWorkUseCaseProvider,
      Provider<MarkReadyForQcUseCase> markReadyForQcUseCaseProvider) {
    return new WorkItemSummaryViewModel_Factory(authRepositoryProvider, workRepositoryProvider, claimWorkUseCaseProvider, startWorkUseCaseProvider, markReadyForQcUseCaseProvider);
  }

  public static WorkItemSummaryViewModel newInstance(AuthRepository authRepository,
      WorkRepository workRepository, ClaimWorkUseCase claimWorkUseCase,
      StartWorkUseCase startWorkUseCase, MarkReadyForQcUseCase markReadyForQcUseCase) {
    return new WorkItemSummaryViewModel(authRepository, workRepository, claimWorkUseCase, startWorkUseCase, markReadyForQcUseCase);
  }
}
