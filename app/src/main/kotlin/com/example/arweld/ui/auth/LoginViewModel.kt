package com.example.arweld.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.logging.AppLogger
import com.example.arweld.core.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appLogger: AppLogger,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState(isLoading = true))
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun login(userId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            appLogger.logLoginAttempt(userId)
            runCatching {
                authRepository.loginWithUserId(userId)
            }.onSuccess {
                appLogger.logLoginSuccess(it)
                onSuccess()
            }.onFailure { throwable ->
                appLogger.logRepositoryError("authRepository.loginWithUserId", throwable)
                _uiState.value = _uiState.value.copy(error = throwable.message)
            }
        }
    }

    fun reloadUsers() {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            runCatching {
                authRepository.availableUsers()
            }.onSuccess { users ->
                _uiState.value = LoginUiState(isLoading = false, users = users)
            }.onFailure { throwable ->
                appLogger.logRepositoryError("authRepository.availableUsers", throwable)
                _uiState.value = LoginUiState(isLoading = false, error = throwable.message)
            }
        }
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val error: String? = null,
)
