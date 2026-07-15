package com.alpwarestudio.darkswitch.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alpwarestudio.darkswitch.R
import com.alpwarestudio.darkswitch.core.force.CompatMode
import com.alpwarestudio.darkswitch.ui.apps.AppsScreen
import com.alpwarestudio.darkswitch.ui.apps.AppsUiState
import com.alpwarestudio.darkswitch.ui.logs.LogsScreen

@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggleGlobal: (Boolean) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onRequestShizuku: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    compatMode: CompatMode,
    onSelectCompatMode: (CompatMode) -> Unit,
    onRestoreAll: () -> Unit,
    appsState: AppsUiState,
    onAppsQueryChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    logs: List<String>,
    logsExpanded: Boolean,
    onToggleLogsExpanded: () -> Unit,
    onClearLogs: () -> Unit
) {
    val ready = state.shizukuGranted && state.hasAccessibilityPermission && state.hasNotificationPermission
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.home_subtitle))
        }
        item {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.setup_title), style = MaterialTheme.typography.titleLarge)
                PermissionRow(stringResource(R.string.permission_shizuku_title), state.shizukuGranted, onRequestShizuku)
                PermissionRow(stringResource(R.string.permission_accessibility_title), state.hasAccessibilityPermission, onOpenAccessibilitySettings)
                if (!state.hasNotificationPermission) PermissionRow(stringResource(R.string.permission_notifications_title), false, onRequestNotificationPermission)
                Text(stringResource(R.string.privacy_disclosure), style = MaterialTheme.typography.bodySmall)
            }}
        }
        item {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.global_enabled_title), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.global_enabled_desc), style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(state.globalEnabled, onToggleGlobal, enabled = ready)
                }
                Text(stringResource(R.string.compat_mode_title), style = MaterialTheme.typography.titleMedium)
                CompatMode.entries.forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = compatMode == mode, onClick = { onSelectCompatMode(mode) }, enabled = ready)
                        Text(mode.title)
                    }
                }
                Button(onClick = onRestoreAll, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.restore_all))
                }
            }}
        }
        item {
            Card { Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.apps_title), style = MaterialTheme.typography.titleLarge)
                AppsScreen(appsState, onAppsQueryChange, onToggleApp, enabled = ready && state.globalEnabled)
            }}
        }
        item {
            Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.diagnostics_title), style = MaterialTheme.typography.titleLarge)
                Text("Shizuku: ${if (state.shizukuGranted) "Ready" else "Not ready"}")
                Text("Accessibility: ${if (state.hasAccessibilityPermission) "Ready" else "Not ready"}")
                Text("Notifications: ${if (state.hasNotificationPermission) "Ready" else "Not ready"}")
                TextButton(onClick = onToggleLogsExpanded) { Text(if (logsExpanded) stringResource(R.string.cd_hide_logs) else stringResource(R.string.cd_show_logs)) }
                if (logsExpanded) LogsScreen(logs, onClearLogs, embeddedInSingleScroll = true, enabled = true)
            }}
        }
    }
}

@Composable
private fun PermissionRow(title: String, granted: Boolean, action: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title); Text(if (granted) stringResource(R.string.status_ready) else stringResource(R.string.permission_status_not_granted), style = MaterialTheme.typography.bodySmall) }
        if (!granted) TextButton(onClick = action) { Text(stringResource(R.string.action_grant)) }
    }
}
