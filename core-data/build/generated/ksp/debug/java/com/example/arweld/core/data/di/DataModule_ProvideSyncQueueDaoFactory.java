package com.example.arweld.core.data.di;

import com.example.arweld.core.data.db.AppDatabase;
import com.example.arweld.core.data.db.dao.SyncQueueDao;
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
public final class DataModule_ProvideSyncQueueDaoFactory implements Factory<SyncQueueDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideSyncQueueDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SyncQueueDao get() {
    return provideSyncQueueDao(databaseProvider.get());
  }

  public static DataModule_ProvideSyncQueueDaoFactory create(
      javax.inject.Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideSyncQueueDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DataModule_ProvideSyncQueueDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideSyncQueueDaoFactory(databaseProvider);
  }

  public static SyncQueueDao provideSyncQueueDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideSyncQueueDao(database));
  }
}
