package com.alpwarestudio.darkswitch.ui.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import com.alpwarestudio.darkswitch.R
import com.alpwarestudio.darkswitch.core.model.AppInfo

/**
 * Screen displaying a searchable list of installed applications with per-app toggle support.
 *
 * The list can be rendered either inside its own scroll container or embedded
 * into an existing parent scroll (controlled by [embeddedInSingleScroll]).
 */
@Composable
fun AppsScreen(
    state: AppsUiState,
    onQueryChange: (String) -> Unit,
    onToggle: (String) -> Unit,
    embeddedInSingleScroll: Boolean = false,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    ) {
        // Search field used to filter the app list by label or package name.
        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            enabled = enabled,
            placeholder = {
                Text(text = stringResource(id = R.string.search_hint))
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            shape = MaterialTheme.shapes.large,
        )

        // Apply current query and state to obtain the visible app list.
        val list = state.filteredApps()

        Card {
            // Choose rendering strategy based on whether this screen owns its scroll container.
            if (embeddedInSingleScroll) {
                Column {
                    list.forEach { app ->
                        AppRow(
                            app = app,
                            checked = state.enabledPackages.contains(app.packageName),
                            enabled = enabled,
                            onToggle = { onToggle(app.packageName) }
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(list, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            checked = state.enabledPackages.contains(app.packageName),
                            enabled = enabled,
                            onToggle = { onToggle(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single row representing an application with an enable/disable switch.
 */
@Composable
private fun AppRow(
    app: AppInfo,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            // Make the entire row tappable for better accessibility and UX.
            .clickable(enabled = enabled) { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, style = MaterialTheme.typography.titleMedium)
            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
        }
        // Explicit switch mirrors the row tap behavior.
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            enabled = enabled
        )
    }
}