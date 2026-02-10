package com.alpwarestudio.darkswitch.core.foreground

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Simple in-memory event bus for propagating the currently foreground package name.
 *
 * This is implemented as a hot {@link StateFlow} so late subscribers always
 * receive the latest known foreground app.
 */
object ForegroundBus {
    // Backing state holding the last observed foreground package name.
    private val _pkg = MutableStateFlow("")
    // Public read-only stream exposed to observers.
    val pkg: StateFlow<String> = _pkg

    /**
     * Publishes a new foreground package value to all collectors.
     */
    fun emit(packageName: String) {
        _pkg.value = packageName
    }
}