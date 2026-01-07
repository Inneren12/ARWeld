package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.policy.QcEvidencePolicy;
import com.example.arweld.core.domain.work.usecase.FailQcUseCase;
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
public final class QcFailReasonViewModel_Factory implements Factory<QcFailReasonViewModel> {
  private final Provider<FailQcUseCase> failQcUseCaseProvider;

  private final Provider<QcEvidencePolicy> qcEvidencePolicyProvider;

  public QcFailReasonViewModel_Factory(Provider<FailQcUseCase> failQcUseCaseProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    this.failQcUseCaseProvider = failQcUseCaseProvider;
    this.qcEvidencePolicyProvider = qcEvidencePolicyProvider;
  }

  @Override
  public QcFailReasonViewModel get() {
    return newInstance(failQcUseCaseProvider.get(), qcEvidencePolicyProvider.get());
  }

  public static QcFailReasonViewModel_Factory create(
      javax.inject.Provider<FailQcUseCase> failQcUseCaseProvider,
      javax.inject.Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new QcFailReasonViewModel_Factory(Providers.asDaggerProvider(failQcUseCaseProvider), Providers.asDaggerProvider(qcEvidencePolicyProvider));
  }

  public static QcFailReasonViewModel_Factory create(Provider<FailQcUseCase> failQcUseCaseProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider) {
    return new QcFailReasonViewModel_Factory(failQcUseCaseProvider, qcEvidencePolicyProvider);
  }

  public static QcFailReasonViewModel newInstance(FailQcUseCase failQcUseCase,
      QcEvidencePolicy qcEvidencePolicy) {
    return new QcFailReasonViewModel(failQcUseCase, qcEvidencePolicy);
  }
}
