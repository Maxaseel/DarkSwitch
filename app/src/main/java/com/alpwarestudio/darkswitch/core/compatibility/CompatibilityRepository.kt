package com.alpwarestudio.darkswitch.core.compatibility

import android.content.Context
import com.alpwarestudio.darkswitch.core.force.CompatMode
import org.json.JSONObject

/** Reads the small, offline-first compatibility database bundled with the APK. */
class CompatibilityRepository(private val context: Context) {
    private val profiles: Map<String, CompatibilityProfile> by lazy { loadProfiles() }

    fun get(packageName: String): CompatibilityProfile =
        profiles[packageName] ?: CompatibilityProfile(packageName)

    private fun loadProfiles(): Map<String, CompatibilityProfile> = runCatching {
        val json = context.assets.open("compatibility_database.json")
            .bufferedReader().use { it.readText() }
        val apps = JSONObject(json).getJSONArray("apps")
        buildMap {
            for (index in 0 until apps.length()) {
                val item = apps.getJSONObject(index)
                val packageName = item.getString("packageName")
                put(
                    packageName,
                    CompatibilityProfile(
                        packageName = packageName,
                        status = runCatching {
                            CompatibilityStatus.valueOf(item.optString("status", "UNKNOWN"))
                        }.getOrDefault(CompatibilityStatus.UNKNOWN),
                        recommendedMode = item.optString("recommendedMode")
                            .takeIf { it.isNotBlank() }
                            ?.let { runCatching { CompatMode.valueOf(it) }.getOrNull() },
                        notes = item.optString("notes").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }.getOrDefault(emptyMap())
}
