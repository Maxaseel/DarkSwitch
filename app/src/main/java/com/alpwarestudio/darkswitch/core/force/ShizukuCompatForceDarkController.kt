package com.alpwarestudio.darkswitch.core.force

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/** Executes the project's best-effort force-dark strategies through Shizuku. */
class ShizukuCompatForceDarkController(
    private val context: Context,
    private val client: ForceDarkUserServiceClient = ForceDarkUserServiceClient(context)
) : ForceDarkController {

    override fun isAvailable(): Boolean =
        runCatching { Shizuku.pingBinder() }.getOrDefault(false) &&
            runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }
                .getOrDefault(false)

    suspend fun apply(mode: CompatMode, enabled: Boolean): Boolean {
        if (!isAvailable()) return false
        return when (mode) {
            CompatMode.AUTO -> false
            CompatMode.HWUI_PROP -> client.setGlobalForceDark(enabled)
            CompatMode.SETTINGS_GLOBAL -> client.runCommand(
                "settings", "put", "global", "force_dark_mode_on", if (enabled) "1" else "0"
            ) == 0
            CompatMode.CMD_UIMODE -> client.runCommand(
                "cmd", "uimode", "night", if (enabled) "yes" else "no"
            ) == 0
        }
    }

    suspend fun applyWithFallback(
        requestedMode: CompatMode,
        enabled: Boolean,
        preferredMode: CompatMode? = null
    ): ApplyResult {
        val candidates = when (requestedMode) {
            CompatMode.AUTO -> buildList {
                preferredMode?.takeIf { it != CompatMode.AUTO }?.let(::add)
                CompatMode.concreteModes.forEach { if (it !in this) add(it) }
            }
            else -> listOf(requestedMode)
        }
        val attempted = mutableListOf<CompatMode>()
        for (candidate in candidates) {
            attempted += candidate
            if (apply(candidate, enabled)) return ApplyResult(true, candidate, attempted)
        }
        return ApplyResult(false, null, attempted)
    }

    /** Restores all known switches to their neutral/off state. */
    suspend fun restoreAll(): Boolean {
        val results = CompatMode.concreteModes.map { apply(it, false) }
        return results.any { it }
    }

    override suspend fun setForceDarkForPackage(packageName: String, enabled: Boolean): Boolean =
        applyWithFallback(CompatMode.AUTO, enabled).success
}
