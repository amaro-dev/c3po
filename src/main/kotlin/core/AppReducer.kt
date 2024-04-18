package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.IReducer

class AppReducer: IReducer<AppState> {
    override fun reduce(action: IAction, currentState: AppState): AppState {
        return when (action) {
            is Action.SelectDevice -> currentState.copy(currentDevice = action.device)
            is Action.SelectTab -> currentState.copy(currentTab = action.tabIndex)
            is Action.DeliverDevices -> currentState.copy(devices = action.devices)
            is Action.DeliverDevicePackages -> currentState.copy(packages = action.packages)
            is Action.DeliverActivities -> currentState.copy(activities = action.activities)
            else -> currentState
        }
    }
}
