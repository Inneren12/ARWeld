package com.example.arweld.core.auth.di

import com.example.arweld.core.auth.AuthRepository
import com.example.arweld.core.auth.LocalAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing authentication dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: LocalAuthRepository
    ): AuthRepository
}
