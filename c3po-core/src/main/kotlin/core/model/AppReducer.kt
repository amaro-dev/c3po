package core.model

import core.model.Action.ILoadSettingsIntoState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer
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
                        // If we change the device, only reset companion state but preserve plugin data
                        // Plugin data clearing should only happen on explicit device change by user
                        .transformIf(currentState.currentDevice != action.device) {
                            it.copy(companionState = CompanionState())
                            // Only clear plugin data if this is a genuine device switch (not a reconnection)
                            // For now, we'll preserve plugin data to avoid unwanted resets
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

                is Action.Companion.UpdateState -> {
                    currentState.copy(companionState = action.state)
                }

                else -> currentState
            }
//        println("New State: $state")
        return state
    }
}
