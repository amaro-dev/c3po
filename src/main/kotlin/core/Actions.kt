package core

import dev.amaro.sonic.IAction
import models.ActivityInfo
import models.AdbDevice
import models.AppPackage

sealed interface Action : IAction {
    data class SelectDevice(val device: AdbDevice) : Action
    data object RefreshDevices : Action
    data class CopyText(val content: String) : Action

    data class DeliverDevicePackages(val packages: List<AppPackage>) : Action
    data class DeliverDevices(val devices: List<AdbDevice>) : Action
    data class DeliverActivities(val activities: List<ActivityInfo>) : Action
    data class DeliverPluginResult(val plugin: String, val items: List<*>) : Action
    data class StartPlugin(val pluginName: String) : Action
    data class SelectPlugin(val pluginName: String) : Action
    data class ClosePlugin(val pluginName: String) : Action
}
