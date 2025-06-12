import core.Action
import core.AppState
import core.SocketMiddleware
import core.IProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import socket.CommandEntry
import socket.SocketDriver
import socket.SocketEvent

@OptIn(ExperimentalCoroutinesApi::class)
class SocketMiddlewareTest {

    @Test
    fun `When device is selected, reset the driver`() = runTest {
        val socketDriver: SocketDriver = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.SelectDevice(mockk()), AppState(), mockk())

        verify { socketDriver.reset() }
    }

    @Test
    fun `When sending a request call the driver`() = runTest {
        val socketDriver: SocketDriver = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.SendSocketRequest("cmd", "arg"), AppState(), mockk())

        verify { socketDriver.send("cmd", "arg") }
    }

    @Test
    fun `When connecting to client, listen to the driver`() = runTest {
        val socketDriver: SocketDriver = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.Companion.Connect, AppState(), mockk())
        advanceUntilIdle()

        verify { socketDriver.connect() }
    }

    @Test
    fun `When driver informs it's connected, updates the state`() = runTest {
        val socketDriver: SocketDriver = mockk(relaxed = true) {
            every { connect() } returns flowOf(SocketEvent.Connected)
        }
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.Companion.Connect, AppState(), processor)
        advanceUntilIdle()

        verify {
            processor.reduce(Action.Companion.UpdateState(AppState().companionState.setIsOnline()))
        }
    }

    @Test
    fun `When driver informs it's disconnected, updates the state`() = runTest {
        val socketDriver: SocketDriver = mockk(relaxed = true) {
            every { connect() } returns flowOf(SocketEvent.Disconnected)
        }
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.Companion.Connect, AppState(), processor)
        advanceUntilIdle()

        verify {
            processor.perform(Action.RefreshDevices)
            processor.reduce(Action.Companion.UpdateState(AppState().companionState.setIsOffline()))
        }
    }

    @Test
    fun `When driver delivers the content, it should deliver to requester`() = runTest {
        val command: CommandEntry = mockk()
        val content: List<String> = listOf("abc")
        val socketDriver: SocketDriver = mockk(relaxed = true) {
            every { connect() } returns flowOf(SocketEvent.Message(command, content))
        }
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        val middleware = SocketMiddleware(socketDriver, this)

        middleware.process(Action.Companion.Connect, AppState(), processor)
        advanceUntilIdle()

        verify {
            processor.perform(Action.DeliverSocketResponse(command, content))
        }
    }
}
