package core.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import org.junit.jupiter.api.Test

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
}