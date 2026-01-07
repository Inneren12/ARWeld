package com.example.arweld.core.data.di;

import com.example.arweld.core.data.db.AppDatabase;
import com.example.arweld.core.data.db.dao.UserDao;
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
public final class DataModule_ProvideUserDaoFactory implements Factory<UserDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideUserDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public UserDao get() {
    return provideUserDao(databaseProvider.get());
  }

  public static DataModule_ProvideUserDaoFactory create(
      javax.inject.Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideUserDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DataModule_ProvideUserDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideUserDaoFactory(databaseProvider);
  }

  public static UserDao provideUserDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideUserDao(database));
  }
}
