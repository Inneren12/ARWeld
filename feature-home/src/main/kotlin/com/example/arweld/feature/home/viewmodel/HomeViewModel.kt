package com.example.arweld.feature.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.data.repository.WorkItemRepository
import com.example.arweld.core.domain.auth.AuthRepository
import com.example.arweld.core.domain.model.Role
import com.example.arweld.core.domain.model.User
import com.example.arweld.core.domain.model.WorkItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen.
 * Demonstrates Hilt dependency injection working with WorkItemRepository.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workItemRepository: WorkItemRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                val user = authRepository.currentUser() ?: authRepository.loginMock(Role.ASSEMBLER)

                // Collect work items
                workItemRepository.observeAll().collect { workItems ->
                    _uiState.value = HomeUiState.Success(
                        currentUser = user,
                        workItemCount = workItems.size,
                        recentWorkItems = workItems.take(5)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

/**
 * UI state for Home screen.
 */
sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val currentUser: User?,
        val workItemCount: Int,
        val recentWorkItems: List<WorkItem>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
