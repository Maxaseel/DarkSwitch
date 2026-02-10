package com.alpwarestudio.darkswitch.core.model

/**
 * Lightweight model representing an installed application.
 *
 * This model is intentionally minimal and used across the core layer
 * where only the package identifier and user-visible label are required.
 */
data class AppInfo(
    // Unique application package identifier.
    val packageName: String,
    // User-visible application name resolved from PackageManager.
    val label: String
)