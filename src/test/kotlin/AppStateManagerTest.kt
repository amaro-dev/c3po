import core.Action
import core.AppState
import core.AppStateManager
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test

class AppStateManagerTest {
    @Test
    fun `On reduce, call reducer with the proper parameters`() {
        val oldState = AppState()
        val reducer: IReducer<AppState> =
            mockk(relaxed = true) {
                every { reduce(any(), any()) } returns AppState()
            }
        val stateManager = spyk(AppStateManager(oldState, reducer))
        val action: IAction = mockk(relaxed = true)

        stateManager.reduce(action)

        verify { reducer.reduce(action, oldState) }
    }

    @Test
    fun `On reduce, perform UpdateState informing the old and new states`() {
        val oldState = AppState()
        val newState = AppState(currentDevice = mockk())
        val reducer: IReducer<AppState> =
            mockk(relaxed = true) {
                every { reduce(any(), any()) } returns newState
            }
        val stateManager = spyk(AppStateManager(oldState, reducer))
        val action = mockk<IAction>(relaxed = true)

        stateManager.reduce(action)

        verify { stateManager.perform(Action.UpdatedState(oldState, newState)) }
    }

    @Test
    fun `After reduce, perform any scheduled actions`() {
        val reducer: IReducer<AppState> =
            mockk(relaxed = true) {
                every { reduce(any(), any()) } returns AppState()
            }
        val stateManager = spyk(AppStateManager(AppState(), reducer))
        val action = mockk<IAction>(relaxed = true)

        val scheduledAction = mockk<IAction>(relaxed = true)

        stateManager.schedule(scheduledAction)

        // Run two times but the scheduled action should be removed from the queue after the first run
        stateManager.reduce(action)
        stateManager.reduce(action)

        verify(exactly = 1) { stateManager.perform(scheduledAction) }
    }
}
