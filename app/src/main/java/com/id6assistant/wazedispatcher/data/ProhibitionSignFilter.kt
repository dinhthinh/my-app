package com.id6assistant.wazedispatcher.data

/**
 * Shared filtering rules for WazeMod prohibition traffic signs displayed on phone and Android Auto HUDs.
 */
object ProhibitionSignFilter {
    val PROHIBITION_SIGN_TYPES: Set<Int> = setOf(
        9,
        28, 29, 30, 31, 39,
        65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
    )

    private val CLEAR_NO_PASSING_TYPES: Set<Int> = setOf(10, 25)

    fun extractProhibitionAlerts(
        alerts: List<WazeAlert>,
        includeMockNoPassing: Boolean = false,
        maxCount: Int = 3,
    ): List<WazeAlert> {
        val alertTypes = alerts.map { it.type }.toSet()
        val clearNoPassing = alertTypes.any { it in CLEAR_NO_PASSING_TYPES }
        val baseRestrictions = alerts
            .filter { it.type in PROHIBITION_SIGN_TYPES && !(it.type == 9 && clearNoPassing) }
            .distinctBy { it.type }

        return if (includeMockNoPassing && baseRestrictions.none { it.type == 9 }) {
            (listOf(WazeAlert(type = 9, distanceM = 0)) + baseRestrictions).take(maxCount)
        } else {
            baseRestrictions.take(maxCount)
        }
    }
}
