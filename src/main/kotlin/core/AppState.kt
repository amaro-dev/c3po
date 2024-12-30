package core

import Settings
import models.AdbDevice
import toBool
import java.util.Properties

data class AppState(
    val devices: List<AdbDevice> = emptyList(),
    val currentDevice: AdbDevice? = null,
    val currentPlugin: String? = null,
    val windows: Map<String, WindowResult<*>> = emptyMap(),
    val settings: Properties = Properties(),
    val settingsState: SettingsState = SettingsState.NotInitialized,
    val commandStatus: CommandStatus = CommandStatus.Idle,
    val errorMessage: String? = null,
    val companionState: CompanionState = CompanionState(0),
) {
    val shouldShowCompanionDialog: Boolean
        get() = currentDevice != null && !settings[Settings.ACCEPT_COMPANION].toBool()

}

enum class SettingsState {
    NotInitialized,
    NotFound,
    Initialized,
}

enum class CommandStatus {
    Idle,
    Running,
    Completed,
    Failed,
}

data class WindowResult<out T>(
    val searchTerm: String,
    val result: List<T>,
)
