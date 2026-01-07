package com.example.arweld.core.data.repository;

import com.example.arweld.core.data.db.dao.WorkItemDao;
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
public final class WorkItemRepositoryImpl_Factory implements Factory<WorkItemRepositoryImpl> {
  private final Provider<WorkItemDao> workItemDaoProvider;

  public WorkItemRepositoryImpl_Factory(Provider<WorkItemDao> workItemDaoProvider) {
    this.workItemDaoProvider = workItemDaoProvider;
  }

  @Override
  public WorkItemRepositoryImpl get() {
    return newInstance(workItemDaoProvider.get());
  }

  public static WorkItemRepositoryImpl_Factory create(
      javax.inject.Provider<WorkItemDao> workItemDaoProvider) {
    return new WorkItemRepositoryImpl_Factory(Providers.asDaggerProvider(workItemDaoProvider));
  }

  public static WorkItemRepositoryImpl_Factory create(Provider<WorkItemDao> workItemDaoProvider) {
    return new WorkItemRepositoryImpl_Factory(workItemDaoProvider);
  }

  public static WorkItemRepositoryImpl newInstance(WorkItemDao workItemDao) {
    return new WorkItemRepositoryImpl(workItemDao);
  }
}
