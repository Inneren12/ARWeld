package com.example.arweld.core.data.seed;

import com.example.arweld.core.data.db.dao.UserDao;
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
public final class DbSeedInitializer_Factory implements Factory<DbSeedInitializer> {
  private final Provider<WorkItemDao> workItemDaoProvider;

  private final Provider<UserDao> userDaoProvider;

  public DbSeedInitializer_Factory(Provider<WorkItemDao> workItemDaoProvider,
      Provider<UserDao> userDaoProvider) {
    this.workItemDaoProvider = workItemDaoProvider;
    this.userDaoProvider = userDaoProvider;
  }

  @Override
  public DbSeedInitializer get() {
    return newInstance(workItemDaoProvider.get(), userDaoProvider.get());
  }

  public static DbSeedInitializer_Factory create(
      javax.inject.Provider<WorkItemDao> workItemDaoProvider,
      javax.inject.Provider<UserDao> userDaoProvider) {
    return new DbSeedInitializer_Factory(Providers.asDaggerProvider(workItemDaoProvider), Providers.asDaggerProvider(userDaoProvider));
  }

  public static DbSeedInitializer_Factory create(Provider<WorkItemDao> workItemDaoProvider,
      Provider<UserDao> userDaoProvider) {
    return new DbSeedInitializer_Factory(workItemDaoProvider, userDaoProvider);
  }

  public static DbSeedInitializer newInstance(WorkItemDao workItemDao, UserDao userDao) {
    return new DbSeedInitializer(workItemDao, userDao);
  }
}
