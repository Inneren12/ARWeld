package com.example.arweld.core.auth.repository;

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
public final class InMemoryAuthRepository_Factory implements Factory<InMemoryAuthRepository> {
  @Override
  public InMemoryAuthRepository get() {
    return newInstance();
  }

  public static InMemoryAuthRepository_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static InMemoryAuthRepository newInstance() {
    return new InMemoryAuthRepository();
  }

  private static final class InstanceHolder {
    static final InMemoryAuthRepository_Factory INSTANCE = new InMemoryAuthRepository_Factory();
  }
}
