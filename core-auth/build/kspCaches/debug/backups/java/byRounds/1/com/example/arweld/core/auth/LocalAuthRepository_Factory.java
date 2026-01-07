package com.example.arweld.core.auth;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class LocalAuthRepository_Factory implements Factory<LocalAuthRepository> {
  @Override
  public LocalAuthRepository get() {
    return newInstance();
  }

  public static LocalAuthRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static LocalAuthRepository newInstance() {
    return new LocalAuthRepository();
  }

  private static final class InstanceHolder {
    static final LocalAuthRepository_Factory INSTANCE = new LocalAuthRepository_Factory();
  }
}
