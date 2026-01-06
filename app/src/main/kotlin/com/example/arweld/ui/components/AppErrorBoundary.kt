package com.example.arweld.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.arweld.core.domain.logging.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ErrorBoundaryViewModel @Inject constructor(
    private val appLogger: AppLogger,
) : ViewModel() {
    fun onUnhandledError(throwable: Throwable) {
        appLogger.logUnhandledError(throwable)
    }
}

@Composable
fun AppErrorBoundary(
    modifier: Modifier = Modifier,
    viewModel: ErrorBoundaryViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val (capturedError, setCapturedError) = remember { mutableStateOf<Throwable?>(null) }

    if (capturedError == null) {
        try {
            content()
        } catch (throwable: Throwable) {
            viewModel.onUnhandledError(throwable)
            setCapturedError(throwable)
        }
    }

    capturedError?.let {
        ErrorFallback(
            modifier = modifier,
            onRetry = { setCapturedError(null) },
        )
    }
}

@Composable
private fun ErrorFallback(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Please retry. If the issue persists, restart the app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        )
        Button(onClick = onRetry) {
            Text(text = "Try again")
        }
    }
}
