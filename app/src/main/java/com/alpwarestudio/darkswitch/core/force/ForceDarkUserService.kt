package com.alpwarestudio.darkswitch.core.force

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlin.system.exitProcess

/**
 * A lightweight bound service used to perform privileged operations via the app's AIDL interface.
 *
 * This service is intended to be invoked by trusted parts of the app to:
 * - toggle system-level force-dark flags (when possible)
 * - execute specific shell commands
 *
 * All operations are best-effort and may fail depending on Android version, OEM restrictions,
 * and the execution context.
 */
class ForceDarkUserService : Service() {

    /**
     * Binder implementation for the AIDL contract.
     *
     * Note: Keep this implementation minimal; it runs on Binder threads.
     */
    private val binder = object : IForceDarkService.Stub() {

        /**
         * Attempts to toggle the HWUI force-dark system property.
         *
         * @return true if the property set call was executed without throwing.
         */
        override fun setGlobalForceDark(enabled: Boolean): Boolean {
            return try {
                // Uses reflection into SystemProperties; may be blocked on some devices/versions.
                setSystemProperty(
                    "debug.hwui.force_dark",
                    if (enabled) "true" else "false"
                )
                true
            } catch (_: Throwable) {
                false
            }
        }

        /**
         * Executes a command via ProcessBuilder.
         *
         * @return process exit code on success, or a negative value on validation/execution failure.
         */
        override fun runCommand(cmd: Array<out String>?): Int {
            if (cmd == null || cmd.isEmpty()) return -1
            return try {
                val p = ProcessBuilder(*cmd)
                    .redirectErrorStream(true)
                    .start()
                p.inputStream.bufferedReader().readText()
                p.waitFor()
            } catch (_: Throwable) {
                -2
            }
        }

        /**
         * Terminates the service process.
         *
         * This is used as an explicit cleanup hook for the companion process hosting this service.
         */
        override fun destroy() {
            try {
                // Exits the process hosting the service; callers should ensure this is safe.
                exitProcess(0)
            } catch (_: Throwable) {
            }
        }

        // SystemProperties is a hidden API; access via reflection is discouraged and may break.
        @SuppressLint("PrivateApi", "DiscouragedPrivateApi")
        /**
         * Sets an Android system property via reflection.
         *
         * Hidden API access can be restricted; callers should treat failures as expected.
         */
        private fun setSystemProperty(key: String, value: String) {
            val cls = Class.forName("android.os.SystemProperties")
            val m = cls.getDeclaredMethod("set", String::class.java, String::class.java)
            m.isAccessible = true
            m.invoke(null, key, value)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder
}