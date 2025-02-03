package core

import core.Action.ILoadSettingsIntoState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer
import transformIf

class AppReducer : IReducer<AppState> {
    override fun reduce(
        action: IAction,
        currentState: AppState,
    ): AppState {
        val state = when (action) {
            is Action.SelectDevice -> {
                currentState.copy(currentDevice = action.device)
                    // If we change the device, clear plugin status
                    .transformIf(currentState.currentDevice != action.device) {
                        it.copy(windows = emptyMap())
                    }
            }
            is Action.DeliverDevices ->
                currentState.copy(
                    devices = action.devices,
                    commandStatus = CommandStatus.Completed,
                )

            is Action.DeliverPluginResult -> {
                currentState.copy(
                    windows =
                        currentState.windows.plus(
                            Pair(
                                action.plugin,
                                WindowResult(action.searchTerm, action.items),
                            ),
                        ),
                    currentPlugin = action.plugin,
                    commandStatus = CommandStatus.Completed,
                )
            }

            is ILoadSettingsIntoState ->
                currentState.copy(
                    settings = action.props,
                    settingsState = SettingsState.Initialized,
                )

            is Action.SettingsNotFound ->
                currentState.copy(
                    settingsState = SettingsState.NotFound,
                )

            is Action.ClearPlugins -> currentState.copy(windows = emptyMap())
            is Action.SelectPlugin -> currentState.copy(currentPlugin = action.pluginName)
            is Action.ClosePlugin ->
                currentState.copy(
                    windows = currentState.windows.minus(action.pluginName),
                    currentPlugin =
                        if (currentState.currentPlugin == action.pluginName) {
                            currentState.windows.keys.firstOrNull()
                        } else {
                            currentState.currentPlugin
                        },
                )

            is Action.SetCommandRunning ->
                currentState.copy(
                    commandStatus = CommandStatus.Running,
                    errorMessage = null,
                )

            is Action.SetCommandCompleted -> currentState.copy(commandStatus = CommandStatus.Completed)
            is Action.SetCommandError ->
                currentState.copy(
                    commandStatus = CommandStatus.Failed,
                    errorMessage = action.message,
                )

            is Action.ClearError ->
                currentState.copy(
                    errorMessage = null,
                    commandStatus = CommandStatus.Idle,
                )

            is Action.ChangeFilter ->
                currentState.copy(
                    windows =
                        currentState.windows.plus(
                            Pair(
                                action.pluginName,
                                WindowResult(
                                    action.searchTerm,
                                    currentState.windows[action.pluginName]?.result ?: emptyList(),
                                ),
                            ),
                        ),
                )

            is Action.Companion.UpdateState -> {
                currentState.copy(companionState = action.state)
            }

            else -> currentState
        }
//        println("New State: $state")
        return state
    }
}
