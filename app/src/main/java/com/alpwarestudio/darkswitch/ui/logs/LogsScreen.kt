package com.alpwarestudio.darkswitch.ui.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Screen displaying in-memory runtime logs.
 *
 * Logs are provided as plain text lines and can be cleared via the action button.
 * The content can either manage its own scroll container or be embedded into
 * a parent scroll layout (controlled by [embeddedInSingleScroll]).
 */
@Composable
fun LogsScreen(
    lines: List<String>,
    onClear: () -> Unit,
    embeddedInSingleScroll: Boolean = false,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Action button to clear all in-memory log entries.
        Button(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text("Clear")
        }

        // Container for rendering log lines.
        Card {
            // Choose rendering strategy based on whether this screen owns scrolling.
            if (embeddedInSingleScroll) {
                Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Render logs directly when hosted inside a parent scroll container.
                    lines.forEach { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                // Use LazyColumn when this screen is responsible for its own scrolling.
                LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                    items(lines) { line ->
                        Text(line, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}