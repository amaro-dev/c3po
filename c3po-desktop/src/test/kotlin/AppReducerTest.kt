import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import core.Action
import core.AppReducer
import core.AppState
import core.CompanionState
import io.mockk.mockk
import models.AdbDevice
import models.CommandStatus
import models.SettingsState
import models.WindowResult
import org.junit.jupiter.api.Test
import java.util.Properties

class AppReducerTest {
    @Test
    fun `Reduce SelectDevice action`() {
        val initialState = AppState()
        val device: AdbDevice = mockk(relaxed = true)
        val action = Action.SelectDevice(device)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::currentDevice).isEqualTo(device)
        }
    }

    @Test
    fun `Reduce SelectDevice action changing current one clears windows`() {
        val device1: AdbDevice = mockk(relaxed = true)
        val device2: AdbDevice = mockk(relaxed = true)
        val initialState =
            AppState(
                currentDevice = device1,
                windows = mapOf("" to WindowResult("", listOf(""))),
                currentPlugin = "plugin",
            )
        val action = Action.SelectDevice(device2)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::currentDevice).isEqualTo(device2)
            prop(AppState::windows).isEmpty()
            prop(AppState::currentPlugin).isNull()
        }
    }

    @Test
    fun `Reduce DeliverDevices action`() {
        val initialState = AppState(commandStatus = CommandStatus.Running)
        val device1: AdbDevice = mockk(relaxed = true)
        val device2: AdbDevice = mockk(relaxed = true)
        val action = Action.DeliverDevices(listOf(device1, device2))
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::devices).containsExactlyInAnyOrder(device1, device2)
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
        }
    }

    @Test
    fun `Reduce DeliverPluginResult action`() {
        val initialState = AppState(commandStatus = CommandStatus.Running)
        val result = listOf("")
        val action = Action.DeliverPluginResult("plugin", result)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::windows).isEqualTo(mapOf("plugin" to WindowResult("", result)))
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
            prop(AppState::currentPlugin).isEqualTo("plugin")
        }
    }

    @Test
    fun `Reduce DeliverPluginResult action with new search term`() {
        val initialState = AppState(commandStatus = CommandStatus.Running)
        val result = listOf("")
        val action = Action.DeliverPluginResult("plugin", result, "search")
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::windows).isEqualTo(mapOf("plugin" to WindowResult("search", result)))
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
            prop(AppState::currentPlugin).isEqualTo("plugin")
        }
    }

    @Test
    fun `Reduce DeliverPluginResult action with previous result`() {
        val initialState =
            AppState(
                commandStatus = CommandStatus.Running,
                windows = mapOf("plugin" to WindowResult("previous", listOf("old"))),
            )
        val result = listOf("new")
        val action = Action.DeliverPluginResult("plugin", result)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::windows).isEqualTo(mapOf("plugin" to WindowResult("previous", result)))
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
            prop(AppState::currentPlugin).isEqualTo("plugin")
        }
    }

    @Test
    fun `Reduce LoadSettingsIntoState action`() {
        val initialState = AppState()
        val properties = mockk<Properties>(relaxed = true)
        val action = Action.LoadSettingsIntoState(properties)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::settings).isEqualTo(properties)
            prop(AppState::settingsState).isEqualTo(SettingsState.Initialized)
        }
    }

    @Test
    fun `Reduce LoadSettingsIntoStateAndSave action`() {
        val initialState = AppState()
        val properties = mockk<Properties>(relaxed = true)
        val action = Action.LoadSettingsIntoStateAndSave(properties)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::settings).isEqualTo(properties)
            prop(AppState::settingsState).isEqualTo(SettingsState.Initialized)
        }
    }

    @Test
    fun `Reduce SettingsNotFound action`() {
        val initialState = AppState()
        val action = Action.SettingsNotFound
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::settingsState).isEqualTo(SettingsState.NotFound)
        }
    }

    @Test
    fun `Reduce ClearPlugins `() {
        val initialState =
            AppState(
                windows = mapOf("plugin" to WindowResult("", listOf(""))),
                currentPlugin = "plugin",
            )
        val action = Action.ClearPlugins
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::windows).isEmpty()
            prop(AppState::currentPlugin).isNull()
        }
    }

    @Test
    fun `Reduce SelectPlugin `() {
        val initialState =
            AppState(
                currentPlugin = "plugin",
            )
        val action = Action.SelectPlugin("new-plugin")
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::currentPlugin).isEqualTo("new-plugin")
        }
    }

    @Test
    fun `Reduce ChangeFilter `() {
        val initialState =
            AppState(
                windows = mapOf("plugin" to WindowResult("abc", listOf("result"))),
            )
        val action = Action.ChangeFilter("plugin", "123")
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::windows).isEqualTo(mapOf("plugin" to WindowResult("123", listOf("result"))))
        }
    }

    @Test
    fun `Reduce SetCommandRunning`() {
        val initialState =
            AppState(
                commandStatus = CommandStatus.Failed,
                errorMessage = "some error",
            )
        val action = Action.SetCommandRunning
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Running)
            prop(AppState::errorMessage).isNull()
        }
    }

    @Test
    fun `Reduce SetCommandCompleted`() {
        val initialState =
            AppState(
                commandStatus = CommandStatus.Failed,
                errorMessage = "some error",
            )
        val action = Action.SetCommandCompleted
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Completed)
            prop(AppState::errorMessage).isNull()
        }
    }

    @Test
    fun `Reduce SetCommandError`() {
        val initialState =
            AppState(
                commandStatus = CommandStatus.Idle,
            )
        val action = Action.SetCommandError("some-error")
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Failed)
            prop(AppState::errorMessage).isEqualTo("some-error")
        }
    }

    @Test
    fun `Reduce ClearError`() {
        val initialState =
            AppState(
                commandStatus = CommandStatus.Failed,
                errorMessage = "some error",
            )
        val action = Action.ClearError
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::commandStatus).isEqualTo(CommandStatus.Idle)
            prop(AppState::errorMessage).isNull()
        }
    }

    @Test
    fun `Reduce UpdateState Companion`() {
        val initialState = AppState()
        val newCompState = CompanionState(4)
        val action = Action.Companion.UpdateState(newCompState)
        val newState = AppReducer().reduce(action, initialState)
        assertThat(newState).all {
            prop(AppState::companionState).isEqualTo(newCompState)
        }
    }

    @Test
    fun `Reduce ClearDevice erases any device related state`() {
        val mockedDevice: AdbDevice = mockk()
        val initialState =
            AppState(
                currentDevice = mockk(),
                currentPlugin = "plugin",
                windows = mapOf("plugin" to WindowResult<String>("", emptyList())),
                companionState = CompanionState().setIsOnline(),
                devices = listOf(mockedDevice),
            )

        val newState = AppReducer().reduce(Action.ClearDevice, initialState)
        assertThat(newState).isEqualTo(AppState(devices = listOf(mockedDevice)))
    }

    @Test
    fun `Reduce unknown action returns same state`() {
        val initialState = AppState(currentPlugin = "abc", currentDevice = mockk())

        val newState = AppReducer().reduce(Action.DoNothing, initialState)
        assertThat(newState).isEqualTo(initialState)
    }
}
