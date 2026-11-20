package com.id6assistant.wazedispatcher.data

import android.os.Bundle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class WazeModBundleParserTest {
    @Test
    fun `null bundle returns safe default snapshot`() {
        val snap = WazeModBundleParser.parse(null)
        assertFalse(snap.navigating)
        assertEquals(0, snap.speed)
        assertEquals(0, snap.alerts.size)
    }

    @Test
    fun `empty bundle returns default fields`() {
        val snap = WazeModBundleParser.parse(Bundle())
        assertEquals(0, snap.speedLimit)
        assertEquals("", snap.currentStreet)
    }
}
