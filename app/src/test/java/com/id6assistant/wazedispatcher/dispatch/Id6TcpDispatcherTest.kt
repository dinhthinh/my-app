package com.id6assistant.wazedispatcher.dispatch

import com.id6assistant.sdk.Id6Bits
import com.id6assistant.sdk.Id6ProtocolCodec
import com.id6assistant.sdk.NavInput
import com.id6assistant.sdk.SliInput
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.ServerSocket
import java.util.concurrent.CopyOnWriteArrayList

class Id6TcpDispatcherTest {
    @Test
    fun `deduplicates identical SLI and throttles NAV at 200ms`() = runTest {
        var fakeNow = 1_000L
        val received = CopyOnWriteArrayList<ByteArray>()

        ServerSocket(0).use { server ->
            val acceptThread = Thread {
                repeat(3) {
                    try {
                        server.accept().use { client ->
                            received.add(client.getInputStream().readBytes())
                        }
                    } catch (_: Exception) {
                    }
                }
            }
            acceptThread.start()

            val dispatcher = Id6TcpDispatcher(
                clockMs = { fakeNow },
                navMinIntervalMs = 200L,
            ).apply {
                host = "127.0.0.1"
                port = server.localPort
            }

            val telemetry = MappedId6Telemetry(
                sli = SliInput(limitKmh = 60, overspeed = false, noPassing = false),
                nav = NavInput(active = true, maneuver = Id6Bits.ID6_NAV_MANEUVER_LEFT, distanceM = 200),
            )

            // First dispatch sends both SLI and NAV
            val firstLogs = dispatcher.dispatch(telemetry)
            assertEquals(2, firstLogs.size)
            assertTrue(firstLogs.all { it.success })

            // Second dispatch at +50ms: identical SLI is skipped, NAV is throttled
            fakeNow += 50L
            val secondLogs = dispatcher.dispatch(telemetry)
            assertEquals(0, secondLogs.size)

            // Third dispatch at +250ms: SLI still skipped, NAV is sent
            fakeNow += 200L
            val thirdLogs = dispatcher.dispatch(telemetry)
            assertEquals(1, thirdLogs.size)
            assertEquals("0x04 NAV", thirdLogs.first().typeLabel)

            acceptThread.join(2000)
            assertEquals(3, received.size)
            val decodedSli = Id6ProtocolCodec.decodePacket(received[0])
            assertEquals(60, decodedSli?.limitKmh)
        }
    }
}
