package com.id6assistant.wazedispatcher.dispatch

import com.id6assistant.sdk.Id6Bits
import com.id6assistant.wazedispatcher.data.WazeAlert
import com.id6assistant.wazedispatcher.data.WazeLane
import com.id6assistant.wazedispatcher.data.WazeModSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WazeModToId6MapperTest {
    @Test
    fun `maps no-passing and limit change alerts into SLI`() {
        val snap = WazeModSnapshot(
            speedLimit = 0,
            speedMin = 30,
            overspeed = true,
            alerts = listOf(
                WazeAlert(type = 8, distanceM = 100, value = 60),
                WazeAlert(type = 9, distanceM = 150),
            ),
        )
        val mapped = WazeModToId6Mapper.map(snap)
        assertEquals(60, mapped.sli.limitKmh)
        assertEquals(30, mapped.sli.minKmh)
        assertTrue(mapped.sli.overspeed)
        assertTrue(mapped.sli.noPassing)
    }

    @Test
    fun `clears no-passing when end-of-prohibition alert present`() {
        val snap = WazeModSnapshot(
            speedLimit = 80,
            alerts = listOf(
                WazeAlert(type = 9, distanceM = 50),
                WazeAlert(type = 10, distanceM = 10),
            ),
        )
        val mapped = WazeModToId6Mapper.map(snap)
        assertFalse(mapped.sli.noPassing)
    }

    @Test
    fun `maps all WazeMod turn codes and street name`() {
        val snap = WazeModSnapshot(
            navigating = true,
            turn = 11,
            turnDistanceM = 180,
            roundaboutExit = 2,
            lanes = listOf(WazeLane(directions = 5, recommended = 4)),
            nextStreet = "Lê Lợi",
        )
        val mapped = WazeModToId6Mapper.map(snap)
        assertTrue(mapped.nav.active)
        assertEquals(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, mapped.nav.maneuver)
        assertEquals(-90, mapped.nav.roundaboutAngleDeg)
        assertEquals(2, mapped.nav.roundaboutExit)
        assertEquals(180L, mapped.nav.distanceM)
        assertEquals(1, mapped.nav.lanes.size)
        assertEquals("Lê Lợi", mapped.nav.street)
    }

    @Test
    fun `inactive navigation emits nav active false`() {
        val mapped = WazeModToId6Mapper.map(WazeModSnapshot(navigating = false))
        assertFalse(mapped.nav.active)
    }
}
