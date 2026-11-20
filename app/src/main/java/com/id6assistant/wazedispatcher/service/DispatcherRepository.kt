package com.id6assistant.wazedispatcher.service

import com.id6assistant.wazedispatcher.data.MockScenarioConfig
import com.id6assistant.wazedispatcher.data.MockSimulatorState
import com.id6assistant.wazedispatcher.data.WazeAlert
import com.id6assistant.wazedispatcher.data.WazeModSnapshot
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import com.id6assistant.wazedispatcher.dispatch.PacketLogEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TelemetrySourceMode {
    LIVE_AIDL,
    MOCK_SIMULATOR,
}

data class DispatcherUiState(
    val running: Boolean = false,
    val sourceMode: TelemetrySourceMode = TelemetrySourceMode.MOCK_SIMULATOR,
    val sourceStatus: WazeSourceStatus = WazeSourceStatus.DISCONNECTED,
    val statusMessage: String? = null,
    val targetHost: String = "10.0.2.2",
    val targetPort: Int = 9090,
    val snapshot: WazeModSnapshot = WazeModSnapshot(),
    val mockConfig: MockScenarioConfig = MockScenarioConfig(),
    val mockSimulator: MockSimulatorState = MockSimulatorState(),
    val packetLogs: List<PacketLogEntry> = emptyList(),
)

object DispatcherRepository {
    private val _state = MutableStateFlow(DispatcherUiState())
    val state: StateFlow<DispatcherUiState> = _state.asStateFlow()

    fun setRunning(running: Boolean) {
        _state.update { it.copy(running = running) }
    }

    fun setSourceMode(mode: TelemetrySourceMode) {
        _state.update { it.copy(sourceMode = mode) }
    }

    fun setTarget(host: String, port: Int) {
        _state.update { it.copy(targetHost = host.trim(), targetPort = port) }
    }

    fun updateMockConfig(transform: (MockScenarioConfig) -> MockScenarioConfig) {
        _state.update { it.copy(mockConfig = transform(it.mockConfig)) }
    }

    fun updateMockSimulator(transform: (MockSimulatorState) -> MockSimulatorState) {
        _state.update { current ->
            val nextSim = transform(current.mockSimulator)
            val baseAlerts = current.mockConfig.alerts.filterNot { it.type == 9 }
            val syncedAlerts = if (nextSim.noPassingSign) {
                listOf(WazeAlert(type = 9, distanceM = 120)) + baseAlerts.take(2)
            } else {
                baseAlerts
            }
            current.copy(
                mockSimulator = nextSim,
                mockConfig = current.mockConfig.copy(
                    speedLimit = nextSim.maxSpeedKmh,
                    speedMin = nextSim.minSpeedKmh,
                    overspeed = nextSim.overspeedBlink,
                    turnDistanceM = nextSim.navDistanceM.toInt().coerceAtLeast(0),
                    roundaboutExit = nextSim.navRoundaboutExit,
                    nextStreet = nextSim.navStreet,
                    alerts = syncedAlerts,
                ),
            )
        }
    }

    fun updateStatus(status: WazeSourceStatus, message: String?) {
        _state.update { it.copy(sourceStatus = status, statusMessage = message) }
    }

    fun updateSnapshot(snapshot: WazeModSnapshot) {
        _state.update { it.copy(snapshot = snapshot) }
    }

    fun appendLogs(entries: List<PacketLogEntry>) {
        if (entries.isEmpty()) return
        _state.update { current ->
            val combined = (entries.reversed() + current.packetLogs).take(40)
            current.copy(packetLogs = combined)
        }
    }

    fun clearLogs() {
        _state.update { it.copy(packetLogs = emptyList()) }
    }
}
