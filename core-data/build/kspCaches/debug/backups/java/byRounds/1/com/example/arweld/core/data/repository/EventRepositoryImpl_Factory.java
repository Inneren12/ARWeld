package com.example.arweld.core.data.repository;

import com.example.arweld.core.data.db.dao.EventDao;
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
public final class EventRepositoryImpl_Factory implements Factory<EventRepositoryImpl> {
  private final Provider<EventDao> eventDaoProvider;

  public EventRepositoryImpl_Factory(Provider<EventDao> eventDaoProvider) {
    this.eventDaoProvider = eventDaoProvider;
  }

  @Override
  public EventRepositoryImpl get() {
    return newInstance(eventDaoProvider.get());
  }

  public static EventRepositoryImpl_Factory create(
      javax.inject.Provider<EventDao> eventDaoProvider) {
    return new EventRepositoryImpl_Factory(Providers.asDaggerProvider(eventDaoProvider));
  }

  public static EventRepositoryImpl_Factory create(Provider<EventDao> eventDaoProvider) {
    return new EventRepositoryImpl_Factory(eventDaoProvider);
  }

  public static EventRepositoryImpl newInstance(EventDao eventDao) {
    return new EventRepositoryImpl(eventDao);
  }
}
