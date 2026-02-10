package com.alpwarestudio.darkswitch.core.force

import android.content.Context
import rikka.shizuku.Shizuku

/**
 * {@link ForceDarkController} implementation backed by Shizuku.
 *
 * Uses a companion user service process to execute operations that normally require elevated
 * privileges (e.g., changing hidden properties or running shell commands).
 */
class ShizukuCompatForceDarkController(
    private val context: Context,
    private val client: ForceDarkUserServiceClient = ForceDarkUserServiceClient(context)
) : ForceDarkController {

    /**
     * Returns true when Shizuku is reachable and the app has been granted Shizuku permission.
     */
    override fun isAvailable(): Boolean =
        // `pingBinder` verifies Shizuku is running and its binder is accessible.
        runCatching { Shizuku.pingBinder() }.getOrDefault(false) &&
        // Permission gate: without it, binding/IPC to the user service will fail.
        runCatching { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED }
            .getOrDefault(false)

    /**
     * Applies the selected compat strategy.
     *
     * This is a device/OEM-dependent best-effort operation.
     */
    suspend fun apply(mode: CompatMode, enabled: Boolean): Boolean {
        if (!isAvailable()) return false

        // Delegate to the user service for the chosen compat mechanism.
        return when (mode) {
            CompatMode.HWUI_PROP -> client.setGlobalForceDark(enabled)

            CompatMode.SETTINGS_GLOBAL -> {
                val v = if (enabled) "1" else "0"
                client.runCommand("settings", "put", "global", "force_dark_mode_on", v) == 0
            }

            CompatMode.CMD_UIMODE -> {
                val v = if (enabled) "yes" else "no"
                client.runCommand("cmd", "uimode", "night", v) == 0
            }
        }
    }

    /**
     * Current Shizuku-based implementation toggles a global flag rather than a per-package override.
     *
     * @param packageName currently unused; kept to satisfy the controller interface
     */
    override suspend fun setForceDarkForPackage(packageName: String, enabled: Boolean): Boolean {
        // Package-level control is not supported by this strategy; fall back to global force-dark.
        return client.setGlobalForceDark(enabled)
    }
}