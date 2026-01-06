package com.example.arweld.navigation

import androidx.lifecycle.ViewModel
import com.example.arweld.core.domain.logging.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NavigationLoggerViewModel @Inject constructor(
    private val appLogger: AppLogger,
) : ViewModel() {
    fun logNavigation(route: String) {
        appLogger.logNavigation(route)
    }
}
