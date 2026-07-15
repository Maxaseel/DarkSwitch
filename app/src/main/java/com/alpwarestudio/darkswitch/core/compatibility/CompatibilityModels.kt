package com.alpwarestudio.darkswitch.core.compatibility

import com.alpwarestudio.darkswitch.core.force.CompatMode

enum class CompatibilityStatus { WORKING, PARTIAL, NOT_WORKING, UNKNOWN }

data class CompatibilityProfile(
    val packageName: String,
    val status: CompatibilityStatus = CompatibilityStatus.UNKNOWN,
    val recommendedMode: CompatMode? = null,
    val notes: String? = null
)
