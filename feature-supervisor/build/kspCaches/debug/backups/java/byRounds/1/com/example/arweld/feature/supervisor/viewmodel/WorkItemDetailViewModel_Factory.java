package com.example.arweld.feature.supervisor.viewmodel;

import androidx.lifecycle.SavedStateHandle;
import com.example.arweld.core.data.db.dao.EvidenceDao;
import com.example.arweld.feature.supervisor.usecase.GetWorkItemDetailUseCase;
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
public final class WorkItemDetailViewModel_Factory implements Factory<WorkItemDetailViewModel> {
  private final Provider<GetWorkItemDetailUseCase> getWorkItemDetailUseCaseProvider;

  private final Provider<EvidenceDao> evidenceDaoProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public WorkItemDetailViewModel_Factory(
      Provider<GetWorkItemDetailUseCase> getWorkItemDetailUseCaseProvider,
      Provider<EvidenceDao> evidenceDaoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.getWorkItemDetailUseCaseProvider = getWorkItemDetailUseCaseProvider;
    this.evidenceDaoProvider = evidenceDaoProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public WorkItemDetailViewModel get() {
    return newInstance(getWorkItemDetailUseCaseProvider.get(), evidenceDaoProvider.get(), savedStateHandleProvider.get());
  }

  public static WorkItemDetailViewModel_Factory create(
      javax.inject.Provider<GetWorkItemDetailUseCase> getWorkItemDetailUseCaseProvider,
      javax.inject.Provider<EvidenceDao> evidenceDaoProvider,
      javax.inject.Provider<SavedStateHandle> savedStateHandleProvider) {
    return new WorkItemDetailViewModel_Factory(Providers.asDaggerProvider(getWorkItemDetailUseCaseProvider), Providers.asDaggerProvider(evidenceDaoProvider), Providers.asDaggerProvider(savedStateHandleProvider));
  }

  public static WorkItemDetailViewModel_Factory create(
      Provider<GetWorkItemDetailUseCase> getWorkItemDetailUseCaseProvider,
      Provider<EvidenceDao> evidenceDaoProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new WorkItemDetailViewModel_Factory(getWorkItemDetailUseCaseProvider, evidenceDaoProvider, savedStateHandleProvider);
  }

  public static WorkItemDetailViewModel newInstance(
      GetWorkItemDetailUseCase getWorkItemDetailUseCase, EvidenceDao evidenceDao,
      SavedStateHandle savedStateHandle) {
    return new WorkItemDetailViewModel(getWorkItemDetailUseCase, evidenceDao, savedStateHandle);
  }
}
