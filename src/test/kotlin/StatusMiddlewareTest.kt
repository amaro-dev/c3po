import core.Action
import core.AppState
import core.CommandStatus
import core.StatusMiddleware
import dev.amaro.sonic.IProcessor
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class StatusMiddlewareTest {
    @Test
    fun `If status is completed and is NOT running`() = runTest {
        val middleware = StatusMiddleware(this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Completed), processor)
        advanceTimeBy(3001)
        verify { processor.reduce(Action.ClearError) }
    }

    @Test
    fun `If status is completed and is running`() = runTest {
        val middleware = StatusMiddleware(this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Completed), processor)
        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Completed), processor)
        advanceTimeBy(3005)
        verify(exactly = 1) { processor.reduce(Action.ClearError) }
    }

    @Test
    fun `If status is error and is NOT running`() = runTest {
        val middleware = StatusMiddleware(this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Failed), processor)
        advanceTimeBy(3001)
        verify { processor.reduce(Action.ClearError) }
    }

    @Test
    fun `If status is error and is running`() = runTest {
        val middleware = StatusMiddleware(this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Failed), processor)
        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Failed), processor)
        advanceTimeBy(3005)
        verify(exactly = 1) { processor.reduce(Action.ClearError) }
    }

    @Test
    fun `If status is not a result do nothing`() = runTest {
        val middleware = StatusMiddleware(this)
        val processor: IProcessor<AppState> = mockk(relaxed = true)

        middleware.process(mockk(relaxed = true), AppState(commandStatus = CommandStatus.Idle), processor)
        advanceTimeBy(3005)
        verify(exactly = 0) { processor.reduce(any()) }
    }
}
