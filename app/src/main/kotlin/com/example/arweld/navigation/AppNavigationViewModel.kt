package com.example.arweld.navigation

import androidx.lifecycle.ViewModel
import com.example.arweld.core.domain.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppNavigationViewModel @Inject constructor(
    val authRepository: AuthRepository
) : ViewModel()
