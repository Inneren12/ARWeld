package com.example.arweld.feature.supervisor.viewmodel;

import com.example.arweld.feature.supervisor.usecase.CalculateKpisUseCase;
import com.example.arweld.feature.supervisor.usecase.GetQcBottleneckUseCase;
import com.example.arweld.feature.supervisor.usecase.GetUserActivityUseCase;
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
public final class SupervisorDashboardViewModel_Factory implements Factory<SupervisorDashboardViewModel> {
  private final Provider<CalculateKpisUseCase> calculateKpisUseCaseProvider;

  private final Provider<GetQcBottleneckUseCase> getQcBottleneckUseCaseProvider;

  private final Provider<GetUserActivityUseCase> getUserActivityUseCaseProvider;

  public SupervisorDashboardViewModel_Factory(
      Provider<CalculateKpisUseCase> calculateKpisUseCaseProvider,
      Provider<GetQcBottleneckUseCase> getQcBottleneckUseCaseProvider,
      Provider<GetUserActivityUseCase> getUserActivityUseCaseProvider) {
    this.calculateKpisUseCaseProvider = calculateKpisUseCaseProvider;
    this.getQcBottleneckUseCaseProvider = getQcBottleneckUseCaseProvider;
    this.getUserActivityUseCaseProvider = getUserActivityUseCaseProvider;
  }

  @Override
  public SupervisorDashboardViewModel get() {
    return newInstance(calculateKpisUseCaseProvider.get(), getQcBottleneckUseCaseProvider.get(), getUserActivityUseCaseProvider.get());
  }

  public static SupervisorDashboardViewModel_Factory create(
      javax.inject.Provider<CalculateKpisUseCase> calculateKpisUseCaseProvider,
      javax.inject.Provider<GetQcBottleneckUseCase> getQcBottleneckUseCaseProvider,
      javax.inject.Provider<GetUserActivityUseCase> getUserActivityUseCaseProvider) {
    return new SupervisorDashboardViewModel_Factory(Providers.asDaggerProvider(calculateKpisUseCaseProvider), Providers.asDaggerProvider(getQcBottleneckUseCaseProvider), Providers.asDaggerProvider(getUserActivityUseCaseProvider));
  }

  public static SupervisorDashboardViewModel_Factory create(
      Provider<CalculateKpisUseCase> calculateKpisUseCaseProvider,
      Provider<GetQcBottleneckUseCase> getQcBottleneckUseCaseProvider,
      Provider<GetUserActivityUseCase> getUserActivityUseCaseProvider) {
    return new SupervisorDashboardViewModel_Factory(calculateKpisUseCaseProvider, getQcBottleneckUseCaseProvider, getUserActivityUseCaseProvider);
  }

  public static SupervisorDashboardViewModel newInstance(CalculateKpisUseCase calculateKpisUseCase,
      GetQcBottleneckUseCase getQcBottleneckUseCase,
      GetUserActivityUseCase getUserActivityUseCase) {
    return new SupervisorDashboardViewModel(calculateKpisUseCase, getQcBottleneckUseCase, getUserActivityUseCase);
  }
}
