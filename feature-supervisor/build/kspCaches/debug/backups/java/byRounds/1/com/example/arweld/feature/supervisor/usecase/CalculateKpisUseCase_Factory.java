package com.example.arweld.feature.supervisor.usecase;

import com.example.arweld.core.data.db.dao.EventDao;
import com.example.arweld.core.data.db.dao.WorkItemDao;
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
public final class CalculateKpisUseCase_Factory implements Factory<CalculateKpisUseCase> {
  private final Provider<WorkItemDao> workItemDaoProvider;

  private final Provider<EventDao> eventDaoProvider;

  public CalculateKpisUseCase_Factory(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider) {
    this.workItemDaoProvider = workItemDaoProvider;
    this.eventDaoProvider = eventDaoProvider;
  }

  @Override
  public CalculateKpisUseCase get() {
    return newInstance(workItemDaoProvider.get(), eventDaoProvider.get());
  }

  public static CalculateKpisUseCase_Factory create(
      javax.inject.Provider<WorkItemDao> workItemDaoProvider,
      javax.inject.Provider<EventDao> eventDaoProvider) {
    return new CalculateKpisUseCase_Factory(Providers.asDaggerProvider(workItemDaoProvider), Providers.asDaggerProvider(eventDaoProvider));
  }

  public static CalculateKpisUseCase_Factory create(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider) {
    return new CalculateKpisUseCase_Factory(workItemDaoProvider, eventDaoProvider);
  }

  public static CalculateKpisUseCase newInstance(WorkItemDao workItemDao, EventDao eventDao) {
    return new CalculateKpisUseCase(workItemDao, eventDao);
  }
}
