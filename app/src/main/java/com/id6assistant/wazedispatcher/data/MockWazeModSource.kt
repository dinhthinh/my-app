package com.id6assistant.wazedispatcher.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class MockScenarioConfig(
    val navigating: Boolean = true,
    val speed: Int = 64,
    val speedLimit: Int = 60,
    val speedMin: Int = 30,
    val overspeed: Boolean = true,
    val turn: Int = 2,
    val turnDistanceM: Int = 250,
    val roundaboutExit: Int = 0,
    val nextStreet: String = "Lê Lợi",
    val currentStreet: String = "Nguyễn Huệ",
    val avgZone: Boolean = true,
    val avgRemainingM: Int = 1200,
    val avgRecommendedSpeed: Int = 58,
    val avgPercent: Int = 45,
    val alerts: List<WazeAlert> = listOf(
        WazeAlert(type = 2, distanceM = 320),
        WazeAlert(type = 9, distanceM = 150),
        WazeAlert(type = 6, distanceM = 500, jamSeverity = 3, jamDelayMin = 7),
    ),
)

class MockWazeModSource(
    private val configProvider: () -> MockScenarioConfig,
) {
    fun stream(intervalMs: Long = 500L): Flow<WazeModSnapshot> = flow {
        var tick = 0
        while (true) {
            val cfg = configProvider()
            val dynamicDist = if (cfg.navigating) maxOf(10, cfg.turnDistanceM - (tick * 15) % 200) else 0
            emit(
                WazeModSnapshot(
                    timestamp = System.currentTimeMillis(),
                    navigating = cfg.navigating,
                    speed = cfg.speed,
                    speedLimit = cfg.speedLimit,
                    speedMin = cfg.speedMin,
                    overspeed = cfg.overspeed,
                    turn = cfg.turn,
                    thenTurn = 1,
                    turnDistanceM = dynamicDist,
                    roundaboutExit = cfg.roundaboutExit,
                    lanes = if (cfg.navigating) {
                        listOf(
                            WazeLane(directions = 1, recommended = 0),
                            WazeLane(directions = 5, recommended = 4),
                            WazeLane(directions = 17, recommended = 0),
                        )
                    } else {
                        emptyList()
                    },
                    currentStreet = cfg.currentStreet,
                    nextStreet = cfg.nextStreet,
                    etaClock = "20:49",
                    etaMinutes = 12,
                    remainingM = 4800,
                    avgZone = cfg.avgZone,
                    avgRemainingM = cfg.avgRemainingM,
                    avgRecommendedSpeed = cfg.avgRecommendedSpeed,
                    avgPercent = cfg.avgPercent,
                    alerts = cfg.alerts.take(3),
                )
            )
            tick++
            delay(intervalMs)
        }
    }
}
