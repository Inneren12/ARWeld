package com.example.arweld.feature.work.viewmodel;

import com.example.arweld.core.domain.event.EventRepository;
import com.example.arweld.core.domain.evidence.EvidenceRepository;
import com.example.arweld.core.domain.policy.QcEvidencePolicy;
import com.example.arweld.core.domain.work.WorkRepository;
import com.example.arweld.core.domain.work.usecase.FailQcUseCase;
import com.example.arweld.core.domain.work.usecase.PassQcUseCase;
import com.example.arweld.core.domain.work.usecase.StartQcInspectionUseCase;
import com.example.arweld.feature.work.camera.PhotoCaptureService;
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
public final class QcStartViewModel_Factory implements Factory<QcStartViewModel> {
  private final Provider<StartQcInspectionUseCase> startQcInspectionUseCaseProvider;

  private final Provider<WorkRepository> workRepositoryProvider;

  private final Provider<EventRepository> eventRepositoryProvider;

  private final Provider<EvidenceRepository> evidenceRepositoryProvider;

  private final Provider<QcEvidencePolicy> qcEvidencePolicyProvider;

  private final Provider<PassQcUseCase> passQcUseCaseProvider;

  private final Provider<FailQcUseCase> failQcUseCaseProvider;

  private final Provider<PhotoCaptureService> photoCaptureServiceProvider;

  public QcStartViewModel_Factory(
      Provider<StartQcInspectionUseCase> startQcInspectionUseCaseProvider,
      Provider<WorkRepository> workRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<EvidenceRepository> evidenceRepositoryProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider,
      Provider<PassQcUseCase> passQcUseCaseProvider, Provider<FailQcUseCase> failQcUseCaseProvider,
      Provider<PhotoCaptureService> photoCaptureServiceProvider) {
    this.startQcInspectionUseCaseProvider = startQcInspectionUseCaseProvider;
    this.workRepositoryProvider = workRepositoryProvider;
    this.eventRepositoryProvider = eventRepositoryProvider;
    this.evidenceRepositoryProvider = evidenceRepositoryProvider;
    this.qcEvidencePolicyProvider = qcEvidencePolicyProvider;
    this.passQcUseCaseProvider = passQcUseCaseProvider;
    this.failQcUseCaseProvider = failQcUseCaseProvider;
    this.photoCaptureServiceProvider = photoCaptureServiceProvider;
  }

  @Override
  public QcStartViewModel get() {
    return newInstance(startQcInspectionUseCaseProvider.get(), workRepositoryProvider.get(), eventRepositoryProvider.get(), evidenceRepositoryProvider.get(), qcEvidencePolicyProvider.get(), passQcUseCaseProvider.get(), failQcUseCaseProvider.get(), photoCaptureServiceProvider.get());
  }

  public static QcStartViewModel_Factory create(
      javax.inject.Provider<StartQcInspectionUseCase> startQcInspectionUseCaseProvider,
      javax.inject.Provider<WorkRepository> workRepositoryProvider,
      javax.inject.Provider<EventRepository> eventRepositoryProvider,
      javax.inject.Provider<EvidenceRepository> evidenceRepositoryProvider,
      javax.inject.Provider<QcEvidencePolicy> qcEvidencePolicyProvider,
      javax.inject.Provider<PassQcUseCase> passQcUseCaseProvider,
      javax.inject.Provider<FailQcUseCase> failQcUseCaseProvider,
      javax.inject.Provider<PhotoCaptureService> photoCaptureServiceProvider) {
    return new QcStartViewModel_Factory(Providers.asDaggerProvider(startQcInspectionUseCaseProvider), Providers.asDaggerProvider(workRepositoryProvider), Providers.asDaggerProvider(eventRepositoryProvider), Providers.asDaggerProvider(evidenceRepositoryProvider), Providers.asDaggerProvider(qcEvidencePolicyProvider), Providers.asDaggerProvider(passQcUseCaseProvider), Providers.asDaggerProvider(failQcUseCaseProvider), Providers.asDaggerProvider(photoCaptureServiceProvider));
  }

  public static QcStartViewModel_Factory create(
      Provider<StartQcInspectionUseCase> startQcInspectionUseCaseProvider,
      Provider<WorkRepository> workRepositoryProvider,
      Provider<EventRepository> eventRepositoryProvider,
      Provider<EvidenceRepository> evidenceRepositoryProvider,
      Provider<QcEvidencePolicy> qcEvidencePolicyProvider,
      Provider<PassQcUseCase> passQcUseCaseProvider, Provider<FailQcUseCase> failQcUseCaseProvider,
      Provider<PhotoCaptureService> photoCaptureServiceProvider) {
    return new QcStartViewModel_Factory(startQcInspectionUseCaseProvider, workRepositoryProvider, eventRepositoryProvider, evidenceRepositoryProvider, qcEvidencePolicyProvider, passQcUseCaseProvider, failQcUseCaseProvider, photoCaptureServiceProvider);
  }

  public static QcStartViewModel newInstance(StartQcInspectionUseCase startQcInspectionUseCase,
      WorkRepository workRepository, EventRepository eventRepository,
      EvidenceRepository evidenceRepository, QcEvidencePolicy qcEvidencePolicy,
      PassQcUseCase passQcUseCase, FailQcUseCase failQcUseCase,
      PhotoCaptureService photoCaptureService) {
    return new QcStartViewModel(startQcInspectionUseCase, workRepository, eventRepository, evidenceRepository, qcEvidencePolicy, passQcUseCase, failQcUseCase, photoCaptureService);
  }
}
