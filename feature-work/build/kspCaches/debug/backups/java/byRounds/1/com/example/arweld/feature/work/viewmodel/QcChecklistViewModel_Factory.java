package com.example.arweld.feature.work.viewmodel;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class QcChecklistViewModel_Factory implements Factory<QcChecklistViewModel> {
  private final Provider<Context> contextProvider;

  public QcChecklistViewModel_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public QcChecklistViewModel get() {
    return newInstance(contextProvider.get());
  }

  public static QcChecklistViewModel_Factory create(
      javax.inject.Provider<Context> contextProvider) {
    return new QcChecklistViewModel_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static QcChecklistViewModel_Factory create(Provider<Context> contextProvider) {
    return new QcChecklistViewModel_Factory(contextProvider);
  }

  public static QcChecklistViewModel newInstance(Context context) {
    return new QcChecklistViewModel(context);
  }
}
