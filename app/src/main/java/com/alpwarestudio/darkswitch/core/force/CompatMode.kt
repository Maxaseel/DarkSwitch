package com.alpwarestudio.darkswitch.core.force

/** Supported best-effort force-dark mechanisms. AUTO tries safe candidates in order. */
enum class CompatMode(val title: String, val requiresRestartHint: Boolean) {
    AUTO("Automatic (recommended)", true),
    HWUI_PROP("Standard (HWUI)", true),
    SETTINGS_GLOBAL("Alternative (global setting)", true),
    CMD_UIMODE("Experimental (UI mode)", true);

    companion object {
        val concreteModes = listOf(HWUI_PROP, SETTINGS_GLOBAL, CMD_UIMODE)
    }
}
