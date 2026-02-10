package com.alpwarestudio.darkswitch

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alpwarestudio.darkswitch.core.foreground.UsageAccess
import com.alpwarestudio.darkswitch.core.shizuku.ShizukuGate
import com.alpwarestudio.darkswitch.ui.apps.AppsViewModel
import com.alpwarestudio.darkswitch.ui.home.HomeScreen
import com.alpwarestudio.darkswitch.ui.home.HomeViewModel
import com.alpwarestudio.darkswitch.ui.logs.LogsViewModel
import com.alpwarestudio.darkswitch.ui.theme.DarkSwitchTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * Main activity hosting the Compose UI.
 *
 * This activity wires together:
 * - Home, Apps, and Logs view models
 * - permission request launchers
 * - Shizuku permission lifecycle
 * - top-level Compose navigation and state holders
 */
class MainActivity : ComponentActivity() {

    // ViewModel managing global state, permissions, and service lifecycle.
    private val homeVm: HomeViewModel by viewModels()
    // ViewModel backing the per-app enablement screen.
    private val appsVm: AppsViewModel by viewModels()
    // ViewModel exposing runtime diagnostics logs.
    private val logsVm: LogsViewModel by viewModels()
    // Helper handling Shizuku permission and binder lifecycle.
    private val shizukuGate = ShizukuGate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Register Shizuku permission listener for this activity lifecycle.
        shizukuGate.register()

        installSplashScreen()

        setContent {
            DarkSwitchTheme {
                SetupSystemBars()

                // Local UI state controlling expansion of the logs panel.
                val logsExpanded = remember { mutableStateOf(false) }

                // Activity result launcher for POST_NOTIFICATIONS permission (Android 13+).
                val notifPermLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ ->
                    homeVm.refreshStatus()
                }

                // Collect UI state from ViewModels with lifecycle awareness.
                val homeState by homeVm.uiState.collectAsStateWithLifecycle()
                val settings by homeVm.settings.collectAsStateWithLifecycle()
                val appsState by appsVm.uiState.collectAsStateWithLifecycle()
                val logs by logsVm.lines.collectAsStateWithLifecycle()

                // One-time side effect to load installed apps when the UI starts.
                LaunchedEffect(Unit) {
                    appsVm.loadApps()
                }

                HomeScreen(
                    state = homeState,
                    onToggleGlobal = { homeVm.setGlobalEnabled(it) },
                    onOpenUsageAccess = { startActivity(UsageAccess.usageAccessSettingsIntent()) },
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            homeVm.refreshStatus()
                        }
                    },
                    onRequestShizuku = {
                        shizukuGate.requestPermission { _ ->
                            homeVm.refreshStatus()
                        }
                    },
                    onOpenAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    compatMode = settings.compatMode,
                    onSelectCompatMode = { homeVm.setCompatMode(it) },
                    appsState = appsState,
                    onAppsQueryChange = { appsVm.setQuery(it) },
                    onToggleApp = { appsVm.togglePackage(it) },
                    logs = logs,
                    logsExpanded = logsExpanded.value,
                    onToggleLogsExpanded = { logsExpanded.value = !logsExpanded.value },
                    onClearLogs = { logsVm.clear() }
                )
            }
        }
    }

    /**
     * Refresh permission and onboarding status when returning to the foreground.
     */
    override fun onResume() {
        super.onResume()
        homeVm.refreshStatus()
    }

    /**
     * Cleans up listeners tied to the activity lifecycle.
     */
    override fun onDestroy() {
        shizukuGate.unregister()
        super.onDestroy()
    }
}

/**
 * Configures transparent system bars and icon contrast based on theme.
 */
@Composable
fun SetupSystemBars() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
    }
}