package com.id6assistant.wazedispatcher.service

import com.id6assistant.wazedispatcher.data.MockScenarioConfig
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
