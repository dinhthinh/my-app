package com.id6assistant.wazedispatcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ProhibitionSignFilterTest {
    @Test
    fun filtersProhibitionTypesDeduplicatesAndCapsAtThree() {
        val alerts = listOf(
            WazeAlert(type = 1, distanceM = 100), // not prohibition
            WazeAlert(type = 28, distanceM = 120),
            WazeAlert(type = 28, distanceM = 200), // duplicate
            WazeAlert(type = 29, distanceM = 300),
            WazeAlert(type = 30, distanceM = 400),
            WazeAlert(type = 31, distanceM = 500), // exceeds max 3
        )

        val result = ProhibitionSignFilter.extractProhibitionAlerts(alerts)
        assertEquals(listOf(28, 29, 30), result.map { it.type })
    }

    @Test
    fun clearNoPassingAlertRemovesNoPassingSign() {
        val alerts = listOf(
            WazeAlert(type = 9, distanceM = 80),
            WazeAlert(type = 10, distanceM = 90), // clears no passing
            WazeAlert(type = 28, distanceM = 150),
        )

        val result = ProhibitionSignFilter.extractProhibitionAlerts(alerts)
        assertEquals(listOf(28), result.map { it.type })
    }

    @Test
    fun clearNoPassingAlertType25RemovesNoPassingSign() {
        val alerts = listOf(
            WazeAlert(type = 9, distanceM = 80),
            WazeAlert(type = 25, distanceM = 90),
        )

        val result = ProhibitionSignFilter.extractProhibitionAlerts(alerts)
        assertEquals(emptyList<Int>(), result.map { it.type })
    }

    @Test
    fun mockNoPassingPrependsType9WhenAbsent() {
        val alerts = listOf(
            WazeAlert(type = 28, distanceM = 150),
            WazeAlert(type = 29, distanceM = 200),
            WazeAlert(type = 30, distanceM = 250),
        )

        val result = ProhibitionSignFilter.extractProhibitionAlerts(alerts, includeMockNoPassing = true)
        assertEquals(listOf(9, 28, 29), result.map { it.type })
    }
}
