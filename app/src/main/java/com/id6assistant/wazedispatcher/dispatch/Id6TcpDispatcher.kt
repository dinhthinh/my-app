package com.id6assistant.wazedispatcher.dispatch

import com.id6assistant.sdk.Id6Bits
import com.id6assistant.sdk.Id6ProtocolCodec
import com.id6assistant.sdk.NavInput
import com.id6assistant.sdk.SliInput
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

data class PacketLogEntry(
    val timestampMs: Long,
    val typeLabel: String,
    val hex: String,
    val success: Boolean,
    val error: String? = null,
)

class Id6TcpDispatcher(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val clockMs: () -> Long = { System.currentTimeMillis() },
    private val connectTimeoutMs: Int = 800,
    private val navMinIntervalMs: Long = 200L,
) {
    @Volatile
    var host: String = "172.16.224.1"

    @Volatile
    var port: Int = 9090

    private var lastSliHex: String? = null
    private var lastNavSentMs: Long = 0L
    private var lastNavActive: Boolean = false

    fun resetState() {
        lastSliHex = null
        lastNavSentMs = 0L
        lastNavActive = false
    }

    suspend fun dispatch(
        mapped: MappedId6Telemetry,
        forceSli: Boolean = false,
    ): List<PacketLogEntry> {
        val logs = mutableListOf<PacketLogEntry>()

        val sliBytes = Id6ProtocolCodec.encodeSliPacket(mapped.sli)
        val sliHex = Id6ProtocolCodec.toHexString(sliBytes)
        if (forceSli || sliHex != lastSliHex) {
            val entry = sendFrame("0x01 SLI", sliBytes, sliHex)
            if (entry.success) {
                lastSliHex = sliHex
            }
            logs.add(entry)
        }

        val now = clockMs()
        val nav = mapped.nav
        val shouldSendNav = when {
            nav.active -> (now - lastNavSentMs) >= navMinIntervalMs || !lastNavActive
            lastNavActive -> true
            else -> false
        }

        if (shouldSendNav) {
            val navBytes = Id6ProtocolCodec.encodeNavPacket(nav)
            val navHex = Id6ProtocolCodec.toHexString(navBytes)
            val label = if (nav.active) "0x04 NAV" else "0x04 NAV_CLR"
            val entry = sendFrame(label, navBytes, navHex)
            if (entry.success) {
                lastNavSentMs = now
                lastNavActive = nav.active
            }
            logs.add(entry)
        }

        return logs
    }

    suspend fun clearNav(): PacketLogEntry {
        val clearBytes = Id6ProtocolCodec.encodeNavPacket(
            NavInput(
                active = false,
                maneuver = Id6Bits.ID6_NAV_MANEUVER_UNKNOWN,
                distanceM = 0L,
            )
        )
        val hex = Id6ProtocolCodec.toHexString(clearBytes)
        lastNavActive = false
        return sendFrame("0x04 NAV_CLR", clearBytes, hex)
    }

    suspend fun sendBlankSli(): PacketLogEntry {
        val blankBytes = Id6ProtocolCodec.encodeSliPacket(SliInput(limitKmh = 0))
        val hex = Id6ProtocolCodec.toHexString(blankBytes)
        lastSliHex = hex
        return sendFrame("0x01 SLI_CLR", blankBytes, hex)
    }

    private suspend fun sendFrame(
        label: String,
        payload: ByteArray,
        hex: String,
    ): PacketLogEntry = withContext(ioDispatcher) {
        val ts = clockMs()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), connectTimeoutMs)
                socket.soTimeout = connectTimeoutMs
                socket.getOutputStream().use { out ->
                    out.write(payload)
                    out.flush()
                }
            }
            PacketLogEntry(
                timestampMs = ts,
                typeLabel = label,
                hex = hex,
                success = true,
            )
        } catch (e: Exception) {
            PacketLogEntry(
                timestampMs = ts,
                typeLabel = label,
                hex = hex,
                success = false,
                error = e.message ?: e.javaClass.simpleName,
            )
        }
    }
}
