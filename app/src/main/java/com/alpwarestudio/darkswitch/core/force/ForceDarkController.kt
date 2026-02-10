package com.alpwarestudio.darkswitch.core.force

/**
 * Abstraction for mechanisms that enable or disable force-dark behavior
 * for a specific application package.
 *
 * Implementations may rely on system properties, global settings,
 * or shell commands depending on device and Android version.
 */
interface ForceDarkController {
    /**
     * Returns whether this controller can operate on the current device.
     */
    fun isAvailable(): Boolean

    /**
     * Applies or removes force-dark for the given package.
     *
     * @param packageName target application package name
     * @param enabled true to enable force-dark, false to disable
     * @return true if the operation was executed successfully
     */
    suspend fun setForceDarkForPackage(packageName: String, enabled: Boolean): Boolean
}