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
public final class AndroidDeviceInfoProvider_Factory implements Factory<AndroidDeviceInfoProvider> {
  @Override
  public AndroidDeviceInfoProvider get() {
    return newInstance();
  }

  public static AndroidDeviceInfoProvider_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static AndroidDeviceInfoProvider newInstance() {
    return new AndroidDeviceInfoProvider();
  }

  private static final class InstanceHolder {
    static final AndroidDeviceInfoProvider_Factory INSTANCE = new AndroidDeviceInfoProvider_Factory();
  }
}
