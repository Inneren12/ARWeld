package com.example.arweld.core.data.di;

import com.example.arweld.core.domain.evidence.EvidenceRepository;
import com.example.arweld.core.domain.policy.QcEvidencePolicy;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
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
public final class DataModule_ProvideQcEvidencePolicyFactory implements Factory<QcEvidencePolicy> {
  private final Provider<EvidenceRepository> evidenceRepositoryProvider;

  public DataModule_ProvideQcEvidencePolicyFactory(
      Provider<EvidenceRepository> evidenceRepositoryProvider) {
    this.evidenceRepositoryProvider = evidenceRepositoryProvider;
  }

  @Override
  public QcEvidencePolicy get() {
    return provideQcEvidencePolicy(evidenceRepositoryProvider.get());
  }

  public static DataModule_ProvideQcEvidencePolicyFactory create(
      javax.inject.Provider<EvidenceRepository> evidenceRepositoryProvider) {
    return new DataModule_ProvideQcEvidencePolicyFactory(Providers.asDaggerProvider(evidenceRepositoryProvider));
  }

  public static DataModule_ProvideQcEvidencePolicyFactory create(
      Provider<EvidenceRepository> evidenceRepositoryProvider) {
    return new DataModule_ProvideQcEvidencePolicyFactory(evidenceRepositoryProvider);
  }

  public static QcEvidencePolicy provideQcEvidencePolicy(EvidenceRepository evidenceRepository) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideQcEvidencePolicy(evidenceRepository));
  }
}
