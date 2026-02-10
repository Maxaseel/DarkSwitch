package com.alpwarestudio.darkswitch.core.foreground

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service used to observe foreground app changes.
 *
 * This service listens for window-related accessibility events and emits
 * the currently visible package name via {@link ForegroundBus}.
 *
 * Note: No accessibility data is read or stored; events are used solely
 * to infer the active application.
 */
@SuppressLint("AccessibilityPolicy")
class ForegroundAccessibilityService : AccessibilityService() {

    /**
     * Receives accessibility events and filters them to detect foreground app changes.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        // Limit handling to window change events to reduce noise and overhead.
        val typeOk = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

        if (!typeOk) return

        // Extract the source package; may be null or empty depending on the event.
        val pkg = event.packageName?.toString()?.trim().orEmpty()
        if (pkg.isEmpty()) return

        // Notify observers about the currently foreground package.
        ForegroundBus.emit(pkg)
    }

    /**
     * Called when the system interrupts the accessibility service.
     *
     * No-op: this service maintains no ongoing state.
     */
    override fun onInterrupt() = Unit
}