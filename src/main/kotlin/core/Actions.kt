package core

import dev.amaro.sonic.IAction
import dev.amaro.sonic.ISideEffectAction
import models.AdbDevice
import socket.CommandEntry
import java.util.Properties

sealed interface Action : IAction {
    interface CommandAction : Action

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
    ) : Action,
        ISideEffectAction {
        override val sideEffect: IAction = SaveSettings
    }

    data object SaveSettings : Action

    data object SettingsNotFound : Action

    data class LoadSettingsIntoState(
        val props: Properties,
    ) : Action

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

    data object CheckForCompanion : Action

    data object PrepareCompanion : Action

    data object ConnectCompanion : Action

    data class UpdateCompanionState(
        val state: CompanionState,
    ) : Action

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
