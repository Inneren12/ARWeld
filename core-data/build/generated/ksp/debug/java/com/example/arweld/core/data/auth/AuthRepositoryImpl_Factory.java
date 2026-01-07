package com.example.arweld.core.data.auth;

import android.content.Context;
import com.example.arweld.core.data.db.dao.UserDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AuthRepositoryImpl_Factory implements Factory<AuthRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<UserDao> userDaoProvider;

  public AuthRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<UserDao> userDaoProvider) {
    this.contextProvider = contextProvider;
    this.userDaoProvider = userDaoProvider;
  }

  @Override
  public AuthRepositoryImpl get() {
    return newInstance(contextProvider.get(), userDaoProvider.get());
  }

  public static AuthRepositoryImpl_Factory create(javax.inject.Provider<Context> contextProvider,
      javax.inject.Provider<UserDao> userDaoProvider) {
    return new AuthRepositoryImpl_Factory(Providers.asDaggerProvider(contextProvider), Providers.asDaggerProvider(userDaoProvider));
  }

  public static AuthRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<UserDao> userDaoProvider) {
    return new AuthRepositoryImpl_Factory(contextProvider, userDaoProvider);
  }

  public static AuthRepositoryImpl newInstance(Context context, UserDao userDao) {
    return new AuthRepositoryImpl(context, userDao);
  }
}
