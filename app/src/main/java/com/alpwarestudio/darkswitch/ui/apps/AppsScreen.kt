package com.alpwarestudio.darkswitch.ui.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpwarestudio.darkswitch.R
import com.alpwarestudio.darkswitch.core.compatibility.CompatibilityStatus

@Composable
fun AppsScreen(
    state: AppsUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    embeddedInSingleScroll: Boolean = true,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, null) }
        )
        state.filteredApps().forEach { row ->
            val app = row.app
            Row(
                modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { onToggle(app.packageName) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(app.label, style = MaterialTheme.typography.titleMedium)
                    Text(app.packageName, style = MaterialTheme.typography.bodySmall)
                    val label = when (row.compatibility.status) {
                        CompatibilityStatus.WORKING -> stringResource(R.string.compat_working)
                        CompatibilityStatus.PARTIAL -> stringResource(R.string.compat_partial)
                        CompatibilityStatus.NOT_WORKING -> stringResource(R.string.compat_not_working)
                        CompatibilityStatus.UNKNOWN -> stringResource(R.string.compat_unknown)
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall)
                    row.compatibility.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
                Switch(
                    checked = app.packageName in state.enabledPackages,
                    onCheckedChange = { onToggle(app.packageName) },
                    enabled = enabled
                )
            }
            HorizontalDivider()
        }
    }
}
