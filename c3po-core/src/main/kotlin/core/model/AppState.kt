package core.model

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
    val hasDeviceSet: Boolean
        get() = currentDevice != null
}
