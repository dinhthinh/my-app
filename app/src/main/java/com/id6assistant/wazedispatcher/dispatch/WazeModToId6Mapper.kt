package com.id6assistant.wazedispatcher.dispatch

import com.id6assistant.sdk.Id6Bits
import com.id6assistant.sdk.NavInput
import com.id6assistant.sdk.NavLaneInput
import com.id6assistant.sdk.SliInput
import com.id6assistant.wazedispatcher.data.WazeModSnapshot

data class MappedId6Telemetry(
    val sli: SliInput,
    val nav: NavInput,
)

object WazeModToId6Mapper {
    private data class ManeuverMapping(
        val maneuver: Int,
        val angleDeg: Int = 0,
    )

    private val turnMap: Map<Int, ManeuverMapping> = mapOf(
        0 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_UNKNOWN),
        1 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_STRAIGHT),
        2 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_LEFT),
        3 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_RIGHT),
        4 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_SLIGHT_LEFT),
        5 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_SLIGHT_RIGHT),
        6 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_SHARP_LEFT),
        7 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_SHARP_RIGHT),
        8 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_U_TURN_LEFT),
        10 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_ENTER),
        11 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, -90),
        12 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, 90),
        13 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_KEEP_LEFT),
        14 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_KEEP_RIGHT),
        15 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_OFF_RAMP_LEFT),
        16 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_OFF_RAMP_RIGHT),
        17 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_DESTINATION),
        18 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_STRAIGHT),
        19 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, 180),
        20 to ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, 360),
    )

    fun map(snapshot: WazeModSnapshot): MappedId6Telemetry {
        val alertTypes = snapshot.alerts.map { it.type }
        val hasNoPassing = alertTypes.contains(9) &&
            !alertTypes.contains(10) &&
            !alertTypes.contains(25)

        val fallbackLimit = snapshot.alerts
            .firstOrNull { it.type == 8 && it.value > 0 }
            ?.value ?: 0
        val clearLimit = alertTypes.contains(22) || alertTypes.contains(25)

        val effectiveLimit = when {
            snapshot.speedLimit > 0 -> snapshot.speedLimit
            clearLimit -> 0
            else -> fallbackLimit
        }

        val sli = SliInput(
            limitKmh = effectiveLimit,
            minKmh = snapshot.speedMin,
            overspeed = snapshot.overspeed,
            noPassing = hasNoPassing,
        )

        val nav = if (snapshot.navigating) {
            val mappedTurn = turnMap[snapshot.turn] ?: ManeuverMapping(Id6Bits.ID6_NAV_MANEUVER_STRAIGHT)
            NavInput(
                active = true,
                maneuver = mappedTurn.maneuver,
                distanceM = maxOf(0L, snapshot.turnDistanceM.toLong()),
                roundaboutExit = snapshot.roundaboutExit,
                roundaboutAngleDeg = mappedTurn.angleDeg,
                lanes = snapshot.lanes
                    .take(Id6Bits.ID6_NAV_MAX_LANES)
                    .map { NavLaneInput(directions = it.directions, recommended = it.recommended) },
                street = snapshot.nextStreet.ifBlank { snapshot.currentStreet },
            )
        } else {
            NavInput(
                active = false,
                maneuver = Id6Bits.ID6_NAV_MANEUVER_UNKNOWN,
                distanceM = 0L,
            )
        }

        return MappedId6Telemetry(sli = sli, nav = nav)
    }
}
