package com.example.arweld.core.data.work;

import com.example.arweld.core.domain.work.WorkRepository;
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
public final class ResolveWorkItemByCodeUseCaseImpl_Factory implements Factory<ResolveWorkItemByCodeUseCaseImpl> {
  private final Provider<WorkRepository> workRepositoryProvider;

  public ResolveWorkItemByCodeUseCaseImpl_Factory(Provider<WorkRepository> workRepositoryProvider) {
    this.workRepositoryProvider = workRepositoryProvider;
  }

  @Override
  public ResolveWorkItemByCodeUseCaseImpl get() {
    return newInstance(workRepositoryProvider.get());
  }

  public static ResolveWorkItemByCodeUseCaseImpl_Factory create(
      javax.inject.Provider<WorkRepository> workRepositoryProvider) {
    return new ResolveWorkItemByCodeUseCaseImpl_Factory(Providers.asDaggerProvider(workRepositoryProvider));
  }

  public static ResolveWorkItemByCodeUseCaseImpl_Factory create(
      Provider<WorkRepository> workRepositoryProvider) {
    return new ResolveWorkItemByCodeUseCaseImpl_Factory(workRepositoryProvider);
  }

  public static ResolveWorkItemByCodeUseCaseImpl newInstance(WorkRepository workRepository) {
    return new ResolveWorkItemByCodeUseCaseImpl(workRepository);
  }
}
