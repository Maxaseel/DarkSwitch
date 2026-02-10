package com.alpwarestudio.darkswitch.core.apps

import android.content.Intent
import android.content.pm.PackageManager
import com.alpwarestudio.darkswitch.core.model.AppInfo

/**
 * Utilities for discovering apps installed on the device via the PackageManager.
 */
object InstalledApps {

    /**
     * Returns apps that expose a launcher entry (ACTION_MAIN + CATEGORY_LAUNCHER).
     *
     * Note: This is a best-effort list based on intent resolution; it may differ by OEM/launcher.
     */
    fun loadLauncherApps(pm: PackageManager): List<AppInfo> {
        // Query activities that can be launched from the home screen.
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)

        return resolved
            .map { ri ->
                val pkg = ri.activityInfo.packageName
                val label = ri.loadLabel(pm).toString()
                AppInfo(packageName = pkg, label = label)
            }
            // Deduplicate in case multiple launcher activities map to the same package.
            .distinctBy { it.packageName }
            // Sort alphabetically for stable UI presentation.
            .sortedBy { it.label.lowercase() }
    }
}