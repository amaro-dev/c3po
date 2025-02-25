import assertk.assertThat
import assertk.assertions.startsWith
import core.Action
import core.AppState
import core.SocketMiddleware
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import socket.SocketClient

class SocketMiddlewareTest {
    @Test
    fun `When device is selected, close the connection if it's open`() {
        val socketClient: SocketClient = mockk(relaxed = true) {
            every { isLive } returns true
        }

        val middleware = SocketMiddleware(socketClient)

        middleware.process(Action.SelectDevice(mockk()), AppState(), mockk())

        verify { socketClient.close() }
    }

    @Test
    fun `When device is selected, do not close the connection if it's closed`() {
        val socketClient: SocketClient = mockk(relaxed = true) {
            every { isLive } returns false
        }

        val middleware = SocketMiddleware(socketClient)

        middleware.process(Action.SelectDevice(mockk()), AppState(), mockk())

        verify(exactly = 0) { socketClient.close() }
    }

    @Test
    fun `When sending a request with args, handle it properly to the client`() {
        val socketClient: SocketClient = mockk(relaxed = true)
        val slot = CapturingSlot<String>()

        val middleware = SocketMiddleware(socketClient)

        middleware.process(Action.SendSocketRequest("cmd", "arg"), AppState(), mockk())

        verify { socketClient.send(capture(slot)) }
        assertThat(slot.captured).startsWith("cmd: arg/")
    }

    @Test
    fun `When sending a request with NO args, handle it properly to the client`() {
        val socketClient: SocketClient = mockk(relaxed = true)
        val slot = CapturingSlot<String>()

        val middleware = SocketMiddleware(socketClient)

        middleware.process(Action.SendSocketRequest("cmd", null), AppState(), mockk())

        verify { socketClient.send(capture(slot)) }
        assertThat(slot.captured).startsWith("cmd/")
    }
}
