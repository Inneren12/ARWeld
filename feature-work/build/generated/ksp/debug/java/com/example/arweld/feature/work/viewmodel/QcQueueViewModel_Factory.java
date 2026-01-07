package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.work.WorkRepository;
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
public final class QcQueueViewModel_Factory implements Factory<QcQueueViewModel> {
  private final Provider<WorkRepository> workRepositoryProvider;

  public QcQueueViewModel_Factory(Provider<WorkRepository> workRepositoryProvider) {
    this.workRepositoryProvider = workRepositoryProvider;
  }

  @Override
  public QcQueueViewModel get() {
    return newInstance(workRepositoryProvider.get());
  }

  public static QcQueueViewModel_Factory create(
      javax.inject.Provider<WorkRepository> workRepositoryProvider) {
    return new QcQueueViewModel_Factory(Providers.asDaggerProvider(workRepositoryProvider));
  }

  public static QcQueueViewModel_Factory create(Provider<WorkRepository> workRepositoryProvider) {
    return new QcQueueViewModel_Factory(workRepositoryProvider);
  }

  public static QcQueueViewModel newInstance(WorkRepository workRepository) {
    return new QcQueueViewModel(workRepository);
  }
}
