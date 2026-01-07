package com.example.arweld.feature.supervisor.usecase;

import com.example.arweld.core.data.db.dao.EventDao;
import com.example.arweld.core.data.db.dao.UserDao;
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
public final class GetQcBottleneckUseCase_Factory implements Factory<GetQcBottleneckUseCase> {
  private final Provider<WorkItemDao> workItemDaoProvider;

  private final Provider<EventDao> eventDaoProvider;

  private final Provider<UserDao> userDaoProvider;

  public GetQcBottleneckUseCase_Factory(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider, Provider<UserDao> userDaoProvider) {
    this.workItemDaoProvider = workItemDaoProvider;
    this.eventDaoProvider = eventDaoProvider;
    this.userDaoProvider = userDaoProvider;
  }

  @Override
  public GetQcBottleneckUseCase get() {
    return newInstance(workItemDaoProvider.get(), eventDaoProvider.get(), userDaoProvider.get());
  }

  public static GetQcBottleneckUseCase_Factory create(
      javax.inject.Provider<WorkItemDao> workItemDaoProvider,
      javax.inject.Provider<EventDao> eventDaoProvider,
      javax.inject.Provider<UserDao> userDaoProvider) {
    return new GetQcBottleneckUseCase_Factory(Providers.asDaggerProvider(workItemDaoProvider), Providers.asDaggerProvider(eventDaoProvider), Providers.asDaggerProvider(userDaoProvider));
  }

  public static GetQcBottleneckUseCase_Factory create(Provider<WorkItemDao> workItemDaoProvider,
      Provider<EventDao> eventDaoProvider, Provider<UserDao> userDaoProvider) {
    return new GetQcBottleneckUseCase_Factory(workItemDaoProvider, eventDaoProvider, userDaoProvider);
  }

  public static GetQcBottleneckUseCase newInstance(WorkItemDao workItemDao, EventDao eventDao,
      UserDao userDao) {
    return new GetQcBottleneckUseCase(workItemDao, eventDao, userDao);
  }
}
