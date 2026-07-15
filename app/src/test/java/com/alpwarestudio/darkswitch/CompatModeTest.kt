package com.alpwarestudio.darkswitch

import com.alpwarestudio.darkswitch.core.force.CompatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class CompatModeTest {
    @Test fun concreteModesExcludeAutoAndRemainStable() {
        assertFalse(CompatMode.concreteModes.contains(CompatMode.AUTO))
        assertEquals(3, CompatMode.concreteModes.size)
    }
}
