package com.example.arweld.core.auth.di

import com.example.arweld.core.auth.repository.InMemoryAuthRepository
import com.example.arweld.core.domain.auth.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: InMemoryAuthRepository): AuthRepository
}
