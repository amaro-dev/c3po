package core

import dev.amaro.sonic.IAction
import models.AdbDevice
import socket.CommandEntry
import java.util.Properties

sealed interface Action : IAction {
    data class UpdatedState(val old: AppState, val new: AppState) : Action
    interface CommandAction : Action

    data object DoNothing : Action

    data class SelectDevice(
        val device: AdbDevice,
    ) : Action

    data object RefreshDevices : CommandAction

    data object ClearPlugins : Action

    data class CopyText(
        val content: String,
    ) : Action

    data object SetCommandRunning : Action

    data object SetCommandCompleted : Action

    data class SetCommandError(
        val message: String,
    ) : Action

    data object ClearError : Action


    data object LoadSettings : Action
    data class ChangeSettingsProperty(
        val key: String,
        val value: String,
    ) : Action
    data object SaveSettings : Action
    data object SettingsNotFound : Action
    interface ILoadSettingsIntoState {
        val props: Properties
    }
    data class LoadSettingsIntoState(override val props: Properties) : Action, ILoadSettingsIntoState
    data class LoadSettingsIntoStateAndSave(
        override val props: Properties,
    ) : Action, ILoadSettingsIntoState

    data class DeliverDevices(
        val devices: List<AdbDevice>,
    ) : Action

    data class DeliverPluginResult(
        val plugin: String,
        val items: List<*>,
        val searchTerm: String = "",
    ) : Action

    data class StartPlugin(
        val pluginName: String,
    ) : Action

    data class SelectPlugin(
        val pluginName: String,
    ) : Action

    data class ClosePlugin(
        val pluginName: String,
    ) : Action

    data class ChangeFilter(
        val pluginName: String,
        val searchTerm: String,
    ) : Action

    sealed interface Companion : Action {
        data object CheckInstalled : Action
        data object CheckRunning : Action
        data object CheckPorts : Action
        data object Prepare : Action
        data object StartService : Action
        data object Connect : Action
        data object Install : Action
        data object SkipForDevice : Action
        data object DoNotUse : Action
        data class UpdateState(val state: CompanionState) : Action
    }


    data class DeliverSocketResponse(
        val reference: CommandEntry,
        val content: List<String>,
    ) : Action

    data class SendSocketRequest(
        val command: String,
        val id: String,
        val arg: String?,
    ) : Action

    data object ListServices : Action
}
