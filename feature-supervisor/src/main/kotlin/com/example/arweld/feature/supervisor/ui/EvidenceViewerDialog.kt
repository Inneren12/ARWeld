package com.example.arweld.feature.supervisor.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.text.SimpleDateFormat
import java.util.*

/**
 * Full-screen dialog for viewing evidence details.
 * Displays image preview, metadata fields, and evidence properties.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvidenceViewerDialog(
    evidence: Evidence,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (evidence.kind) {
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
                    }
                )
            }
        ) { paddingValues ->
            var imageError by remember { mutableStateOf(false) }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Metadata parsing (must NOT wrap composables in try/catch) ---
                val metaJsonRaw = evidence.metaJson?.trim()
                val metaJsonForDisplay = metaJsonRaw?.takeUnless {
                    it.isBlank() || it == "null" || it == "{}"
                }
                val parsedMetaEntries: List<Pair<String, String>>? = remember(metaJsonForDisplay) {
                    val raw = metaJsonForDisplay ?: return@remember null
                    runCatching {
                        val json = Json { ignoreUnknownKeys = true }
                        val metaObject = json.parseToJsonElement(raw).jsonObject
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

                // Image preview section
                if (evidence.kind == EvidenceKind.PHOTO || evidence.kind == EvidenceKind.AR_SCREENSHOT) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 400.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(evidence.uri))
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
                    if (evidence.uri.isBlank() || imageError) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = if (evidence.uri.isBlank()) {
                                    "Preview failed - URI is empty"
                                } else {
                                    "Preview failed - Image could not be loaded"
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
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
                                text = "Preview not available for ${evidence.kind.name}",
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
                            value = when (evidence.kind) {
                                EvidenceKind.PHOTO -> "Photo"
                                EvidenceKind.AR_SCREENSHOT -> "AR Screenshot"
                                EvidenceKind.VIDEO -> "Video"
                                EvidenceKind.MEASUREMENT -> "Measurement"
                            }
                        )

                        EvidenceProperty(
                            label = "Created At",
                            value = formatTimestamp(evidence.createdAt)
                        )

                        EvidenceProperty(
                            label = "Event ID",
                            value = evidence.eventId
                        )

                        EvidenceProperty(
                            label = "SHA-256",
                            value = evidence.sha256
                        )

                        if (evidence.uri.isNotEmpty()) {
                            EvidenceProperty(
                                label = "URI",
                                value = evidence.uri
                            )
                        }
                    }
                }

                // Metadata section
                if (metaJsonForDisplay != null) {
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

                            val entries = parsedMetaEntries
                            if (entries != null) {
                                entries.forEach { (key, displayValue) ->
                                    EvidenceProperty(
                                        label = formatMetaKey(key),
                                        value = displayValue
                                    )
                                }
                            } else {
                                Text(
                                    text = "Raw metadata: $metaJsonForDisplay",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatMetaKey(key: String): String {
    // Convert camelCase or snake_case to Title Case
    return key
        .replace(Regex("([a-z])([A-Z])"), "$1 $2")
        .replace("_", " ")
        .split(" ")
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}
