package com.example.arweld.feature.arview.ui.arview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import com.example.arweld.feature.arview.R
import com.example.arweld.feature.arview.arcore.ARViewController
import com.example.arweld.feature.arview.arcore.ARViewLifecycleHost
import com.example.arweld.feature.arview.alignment.ManualAlignmentState

@Composable
fun ARViewScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    infoOverlay: @Composable () -> Unit = {},
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val controller = remember { ARViewController(context) }
    val errorMessage = controller.errorMessage.collectAsState()
    val manualState by controller.manualAlignmentState.collectAsState()

    LaunchedEffect(controller) {
        controller.loadTestNodeModel()
    }

    DisposableEffect(lifecycleOwner, controller) {
        val lifecycleHost = ARViewLifecycleHost(lifecycleOwner.lifecycle, controller)
        lifecycleHost.start()
        onDispose { lifecycleHost.stop() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.ar_view_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AndroidView(
                factory = { controller.getView() },
                modifier = Modifier.fillMaxSize()
            )
            errorMessage.value?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                )
            }
            ManualAlignmentOverlay(
                state = manualState,
                onStartManualAlignment = { controller.startManualAlignment() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
            infoOverlay()
        }
    }
}

@Composable
private fun ManualAlignmentOverlay(
    state: ManualAlignmentState,
    onStartManualAlignment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(onClick = onStartManualAlignment) {
            Text(text = stringResource(id = R.string.manual_align_button))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.manual_align_progress, state.collectedCount),
            color = MaterialTheme.colorScheme.onSurface,
        )
        state.statusMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
