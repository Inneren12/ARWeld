package com.example.arweld.feature.drawingimport.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.arweld.feature.drawingimport.ui.format.shortenSha

@Composable
fun ArtifactSummaryRow(
    label: String,
    relPath: String,
    sha256: String,
    pixelSha256: String? = null,
    sizeLabel: String? = null,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = relPath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "SHA: ${shortenSha(sha256)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        pixelSha256?.let { pixelSha ->
            Text(
                text = "Pixel SHA: ${shortenSha(pixelSha)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        sizeLabel?.let { size ->
            Text(
                text = "Size: $size",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
