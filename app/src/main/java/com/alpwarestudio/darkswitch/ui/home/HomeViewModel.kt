package com.alpwarestudio.darkswitch.ui.home

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpwarestudio.darkswitch.core.force.CompatMode
import com.alpwarestudio.darkswitch.core.foreground.UsageAccess
import com.alpwarestudio.darkswitch.core.shizuku.ShizukuGate
import com.alpwarestudio.darkswitch.data.prefs.SettingsModel
import com.alpwarestudio.darkswitch.data.prefs.SettingsStore
import com.alpwarestudio.darkswitch.worker.ServiceStarter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.ComponentName
import android.provider.Settings
import com.alpwarestudio.darkswitch.core.foreground.ForegroundAccessibilityService

/**
 * UI-facing immutable state for the Home screen.
 *
 * Captures the global enablement switch, permission/onboarding status,
 * and the currently selected compat strategy.
 */
data class HomeUiState(
    val globalEnabled: Boolean = false,
    val hasUsageAccess: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val shizukuInstalled: Boolean = false,
    val shizukuGranted: Boolean = false,
    val hasAccessibilityPermission: Boolean = false,
    val compatMode: CompatMode = CompatMode.HWUI_PROP
)

/**
 * ViewModel backing the Home screen.
 *
 * Responsible for:
 * - observing persisted settings
 * - checking permission/onboarding status (Usage Access, notifications, accessibility, Shizuku)
 * - starting/stopping the companion service when the global toggle changes
 */
class HomeViewModel(app: Application) : AndroidViewModel(app) {

    // Persistent user settings (global enabled flag, compat mode, enabled packages).
    private val store = SettingsStore(app.applicationContext)
    // Encapsulates Shizuku binder/permission checks and request flow.
    private val shizukuGate = ShizukuGate()

    // Backing state updated as permissions/settings change.
    private val _uiState = MutableStateFlow(
        HomeUiState(
            globalEnabled = false,
            hasUsageAccess = UsageAccess.hasUsageAccess(app),
            hasNotificationPermission = if (Build.VERSION.SDK_INT >= 33) {
                ContextCompat.checkSelfPermission(
                    app,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true,
            shizukuInstalled = shizukuGate.isBinderAlive(),
            shizukuGranted = shizukuGate.isBinderAlive() && shizukuGate.isPermissionGranted(),
            hasAccessibilityPermission = hasA11yAccess(app),
        )
    )
    // Public read-only stream for UI observation.
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Hot stream of persisted settings.
     */
    val settings: StateFlow<SettingsModel> = store.settingsModelFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsModel()
    )

    init {
        viewModelScope.launch {
            // Keep UI state in sync with persisted settings.
            settings.collect { s ->
                refreshStatus(globalEnabledOverride = s.globalEnabled)
            }
        }
    }

    /**
     * Refreshes permission/onboarding status and recomputes {@link HomeUiState}.
     *
     * @param globalEnabledOverride optional override used after persisting the global toggle
     */
    fun refreshStatus(globalEnabledOverride: Boolean? = null) {
        val ctx = getApplication<Application>()
        val globalEnabled = globalEnabledOverride ?: settings.value.globalEnabled

        // POST_NOTIFICATIONS is required only on Android 13+ (Tiramisu).
        val hasNotif = if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        // Shizuku is considered installed/available when its binder is reachable.
        val binderAlive = shizukuGate.isBinderAlive()

        _uiState.value = HomeUiState(
            globalEnabled = globalEnabled,
            hasUsageAccess = UsageAccess.hasUsageAccess(ctx),
            hasNotificationPermission = hasNotif,
            shizukuInstalled = binderAlive,
            shizukuGranted = binderAlive && shizukuGate.isPermissionGranted(),
            hasAccessibilityPermission = hasA11yAccess(ctx),
            compatMode = settings.value.compatMode
        )
    }

    /**
     * Persists the global enablement flag and starts/stops the service accordingly.
     */
    fun setGlobalEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.setGlobalEnabled(enabled)

            val ctx = getApplication<Application>().applicationContext
            // Start/stop background logic immediately to reflect the user's intent.
            if (enabled) {
                ServiceStarter.start(ctx)
            } else {
                ServiceStarter.stop(ctx)
            }

            refreshStatus(globalEnabledOverride = enabled)
        }
    }

    /**
     * Persists the selected compat strategy.
     */
    fun setCompatMode(mode: CompatMode) {
        viewModelScope.launch {
            store.setCompatMode(mode)
            refreshStatus()
        }
    }

    /**
     * Returns whether our accessibility service is enabled in system settings.
     *
     * This is used to gate foreground-app detection via accessibility events.
     */
    private fun hasA11yAccess(ctx: Application): Boolean {
        return try {
            // Quick global check before scanning the enabled services list.
            val enabled = Settings.Secure.getInt(
                ctx.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            ) == 1
            if (!enabled) return false

            val enabledServices = Settings.Secure.getString(
                ctx.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            // Compare against this app's accessibility service component name.
            val expected = ComponentName(ctx, ForegroundAccessibilityService::class.java)
                .flattenToString()

            enabledServices.split(':').any { it.equals(expected, ignoreCase = true) }
        } catch (_: Throwable) {
            false
        }
    }
}