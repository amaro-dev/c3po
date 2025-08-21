package core.model

import core.model.Action.ILoadSettingsIntoState
import core.update
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer

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
                }

                is Action.ClearDevice -> {
                    currentState.copy(
                        currentDevice = null,
                        currentPlugin = null,
                        windows = emptyMap(),
                    )
                }

                is Action.DeliverDevices ->
                    currentState.copy(
                        devices = action.devices,
                        commandStatus = CommandStatus.Completed,
                    )

                is Action.DeliverPluginResult -> {
                    val existingWindow = currentState.windows[action.plugin]
                    val searchTerm = action.searchTerm ?: existingWindow?.searchTerm ?: ""
                    val filterState = existingWindow?.filterState ?: emptyMap()
                    currentState.copy(
                        windows =
                            currentState.windows.plus(
                                Pair(
                                    action.plugin,
                                    WindowResult(searchTerm, action.items, filterState),
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
                is Action.SelectPlugin -> {
                    currentState.copy(currentPlugin = action.pluginName)
                }
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

                is Action.UpdatePluginFilters ->
                    currentState.copy(
                        windows =
                            currentState.windows.update(action.pluginName) {
                                it.copy(filterState = action.filters)
                            },
                    )

                is Action.UpdatePackageSleepState -> {
                    currentState.copy(
                        windows = currentState.windows.update(action.pluginName) { window ->
                            val packages = window.result as? List<AppPackage> ?: return@update window
                            val updatedPackages = packages.map { pkg ->
                                if (pkg.packageName == action.packageName) {
                                    pkg.copy(sleepState = action.sleepState)
                                } else {
                                    pkg
                                }
                            }
                            window.copy(result = updatedPackages)
                        }
                    )
                }

                else -> currentState
            }
        return state
    }
}