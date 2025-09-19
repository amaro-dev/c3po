package plugins.device.structure

import core.command.CommandExecutor
import core.model.Action
import core.model.AdbDevice
import core.model.AppState
import dev.amaro.sonic.IProcessor
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DevicePluginMiddlewareTest {

    private lateinit var middleware: DevicePluginMiddleware
    private lateinit var mockExecutor: CommandExecutor
    private lateinit var mockProcessor: IProcessor<AppState>
    private lateinit var appState: AppState
    private val pluginName = "DEVICE"
    private val capturedActions = mutableListOf<Action>()

    @BeforeEach
    fun setup() {
        mockExecutor = mockk()
        mockProcessor = mockk(relaxed = true)
        middleware = DevicePluginMiddleware(pluginName, mockExecutor)

        // Create mock device
        val mockDevice = mockk<AdbDevice>()

        appState = AppState(currentDevice = mockDevice)

        // Capture all actions sent to processor
        val actionSlot = slot<Action>()
        every { mockProcessor.reduce(capture(actionSlot)) } answers {
            capturedActions.add(actionSlot.captured)
        }
    }

    @Test
    fun `ConfirmRestartDevice action is properly reduced`() = runTest {
        middleware.asyncProcess(Action.ConfirmRestartDevice, appState, mockProcessor)
        verify { mockProcessor.reduce(Action.ConfirmRestartDevice) }
    }

    @Test
    fun `DismissRestartConfirmation action is properly reduced`() = runTest {
        middleware.asyncProcess(Action.DismissRestartConfirmation, appState, mockProcessor)
        verify { mockProcessor.reduce(Action.DismissRestartConfirmation) }
    }

    @Test
    fun `RestartDevice with no device connected shows error and does not complete command`() = runTest {
        val stateWithoutDevice = AppState(currentDevice = null)
        middleware.asyncProcess(Action.RestartDevice, stateWithoutDevice, mockProcessor)
        verify { mockProcessor.reduce(match<Action.SetCommandError> { it.message.contains("No device connected for restart") }) }
    }

    @Test
    fun `confirmation dialog actions are handled by middleware`() = runTest {
        capturedActions.clear()
        middleware.asyncProcess(Action.ConfirmRestartDevice, appState, mockProcessor)
        middleware.asyncProcess(Action.DismissRestartConfirmation, appState, mockProcessor)
        verify {
            mockProcessor.reduce(Action.ConfirmRestartDevice)
            mockProcessor.reduce(Action.DismissRestartConfirmation)
        }
    }

    @Test
    fun `OpenDeviceSettings with no device connected shows error and does not complete command`() = runTest {
        val stateWithoutDevice = AppState(currentDevice = null)
        middleware.asyncProcess(Action.OpenDeviceSettings, stateWithoutDevice, mockProcessor)
        verify { mockProcessor.reduce(match<Action.SetCommandError> { it.message.contains("No device connected for operation") }) }
    }
}
