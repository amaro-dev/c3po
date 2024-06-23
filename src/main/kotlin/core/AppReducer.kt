package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer

class AppReducer : IReducer<AppState> {
    override fun reduce(action: IAction, currentState: AppState): AppState {
        return when (action) {
            is Action.SelectDevice -> currentState.copy(currentDevice = action.device)
            is Action.DeliverDevices -> currentState.copy(devices = action.devices)
            is Action.DeliverDevicePackages -> currentState.copy(packages = action.packages)
            is Action.DeliverActivities -> currentState.copy(activities = action.activities)
            is Action.DeliverPluginResult -> {
                currentState.copy(
                    windows = currentState.windows.plus(Pair(action.plugin, action.items)),
                    currentPlugin = action.plugin
                )
            }
            is Action.LoadSettingsIntoState -> currentState.copy(
                settings = action.props,
                settingsState = SettingsState.Initialized
            )

            is Action.SettingsNotFound -> currentState.copy(
                settingsState = SettingsState.NotFound
            )
            is Action.SelectPlugin -> currentState.copy(currentPlugin = action.pluginName)
            is Action.ClosePlugin -> currentState.copy(
                windows = currentState.windows.minus(action.pluginName),
                currentPlugin = if (currentState.currentPlugin == action.pluginName)
                    currentState.windows.keys.firstOrNull()
                else
                    currentState.currentPlugin
            )

            else -> currentState
        }
    }
}
