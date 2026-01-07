package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.policy.QcEvidencePolicy;
import com.example.arweld.core.domain.work.usecase.PassQcUseCase;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class QcPassConfirmViewModel_Factory implements Factory<QcPassConfirmViewModel> {
  private final Provider<PassQcUseCase> passQcUseCaseProvider;

  private final Provider<QcEvidencePolicy> qcEvidencePolicyProvider;

  public QcPassConfirmViewModel_Factory(Provider<PassQcUseCase> passQcUseCaseProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    this.passQcUseCaseProvider = passQcUseCaseProvider;
    this.qcEvidencePolicyProvider = qcEvidencePolicyProvider;
  }

  @Override
  public QcPassConfirmViewModel get() {
    return newInstance(passQcUseCaseProvider.get(), qcEvidencePolicyProvider.get());
  }

  public static QcPassConfirmViewModel_Factory create(
      javax.inject.Provider<PassQcUseCase> passQcUseCaseProvider,
      javax.inject.Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new QcPassConfirmViewModel_Factory(Providers.asDaggerProvider(passQcUseCaseProvider), Providers.asDaggerProvider(qcEvidencePolicyProvider));
  }

  public static QcPassConfirmViewModel_Factory create(Provider<PassQcUseCase> passQcUseCaseProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new QcPassConfirmViewModel_Factory(passQcUseCaseProvider, qcEvidencePolicyProvider);
  }

  public static QcPassConfirmViewModel newInstance(PassQcUseCase passQcUseCase,
      QcEvidencePolicy qcEvidencePolicy) {
    return new QcPassConfirmViewModel(passQcUseCase, qcEvidencePolicy);
  }
}
