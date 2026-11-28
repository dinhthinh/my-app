package com.id6assistant.wazedispatcher.car

import com.id6assistant.wazedispatcher.data.WazeAlert
import com.id6assistant.wazedispatcher.data.WazeModSnapshot
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import com.id6assistant.wazedispatcher.service.DispatcherUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CarHudUiModelTest {
    @Test
    fun stoppedServiceMapsToIdleDisconnected() {
        val state = DispatcherUiState(
            running = false,
            sourceStatus = WazeSourceStatus.DISCONNECTED,
            targetHost = "192.168.1.50",
            targetPort = 9090,
        )

        val model = CarHudUiModel.from(state)
        assertEquals(CarHudMode.IDLE_DISCONNECTED, model.mode)
        assertEquals("WazeMod · DISCONNECTED · 192.168.1.50:9090", model.targetLabel)
    }

    @Test
    fun permissionRequiredOverridesRunningIntoPermissionBanner() {
        val state = DispatcherUiState(
            running = true,
            sourceStatus = WazeSourceStatus.PERMISSION_REQUIRED,
        )

        val model = CarHudUiModel.from(state)
        assertEquals(CarHudMode.PERMISSION_REQUIRED, model.mode)
    }

    @Test
    fun activeConnectedStateFormatsSignsSpeedAndTopThreeAlerts() {
        val state = DispatcherUiState(
            running = true,
            sourceStatus = WazeSourceStatus.CONNECTED,
            snapshot = WazeModSnapshot(
                speed = 92,
                speedLimit = 80,
                speedMin = 50,
                overspeed = true,
                avgZone = true,
                avgPercent = 120,
                avgRemainingM = 340,
                avgRecommendedSpeed = 74,
                alerts = listOf(
                    WazeAlert(type = 2, distanceM = 120),
                    WazeAlert(type = 9, distanceM = 200),
                    WazeAlert(type = 28, distanceM = 250),
                    WazeAlert(type = 1, distanceM = 400),
                ),
            ),
        )

        val model = CarHudUiModel.from(state)
        assertEquals(CarHudMode.ACTIVE_HUD, model.mode)
        assertTrue(model.overspeed)
        assertEquals(100, model.avgPercent)
        assertEquals(listOf(9, 28), model.prohibitionTypes)
        assertEquals(4, model.totalAlertsCount)
        assertEquals(3, model.alerts.size)
        assertEquals("SPD", model.alerts[0].badge)
    }
}
