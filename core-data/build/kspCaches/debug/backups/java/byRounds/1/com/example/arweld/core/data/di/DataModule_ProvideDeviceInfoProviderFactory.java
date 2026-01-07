package com.example.arweld.core.data.di;

import com.example.arweld.core.domain.system.DeviceInfoProvider;
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
public final class DataModule_ProvideDeviceInfoProviderFactory implements Factory<DeviceInfoProvider> {
  @Override
  public DeviceInfoProvider get() {
    return provideDeviceInfoProvider();
  }

  public static DataModule_ProvideDeviceInfoProviderFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static DeviceInfoProvider provideDeviceInfoProvider() {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideDeviceInfoProvider());
  }

  private static final class InstanceHolder {
    static final DataModule_ProvideDeviceInfoProviderFactory INSTANCE = new DataModule_ProvideDeviceInfoProviderFactory();
  }
}
