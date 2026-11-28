package com.id6assistant.wazedispatcher.car

import com.id6assistant.wazedispatcher.data.AlertCategory
import com.id6assistant.wazedispatcher.data.ProhibitionSignFilter
import com.id6assistant.wazedispatcher.data.WazeAlertCatalog
import com.id6assistant.wazedispatcher.data.WazeSourceStatus
import com.id6assistant.wazedispatcher.service.DispatcherUiState

enum class CarHudMode {
    IDLE_DISCONNECTED,
    PERMISSION_REQUIRED,
    ACTIVE_HUD,
}

data class CarHudAlertItem(
    val badge: String,
    val category: AlertCategory,
    val detail: String,
)

data class CarHudUiModel(
    val mode: CarHudMode,
    val sourceStatus: WazeSourceStatus,
    val targetLabel: String,
    val speedLimit: Int,
    val speedMin: Int,
    val prohibitionTypes: List<Int>,
    val speed: Int,
    val overspeed: Boolean,
    val avgZone: Boolean,
    val avgPercent: Int,
    val avgRemainingM: Int,
    val avgRecommendedSpeed: Int,
    val totalAlertsCount: Int,
    val alerts: List<CarHudAlertItem>,
) {
    companion object {
        fun from(state: DispatcherUiState): CarHudUiModel {
            val mode = when {
                state.sourceStatus == WazeSourceStatus.PERMISSION_REQUIRED ||
                    state.sourceStatus == WazeSourceStatus.REVOKED -> CarHudMode.PERMISSION_REQUIRED
                !state.running -> CarHudMode.IDLE_DISCONNECTED
                else -> CarHudMode.ACTIVE_HUD
            }
            val snap = state.snapshot
            val prohibitions = ProhibitionSignFilter
                .extractProhibitionAlerts(snap.alerts)
                .map { it.type }
            val topAlerts = snap.alerts.take(3).map { alert ->
                val desc = WazeAlertCatalog.describe(alert.type)
                CarHudAlertItem(
                    badge = desc.badge,
                    category = desc.category,
                    detail = WazeAlertCatalog.formatDetail(alert),
                )
            }
            return CarHudUiModel(
                mode = mode,
                sourceStatus = state.sourceStatus,
                targetLabel = "WazeMod · ${state.sourceStatus.name} · ${state.targetHost}:${state.targetPort}",
                speedLimit = snap.speedLimit,
                speedMin = snap.speedMin,
                prohibitionTypes = prohibitions,
                speed = snap.speed,
                overspeed = snap.overspeed,
                avgZone = snap.avgZone,
                avgPercent = snap.avgPercent.coerceIn(0, 100),
                avgRemainingM = snap.avgRemainingM,
                avgRecommendedSpeed = snap.avgRecommendedSpeed,
                totalAlertsCount = snap.alerts.size,
                alerts = topAlerts,
            )
        }
    }
}
