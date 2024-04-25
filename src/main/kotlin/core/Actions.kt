package core

import dev.amaro.sonic.IAction
import models.ActivityInfo
import models.AdbDevice
import models.AppPackage

sealed interface Action : IAction {
    data class SelectDevice(val device: AdbDevice) : Action
    data class SelectTab(val tabIndex: Int) : Action
    data object RefreshDevices : Action
    data object RefreshPackages : Action
    data class StopApp(val app: AppPackage) : Action
    data class UninstallApp(val app: AppPackage) : Action
    data class ClearAppData(val app: AppPackage) : Action
    data class StartActivity(val activity: ActivityInfo) : Action

    data class DeliverDevicePackages(val packages: List<AppPackage>) : Action
    data class DeliverDevices(val devices: List<AdbDevice>) : Action
    data class DeliverActivities(val activities: List<ActivityInfo>) : Action
}
