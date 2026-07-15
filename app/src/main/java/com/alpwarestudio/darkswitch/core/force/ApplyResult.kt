package com.alpwarestudio.darkswitch.core.force

data class ApplyResult(
    val success: Boolean,
    val appliedMode: CompatMode? = null,
    val attemptedModes: List<CompatMode> = emptyList()
)
