package com.alpwarestudio.darkswitch.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alpwarestudio.darkswitch.core.force.CompatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Preferences DataStore used for persisting lightweight app settings.
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Immutable snapshot of user settings persisted in DataStore.
 */
data class SettingsModel(
    // Whether the global force-dark toggle is enabled.
    val globalEnabled: Boolean = false,
    // Package names explicitly enabled for force-dark (when per-app mode is used).
    val enabledPackages: Set<String> = emptySet(),
    // Selected compat strategy used when applying force-dark.
    val compatMode: CompatMode = CompatMode.HWUI_PROP
)

/**
 * DataStore-backed persistence layer for {@link SettingsModel}.
 */
class SettingsStore(private val context: Context) {

    // DataStore preference keys.
    private object Keys {
        val GLOBAL_ENABLED = booleanPreferencesKey("global_enabled")
        val ENABLED_PACKAGES = stringSetPreferencesKey("enabled_packages")
        val COMPAT_MODE = stringPreferencesKey("compat_mode")
    }

    /**
     * Hot stream of settings snapshots.
     *
     * Unknown/invalid enum values are safely mapped to the default compat mode.
     */
    val settingsModelFlow: Flow<SettingsModel> = context.dataStore.data.map { prefs ->
        // Stored as enum name for stability across app upgrades.
        val modeStr = prefs[Keys.COMPAT_MODE]
        // Defensive parsing in case a stored value becomes outdated or corrupted.
        val mode = runCatching { CompatMode.valueOf(modeStr ?: CompatMode.HWUI_PROP.name) }
            .getOrDefault(CompatMode.HWUI_PROP)

        val s = SettingsModel(
            globalEnabled = prefs[Keys.GLOBAL_ENABLED] ?: false,
            enabledPackages = prefs[Keys.ENABLED_PACKAGES] ?: emptySet(),
            compatMode = mode
        )
        s
    }

    suspend fun setCompatMode(mode: CompatMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COMPAT_MODE] = mode.name
        }
    }

    suspend fun setGlobalEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GLOBAL_ENABLED] = enabled
        }
    }

    /**
     * Adds or removes a package name from the enabled set.
     */
    suspend fun setPackageEnabled(packageName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_PACKAGES] ?: emptySet()
            val next = if (enabled) current + packageName else current - packageName
            prefs[Keys.ENABLED_PACKAGES] = next
        }
    }

    suspend fun setEnabledPackages(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ENABLED_PACKAGES] = packages.toSet()
        }
    }

    /**
     * Toggles a package name in the enabled set.
     */
    suspend fun togglePackage(packageName: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.ENABLED_PACKAGES] ?: emptySet()
            val next = if (current.contains(packageName)) current - packageName else current + packageName
            prefs[Keys.ENABLED_PACKAGES] = next
        }
    }
}