package com.example.arweld.core.data.di;

import com.example.arweld.core.data.db.AppDatabase;
import com.example.arweld.core.data.db.dao.EvidenceDao;
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
public final class DataModule_ProvideEvidenceDaoFactory implements Factory<EvidenceDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DataModule_ProvideEvidenceDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public EvidenceDao get() {
    return provideEvidenceDao(databaseProvider.get());
  }

  public static DataModule_ProvideEvidenceDaoFactory create(
      javax.inject.Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideEvidenceDaoFactory(Providers.asDaggerProvider(databaseProvider));
  }

  public static DataModule_ProvideEvidenceDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DataModule_ProvideEvidenceDaoFactory(databaseProvider);
  }

  public static EvidenceDao provideEvidenceDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DataModule.INSTANCE.provideEvidenceDao(database));
  }
}
