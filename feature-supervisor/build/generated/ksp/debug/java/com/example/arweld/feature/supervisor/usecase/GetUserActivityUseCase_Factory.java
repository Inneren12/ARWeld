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
public final class GetUserActivityUseCase_Factory implements Factory<GetUserActivityUseCase> {
  private final Provider<UserDao> userDaoProvider;

  private final Provider<EventDao> eventDaoProvider;

  private final Provider<WorkItemDao> workItemDaoProvider;

  public GetUserActivityUseCase_Factory(Provider<UserDao> userDaoProvider,
      Provider<EventDao> eventDaoProvider, Provider<WorkItemDao> workItemDaoProvider) {
    this.userDaoProvider = userDaoProvider;
    this.eventDaoProvider = eventDaoProvider;
    this.workItemDaoProvider = workItemDaoProvider;
  }

  @Override
  public GetUserActivityUseCase get() {
    return newInstance(userDaoProvider.get(), eventDaoProvider.get(), workItemDaoProvider.get());
  }

  public static GetUserActivityUseCase_Factory create(
      javax.inject.Provider<UserDao> userDaoProvider,
      javax.inject.Provider<EventDao> eventDaoProvider,
      javax.inject.Provider<WorkItemDao> workItemDaoProvider) {
    return new GetUserActivityUseCase_Factory(Providers.asDaggerProvider(userDaoProvider), Providers.asDaggerProvider(eventDaoProvider), Providers.asDaggerProvider(workItemDaoProvider));
  }

  public static GetUserActivityUseCase_Factory create(Provider<UserDao> userDaoProvider,
      Provider<EventDao> eventDaoProvider, Provider<WorkItemDao> workItemDaoProvider) {
    return new GetUserActivityUseCase_Factory(userDaoProvider, eventDaoProvider, workItemDaoProvider);
  }

  public static GetUserActivityUseCase newInstance(UserDao userDao, EventDao eventDao,
      WorkItemDao workItemDao) {
    return new GetUserActivityUseCase(userDao, eventDao, workItemDao);
  }
}
