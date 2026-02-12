package com.alpwarestudio.darkswitch.ui.home

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alpwarestudio.darkswitch.core.force.CompatMode
import com.alpwarestudio.darkswitch.ui.apps.AppsScreen
import com.alpwarestudio.darkswitch.ui.apps.AppsUiState
import com.alpwarestudio.darkswitch.ui.logs.LogsScreen
import com.alpwarestudio.darkswitch.R

/**
 * Main entry screen of the app.
 *
 * This screen orchestrates:
 * - permission onboarding (Usage Access, Accessibility, Notifications, Shizuku)
 * - global force-dark toggle
 * - compat mode selection
 * - per-app enablement
 * - runtime logs
 *
 * UI elements are progressively enabled based on granted permissions.
 */
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
    appsState: AppsUiState,
    onAppsQueryChange: (String) -> Unit,
    onToggleApp: (String) -> Unit,
    logs: List<String>,
    logsExpanded: Boolean,
    onToggleLogsExpanded: () -> Unit,
    onClearLogs: () -> Unit
) {
    // Aggregate permission state required for enabling force-dark features.
    val allPermissionsGranted = state.hasUsageAccess && state.hasAccessibilityPermission && state.shizukuGranted && state.hasNotificationPermission
    // UI is considered active only when global toggle is on and all permissions are granted.
    val uiEnabled = state.globalEnabled && allPermissionsGranted
    val disabledAlpha = 0.45f

    // Controls visibility of the Shizuku help dialog.
    val shizukuInfoOpen = remember { mutableStateOf(false) }

    // Single scroll container hosting the entire home layout.
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Spacer(modifier = Modifier.padding(top = 16.dp))

                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(R.string.home_subtitle),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            // Permission onboarding card: shown only while at least one permission is missing.
            // Permissions (only show what is still needed; hide card completely when all granted)
            val needsUsage = !state.hasUsageAccess
            val needsShizuku = !state.shizukuGranted
            val needsNotif = !state.hasNotificationPermission
            val needsA11y = !state.hasAccessibilityPermission

            AnimatedVisibility(visible = needsUsage || needsA11y || needsShizuku || needsNotif) {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(stringResource(R.string.permissions_title), style = MaterialTheme.typography.titleMedium)

                        if (needsUsage) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.permission_usage_access_title))
                                    Text(stringResource(R.string.permission_status_not_granted), style = MaterialTheme.typography.bodyMedium)
                                }
                                Button(onClick = onOpenUsageAccess) {
                                    Text(stringResource(R.string.action_grant))
                                }
                            }
                        }

                        if (needsA11y) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.permission_accessibility_title))
                                    Text(stringResource(R.string.permission_status_not_granted), style = MaterialTheme.typography.bodyMedium)
                                }
                                Button(onClick = onOpenAccessibilitySettings) {
                                    Text(stringResource(R.string.action_grant))
                                }
                            }
                        }

                        if (needsNotif) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(stringResource(R.string.permission_notifications_title))
                                    Text(stringResource(R.string.permission_status_not_granted), style = MaterialTheme.typography.bodyMedium)
                                }
                                Button(onClick = onRequestNotificationPermission) {
                                    Text(stringResource(R.string.action_grant))
                                }
                            }
                        }

                        if (needsShizuku) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    IconButton(
                                        onClick = { shizukuInfoOpen.value = true },
                                        modifier = Modifier.padding(start = 0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = stringResource(R.string.cd_shizuku_help)
                                        )
                                    }
                                    Column {
                                        Text(stringResource(R.string.permission_shizuku_title))
                                        Text(
                                            if (!state.shizukuInstalled) stringResource(R.string.shizuku_status_not_running) else stringResource(R.string.shizuku_status_permission_needed),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Button(
                                    onClick = onRequestShizuku,
                                    enabled = state.shizukuInstalled
                                ) {
                                    Text(stringResource(R.string.action_request))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Informational dialog explaining Shizuku setup and requirements.
        // Shizuku info dialog
        item {
            if (shizukuInfoOpen.value) {
                AlertDialog(
                    onDismissRequest = { shizukuInfoOpen.value = false },
                    confirmButton = {
                        TextButton(onClick = { shizukuInfoOpen.value = false }) { Text(stringResource(R.string.action_ok)) }
                    },
                    title = { Text(stringResource(R.string.shizuku_setup_title)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(stringResource(R.string.shizuku_setup_intro))

                            Text(stringResource(R.string.shizuku_setup_beginners_title))
                            Text(stringResource(R.string.shizuku_setup_beginners_steps))

                            Text(stringResource(R.string.shizuku_setup_advanced_title))
                            Text(stringResource(R.string.shizuku_setup_advanced_steps))

                            Text(stringResource(R.string.shizuku_setup_notes))
                        }
                    }
                )
            }
        }

        item {
            // Global master switch controlling whether force-dark logic is applied.
            // Global Enabled (always visible)
            Card(modifier = Modifier.alpha(if (allPermissionsGranted) 1f else disabledAlpha)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.global_enabled_title), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.global_enabled_desc),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = state.globalEnabled,
                            onCheckedChange = onToggleGlobal,
                            enabled = allPermissionsGranted
                        )
                    }
                }
            }
        }

        item {
            // Compat strategy selector cycling through available force-dark mechanisms.
            // Compat Mode (above logs)
            Card(modifier = Modifier.alpha(if (uiEnabled) 1f else disabledAlpha)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(stringResource(R.string.compat_mode_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.compat_mode_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(compatMode.title, style = MaterialTheme.typography.bodyMedium)
                        IconButton(
                            onClick = {
                                val next = when (compatMode) {
                                    CompatMode.HWUI_PROP -> CompatMode.SETTINGS_GLOBAL
                                    CompatMode.SETTINGS_GLOBAL -> CompatMode.CMD_UIMODE
                                    CompatMode.CMD_UIMODE -> CompatMode.HWUI_PROP
                                }
                                onSelectCompatMode(next)
                            },
                            enabled = uiEnabled
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.cd_change_compat_mode)
                            )
                        }
                    }
                }
            }
        }

        item {
            // Per-application force-dark enablement list.
            // Apps (always visible under Global/Permissions)
            Card(modifier = Modifier.alpha(if (uiEnabled) 1f else disabledAlpha)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppsScreen(
                        state = appsState,
                        onQueryChange = onAppsQueryChange,
                        onToggle = onToggleApp,
                        embeddedInSingleScroll = true,
                        enabled = uiEnabled
                    )
                }
            }
        }

        item {
            // Runtime diagnostics log panel, expandable on demand.
            // Logs (expandable at the bottom)
            Card(modifier = Modifier.alpha(if (uiEnabled) 1f else disabledAlpha)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.logs_title), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = onToggleLogsExpanded, enabled = uiEnabled) {
                            Icon(
                                imageVector = if (logsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (logsExpanded) stringResource(R.string.cd_hide_logs) else stringResource(R.string.cd_show_logs)
                            )
                        }
                    }

                    AnimatedVisibility(visible = uiEnabled && logsExpanded) {
                        LogsScreen(
                            lines = logs,
                            onClear = onClearLogs,
                            embeddedInSingleScroll = true,
                            enabled = uiEnabled
                        )
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.footer_open_source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.footer_developed_by),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}