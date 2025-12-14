package com.example.arweld.ui.auth

import androidx.lifecycle.ViewModel
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun onRoleSelected(role: Role) {
        authRepository.loginMock(role)
    }
}
