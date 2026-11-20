package com.id6assistant.wazedispatcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WazeAlertCatalogTest {
    @Test
    fun `catalog covers all documented types 1 through 75`() {
        for (type in 1..75) {
            val desc = WazeAlertCatalog.describe(type)
            assertEquals(type, desc.type)
            assertTrue("Label for $type must not be blank", desc.label.isNotBlank())
            assertTrue("Label for $type should be specific", !desc.label.startsWith("Cảnh báo #"))
        }
    }

    @Test
    fun `unknown alert type falls back gracefully`() {
        val desc = WazeAlertCatalog.describe(999)
        assertEquals(999, desc.type)
        assertEquals("Cảnh báo #999", desc.label)
        assertEquals(AlertCategory.GENERAL, desc.category)
    }

    @Test
    fun `formatDetail includes jam metadata and speed value`() {
        val jam = WazeAlert(type = 6, distanceM = 500, jamSeverity = 4, jamDelayMin = 12)
        assertEquals("Kẹt xe (Mức 4 · +12 phút) · 500 m", WazeAlertCatalog.formatDetail(jam))

        val limit = WazeAlert(type = 8, distanceM = 200, value = 50)
        assertEquals("Thay đổi giới hạn (50 km/h) · 200 m", WazeAlertCatalog.formatDetail(limit))
    }
}
