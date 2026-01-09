package com.example.arweld.feature.work.di

import com.example.arweld.feature.work.viewmodel.QcChecklistItemsProvider
import com.example.arweld.feature.work.viewmodel.ResourceQcChecklistItemsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class QcChecklistModule {
    @Binds
    abstract fun bindChecklistItemsProvider(
        provider: ResourceQcChecklistItemsProvider,
    ): QcChecklistItemsProvider
}
