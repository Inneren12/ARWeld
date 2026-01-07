package com.example.arweld.core.data.di;

import com.example.arweld.core.data.db.AppDatabase;
import com.example.arweld.core.data.db.dao.EventDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideEventDaoFactory implements Factory<EventDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideEventDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public EventDao get() {
    return provideEventDao(databaseProvider.get());
  }

  public static DataModule_ProvideEventDaoFactory create(
      javax.inject.Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideEventDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DataModule_ProvideEventDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideEventDaoFactory(databaseProvider);
  }

  public static EventDao provideEventDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideEventDao(database));
  }
}
