package com.id6assistant.wazedispatcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.id6assistant.sdk.Id6Bits
import com.id6assistant.sdk.Id6ProtocolCodec
import com.id6assistant.wazedispatcher.data.MockSimulatorState

private val CardBg = Color(0xFF0F172A)
private val InnerBoxBg = Color(0xFF111C2E)
private val CardBorder = Color(0xFF1E293B)
private val InputBg = Color(0xFF0B1220)
private val InputBorder = Color(0xFF334155)
private val AccentCyan = Color(0xFF38BDF8)
private val PrimaryBlue = Color(0xFF0284C7)
private val EmeraldGreen = Color(0xFF10B981)
private val TextPrimary = Color(0xFFF8FAFC)
private val TextMuted = Color(0xFF94A3B8)

private data class ManeuverOption(val code: Int, val label: String)

private val maneuverOptions = listOf(
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_UNKNOWN, "0 - Unknown"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_DEPART, "1 - Depart"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_STRAIGHT, "2 - Straight"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_KEEP_LEFT, "3 - Keep Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_KEEP_RIGHT, "4 - Keep Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_SLIGHT_LEFT, "5 - Slight Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_SLIGHT_RIGHT, "6 - Slight Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_LEFT, "7 - Turn Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_RIGHT, "8 - Turn Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_SHARP_LEFT, "9 - Sharp Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_SHARP_RIGHT, "10 - Sharp Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_U_TURN_LEFT, "11 - U-Turn Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_U_TURN_RIGHT, "12 - U-Turn Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_OFF_RAMP_LEFT, "21 - Off-Ramp Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_OFF_RAMP_RIGHT, "22 - Off-Ramp Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_FORK_LEFT, "25 - Fork Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_FORK_RIGHT, "26 - Fork Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_MERGE_LEFT, "27 - Merge Left"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_MERGE_RIGHT, "28 - Merge Right"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_ENTER, "30 - Roundabout Enter"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_ENTER_CW, "32 - Roundabout Enter CW"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT_CW, "33 - Roundabout Exit CW"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_ROUNDABOUT_EXIT, "34 - Roundabout Exit"),
    ManeuverOption(Id6Bits.ID6_NAV_MANEUVER_DESTINATION, "39 - Destination"),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MockSimulatorPanel(
    sim: MockSimulatorState,
    onUpdate: (autoSendEligible: Boolean, (MockSimulatorState) -> MockSimulatorState) -> Unit,
    onSendSli: () -> Unit,
    onSendLane: () -> Unit,
    onSendCombined: () -> Unit,
    onSendNav: () -> Unit,
    onClearNav: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sliInput = sim.toSliInput()
    val laneInput = sim.toLaneInput()
    val navInput = sim.toNavInput(active = true)

    val sliPacket = Id6ProtocolCodec.encodeSliPacket(sliInput)
    val lanePacket = Id6ProtocolCodec.encodeLanePacket(laneInput)
    val combinedPacket = Id6ProtocolCodec.encodeCombinedPacket(sliInput, laneInput)
    val navPacket = Id6ProtocolCodec.encodeNavPacket(navInput)

    val sliHex = Id6ProtocolCodec.toHexString(sliPacket)
    val laneHex = Id6ProtocolCodec.toHexString(lanePacket)
    val combinedHex = Id6ProtocolCodec.toHexString(combinedPacket)
    val navHex = Id6ProtocolCodec.toHexString(navPacket)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 3. TRAFFIC SIGN SIMULATOR
        SimulatorCard(
            title = "3. TRAFFIC SIGN SIMULATOR",
            hexBadge = "[5B] $sliHex",
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(InnerBoxBg, RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    SimulatorNumberField(
                        label = "Max Speed (km/h):",
                        value = sim.maxSpeedKmh,
                        hint = "1..250 (0 = 0xFF \"--\")",
                        minValue = 0,
                        maxValue = Id6Bits.ID6_SPEED_MAX_VALID,
                        onValueChange = { v ->
                            onUpdate(true) { it.copy(maxSpeedKmh = v) }
                        },
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(InnerBoxBg, RoundedCornerShape(10.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    SimulatorNumberField(
                        label = "Min Speed (km/h):",
                        value = sim.minSpeedKmh,
                        hint = "1..250 (0 = 0x00 Hide)",
                        minValue = 0,
                        maxValue = Id6Bits.ID6_SPEED_MAX_VALID,
                        onValueChange = { v ->
                            onUpdate(true) { it.copy(minSpeedKmh = v) }
                        },
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SimulatorCheckboxBox(
                    label = "Overspeed Blink (bit 2 = 0x04)",
                    checked = sim.overspeedBlink,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(overspeedBlink = checked) }
                    },
                    modifier = Modifier.weight(1f),
                )
                SimulatorCheckboxBox(
                    label = "No-Passing Sign (bit 0 = 0x01)",
                    checked = sim.noPassingSign,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(noPassingSign = checked) }
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onSendSli,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Send Traffic Sign Only (0x01 • 5 Bytes)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // 4. ADAS LANE & DISTANCE SIMULATOR
        SimulatorCard(
            title = "4. ADAS LANE & DISTANCE SIMULATOR",
            hexBadge = "[3B] $laneHex",
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InnerBoxBg, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Lane Tracking Bitmask",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                SimulatorCheckboxRow(
                    label = "Lane Active (bit 0 = 0x01)",
                    checked = sim.laneActive,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(laneActive = checked) }
                    },
                )
                SimulatorCheckboxRow(
                    label = "Left Lane Warn (bit 1 = 0x02)",
                    checked = sim.leftLaneWarn,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(leftLaneWarn = checked) }
                    },
                )
                SimulatorCheckboxRow(
                    label = "Right Lane Warn (bit 2 = 0x04)",
                    checked = sim.rightLaneWarn,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(rightLaneWarn = checked) }
                    },
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InnerBoxBg, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Forward Vehicle Distance & Warn",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                SimulatorCheckboxRow(
                    label = "Distance Warn (bit 3 = 0x08)",
                    checked = sim.distWarn,
                    onCheckedChange = { checked ->
                        onUpdate(true) { it.copy(distWarn = checked) }
                    },
                )
                SimulatorNumberField(
                    label = "Dist (m):",
                    value = sim.distM,
                    hint = "1..250 (0 = 0xFF \"---\")",
                    minValue = 0,
                    maxValue = Id6Bits.ID6_DIST_MAX_VALID,
                    onValueChange = { v ->
                        onUpdate(true) { it.copy(distM = v) }
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onSendLane,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = "Send Lane & Distance Only (0x02 • 3 Bytes)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // COMBINED 7-BYTE FRAME
        val b0 = combinedPacket[1].toInt() and 0xFF
        val b1 = combinedPacket[2].toInt() and 0xFF
        val b2 = combinedPacket[3].toInt() and 0xFF
        val minB = combinedPacket[4].toInt() and 0xFF
        val flagsB = combinedPacket[5].toInt() and 0xFF
        val distB = combinedPacket[6].toInt() and 0xFF

        val b0Sub = if (b0 == Id6Bits.ID6_SPEED_BLANK) "--" else "$b0 km/h"
        val b1Sub = when (b1) {
            Id6Bits.ID6_SLI_STATUS_OVERSPEED -> "BLINK"
            Id6Bits.ID6_SLI_STATUS_BLANK -> "BLANK"
            else -> "STEADY"
        }
        val b2Sub = if (b2 == Id6Bits.ID6_SLI_NPI_ON) "NPI ON" else "NPI OFF"
        val minSub = if (minB in 1..Id6Bits.ID6_SPEED_MAX_VALID) "$minB km/h" else "HIDDEN"
        val flagsBinary = "0b" + flagsB.toString(2).padStart(8, '0')
        val distSub = if (distB in 1..Id6Bits.ID6_DIST_MAX_VALID) "$distB m" else "---"

        SimulatorCard(
            title = "COMBINED 7-BYTE FRAME (Header 0x01 | 0x02 = 0x03 + 4B SLI + 2B LANE):",
            hexBadge = combinedHex,
            hexColor = AccentCyan,
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ByteBreakdownBox(
                    name = "HDR",
                    hexValue = "0x%02X".format(combinedPacket[0].toInt() and 0xFF),
                    subLabel = "SLI | LANE",
                    borderColor = Color(0xFF0EA5E9),
                    bgColor = Color(0xFF0C2438),
                )
                ByteBreakdownBox(
                    name = "CAN_B0",
                    hexValue = "0x%02X".format(b0),
                    subLabel = b0Sub,
                    borderColor = Color(0xFF10B981),
                    bgColor = Color(0xFF0A2622),
                )
                ByteBreakdownBox(
                    name = "CAN_B1",
                    hexValue = "0x%02X".format(b1),
                    subLabel = b1Sub,
                    borderColor = Color(0xFF10B981),
                    bgColor = Color(0xFF0A2622),
                )
                ByteBreakdownBox(
                    name = "CAN_B2",
                    hexValue = "0x%02X".format(b2),
                    subLabel = b2Sub,
                    borderColor = Color(0xFF10B981),
                    bgColor = Color(0xFF0A2622),
                )
                ByteBreakdownBox(
                    name = "MIN_KMH",
                    hexValue = "0x%02X".format(minB),
                    subLabel = minSub,
                    borderColor = Color(0xFF10B981),
                    bgColor = Color(0xFF0A2622),
                )
                ByteBreakdownBox(
                    name = "FLAGS",
                    hexValue = "0x%02X".format(flagsB),
                    subLabel = flagsBinary,
                    borderColor = Color(0xFFD97706),
                    bgColor = Color(0xFF271B0C),
                )
                ByteBreakdownBox(
                    name = "DIST_M",
                    hexValue = "0x%02X".format(distB),
                    subLabel = distSub,
                    borderColor = Color(0xFFD97706),
                    bgColor = Color(0xFF271B0C),
                )
            }

            SimulatorCheckboxRow(
                label = "Auto-send packet immediately when any Traffic Sign or ADAS control changes",
                checked = sim.autoSendCombined,
                onCheckedChange = { checked ->
                    onUpdate(false) { it.copy(autoSendCombined = checked) }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Button(
                    onClick = onSendCombined,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = "Send Combined SLI + LANE Packet (0x03 • 7 Bytes)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // TURN-BY-TURN NAVIGATION SIMULATOR (HEADER 0x04 • 6WA & HUD)
        SimulatorCard(
            title = "TURN-BY-TURN NAVIGATION SIMULATOR (HEADER 0x04 • 6WA & HUD)",
        ) {
            ManeuverDropdown(
                selectedCode = sim.navManeuver,
                onSelect = { code ->
                    onUpdate(false) { it.copy(navManeuver = code) }
                },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SimulatorNumberField(
                        label = "Distance (m):",
                        value = sim.navDistanceM.toInt(),
                        hint = null,
                        minValue = 0,
                        maxValue = 999999,
                        onValueChange = { v ->
                            onUpdate(false) { it.copy(navDistanceM = v.toLong()) }
                        },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SimulatorNumberField(
                        label = "Roundabout Exit:",
                        value = sim.navRoundaboutExit,
                        hint = null,
                        minValue = 0,
                        maxValue = 15,
                        onValueChange = { v ->
                            onUpdate(false) { it.copy(navRoundaboutExit = v) }
                        },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    SimulatorNumberField(
                        label = "Angle (deg):",
                        value = sim.navAngleDeg,
                        hint = null,
                        minValue = -360,
                        maxValue = 360,
                        allowNegative = true,
                        onValueChange = { v ->
                            onUpdate(false) { it.copy(navAngleDeg = v) }
                        },
                    )
                }
            }

            SimulatorTextField(
                label = "Street:",
                value = sim.navStreet,
                onValueChange = { text ->
                    onUpdate(false) { it.copy(navStreet = text) }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "NAV FRAME (0x04):",
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = navHex,
                    color = AccentCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onSendNav,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = "Send Turn-by-Turn NAV (0x04)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                OutlinedButton(
                    onClick = onClearNav,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        text = "Clear 6WA/HUD NAV (0x04)",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorCard(
    title: String,
    hexBadge: String? = null,
    hexColor: Color = TextMuted,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                color = AccentCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            if (hexBadge != null) {
                Box(
                    modifier = Modifier
                        .background(InputBg, RoundedCornerShape(6.dp))
                        .border(1.dp, InputBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = hexBadge,
                        color = hexColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        content()
    }
}

@Composable
private fun SimulatorCheckboxBox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(InnerBoxBg, RoundedCornerShape(10.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        SimulatorCheckboxRow(
            label = label,
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SimulatorCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(20.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryBlue,
                uncheckedColor = TextMuted,
                checkmarkColor = Color.White,
            ),
        )
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun SimulatorNumberField(
    label: String,
    value: Int,
    hint: String?,
    minValue: Int,
    maxValue: Int,
    allowNegative: Boolean = false,
    onValueChange: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(value.toString()) }

    LaunchedEffect(value) {
        if (text.toIntOrNull() != value) {
            text = value.toString()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
        )
        BasicTextField(
            value = text,
            onValueChange = { raw ->
                val filtered = raw.filterIndexed { index, c ->
                    c.isDigit() || (allowNegative && index == 0 && c == '-')
                }
                text = filtered
                val parsed = filtered.toIntOrNull()
                if (parsed != null) {
                    onValueChange(parsed.coerceIn(minValue, maxValue))
                } else if (filtered.isEmpty()) {
                    onValueChange(0.coerceIn(minValue, maxValue))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (allowNegative) KeyboardType.Ascii else KeyboardType.Number,
            ),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(AccentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(6.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
        if (hint != null) {
            Text(
                text = hint,
                color = Color(0xFF64748B),
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun SimulatorTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 13.sp,
            ),
            cursorBrush = SolidColor(AccentCyan),
            modifier = Modifier
                .fillMaxWidth()
                .background(InputBg, RoundedCornerShape(6.dp))
                .border(1.dp, InputBorder, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun ByteBreakdownBox(
    name: String,
    hexValue: String,
    subLabel: String,
    borderColor: Color,
    bgColor: Color,
) {
    Column(
        modifier = Modifier
            .widthIn(min = 72.dp)
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = name,
            color = borderColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = hexValue,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subLabel,
            color = TextMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ManeuverDropdown(
    selectedCode: Int,
    onSelect: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = maneuverOptions.firstOrNull { it.code == selectedCode }
        ?: ManeuverOption(selectedCode, "$selectedCode - Custom")

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Maneuver:",
            color = TextMuted,
            fontSize = 11.sp,
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(InputBg, RoundedCornerShape(6.dp))
                    .border(1.dp, InputBorder, RoundedCornerShape(6.dp))
                    .clickable { expanded = true }
                    .padding(horizontal = 10.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedOption.label,
                    color = TextPrimary,
                    fontSize = 13.sp,
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Maneuver",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1E293B)),
            ) {
                maneuverOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = if (option.code == selectedCode) AccentCyan else TextPrimary,
                                fontSize = 13.sp,
                            )
                        },
                        onClick = {
                            onSelect(option.code)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
