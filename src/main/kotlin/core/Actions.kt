package core

import dev.amaro.sonic.IAction
import models.AdbDevice
import java.util.*

sealed interface Action : IAction {
    interface CommandAction : Action
    data class SelectDevice(val device: AdbDevice) : Action
    data object RefreshDevices : CommandAction
    data class CopyText(val content: String) : Action
    data object SetCommandRunning : Action
    data object SetCommandCompleted : Action

    data object LoadSettings : Action
    data class SaveSettings(val adbPath: String) : Action
    data object SettingsNotFound : Action
    data class LoadSettingsIntoState(val props: Properties) : Action

    data class DeliverDevices(val devices: List<AdbDevice>) : Action
    data class DeliverPluginResult(val plugin: String, val items: List<*>) : Action
    data class StartPlugin(val pluginName: String) : Action
    data class SelectPlugin(val pluginName: String) : Action
    data class ClosePlugin(val pluginName: String) : Action
}
