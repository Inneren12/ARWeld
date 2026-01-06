package com.example.arweld.feature.arview.ui.arview

import android.content.pm.ApplicationInfo
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.arweld.core.domain.evidence.ArScreenshotMeta
import com.example.arweld.feature.arview.R
import com.example.arweld.feature.arview.alignment.AlignmentEventLogger
import com.example.arweld.feature.arview.arcore.ARViewController
import com.example.arweld.feature.arview.arcore.ARViewLifecycleHost
import com.example.arweld.feature.arview.alignment.ManualAlignmentState
import com.example.arweld.feature.arview.tracking.TrackingQuality
import com.example.arweld.feature.arview.tracking.TrackingStatus
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ARViewScreen(
    modifier: Modifier = Modifier,
    workItemId: String? = null,
    onBack: () -> Unit,
    infoOverlay: @Composable () -> Unit = {},
    onScreenshotCaptured: ((Uri, ArScreenshotMeta) -> Unit)? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val alignmentEventLogger = remember(context) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AlignmentEventLoggerEntryPoint::class.java,
        ).alignmentEventLogger()
    }
    val isDebuggable = remember(context) {
        (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
    val scope = rememberCoroutineScope()
    val controller = remember(alignmentEventLogger, workItemId, context) {
        ARViewController(
            context = context,
            alignmentEventLogger = alignmentEventLogger,
            workItemId = workItemId,
        )
    }
    val errorMessage = controller.errorMessage.collectAsState()
    val manualState by controller.manualAlignmentState.collectAsState()
    val trackingStatus by controller.trackingStatus.collectAsState()
    val alignmentScore by controller.alignmentScore.collectAsState()
    val detectedMarkers by controller.detectedMarkers.collectAsState()
    val intrinsicsReady by controller.intrinsicsAvailable.collectAsState()
    val renderFps by controller.renderFps.collectAsState()

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
            if (isDebuggable) {
                DiagnosticOverlay(
                    markerCount = detectedMarkers.size,
                    intrinsicsReady = intrinsicsReady,
                    alignmentScore = alignmentScore,
                    renderFps = renderFps,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                )
            }
            ManualAlignmentOverlay(
                state = manualState,
                onStartManualAlignment = { controller.startManualAlignment() },
                onResetManualAlignment = { controller.resetManualAlignment() },
                onCancelManualAlignment = { controller.cancelManualAlignment() },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
            ) {
                TrackingIndicator(
                    status = trackingStatus,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(id = R.string.alignment_score_label, alignmentScore.toDouble()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                ScreenshotButton(
                    onCapture = {
                        scope.launch {
                            if (onScreenshotCaptured == null || workItemId == null) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.capture_ar_screenshot_error),
                                    Toast.LENGTH_SHORT,
                                ).show()
                                return@launch
                            }

                            val message = runCatching {
                                val uri = controller.captureArScreenshotToFile(workItemId)
                                val meta = controller.currentScreenshotMeta()

                                onScreenshotCaptured.invoke(uri, meta)

                                context.getString(
                                    R.string.capture_ar_screenshot_success,
                                    uri.lastPathSegment,
                                )
                            }.getOrElse {
                                context.getString(R.string.capture_ar_screenshot_error)
                            }

                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    },
                )
                if (isDebuggable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    DebugMarkerButton(onTrigger = controller::triggerDebugMarkerDetection)
                }
            }
            infoOverlay()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AlignmentEventLoggerEntryPoint {
    fun alignmentEventLogger(): AlignmentEventLogger
}

@Composable
private fun ManualAlignmentOverlay(
    state: ManualAlignmentState,
    onStartManualAlignment: () -> Unit,
    onResetManualAlignment: () -> Unit,
    onCancelManualAlignment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (!state.isActive) {
            Button(onClick = onStartManualAlignment) {
                Text(text = stringResource(id = R.string.manual_align_button))
            }
            state.statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            Text(
                text = stringResource(id = R.string.manual_align_progress, state.collectedCount),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onResetManualAlignment) {
                    Text(text = stringResource(id = R.string.manual_align_reset))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onCancelManualAlignment) {
                    Text(text = stringResource(id = R.string.manual_align_cancel))
                }
            }
            state.statusMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DiagnosticOverlay(
    markerCount: Int,
    intrinsicsReady: Boolean,
    alignmentScore: Float,
    renderFps: Double,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = stringResource(id = R.string.diagnostic_overlay_title),
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(id = R.string.diagnostic_marker_count, markerCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    id = R.string.diagnostic_intrinsics_status,
                    if (intrinsicsReady) {
                        stringResource(id = R.string.diagnostic_value_ok)
                    } else {
                        stringResource(id = R.string.diagnostic_value_missing)
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.diagnostic_alignment_score, alignmentScore.toDouble()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.diagnostic_render_fps, renderFps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TrackingIndicator(
    status: TrackingStatus,
    modifier: Modifier = Modifier,
) {
    val indicatorColor = when (status.quality) {
        TrackingQuality.GOOD -> Color(0xFF4CAF50)
        TrackingQuality.WARNING -> Color(0xFFFFC107)
        TrackingQuality.POOR -> Color(0xFFF44336)
    }

    val label = when (status.quality) {
        TrackingQuality.GOOD -> stringResource(id = R.string.tracking_ok)
        TrackingQuality.WARNING -> stringResource(id = R.string.tracking_unstable)
        TrackingQuality.POOR -> stringResource(id = R.string.tracking_poor)
    }

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(14.dp),
                    color = indicatorColor,
                    shape = RoundedCornerShape(7.dp),
                    content = {},
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.labelLarge)
            }
            status.reason?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScreenshotButton(
    onCapture: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onCapture, modifier = modifier) {
        Text(text = stringResource(id = R.string.capture_ar_screenshot))
    }
}

@Composable
private fun DebugMarkerButton(
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(onClick = onTrigger, modifier = modifier) {
        Text(text = stringResource(id = R.string.simulate_marker_button))
    }
}
