package core

import core.Action.ILoadSettingsIntoState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer
import models.CommandStatus
import models.SettingsState
import models.WindowResult
import transformIf
import update

class AppReducer : IReducer<AppState> {
    override fun reduce(
        action: IAction,
        currentState: AppState,
    ): AppState {
        val state =
            when (action) {
                is Action.SelectDevice -> {
                    currentState
                        .copy(currentDevice = action.device)
                        // If we change the device, clear plugin status
                        .transformIf(currentState.currentDevice != action.device) {
                            it.copy(windows = emptyMap(), currentPlugin = null, companionState = CompanionState())
                        }
                }

                is Action.ClearDevice -> {
                    currentState.copy(
                        currentDevice = null,
                        currentPlugin = null,
                        companionState = CompanionState(),
                        windows = emptyMap(),
                    )
                }

                is Action.DeliverDevices ->
                    currentState.copy(
                        devices = action.devices,
                        commandStatus = CommandStatus.Completed,
                    )

                is Action.DeliverPluginResult -> {
                    val searchTerm = action.searchTerm ?: currentState.windows[action.plugin]?.searchTerm ?: ""
                    currentState.copy(
                        windows =
                            currentState.windows.plus(
                                Pair(
                                    action.plugin,
                                    WindowResult(searchTerm, action.items),
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

                is Action.ClearPlugins -> currentState.copy(windows = emptyMap(), currentPlugin = null)
                is Action.SelectPlugin -> currentState.copy(currentPlugin = action.pluginName)
                is Action.SetCommandRunning ->
                    currentState.copy(
                        commandStatus = CommandStatus.Running,
                        errorMessage = null,
                    )

                is Action.SetCommandCompleted ->
                    currentState.copy(
                        commandStatus = CommandStatus.Completed,
                        errorMessage = null,
                    )

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
                            currentState.windows.update(action.pluginName) {
                                it.copy(searchTerm = action.searchTerm)
                            },
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
