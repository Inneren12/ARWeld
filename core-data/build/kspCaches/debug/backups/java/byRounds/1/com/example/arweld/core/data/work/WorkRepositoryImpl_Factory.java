package com.example.arweld.core.data.work;

import com.example.arweld.core.data.db.dao.EventDao;
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
public final class WorkRepositoryImpl_Factory implements Factory<WorkRepositoryImpl> {
  private final Provider<WorkItemDao> workItemDaoProvider;

  private final Provider<EventDao> eventDaoProvider;

  public WorkRepositoryImpl_Factory(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider) {
    this.workItemDaoProvider = workItemDaoProvider;
    this.eventDaoProvider = eventDaoProvider;
  }

  @Override
  public WorkRepositoryImpl get() {
    return newInstance(workItemDaoProvider.get(), eventDaoProvider.get());
  }

  public static WorkRepositoryImpl_Factory create(
      javax.inject.Provider<WorkItemDao> workItemDaoProvider,
      javax.inject.Provider<EventDao> eventDaoProvider) {
    return new WorkRepositoryImpl_Factory(Providers.asDaggerProvider(workItemDaoProvider), Providers.asDaggerProvider(eventDaoProvider));
  }

  public static WorkRepositoryImpl_Factory create(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider) {
    return new WorkRepositoryImpl_Factory(workItemDaoProvider, eventDaoProvider);
  }

  public static WorkRepositoryImpl newInstance(WorkItemDao workItemDao, EventDao eventDao) {
    return new WorkRepositoryImpl(workItemDao, eventDao);
  }
}
