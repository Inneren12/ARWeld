package com.example.arweld.core.data.system;

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
public final class DefaultTimeProvider_Factory implements Factory<DefaultTimeProvider> {
  @Override
  public DefaultTimeProvider get() {
    return newInstance();
  }

  public static DefaultTimeProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DefaultTimeProvider newInstance() {
    return new DefaultTimeProvider();
  }

  private static final class InstanceHolder {
    static final DefaultTimeProvider_Factory INSTANCE = new DefaultTimeProvider_Factory();
  }
}
