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
import com.id6assistant.wazedispatcher.data.WazeModAidlClient
import com.id6assistant.wazedispatcher.service.DispatcherRepository
import com.id6assistant.wazedispatcher.service.Id6DispatchService
import com.id6assistant.wazedispatcher.ui.DashboardScreen

class MainActivity : ComponentActivity() {
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
                    onUpdateMock = { transform ->
                        DispatcherRepository.updateMockConfig(transform)
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
}
