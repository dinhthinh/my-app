package com.id6assistant.wazedispatcher.data

data class WazeLane(
    val directions: Int,
    val recommended: Int,
)

data class WazeAlert(
    val type: Int,
    val distanceM: Int,
    val value: Int = 0,
    val jamSeverity: Int = 0,
    val jamDelayMin: Int = 0,
)

data class WazeModSnapshot(
    val timestamp: Long = 0L,
    val navigating: Boolean = false,
    val speed: Int = 0,
    val speedLimit: Int = 0,
    val speedMin: Int = 0,
    val overspeed: Boolean = false,
    val turn: Int = 0,
    val thenTurn: Int = 0,
    val turnDistanceM: Int = 0,
    val roundaboutExit: Int = 0,
    val lanes: List<WazeLane> = emptyList(),
    val currentStreet: String = "",
    val nextStreet: String = "",
    val etaClock: String = "",
    val etaMinutes: Int = 0,
    val remainingM: Int = 0,
    val avgZone: Boolean = false,
    val avgRemainingM: Int = 0,
    val avgRecommendedSpeed: Int = 0,
    val avgPercent: Int = 0,
    val alerts: List<WazeAlert> = emptyList(),
)
