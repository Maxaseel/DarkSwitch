package com.alpwarestudio.darkswitch.ui.apps

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alpwarestudio.darkswitch.core.apps.InstalledApps
import com.alpwarestudio.darkswitch.core.compatibility.CompatibilityProfile
import com.alpwarestudio.darkswitch.core.compatibility.CompatibilityRepository
import com.alpwarestudio.darkswitch.core.model.AppInfo
import com.alpwarestudio.darkswitch.data.prefs.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppRowModel(val app: AppInfo, val compatibility: CompatibilityProfile)

data class AppsUiState(
    val query: String = "",
    val apps: List<AppRowModel> = emptyList(),
    val enabledPackages: Set<String> = emptySet()
) {
    fun filteredApps(): List<AppRowModel> {
        if (query.isBlank()) return apps
        val q = query.trim().lowercase()
        return apps.filter {
            it.app.label.lowercase().contains(q) || it.app.packageName.lowercase().contains(q)
        }
    }
}

class AppsViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsStore(app.applicationContext)
    private val compatibility = CompatibilityRepository(app.applicationContext)
    private val allApps = MutableStateFlow<List<AppRowModel>>(emptyList())
    private val query = MutableStateFlow("")

    val uiState: StateFlow<AppsUiState> = combine(
        allApps, query, store.settingsModelFlow
    ) { apps, q, settings -> AppsUiState(q, apps, settings.enabledPackages) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState())

    fun loadApps() {
        viewModelScope.launch(Dispatchers.Default) {
            val pm = getApplication<Application>().packageManager
            allApps.value = InstalledApps.loadLauncherApps(pm)
                .filterNot { it.packageName == getApplication<Application>().packageName }
                .map { AppRowModel(it, compatibility.get(it.packageName)) }
        }
    }

    fun setQuery(value: String) { query.value = value }
    fun togglePackage(packageName: String) = viewModelScope.launch { store.togglePackage(packageName) }
}
