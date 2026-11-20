/**
 * AUTO-GENERATED FILE — DO NOT EDIT MANUALLY.
 * Source of truth: `include/id6_protocol_codec.h`
 * Regenerate with: `python3 tools/sync_protocol_sdk.py`
 *
 * Shared Bitwise Wire Codec for the Native Android (Kotlin) Companion App / Service.
 * Produces compact 3-byte (`LANE`), 5-byte (`SLI`), 7-byte (`COMBINED`), and variable-length
 * (`NAV`, `0x04`) binary frames ready to send over TCP port 9090.
 */

package com.id6assistant.sdk

import java.text.Normalizer

object Id6Bits {
    const val ID6_HDR_SLI: Int = 0x01
    const val ID6_HDR_LANE: Int = 0x02
    const val ID6_HDR_COMBINED: Int = 0x03
    const val ID6_HDR_NAV: Int = 0x04
    const val ID6_HDR_MASK: Int = 0x07
    const val ID6_PKT_LEN_SLI: Int = 5
    const val ID6_PKT_LEN_LANE: Int = 3
    const val ID6_PKT_LEN_COMBINED: Int = 7
    const val ID6_PKT_LEN_NAV_MIN: Int = 12
    const val ID6_PKT_LEN_NAV_MAX: Int = 60
    const val ID6_SPEED_MAX_VALID: Int = 250
    const val ID6_SPEED_BLANK: Int = 0xFF
    const val ID6_DIST_MAX_VALID: Int = 250
    const val ID6_DIST_BLANK: Int = 0xFF
    const val ID6_SLI_STATUS_STEADY: Int = 0x00
    const val ID6_SLI_STATUS_BLANK: Int = 0x01
    const val ID6_SLI_STATUS_OVERSPEED: Int = 0x04
    const val ID6_SLI_NPI_OFF: Int = 0x00
    const val ID6_SLI_NPI_ON: Int = 0x01
    const val ID6_LANE_FLAG_ACTIVE: Int = 0x01
    const val ID6_LANE_FLAG_LEFT_WARN: Int = 0x02
    const val ID6_LANE_FLAG_RIGHT_WARN: Int = 0x04
    const val ID6_LANE_FLAG_DIST_WARN: Int = 0x08
    const val ID6_NAV_FLAG_ACTIVE: Int = 0x01
    const val ID6_NAV_MAX_LANES: Int = 8
    const val ID6_NAV_MAX_STREET: Int = 32
    const val ID6_NAV_MANEUVER_UNKNOWN: Int = 0
    const val ID6_NAV_MANEUVER_DEPART: Int = 1
    const val ID6_NAV_MANEUVER_STRAIGHT: Int = 2
    const val ID6_NAV_MANEUVER_KEEP_LEFT: Int = 3
    const val ID6_NAV_MANEUVER_KEEP_RIGHT: Int = 4
    const val ID6_NAV_MANEUVER_SLIGHT_LEFT: Int = 5
    const val ID6_NAV_MANEUVER_SLIGHT_RIGHT: Int = 6
    const val ID6_NAV_MANEUVER_LEFT: Int = 7
    const val ID6_NAV_MANEUVER_RIGHT: Int = 8
    const val ID6_NAV_MANEUVER_SHARP_LEFT: Int = 9
    const val ID6_NAV_MANEUVER_SHARP_RIGHT: Int = 10
    const val ID6_NAV_MANEUVER_U_TURN_LEFT: Int = 11
    const val ID6_NAV_MANEUVER_U_TURN_RIGHT: Int = 12
    const val ID6_NAV_MANEUVER_OFF_RAMP_LEFT: Int = 21
    const val ID6_NAV_MANEUVER_OFF_RAMP_RIGHT: Int = 22
    const val ID6_NAV_MANEUVER_FORK_LEFT: Int = 25
    const val ID6_NAV_MANEUVER_FORK_RIGHT: Int = 26
    const val ID6_NAV_MANEUVER_MERGE_LEFT: Int = 27
    const val ID6_NAV_MANEUVER_MERGE_RIGHT: Int = 28
    const val ID6_NAV_MANEUVER_ROUNDABOUT_ENTER: Int = 30
    const val ID6_NAV_MANEUVER_ROUNDABOUT_ENTER_CW: Int = 32
    const val ID6_NAV_MANEUVER_ROUNDABOUT_EXIT_CW: Int = 33
    const val ID6_NAV_MANEUVER_ROUNDABOUT_EXIT: Int = 34
    const val ID6_NAV_MANEUVER_DESTINATION: Int = 39
    const val ID6_NAV_LANE_STRAIGHT: Int = 0x01
    const val ID6_NAV_LANE_SLIGHT_LEFT: Int = 0x02
    const val ID6_NAV_LANE_LEFT: Int = 0x04
    const val ID6_NAV_LANE_SHARP_LEFT: Int = 0x08
    const val ID6_NAV_LANE_SLIGHT_RIGHT: Int = 0x10
    const val ID6_NAV_LANE_RIGHT: Int = 0x20
    const val ID6_NAV_LANE_SHARP_RIGHT: Int = 0x40
    const val ID6_NAV_LANE_U_TURN: Int = 0x80
}

data class SliInput(
    /** Max speed limit in km/h (1..250). Pass 0 or <= 0 to blank ("--") on 6WA/HUD and CID. */
    val limitKmh: Int,
    /** Minimum speed limit in km/h (1..250) for CID Floating SVG OSD. Pass 0 to hide. */
    val minKmh: Int = 0,
    /** Blink speed limit sign on 6WA/HUD (CAN 0x287 Byte 1 = 0x04). */
    val overspeed: Boolean = false,
    /** Show No-Passing Indicator sign on 6WA/HUD & CID (CAN 0x287 Byte 2 = 0x01). */
    val noPassing: Boolean = false,
)

data class LaneInput(
    /** true = active (100% opacity), false = inactive (50% opacity, "---"). */
    val active: Boolean,
    /** Flash left lane orange (#F59E0B). */
    val leftWarn: Boolean = false,
    /** Flash right lane orange (#F59E0B). */
    val rightWarn: Boolean = false,
    /** Flash vehicle & distance cluster orange (#F59E0B). */
    val distWarn: Boolean = false,
    /** Forward vehicle distance in meters (1..250). Pass <= 0 for no vehicle ("---"). */
    val distanceM: Int = -1,
)

data class NavLaneInput(
    /** Bitmask of allowed lane directions (`Id6Bits.ID6_NAV_LANE_*`). */
    val directions: Int,
    /** Bitmask of recommended lane directions (subset of `directions`; non-zero = highlighted). */
    val recommended: Int,
)

data class NavInput(
    /** True while turn-by-turn guidance is active; false clears the 6WA/HUD guidance panel. */
    val active: Boolean,
    /** Canonical maneuver type (`Id6Bits.ID6_NAV_MANEUVER_*`). */
    val maneuver: Int,
    /** Distance to the next maneuver in meters. */
    val distanceM: Long,
    /** Roundabout exit number (1..15), or 0 when not a roundabout. */
    val roundaboutExit: Int = 0,
    /** Roundabout exit angle in degrees (-360..360), or 0 when unknown. */
    val roundaboutAngleDeg: Int = 0,
    /** Optional lane guidance from left to right (max 8 lanes). */
    val lanes: List<NavLaneInput> = emptyList(),
    /** Next/current street name (auto-transliterated to safe 6WA ASCII, max 32 chars). */
    val street: String = "",
)

data class DecodedId6Frame(
    val header: Int,
    val hasSli: Boolean,
    val hasLane: Boolean,
    val hasNav: Boolean,
    val canB0: Int,
    val canB1: Int,
    val canB2: Int,
    val limitKmh: Int,
    val minKmh: Int,
    val overspeed: Boolean,
    val noPassing: Boolean,
    val adasFlags: Int,
    val laneState: Int,
    val laneLeftWarn: Boolean,
    val laneRightWarn: Boolean,
    val distanceWarn: Boolean,
    val distanceM: Int,
    val navActive: Boolean,
    val navManeuver: Int,
    val navRoundaboutExit: Int,
    val navRoundaboutAngleDeg: Int,
    val navDistanceM: Long,
    val navLanes: List<NavLaneInput>,
    val navStreet: String,
)

object Id6ProtocolCodec {
    private fun packSliBlock(input: SliInput): IntArray {
        val b0: Int
        val b1: Int
        if (input.limitKmh in 1..Id6Bits.ID6_SPEED_MAX_VALID) {
            b0 = input.limitKmh and 0xFF
            b1 = if (input.overspeed) Id6Bits.ID6_SLI_STATUS_OVERSPEED else Id6Bits.ID6_SLI_STATUS_STEADY
        } else {
            b0 = Id6Bits.ID6_SPEED_BLANK
            b1 = Id6Bits.ID6_SLI_STATUS_BLANK
        }
        val b2 = if (input.noPassing) Id6Bits.ID6_SLI_NPI_ON else Id6Bits.ID6_SLI_NPI_OFF
        val minB = if (input.minKmh in 1..Id6Bits.ID6_SPEED_MAX_VALID) input.minKmh and 0xFF else 0x00
        return intArrayOf(b0, b1, b2, minB)
    }

    private fun packLaneBlock(input: LaneInput): IntArray {
        var flags = 0x00
        if (input.active) flags = flags or Id6Bits.ID6_LANE_FLAG_ACTIVE
        if (input.leftWarn) flags = flags or Id6Bits.ID6_LANE_FLAG_LEFT_WARN
        if (input.rightWarn) flags = flags or Id6Bits.ID6_LANE_FLAG_RIGHT_WARN
        if (input.distWarn) flags = flags or Id6Bits.ID6_LANE_FLAG_DIST_WARN
        val distB = if (input.distanceM in 1..Id6Bits.ID6_DIST_MAX_VALID) {
            input.distanceM and 0xFF
        } else {
            Id6Bits.ID6_DIST_BLANK
        }
        return intArrayOf(flags, distB)
    }

    /**
     * Encodes a 5-byte SLI traffic sign packet (`[0x01, can_b0, can_b1, can_b2, min_kmh]`).
     * Bytes 1..3 map 1:1 to BMW K-CAN2 `0x287` Bytes 0..2 for zero-conversion 6WA/HUD passthrough.
     */
    fun encodeSliPacket(input: SliInput): ByteArray {
        val (b0, b1, b2, minB) = packSliBlock(input)
        return byteArrayOf(
            Id6Bits.ID6_HDR_SLI.toByte(),
            b0.toByte(),
            b1.toByte(),
            b2.toByte(),
            minB.toByte(),
        )
    }

    /**
     * Encodes a 3-byte ADAS Lane & Distance packet (`[0x02, adas_flags, dist_m]`).
     */
    fun encodeLanePacket(input: LaneInput): ByteArray {
        val (flags, distB) = packLaneBlock(input)
        return byteArrayOf(
            Id6Bits.ID6_HDR_LANE.toByte(),
            flags.toByte(),
            distB.toByte(),
        )
    }

    /**
     * Encodes a 7-byte combined SLI + ADAS Lane packet (`[0x03, b0, b1, b2, min, flags, dist]`).
     */
    fun encodeCombinedPacket(sli: SliInput, lane: LaneInput): ByteArray {
        val (b0, b1, b2, minB) = packSliBlock(sli)
        val (flags, distB) = packLaneBlock(lane)
        return byteArrayOf(
            Id6Bits.ID6_HDR_COMBINED.toByte(),
            b0.toByte(),
            b1.toByte(),
            b2.toByte(),
            minB.toByte(),
            flags.toByte(),
            distB.toByte(),
        )
    }

    /**
     * Sanitizes a street name (including Vietnamese diacritics) to 6WA-safe ASCII (`A-Za-z0-9 -./`, max 32 chars).
     */
    fun sanitizeClusterStreet(street: String?): String {
        if (street.isNullOrEmpty()) return ""
        val replaced = street.replace('Đ', 'D').replace('đ', 'd')
        val normalized = Normalizer.normalize(replaced, Normalizer.Form.NFD)
        val sb = StringBuilder(Id6Bits.ID6_NAV_MAX_STREET)
        for (ch in normalized) {
            if (sb.length >= Id6Bits.ID6_NAV_MAX_STREET) break
            if (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()) continue
            val isSafe = (ch in 'A'..'Z') ||
                (ch in 'a'..'z') ||
                (ch in '0'..'9') ||
                ch == ' ' || ch == '.' || ch == '-' || ch == '/'
            sb.append(if (isSafe) ch else ' ')
        }
        return sb.toString()
    }

    /**
     * Encodes a variable-length turn-by-turn NAV packet (`0x04`).
     */
    fun encodeNavPacket(input: NavInput): ByteArray {
        val flags = if (input.active) Id6Bits.ID6_NAV_FLAG_ACTIVE else 0x00
        val maneuver = input.maneuver and 0xFF
        val ex = if (input.roundaboutExit in 0..15) input.roundaboutExit else 0
        val angle = input.roundaboutAngleDeg and 0xFFFF
        val dist = maxOf(0L, input.distanceM) and 0xFFFFFFFFL
        val lanes = input.lanes.take(Id6Bits.ID6_NAV_MAX_LANES)
        val street = sanitizeClusterStreet(input.street)

        val totalLen = 12 + lanes.size * 2 + street.length
        val out = ByteArray(totalLen)
        out[0] = Id6Bits.ID6_HDR_NAV.toByte()
        out[1] = flags.toByte()
        out[2] = maneuver.toByte()
        out[3] = ex.toByte()
        out[4] = (angle and 0xFF).toByte()
        out[5] = ((angle shr 8) and 0xFF).toByte()
        out[6] = (dist and 0xFFL).toByte()
        out[7] = ((dist shr 8) and 0xFFL).toByte()
        out[8] = ((dist shr 16) and 0xFFL).toByte()
        out[9] = ((dist shr 24) and 0xFFL).toByte()
        out[10] = lanes.size.toByte()

        var pos = 11
        for (lane in lanes) {
            out[pos++] = (lane.directions and 0xFF).toByte()
            out[pos++] = (lane.recommended and 0xFF).toByte()
        }
        out[pos++] = street.length.toByte()
        for (i in street.indices) {
            out[pos++] = (street[i].code and 0x7F).toByte()
        }
        return out
    }

    /**
     * Decodes a 3, 5, 7-byte, or variable-length `0x04` bitwise packet (returns `null` if malformed).
     */
    fun decodePacket(buf: ByteArray): DecodedId6Frame? {
        if (buf.isEmpty()) return null
        val hdr = buf[0].toInt() and 0xFF
        if (hdr == 0 || (hdr and Id6Bits.ID6_HDR_MASK.inv()) != 0) return null
        if ((hdr and Id6Bits.ID6_HDR_NAV) != 0 && hdr != Id6Bits.ID6_HDR_NAV) return null

        val hasSli = (hdr and Id6Bits.ID6_HDR_SLI) != 0
        val hasLane = (hdr and Id6Bits.ID6_HDR_LANE) != 0
        val hasNav = hdr == Id6Bits.ID6_HDR_NAV

        var canB0 = Id6Bits.ID6_SPEED_BLANK
        var canB1 = Id6Bits.ID6_SLI_STATUS_BLANK
        var canB2 = Id6Bits.ID6_SLI_NPI_OFF
        var limitKmh = -1
        var minKmh = -1
        var overspeed = false
        var noPassing = false
        var adasFlags = 0
        var laneState = 0
        var laneLeftWarn = false
        var laneRightWarn = false
        var distanceWarn = false
        var distanceM = -1

        if (hasNav) {
            if (buf.size < Id6Bits.ID6_PKT_LEN_NAV_MIN || buf.size > Id6Bits.ID6_PKT_LEN_NAV_MAX) {
                return null
            }
            val lcount = buf[10].toInt() and 0xFF
            if (lcount > Id6Bits.ID6_NAV_MAX_LANES) return null
            val slenPos = 11 + lcount * 2
            if (buf.size <= slenPos) return null
            val slen = buf[slenPos].toInt() and 0xFF
            if (slen > Id6Bits.ID6_NAV_MAX_STREET || buf.size != slenPos + 1 + slen) return null

            val uangle = (buf[4].toInt() and 0xFF) or ((buf[5].toInt() and 0xFF) shl 8)
            val angle = if (uangle >= 0x8000) uangle - 0x10000 else uangle
            val distM = (buf[6].toLong() and 0xFFL) or
                ((buf[7].toLong() and 0xFFL) shl 8) or
                ((buf[8].toLong() and 0xFFL) shl 16) or
                ((buf[9].toLong() and 0xFFL) shl 24)

            val lanes = ArrayList<NavLaneInput>(lcount)
            for (i in 0 until lcount) {
                lanes.add(
                    NavLaneInput(
                        directions = buf[11 + i * 2].toInt() and 0xFF,
                        recommended = buf[11 + i * 2 + 1].toInt() and 0xFF,
                    ),
                )
            }
            val streetSb = StringBuilder(slen)
            for (i in 0 until slen) {
                streetSb.append((buf[slenPos + 1 + i].toInt() and 0xFF).toChar())
            }

            return DecodedId6Frame(
                header = hdr,
                hasSli = false,
                hasLane = false,
                hasNav = true,
                canB0 = canB0,
                canB1 = canB1,
                canB2 = canB2,
                limitKmh = limitKmh,
                minKmh = minKmh,
                overspeed = overspeed,
                noPassing = noPassing,
                adasFlags = adasFlags,
                laneState = laneState,
                laneLeftWarn = laneLeftWarn,
                laneRightWarn = laneRightWarn,
                distanceWarn = distanceWarn,
                distanceM = distanceM,
                navActive = (buf[1].toInt() and Id6Bits.ID6_NAV_FLAG_ACTIVE) != 0,
                navManeuver = buf[2].toInt() and 0xFF,
                navRoundaboutExit = buf[3].toInt() and 0xFF,
                navRoundaboutAngleDeg = angle,
                navDistanceM = distM,
                navLanes = lanes,
                navStreet = streetSb.toString(),
            )
        }

        val expectedLen = 1 + (if (hasSli) 4 else 0) + (if (hasLane) 2 else 0)
        if (buf.size != expectedLen) return null

        var offset = 1
        if (hasSli) {
            val b0 = buf[offset].toInt() and 0xFF
            val b1 = buf[offset + 1].toInt() and 0xFF
            val b2 = buf[offset + 2].toInt() and 0xFF
            val minB = buf[offset + 3].toInt() and 0xFF
            offset += 4

            if (b0 in 1..Id6Bits.ID6_SPEED_MAX_VALID && (b1 and Id6Bits.ID6_SLI_STATUS_BLANK) == 0) {
                canB0 = b0
                overspeed = (b1 and Id6Bits.ID6_SLI_STATUS_OVERSPEED) != 0
                canB1 = if (overspeed) Id6Bits.ID6_SLI_STATUS_OVERSPEED else Id6Bits.ID6_SLI_STATUS_STEADY
                limitKmh = b0
            } else {
                canB0 = Id6Bits.ID6_SPEED_BLANK
                canB1 = Id6Bits.ID6_SLI_STATUS_BLANK
                limitKmh = 0
                overspeed = false
            }
            noPassing = (b2 and Id6Bits.ID6_SLI_NPI_ON) != 0
            canB2 = if (noPassing) Id6Bits.ID6_SLI_NPI_ON else Id6Bits.ID6_SLI_NPI_OFF
            minKmh = if (minB in 1..Id6Bits.ID6_SPEED_MAX_VALID) minB else 0
        }

        if (hasLane) {
            val flags = buf[offset].toInt() and 0xFF
            val distB = buf[offset + 1].toInt() and 0xFF
            adasFlags = flags
            laneState = if ((flags and Id6Bits.ID6_LANE_FLAG_ACTIVE) != 0) 1 else 0
            laneLeftWarn = (flags and Id6Bits.ID6_LANE_FLAG_LEFT_WARN) != 0
            laneRightWarn = (flags and Id6Bits.ID6_LANE_FLAG_RIGHT_WARN) != 0
            distanceWarn = (flags and Id6Bits.ID6_LANE_FLAG_DIST_WARN) != 0
            distanceM = if (distB in 1..Id6Bits.ID6_DIST_MAX_VALID) distB else -1
        }

        return DecodedId6Frame(
            header = hdr,
            hasSli = hasSli,
            hasLane = hasLane,
            hasNav = false,
            canB0 = canB0,
            canB1 = canB1,
            canB2 = canB2,
            limitKmh = limitKmh,
            minKmh = minKmh,
            overspeed = overspeed,
            noPassing = noPassing,
            adasFlags = adasFlags,
            laneState = laneState,
            laneLeftWarn = laneLeftWarn,
            laneRightWarn = laneRightWarn,
            distanceWarn = distanceWarn,
            distanceM = distanceM,
            navActive = false,
            navManeuver = 0,
            navRoundaboutExit = 0,
            navRoundaboutAngleDeg = 0,
            navDistanceM = 0L,
            navLanes = emptyList(),
            navStreet = "",
        )
    }

    /**
     * Formats a packet as uppercase space-separated hex (e.g. `"01 3C 04 01 1E"`).
     */
    fun toHexString(buf: ByteArray): String =
        buf.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
}
