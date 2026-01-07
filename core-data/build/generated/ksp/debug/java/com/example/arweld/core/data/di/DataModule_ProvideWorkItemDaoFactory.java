package com.example.arweld.core.data.di;

import com.example.arweld.core.data.db.AppDatabase;
import com.example.arweld.core.data.db.dao.WorkItemDao;
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
public final class DataModule_ProvideWorkItemDaoFactory implements Factory<WorkItemDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideWorkItemDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public WorkItemDao get() {
    return provideWorkItemDao(databaseProvider.get());
  }

  public static DataModule_ProvideWorkItemDaoFactory create(
      javax.inject.Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideWorkItemDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DataModule_ProvideWorkItemDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideWorkItemDaoFactory(databaseProvider);
  }

  public static WorkItemDao provideWorkItemDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideWorkItemDao(database));
  }
}
