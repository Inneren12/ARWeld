package com.example.arweld.core.data.di;

import com.example.arweld.core.domain.system.TimeProvider;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DataModule_ProvideTimeProviderFactory implements Factory<TimeProvider> {
  @Override
  public TimeProvider get() {
    return provideTimeProvider();
  }

  public static DataModule_ProvideTimeProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static TimeProvider provideTimeProvider() {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideTimeProvider());
  }

  private static final class InstanceHolder {
    static final DataModule_ProvideTimeProviderFactory INSTANCE = new DataModule_ProvideTimeProviderFactory();
  }
}
