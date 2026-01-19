package com.example.arweld.feature.supervisor.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.arweld.core.domain.evidence.Evidence
import com.example.arweld.core.domain.evidence.EvidenceKind
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full-screen dialog for viewing evidence details.
 * Displays image preview, metadata fields, and evidence properties.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceViewerDialog(
    evidence: Evidence,
    evidenceList: List<Evidence>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedEvidence by remember(evidenceList, evidence) {
        mutableStateOf(evidenceList.firstOrNull { it.id == evidence.id } ?: evidence)
    }
    val visualEvidence = remember(evidenceList) {
        evidenceList.filter { it.kind == EvidenceKind.PHOTO || it.kind == EvidenceKind.AR_SCREENSHOT }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (selectedEvidence.kind) {
                                EvidenceKind.PHOTO -> "Photo Evidence"
                                EvidenceKind.AR_SCREENSHOT -> "AR Screenshot Evidence"
                                EvidenceKind.VIDEO -> "Video Evidence"
                                EvidenceKind.MEASUREMENT -> "Measurement Evidence"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close"
                            )
                        }
                    },
                    actions = {
                        val shareEnabled = selectedEvidence.uri.isNotBlank() && !isFileMissing(selectedEvidence.uri)
                        IconButton(
                            onClick = {
                                val uri = Uri.parse(selectedEvidence.uri)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = when (selectedEvidence.kind) {
                                        EvidenceKind.PHOTO,
                                        EvidenceKind.AR_SCREENSHOT -> "image/*"
                                        EvidenceKind.VIDEO -> "video/*"
                                        EvidenceKind.MEASUREMENT -> "text/plain"
                                    }
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching {
                                    context.startActivity(
                                        Intent.createChooser(intent, "Share evidence")
                                    )
                                }.onFailure {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Unable to share this evidence.")
                                    }
                                }
                            },
                            enabled = shareEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share evidence"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            var imageError by remember { mutableStateOf(false) }
            var showFullscreen by remember { mutableStateOf(false) }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Metadata parsing (must NOT wrap composables in try/catch) ---
                val metaJsonRaw = selectedEvidence.metaJson?.trim()
                val metaJsonForDisplay = metaJsonRaw?.takeUnless {
                    it.isBlank() || it == "null" || it == "{}"
                }
                val parsedMetaEntries: List<Pair<String, String>>? = remember(metaJsonForDisplay) {
                    val raw = metaJsonForDisplay ?: return@remember null
                    runCatching {
                        val json = Json { ignoreUnknownKeys = true }
                        val metaObject = json.decodeFromString<JsonElement>(raw).jsonObject
                        metaObject.map { (key, value) ->
                            val displayValue = if (value is JsonPrimitive) {
                                value.content
                            } else {
                                value.toString()
                            }
                            key to displayValue
                        }
                    }.getOrNull()
                }
                val parsedMetaMap = remember(metaJsonForDisplay) {
                    val raw = metaJsonForDisplay ?: return@remember emptyMap<String, JsonElement>()
                    runCatching {
                        val json = Json { ignoreUnknownKeys = true }
                        json.decodeFromString<JsonElement>(raw).jsonObject
                    }.getOrElse { emptyMap() }
                }

                val isMissingFile = remember(selectedEvidence.uri) {
                    isFileMissing(selectedEvidence.uri)
                }

                if (visualEvidence.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Thumbnails",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(visualEvidence, key = { it.id }) { item ->
                                    val isSelected = item.id == selectedEvidence.id
                                    ElevatedCard(
                                        modifier = Modifier
                                            .size(96.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ),
                                        onClick = {
                                            selectedEvidence = item
                                            imageError = false
                                        }
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(Uri.parse(item.uri))
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = "Evidence thumbnail",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Image preview section
                if (selectedEvidence.kind == EvidenceKind.PHOTO || selectedEvidence.kind == EvidenceKind.AR_SCREENSHOT) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showFullscreen = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(selectedEvidence.uri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Evidence image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                onError = {
                                    imageError = true
                                }
                            )
                        }
                    }

                    // Show error message if image fails to load
                    if (selectedEvidence.uri.isBlank() || imageError || isMissingFile) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = when {
                                    selectedEvidence.uri.isBlank() -> "Preview failed - URI is empty"
                                    isMissingFile -> "File missing - evidence file could not be found"
                                    else -> "Preview failed - Image could not be loaded"
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    if (showFullscreen && !isMissingFile && !imageError && selectedEvidence.uri.isNotBlank()) {
                        Dialog(onDismissRequest = { showFullscreen = false }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(Uri.parse(selectedEvidence.uri))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Full screen evidence image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                } else {
                    // Placeholder for non-image evidence
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Preview not available for ${selectedEvidence.kind.name}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Evidence properties section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Evidence Properties",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Divider()

                        EvidenceProperty(
                            label = "Kind",
                            value = when (selectedEvidence.kind) {
                                EvidenceKind.PHOTO -> "Photo"
                                EvidenceKind.AR_SCREENSHOT -> "AR Screenshot"
                                EvidenceKind.VIDEO -> "Video"
                                EvidenceKind.MEASUREMENT -> "Measurement"
                            }
                        )

                        EvidenceProperty(
                            label = "Created At",
                            value = formatTimestamp(selectedEvidence.createdAt)
                        )

                        EvidenceProperty(
                            label = "Event ID",
                            value = selectedEvidence.eventId
                        )

                        EvidenceProperty(
                            label = "SHA-256",
                            value = selectedEvidence.sha256
                        )

                        if (selectedEvidence.uri.isNotEmpty()) {
                            EvidenceProperty(
                                label = "URI",
                                value = selectedEvidence.uri
                            )
                        }
                    }
                }

                // Metadata panel
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Metadata",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Divider()

                        EvidenceProperty(
                            label = "Timestamp",
                            value = formatTimestamp(selectedEvidence.createdAt)
                        )

                        EvidenceProperty(
                            label = "SHA-256",
                            value = selectedEvidence.sha256
                        )

                        EvidenceProperty(
                            label = "Marker IDs",
                            value = parsedMetaMap.stringValue("markerIds") ?: "Not available"
                        )

                        EvidenceProperty(
                            label = "Tracking Quality",
                            value = parsedMetaMap.stringValue("trackingQuality") ?: "Not available"
                        )

                        EvidenceProperty(
                            label = "Alignment Score",
                            value = parsedMetaMap.stringValue("alignmentScore") ?: "Not available"
                        )

                        if (metaJsonForDisplay != null && parsedMetaEntries == null) {
                            Text(
                                text = "Raw metadata: $metaJsonForDisplay",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Close button at bottom
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun EvidenceProperty(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun isFileMissing(uriString: String): Boolean {
    if (uriString.isBlank()) return true
    val uri = Uri.parse(uriString)
    val isFileScheme = uri.scheme == null || uri.scheme == "file"
    if (!isFileScheme) return false
    val path = uri.path ?: uriString
    return runCatching { !File(path).exists() }.getOrDefault(false)
}

private fun Map<String, JsonElement>.stringValue(key: String): String? {
    val value = this[key] ?: return null
    return when {
        value is JsonPrimitive -> value.content
        value.jsonArray.isNotEmpty() -> value.jsonArray.joinToString(", ") { it.jsonPrimitive.content }
        else -> value.toString()
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
