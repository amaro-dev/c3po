package core

import models.AdbDevice
import java.util.*

data class AppState(
    val devices: List<AdbDevice> = emptyList(),
    val currentDevice: AdbDevice? = null,
    val currentPlugin: String? = null,
    val windows: Map<String, WindowResult<*>> = emptyMap(),
    val settings: Properties = Properties(),
    val settingsState: SettingsState = SettingsState.NotInitialized,
    val commandStatus: CommandStatus = CommandStatus.Completed
)

enum class SettingsState {
    NotInitialized,
    NotFound,
    Initialized
}

enum class CommandStatus {
    Running,
    Completed,
    Failed
}

data class WindowResult<out T> (
    val searchTerm: String,
    val result: List<T>
)
