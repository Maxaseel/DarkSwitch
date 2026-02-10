package com.alpwarestudio.darkswitch.core.foreground

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

/**
 * Helper utilities for checking and requesting Android Usage Access permission.
 *
 * Usage Access is required to infer foreground app information via system usage stats
 * on devices where accessibility-based detection is unavailable or restricted.
 */
object UsageAccess {

    /**
     * Returns whether the app currently has Usage Access permission granted.
     */
    fun hasUsageAccess(context: Context): Boolean {
        // AppOps is the authoritative source for Usage Access permission state.
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        // Query usage stats access without throwing; result may vary by Android version/OEM.
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        // MODE_ALLOWED indicates the user explicitly granted Usage Access.
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Creates an intent that navigates the user to the Usage Access settings screen.
     */
    fun usageAccessSettingsIntent(): Intent =
        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}