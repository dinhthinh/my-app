package com.id6assistant.wazedispatcher

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.id6assistant.wazedispatcher.data.WazeModAidlClient
import com.id6assistant.wazedispatcher.dispatch.Id6TcpDispatcher
import com.id6assistant.wazedispatcher.dispatch.PacketLogEntry
import com.id6assistant.wazedispatcher.service.DispatcherRepository
import com.id6assistant.wazedispatcher.service.Id6DispatchService
import com.id6assistant.wazedispatcher.ui.DashboardScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val manualDispatcher = Id6TcpDispatcher()
    private val accessLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Id6DispatchService.startOrReload(this)
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme {
                val state by DispatcherRepository.state.collectAsState()
                DashboardScreen(
                    state = state,
                    onToggleRunning = {
                        if (state.running) {
                            Id6DispatchService.stop(this)
                        } else {
                            Id6DispatchService.startOrReload(this)
                        }
                    },
                    onSelectMode = { mode ->
                        DispatcherRepository.setSourceMode(mode)
                        if (state.running) {
                            Id6DispatchService.startOrReload(this)
                        }
                    },
                    onUpdateTarget = { host, port ->
                        DispatcherRepository.setTarget(host, port)
                        if (state.running) {
                            Id6DispatchService.startOrReload(this)
                        }
                    },
                    onUpdateMockSimulator = { autoSendEligible, transform ->
                        DispatcherRepository.updateMockSimulator(transform)
                        val updated = DispatcherRepository.state.value
                        if (autoSendEligible && updated.mockSimulator.autoSendCombined) {
                            sendManualPacket {
                                manualDispatcher.sendCombinedPacket(
                                    updated.mockSimulator.toSliInput(),
                                    updated.mockSimulator.toLaneInput(),
                                )
                            }
                        }
                    },
                    onSendSli = {
                        val sim = DispatcherRepository.state.value.mockSimulator
                        sendManualPacket { manualDispatcher.sendSliPacket(sim.toSliInput()) }
                    },
                    onSendLane = {
                        val sim = DispatcherRepository.state.value.mockSimulator
                        sendManualPacket { manualDispatcher.sendLanePacket(sim.toLaneInput()) }
                    },
                    onSendCombined = {
                        val sim = DispatcherRepository.state.value.mockSimulator
                        sendManualPacket {
                            manualDispatcher.sendCombinedPacket(sim.toSliInput(), sim.toLaneInput())
                        }
                    },
                    onSendNav = {
                        val sim = DispatcherRepository.state.value.mockSimulator
                        sendManualPacket { manualDispatcher.sendNavPacket(sim.toNavInput(active = true)) }
                    },
                    onClearNav = {
                        sendManualPacket { manualDispatcher.clearNav() }
                    },
                    onRequestWazeAccess = {
                        val intent = Intent(WazeModAidlClient.ACTION_REQUEST_ACCESS)
                        accessLauncher.launch(intent)
                    },
                    onClearLogs = {
                        DispatcherRepository.clearLogs()
                    },
                )
            }
        }
    }

    private fun sendManualPacket(block: suspend () -> PacketLogEntry) {
        val current = DispatcherRepository.state.value
        manualDispatcher.host = current.targetHost
        manualDispatcher.port = current.targetPort
        lifecycleScope.launch {
            val entry = block()
            DispatcherRepository.appendLogs(listOf(entry))
        }
    }
}
