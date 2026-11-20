/**
 * AUTO-GENERATED FILE — DO NOT EDIT MANUALLY.
 * Source of truth: `include/id6_protocol_codec.h` via `tools/sync_protocol_sdk.py`
 *
 * Byte-for-byte golden vector tests comparing `Id6ProtocolCodec.kt` against C99/Python reference frames.
 */

package com.id6assistant.sdk

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Id6ProtocolCodecTest {
    @Test
    fun encodeAndDecodeSliMatchesReferenceBytes() {
        val expected = byteArrayOf(0x01.toByte(), 0x3C.toByte(), 0x04.toByte(), 0x01.toByte(), 0x1E.toByte())
        val actual = Id6ProtocolCodec.encodeSliPacket(
            SliInput(limitKmh = 60, minKmh = 30, overspeed = true, noPassing = true),
        )
        assertArrayEquals(expected, actual)

        val decoded = Id6ProtocolCodec.decodePacket(actual)
        assertNotNull(decoded)
        assertTrue(decoded!!.hasSli)
        assertFalse(decoded.hasLane)
        assertEquals(60, decoded.limitKmh)
        assertEquals(30, decoded.minKmh)
        assertTrue(decoded.overspeed)
        assertTrue(decoded.noPassing)

        val blankExpected = byteArrayOf(0x01.toByte(), 0xFF.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte())
        assertArrayEquals(blankExpected, Id6ProtocolCodec.encodeSliPacket(SliInput(limitKmh = 0)))
    }

    @Test
    fun encodeAndDecodeLaneMatchesReferenceBytes() {
        val expected = byteArrayOf(0x02.toByte(), 0x0B.toByte(), 0x19.toByte())
        val actual = Id6ProtocolCodec.encodeLanePacket(
            LaneInput(active = true, leftWarn = true, rightWarn = false, distWarn = true, distanceM = 25),
        )
        assertArrayEquals(expected, actual)

        val decoded = Id6ProtocolCodec.decodePacket(actual)
        assertNotNull(decoded)
        assertFalse(decoded!!.hasSli)
        assertTrue(decoded.hasLane)
        assertEquals(1, decoded.laneState)
        assertTrue(decoded.laneLeftWarn)
        assertFalse(decoded.laneRightWarn)
        assertTrue(decoded.distanceWarn)
        assertEquals(25, decoded.distanceM)
    }

    @Test
    fun encodeAndDecodeCombinedMatchesReferenceBytes() {
        val expected = byteArrayOf(0x03.toByte(), 0x50.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x05.toByte(), 0x2D.toByte())
        val actual = Id6ProtocolCodec.encodeCombinedPacket(
            SliInput(limitKmh = 80, minKmh = 0, overspeed = false, noPassing = true),
            LaneInput(active = true, leftWarn = false, rightWarn = true, distWarn = false, distanceM = 45),
        )
        assertArrayEquals(expected, actual)

        val decoded = Id6ProtocolCodec.decodePacket(actual)
        assertNotNull(decoded)
        assertTrue(decoded!!.hasSli)
        assertTrue(decoded.hasLane)
        assertEquals(80, decoded.limitKmh)
        assertEquals(45, decoded.distanceM)
    }

    @Test
    fun encodeAndDecodeNavMatchesReferenceBytes() {
        val expected = byteArrayOf(0x04.toByte(), 0x01.toByte(), 0x07.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0xFA.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x01.toByte(), 0x00.toByte(), 0x05.toByte(), 0x04.toByte(), 0x14.toByte(), 0x4E.toByte(), 0x67.toByte(), 0x75.toByte(), 0x79.toByte(), 0x65.toByte(), 0x6E.toByte(), 0x20.toByte(), 0x56.toByte(), 0x61.toByte(), 0x6E.toByte(), 0x20.toByte(), 0x4C.toByte(), 0x69.toByte(), 0x6E.toByte(), 0x68.toByte(), 0x20.toByte(), 0x20.toByte(), 0x51.toByte(), 0x2E.toByte(), 0x37.toByte())
        val actual = Id6ProtocolCodec.encodeNavPacket(
            NavInput(
                active = true,
                maneuver = Id6Bits.ID6_NAV_MANEUVER_LEFT,
                distanceM = 250,
                roundaboutExit = 0,
                roundaboutAngleDeg = 0,
                lanes = listOf(NavLaneInput(0x01, 0x00), NavLaneInput(0x05, 0x04)),
                street = "Nguyễn Văn Linh, Q.7",
            ),
        )
        assertArrayEquals(expected, actual)

        val decoded = Id6ProtocolCodec.decodePacket(actual)
        assertNotNull(decoded)
        assertTrue(decoded!!.hasNav)
        assertTrue(decoded.navActive)
        assertEquals(Id6Bits.ID6_NAV_MANEUVER_LEFT, decoded.navManeuver)
        assertEquals(250L, decoded.navDistanceM)
        assertEquals("Nguyen Van Linh  Q.7", decoded.navStreet)
        assertEquals(2, decoded.navLanes.size)
        assertEquals(0x04, decoded.navLanes[1].recommended)
    }

    @Test
    fun encodeAndDecodeRoundaboutWithNegativeAngleMatchesReferenceBytes() {
        val expected = byteArrayOf(0x04.toByte(), 0x01.toByte(), 0x22.toByte(), 0x03.toByte(), 0xA6.toByte(), 0xFF.toByte(), 0xB0.toByte(), 0x04.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x12.toByte(), 0x4C.toByte(), 0x65.toByte(), 0x20.toByte(), 0x4C.toByte(), 0x6F.toByte(), 0x69.toByte(), 0x20.toByte(), 0x2F.toByte(), 0x20.toByte(), 0x44.toByte(), 0x6F.toByte(), 0x6E.toByte(), 0x67.toByte(), 0x20.toByte(), 0x4B.toByte(), 0x68.toByte(), 0x6F.toByte(), 0x69.toByte())
        val actual = Id6ProtocolCodec.encodeNavPacket(
            NavInput(
                active = true,
                maneuver = Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT,
                distanceM = 1200,
                roundaboutExit = 3,
                roundaboutAngleDeg = -90,
                street = "Lê Lợi / Đồng Khởi",
            ),
        )
        assertArrayEquals(expected, actual)

        val decoded = Id6ProtocolCodec.decodePacket(actual)
        assertNotNull(decoded)
        assertEquals(3, decoded!!.navRoundaboutExit)
        assertEquals(-90, decoded.navRoundaboutAngleDeg)
        assertEquals(1200L, decoded.navDistanceM)
        assertEquals("Le Loi / Dong Khoi", decoded.navStreet)
    }

    @Test
    fun rejectMalformedPackets() {
        assertNull(Id6ProtocolCodec.decodePacket(byteArrayOf()))
        assertNull(Id6ProtocolCodec.decodePacket("{}".toByteArray()))
        assertNull(Id6ProtocolCodec.decodePacket(byteArrayOf(0x05, 0x00, 0x00, 0x00, 0x00)))
    }
}
