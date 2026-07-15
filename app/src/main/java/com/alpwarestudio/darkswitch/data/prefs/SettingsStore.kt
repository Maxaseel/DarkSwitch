package com.alpwarestudio.darkswitch.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alpwarestudio.darkswitch.core.force.CompatMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class SettingsModel(
    val globalEnabled: Boolean = false,
    val enabledPackages: Set<String> = emptySet(),
    val compatMode: CompatMode = CompatMode.AUTO,
    val learnedModes: Map<String, CompatMode> = emptyMap()
)

class SettingsStore(private val context: Context) {
    private object Keys {
        val GLOBAL_ENABLED = booleanPreferencesKey("global_enabled")
        val ENABLED_PACKAGES = stringSetPreferencesKey("enabled_packages")
        val COMPAT_MODE = stringPreferencesKey("compat_mode")
        val LEARNED_MODES = stringSetPreferencesKey("learned_modes")
    }

    val settingsModelFlow: Flow<SettingsModel> = context.dataStore.data.map { prefs ->
        SettingsModel(
            globalEnabled = prefs[Keys.GLOBAL_ENABLED] ?: false,
            enabledPackages = prefs[Keys.ENABLED_PACKAGES] ?: emptySet(),
            compatMode = runCatching {
                CompatMode.valueOf(prefs[Keys.COMPAT_MODE] ?: CompatMode.AUTO.name)
            }.getOrDefault(CompatMode.AUTO),
            learnedModes = decodeModes(prefs[Keys.LEARNED_MODES] ?: emptySet())
        )
    }

    suspend fun setCompatMode(mode: CompatMode) = context.dataStore.edit {
        it[Keys.COMPAT_MODE] = mode.name
    }

    suspend fun setGlobalEnabled(enabled: Boolean) = context.dataStore.edit {
        it[Keys.GLOBAL_ENABLED] = enabled
    }

    suspend fun togglePackage(packageName: String) = context.dataStore.edit { prefs ->
        val current = prefs[Keys.ENABLED_PACKAGES] ?: emptySet()
        prefs[Keys.ENABLED_PACKAGES] = if (packageName in current) current - packageName else current + packageName
    }

    suspend fun rememberMode(packageName: String, mode: CompatMode) = context.dataStore.edit { prefs ->
        val current = decodeModes(prefs[Keys.LEARNED_MODES] ?: emptySet()).toMutableMap()
        current[packageName] = mode
        prefs[Keys.LEARNED_MODES] = encodeModes(current)
    }

    suspend fun resetSelections() = context.dataStore.edit { prefs ->
        prefs[Keys.GLOBAL_ENABLED] = false
        prefs[Keys.ENABLED_PACKAGES] = emptySet()
        prefs[Keys.LEARNED_MODES] = emptySet()
    }

    private fun decodeModes(raw: Set<String>): Map<String, CompatMode> = raw.mapNotNull { entry ->
        val separator = entry.lastIndexOf('|')
        if (separator <= 0) return@mapNotNull null
        val pkg = entry.substring(0, separator)
        val mode = runCatching { CompatMode.valueOf(entry.substring(separator + 1)) }.getOrNull()
        mode?.let { pkg to it }
    }.toMap()

    private fun encodeModes(map: Map<String, CompatMode>): Set<String> =
        map.mapTo(mutableSetOf()) { (pkg, mode) -> "$pkg|${mode.name}" }
}
