package plugins.device.structure

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
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
        // When
        middleware.asyncProcess(Action.ConfirmRestartDevice, appState, mockProcessor)

        // Then
        verify { mockProcessor.reduce(Action.ConfirmRestartDevice) }
        assertThat(capturedActions).contains(Action.ConfirmRestartDevice)
    }

    @Test
    fun `DismissRestartConfirmation action is properly reduced`() = runTest {
        // When
        middleware.asyncProcess(Action.DismissRestartConfirmation, appState, mockProcessor)

        // Then
        verify { mockProcessor.reduce(Action.DismissRestartConfirmation) }
        assertThat(capturedActions).contains(Action.DismissRestartConfirmation)
    }

    @Test
    fun `RestartDevice with no device connected shows error and completes command`() = runTest {
        // Given - State with no device
        val stateWithoutDevice = AppState(currentDevice = null)

        // When
        middleware.asyncProcess(Action.RestartDevice, stateWithoutDevice, mockProcessor)

        // Then - Should show error and complete command
        assertThat(capturedActions.size).isEqualTo(2)
        val errorAction = capturedActions[0] as Action.SetCommandError
        assertThat(errorAction.message).contains("No device connected for restart")
        assertThat(capturedActions[1]).isEqualTo(Action.SetCommandCompleted)
    }

    @Test
    fun `confirmation dialog actions are handled by middleware`() = runTest {
        // Clear captured actions
        capturedActions.clear()

        // Test the flow: ConfirmRestartDevice → DismissRestartConfirmation

        // Step 1: Show confirmation dialog
        middleware.asyncProcess(Action.ConfirmRestartDevice, appState, mockProcessor)

        // Step 2: Dismiss confirmation dialog
        middleware.asyncProcess(Action.DismissRestartConfirmation, appState, mockProcessor)

        // Verify both actions were properly processed
        assertThat(capturedActions.size).isEqualTo(2)
        assertThat(capturedActions[0]).isEqualTo(Action.ConfirmRestartDevice)
        assertThat(capturedActions[1]).isEqualTo(Action.DismissRestartConfirmation)
    }
}