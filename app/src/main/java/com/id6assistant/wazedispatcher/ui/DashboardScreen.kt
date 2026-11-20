package com.id6assistant.wazedispatcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.id6assistant.wazedispatcher.data.AlertCategory
import com.id6assistant.wazedispatcher.data.MockSimulatorState
import com.id6assistant.wazedispatcher.data.WazeAlertCatalog
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import com.id6assistant.wazedispatcher.service.DispatcherUiState
import com.id6assistant.wazedispatcher.service.TelemetrySourceMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    state: DispatcherUiState,
    onToggleRunning: () -> Unit,
    onSelectMode: (TelemetrySourceMode) -> Unit,
    onUpdateTarget: (String, Int) -> Unit,
    onUpdateMockSimulator: (autoSendEligible: Boolean, (MockSimulatorState) -> MockSimulatorState) -> Unit,
    onSendSli: () -> Unit,
    onSendLane: () -> Unit,
    onSendCombined: () -> Unit,
    onSendNav: () -> Unit,
    onClearNav: () -> Unit,
    onRequestWazeAccess: () -> Unit,
    onClearLogs: () -> Unit,
) {
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsSheet(
            currentHost = state.targetHost,
            currentPort = state.targetPort,
            onSave = onUpdateTarget,
            onDismiss = { showSettings = false },
        )
    }

    Scaffold(
        containerColor = Color(0xFF0F172A),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "RoadCast ID6",
                        color = Color(0xFFF8FAFC),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${state.targetHost}:${state.targetPort} · ${state.sourceStatus.name}",
                        color = if (state.sourceStatus == WazeSourceStatus.CONNECTED) {
                            Color(0xFF4ADE80)
                        } else {
                            Color(0xFF94A3B8)
                        },
                        fontSize = 12.sp,
                    )
                }
                IconButton(onClick = { showSettings = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFF8FAFC),
                    )
                }
            }

            if (!state.statusMessage.isNullOrBlank()) {
                Text(
                    text = state.statusMessage,
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                )
            }

            // Source Mode & Start/Stop
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = state.sourceMode == TelemetrySourceMode.LIVE_AIDL,
                    onClick = { onSelectMode(TelemetrySourceMode.LIVE_AIDL) },
                    label = { Text("WazeMod AIDL") },
                )
                FilterChip(
                    selected = state.sourceMode == TelemetrySourceMode.MOCK_SIMULATOR,
                    onClick = { onSelectMode(TelemetrySourceMode.MOCK_SIMULATOR) },
                    label = { Text("Mock Simulator") },
                )
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onToggleRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.running) Color(0xFFDC2626) else Color(0xFF16A34A),
                    ),
                ) {
                    Icon(
                        imageVector = if (state.running) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null,
                    )
                    Text(if (state.running) "Dừng" else "Chạy")
                }
            }

            if (state.sourceStatus == WazeSourceStatus.PERMISSION_REQUIRED ||
                state.sourceStatus == WazeSourceStatus.REVOKED
            ) {
                OutlinedButton(
                    onClick = onRequestWazeAccess,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Xin quyền truy cập WazeMod V14+", color = Color(0xFFFBBF24))
                }
            }

            // Speed & Limit Card
            val snap = state.snapshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Tốc độ hiện tại", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${snap.speed}",
                            color = if (snap.overspeed) Color(0xFFF87171) else Color(0xFFF8FAFC),
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = " km/h",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    if (snap.speedMin > 0) {
                        Text(
                            text = "Tối thiểu: ${snap.speedMin} km/h",
                            color = Color(0xFF38BDF8),
                            fontSize = 12.sp,
                        )
                    }
                }

                // Speed Limit Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(6.dp, Color(0xFFEF4444), CircleShape),
                ) {
                    Text(
                        text = if (snap.speedLimit > 0) "${snap.speedLimit}" else "--",
                        color = Color(0xFF0F172A),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                    )
                }
            }

            // Average Speed Zone
            if (snap.avgZone) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Vùng đo tốc độ trung bình", color = Color(0xFFFBBF24), fontWeight = FontWeight.SemiBold)
                        Text("${snap.avgPercent}%", color = Color(0xFFF8FAFC))
                    }
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (snap.avgPercent.coerceIn(0, 100)) / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF59E0B),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Còn ${snap.avgRemainingM} m · Khuyến nghị ${snap.avgRecommendedSpeed} km/h",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                    )
                }
            }

            // Alerts Card (1..75)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Cảnh báo WazeMod (${snap.alerts.size})",
                    color = Color(0xFFF8FAFC),
                    fontWeight = FontWeight.SemiBold,
                )
                if (snap.alerts.isEmpty()) {
                    Text("Không có cảnh báo phía trước.", color = Color(0xFF64748B), fontSize = 12.sp)
                } else {
                    snap.alerts.take(3).forEach { alert ->
                        val desc = WazeAlertCatalog.describe(alert.type)
                        val badgeColor = when (desc.category) {
                            AlertCategory.CAMERA -> Color(0xFFEF4444)
                            AlertCategory.POLICE -> Color(0xFF3B82F6)
                            AlertCategory.TRAFFIC -> Color(0xFFF59E0B)
                            AlertCategory.RESTRICTION -> Color(0xFFEC4899)
                            AlertCategory.WEATHER -> Color(0xFF06B6D4)
                            else -> Color(0xFF64748B)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(badgeColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(desc.badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = WazeAlertCatalog.formatDetail(alert),
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            // Mock Simulator (Web-style 4-section simulator)
            if (state.sourceMode == TelemetrySourceMode.MOCK_SIMULATOR) {
                MockSimulatorPanel(
                    sim = state.mockSimulator,
                    onUpdate = onUpdateMockSimulator,
                    onSendSli = onSendSli,
                    onSendLane = onSendLane,
                    onSendCombined = onSendCombined,
                    onSendNav = onSendNav,
                    onClearNav = onClearNav,
                )
            }

            PacketLogPanel(
                logs = state.packetLogs,
                onClear = onClearLogs,
            )
        }
    }
}
