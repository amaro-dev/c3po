package core.model

import assertk.all
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import assertk.assertions.prop
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.Properties

class AppReducerTest {

    private val reducer = AppReducer()
    private val initialState = AppState()

    @Test
    fun `reduce - DownloadUpdate sets state to Downloading with progress 0`() {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.DownloadUpdate(updateInfo)

        val result = reducer.reduce(action, initialState)

        assertThat(result.updateState).isEqualTo(UpdateState.Downloading)
        assertThat(result.updateInfo).isEqualTo(updateInfo)
        assertThat(result.downloadProgress).isEqualTo(0)
    }

    @Test
    fun `reduce - UpdateDownloadProgress updates progress`() {
        val action = Action.UpdateDownloadProgress(50)
        val stateWithDownload = initialState.copy(
            updateState = UpdateState.Downloading,
            downloadProgress = 25
        )

        val result = reducer.reduce(action, stateWithDownload)

        assertThat(result.downloadProgress).isEqualTo(50)
        assertThat(result.updateState).isEqualTo(UpdateState.Downloading)
    }

    @Test
    fun `reduce - UpdateDownloadComplete sets state to DownloadComplete`() {
        val action = Action.UpdateDownloadComplete("/path/to/file.dmg")
        val stateWithDownload = initialState.copy(
            updateState = UpdateState.Downloading,
            downloadProgress = 100
        )

        val result = reducer.reduce(action, stateWithDownload)

        assertThat(result.updateState).isEqualTo(UpdateState.DownloadComplete)
        assertThat(result.downloadProgress).isEqualTo(100)
    }

    @Test
    fun `reduce - InstallUpdate sets state to Installing`() {
        val action = Action.InstallUpdate("/path/to/file.dmg")
        val stateWithDownload = initialState.copy(
            updateState = UpdateState.DownloadComplete
        )

        val result = reducer.reduce(action, stateWithDownload)

        assertThat(result.updateState).isEqualTo(UpdateState.Installing)
    }

    @Test
    fun `reduce - UpdateInstallComplete resets update state`() {
        val action = Action.UpdateInstallComplete
        val stateWithInstall = initialState.copy(
            updateState = UpdateState.Installing,
            updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        )

        val result = reducer.reduce(action, stateWithInstall)

        assertThat(result.updateState).isEqualTo(UpdateState.NoUpdate)
        assertThat(result.updateInfo).isNull()
    }

    @Test
    fun `reduce - UpdateError sets error state and message`() {
        val action = Action.UpdateError("Network error occurred")
        val stateWithDownload = initialState.copy(
            updateState = UpdateState.Downloading
        )

        val result = reducer.reduce(action, stateWithDownload)

        assertThat(result.updateState).isEqualTo(UpdateState.Error)
        assertThat(result.errorMessage).isEqualTo("Network error occurred")
    }

    @Test
    fun `reduce - CheckForUpdate sets state to CheckingForUpdate`() {
        val action = Action.CheckForUpdate

        val result = reducer.reduce(action, initialState)

        assertThat(result.updateState).isEqualTo(UpdateState.CheckingForUpdate)
    }

    @Test
    fun `reduce - UpdateCheckComplete with update sets UpdateAvailable`() {
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        val action = Action.UpdateCheckComplete(updateInfo)
        val stateWithCheck = initialState.copy(
            updateState = UpdateState.CheckingForUpdate
        )

        val result = reducer.reduce(action, stateWithCheck)

        assertThat(result.updateState).isEqualTo(UpdateState.UpdateAvailable)
        assertThat(result.updateInfo).isEqualTo(updateInfo)
    }

    @Test
    fun `reduce - UpdateCheckComplete with null sets NoUpdate`() {
        val action = Action.UpdateCheckComplete(null)
        val stateWithCheck = initialState.copy(
            updateState = UpdateState.CheckingForUpdate
        )

        val result = reducer.reduce(action, stateWithCheck)

        assertThat(result.updateState).isEqualTo(UpdateState.NoUpdate)
        assertThat(result.updateInfo).isNull()
    }

    @Test
    fun `reduce - DismissUpdate sets UpdateDismissed state`() {
        val action = Action.DismissUpdate
        val stateWithUpdate = initialState.copy(
            updateState = UpdateState.UpdateAvailable,
            updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")
        )

        val result = reducer.reduce(action, stateWithUpdate)

        assertThat(result.updateState).isEqualTo(UpdateState.UpdateDismissed)
        assertThat(result.updateInfo).isEqualTo(stateWithUpdate.updateInfo)
    }

    @Test
    fun `reduce - RestartApplication leaves state unchanged`() {
        val action = Action.RestartApplication
        val currentState = initialState.copy(
            updateState = UpdateState.Installing,
            errorMessage = "Some error"
        )

        val result = reducer.reduce(action, currentState)

        assertThat(result).isEqualTo(currentState)
    }

    @Test
    fun `reduce - existing functionality preserved - SelectDevice`() {
        val device = AdbDevice("device123", "Test Device")
        val action = Action.SelectDevice(device)

        val result = reducer.reduce(action, initialState)

        assertThat(result.currentDevice).isEqualTo(device)
    }

    @Test
    fun `reduce - existing functionality preserved - DeliverDevices`() {
        val devices = listOf(
            AdbDevice("device1", "Device 1"),
            AdbDevice("device2", "Device 2")
        )
        val action = Action.DeliverDevices(devices)

        val result = reducer.reduce(action, initialState)

        assertThat(result.devices).isEqualTo(devices)
        assertThat(result.commandStatus).isEqualTo(CommandStatus.Completed)
    }

    @Test
    fun `reduce - state transitions maintain data consistency`() {
        var state = initialState
        val updateInfo = UpdateInfo("2.1.0", "https://example.com/app.dmg")

        // Check for update
        state = reducer.reduce(Action.CheckForUpdate, state)
        assertThat(state.updateState).isEqualTo(UpdateState.CheckingForUpdate)

        // Update found
        state = reducer.reduce(Action.UpdateCheckComplete(updateInfo), state)
        assertThat(state.updateState).isEqualTo(UpdateState.UpdateAvailable)
        assertThat(state.updateInfo).isEqualTo(updateInfo)

        // Start download
        state = reducer.reduce(Action.DownloadUpdate(updateInfo), state)
        assertThat(state.updateState).isEqualTo(UpdateState.Downloading)
        assertThat(state.downloadProgress).isEqualTo(0)

        // Progress updates
        state = reducer.reduce(Action.UpdateDownloadProgress(50), state)
        assertThat(state.downloadProgress).isEqualTo(50)
        assertThat(state.updateState).isEqualTo(UpdateState.Downloading)

        // Download complete
        state = reducer.reduce(Action.UpdateDownloadComplete("/path/file.dmg"), state)
        assertThat(state.updateState).isEqualTo(UpdateState.DownloadComplete)

        // Install
        state = reducer.reduce(Action.InstallUpdate("/path/file.dmg"), state)
        assertThat(state.updateState).isEqualTo(UpdateState.Installing)

        // Install complete
        state = reducer.reduce(Action.UpdateInstallComplete, state)
        assertThat(state.updateState).isEqualTo(UpdateState.NoUpdate)
        assertThat(state.updateInfo).isNull()
    }

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
    fun `Reduce ClearDevice erases any device related state`() {
        val mockedDevice: AdbDevice = mockk()
        val initialState =
            AppState(
                currentDevice = mockk(),
                currentPlugin = "plugin",
                windows = mapOf("plugin" to WindowResult<String>("", emptyList())),
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