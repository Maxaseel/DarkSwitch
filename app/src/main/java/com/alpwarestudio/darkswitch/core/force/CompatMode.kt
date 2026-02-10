package com.alpwarestudio.darkswitch.core.force

/**
 * Represents alternative strategies to force or hint dark mode on devices
 * where standard APIs are insufficient or OEM-dependent.
 */

enum class CompatMode(
    // `title` is a user-facing label; `requiresRestartHint` indicates whether a reboot may be needed.
    val title: String,
    val requiresRestartHint: Boolean
) {
    // Forces dark rendering via HWUI debug system property (may be restricted on newer Android versions).
    HWUI_PROP("HWUI Prop (debug.hwui.force_dark)", true),
    // Toggles the global force-dark setting used by some system components and OEM skins.
    SETTINGS_GLOBAL("Settings Global (force_dark_mode_on)", true),
    // Fallback approach using the `cmd uimode night` shell command.
    CMD_UIMODE("cmd uimode night (fallback)", true);
}