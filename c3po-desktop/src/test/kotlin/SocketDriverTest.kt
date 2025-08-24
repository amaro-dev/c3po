/* TODO: Fix compilation errors
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import socket.CommandEntry
import socket.SocketClient
import socket.SocketDriver
import socket.SocketEvent
import socket.SocketResponseAggregator

class SocketDriverTest {
    @Test
    fun `When resetting the connection, closes it if it's alive`() {
        val socketClient: SocketClient =
            mockk(relaxed = true) {
                every { isLive } returns true
            }
        val driver = SocketDriver(socketClient, mockk())

        driver.reset()

        verify { socketClient.close() }
    }

    @Test
    fun `When resetting the connection, do not close it if it's not alive`() {
        val socketClient: SocketClient =
            mockk(relaxed = true) {
                every { isLive } returns false
            }
        val driver = SocketDriver(socketClient, mockk())

        driver.reset()

        verify(exactly = 0) { socketClient.close() }
    }

    @Test
    fun `When sending a request with args, handle it properly to the client`() {
        val socketClient: SocketClient = mockk(relaxed = true)
        val slot = CapturingSlot<String>()

        val driver = SocketDriver(socketClient, mockk())

        driver.send("cmd", "arg")

        verify { socketClient.send(capture(slot)) }
        assertThat(slot.captured).startsWith("cmd: arg/")
    }

    @Test
    fun `When sending a request with NO args, handle it properly to the client`() {
        val socketClient: SocketClient = mockk(relaxed = true)
        val slot = CapturingSlot<String>()

        val driver = SocketDriver(socketClient, mockk())

        driver.send("cmd", null)

        verify { socketClient.send(capture(slot)) }
        assertThat(slot.captured).startsWith("cmd/")
    }

    @Test
    fun `When connecting to the client, use the right address and port`() {
        val socketClient: SocketClient = mockk(relaxed = true)

        val driver = SocketDriver(socketClient, mockk())

        driver.connect()

        verify { socketClient.connect("localhost", 9500) }
    }

    @Test
    fun `When connecting to the client, listen for connection messages`() =
        runTest {
            val socketClient: SocketClient =
                mockk(relaxed = true) {
                    every { connect() } returns flowOf("CONNECTED")
                }

            val driver = SocketDriver(socketClient, mockk())

            assertThat(driver.connect().first()).isEqualTo(SocketEvent.Connected)
        }

    @Test
    fun `When connecting to the client, listen for disconnection messages`() =
        runTest {
            val socketClient: SocketClient =
                mockk(relaxed = true) {
                    every { connect() } returns flowOf("DISCONNECTED")
                }

            val driver = SocketDriver(socketClient, mockk())

            assertThat(driver.connect().first()).isEqualTo(SocketEvent.Disconnected)
        }

    @Test
    fun `When connecting to the client, listen for content messages`() =
        runTest {
            val commandEntry: CommandEntry = mockk()
            val content = listOf("abc")
            val aggregator: SocketResponseAggregator =
                mockk(relaxed = true) {
                    every { readyToDeliver() } returns listOf(Pair(commandEntry, content))
                }
            val socketClient: SocketClient =
                mockk(relaxed = true) {
                    every { connect() } returns flowOf("anything")
                }

            val driver = SocketDriver(socketClient, aggregator)

            assertThat(driver.connect().first())
                .isEqualTo(SocketEvent.Message(commandEntry, content))
        }
}
*/
