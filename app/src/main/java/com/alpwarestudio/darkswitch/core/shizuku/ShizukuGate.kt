package com.alpwarestudio.darkswitch.core.shizuku

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/**
 * Small helper responsible for interacting with Shizuku permission APIs.
 *
 * This class encapsulates:
 * - permission state checks
 * - permission request flow
 * - binder availability checks
 *
 * It is intentionally lifecycle-agnostic; callers are responsible for
 * registering and unregistering listeners at the appropriate time.
 */
class ShizukuGate {

    companion object {
        // Request code used for Shizuku permission callbacks.
        const val REQ_CODE = 10001
    }

    // One-shot callback invoked when the permission request result is received.
    private var onResult: ((granted: Boolean) -> Unit)? = null

    /**
     * Listener receiving the result of a Shizuku permission request.
     */
    private val permissionListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == REQ_CODE) {
                onResult?.invoke(grantResult == PackageManager.PERMISSION_GRANTED)
            }
        }

    /**
     * Registers the permission result listener with Shizuku.
     */
    fun register() {
        Shizuku.addRequestPermissionResultListener(permissionListener)
    }

    /**
     * Unregisters the permission result listener.
     */
    fun unregister() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
    }

    /**
     * Returns whether the Shizuku binder is alive and reachable.
     */
    fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    /**
     * Returns whether the app currently holds Shizuku permission.
     */
    fun isPermissionGranted(): Boolean =
        runCatching { Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED }
            .getOrDefault(false)

    /**
     * Requests Shizuku permission if needed and reports the result via callback.
     *
     * The callback is invoked synchronously when possible, or asynchronously
     * once the permission result is delivered.
     */
    fun requestPermission(callback: (granted: Boolean) -> Unit) {
        onResult = callback

        // Cannot proceed if Shizuku service is not running.
        if (!isBinderAlive()) {
            callback(false)
            return
        }

        // Permission already granted; short-circuit.
        if (isPermissionGranted()) {
            callback(true)
            return
        }

        // Shizuku indicates the permission request should not be shown again.
        if (Shizuku.shouldShowRequestPermissionRationale()) {
            callback(false)
            return
        }

        Shizuku.requestPermission(REQ_CODE)
    }
}