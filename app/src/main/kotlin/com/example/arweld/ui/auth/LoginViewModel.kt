package com.example.arweld.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.domain.auth.AuthRepository
import com.example.arweld.domain.model.Role
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun login(role: Role, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.loginMock(role)
            onSuccess()
        }
    }
}
