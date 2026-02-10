package com.alpwarestudio.darkswitch.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpwarestudio.darkswitch.core.apps.InstalledApps
import com.alpwarestudio.darkswitch.core.log.DsLog
import com.alpwarestudio.darkswitch.core.model.AppInfo
import com.alpwarestudio.darkswitch.data.prefs.SettingsModel
import com.alpwarestudio.darkswitch.data.prefs.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI-facing immutable state for the Apps screen.
 *
 * This state combines the full app list, current search query,
 * and the set of user-enabled packages.
 */
data class AppsUiState(
    val query: String = "",
    val apps: List<AppInfo> = emptyList(),
    val enabledPackages: Set<String> = emptySet()
) {
    /**
     * Returns the app list filtered by the current query.
     *
     * Matching is performed against both the user-visible label and
     * the package name for convenience.
     */
    fun filteredApps(): List<AppInfo> {
        if (query.isBlank()) return apps
        val q = query.trim().lowercase()
        return apps.filter { it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q) }
    }
}

/**
 * ViewModel backing the Apps screen.
 *
 * Responsible for:
 * - loading installed launcher apps
 * - maintaining the current search query
 * - exposing a combined UI state derived from settings and app data
 */
class AppsViewModel(app: Application) : AndroidViewModel(app) {

    // Persistent user settings (enabled packages, compat mode, etc.).
    private val store = SettingsStore(app.applicationContext)

    // In-memory cache of all launcher apps discovered on the device.
    private val allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    // Current search query entered by the user.
    private val query = MutableStateFlow("")

    /**
     * Hot stream of persisted user settings exposed to the UI layer.
     */
    val settingsModel: StateFlow<SettingsModel> = store.settingsModelFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsModel()
    )

    /**
     * Combined UI state derived from query, installed apps, and user settings.
     */
    val uiState: StateFlow<AppsUiState> = combine(
        query,
        allApps,
        settingsModel
    ) { q, apps, s ->
        AppsUiState(
            query = q,
            apps = apps,
            enabledPackages = s.enabledPackages
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppsUiState()
    )

    /**
     * Loads the list of launcher apps using the system PackageManager.
     */
    fun loadApps() {
        val pm = getApplication<Application>().packageManager
        allApps.value = InstalledApps.loadLauncherApps(pm)
    }

    /**
     * Updates the current search query.
     */
    fun setQuery(q: String) {
        query.value = q
    }

    /**
     * Toggles the enabled state of the given package and persists the change.
     */
    fun togglePackage(pkg: String) {
        viewModelScope.launch {
            store.togglePackage(pkg)
        }
    }
}