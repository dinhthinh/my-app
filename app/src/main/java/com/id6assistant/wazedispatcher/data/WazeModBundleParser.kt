package com.id6assistant.wazedispatcher.data

import android.os.Bundle

object WazeModBundleParser {
    @Suppress("DEPRECATION")
    fun parse(bundle: Bundle?): WazeModSnapshot {
        if (bundle == null) return WazeModSnapshot()

        val lanes = bundle.getParcelableArray("lanes")
            ?.filterIsInstance<Bundle>()
            ?.map { lane ->
                WazeLane(
                    directions = lane.getInt("directions", 0),
                    recommended = lane.getInt("recommended", 0),
                )
            }
            ?: emptyList()

        val alerts = bundle.getParcelableArray("alerts")
            ?.filterIsInstance<Bundle>()
            ?.map { alert ->
                WazeAlert(
                    type = alert.getInt("type", 0),
                    distanceM = alert.getInt("distance_m", 0),
                    value = alert.getInt("value", 0),
                    jamSeverity = alert.getInt("jam_severity", 0),
                    jamDelayMin = alert.getInt("jam_delay_min", 0),
                )
            }
            ?: emptyList()

        return WazeModSnapshot(
            timestamp = bundle.getLong("timestamp", 0L),
            navigating = bundle.getBoolean("navigating", false),
            speed = bundle.getInt("speed", 0),
            speedLimit = bundle.getInt("speed_limit", 0),
            speedMin = bundle.getInt("speed_min", 0),
            overspeed = bundle.getBoolean("overspeed", false),
            turn = bundle.getInt("turn", 0),
            thenTurn = bundle.getInt("then_turn", 0),
            turnDistanceM = bundle.getInt("turn_distance_m", 0),
            roundaboutExit = bundle.getInt("roundabout_exit", 0),
            lanes = lanes,
            currentStreet = bundle.getString("current_street", "") ?: "",
            nextStreet = bundle.getString("next_street", "") ?: "",
            etaClock = bundle.getString("eta_clock", "") ?: "",
            etaMinutes = bundle.getInt("eta_minutes", 0),
            remainingM = bundle.getInt("remaining_m", 0),
            avgZone = bundle.getBoolean("avg_zone", false),
            avgRemainingM = bundle.getInt("avg_remaining_m", 0),
            avgRecommendedSpeed = bundle.getInt("avg_recommended_speed", 0),
            avgPercent = bundle.getInt("avg_percent", 0),
            alerts = alerts,
        )
    }
}
