package core.model

import dev.amaro.sonic.IAction
import java.util.Properties

sealed interface Action : IAction {
    data class UpdatedState(
        val old: AppState,
        val new: AppState,
    ) : Action

    interface CommandAction : Action

    data object DoNothing : Action

    data class SelectDevice(
        val device: AdbDevice,
    ) : Action

    data object RefreshDevices : CommandAction

    data object ClearDevice : Action

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

    data class LoadSettingsIntoState(
        override val props: Properties,
    ) : Action,
        ILoadSettingsIntoState

    data class LoadSettingsIntoStateAndSave(
        override val props: Properties,
    ) : Action,
        ILoadSettingsIntoState

    data class DeliverDevices(
        val devices: List<AdbDevice>,
    ) : Action

    data class DeliverPluginResult(
        val plugin: String,
        val items: List<*>,
        val searchTerm: String? = null,
    ) : Action

    data class StartPlugin(
        val pluginName: String,
    ) : Action

    data class SelectPlugin(
        val pluginName: String,
    ) : Action

    data class ChangeFilter(
        val pluginName: String,
        val searchTerm: String,
    ) : Action

    data class UpdatePluginFilters(
        val pluginName: String,
        val filters: Map<String, Any>,
    ) : Action

    data class UpdatePackageSleepState(
        val pluginName: String,
        val packageName: String,
        val sleepState: SleepState,
    ) : Action

    data object StartUSBMonitoring : Action

    data object StopUSBMonitoring : Action

    // Update-related actions
    data object CheckForUpdate : Action

    data class DownloadUpdate(
        val updateInfo: UpdateInfo,
    ) : Action

    data object CancelDownload : Action
    data object UpdateCancelled : Action

    data class UpdateCheckComplete(
        val updateInfo: UpdateInfo?,
    ) : Action

    data class UpdateDownloadProgress(
        val progress: Int,
    ) : Action

    data class UpdateError(
        val message: String,
    ) : Action

    data object DismissUpdate : Action

    data class UpdateDownloadComplete(
        val filePath: String,
    ) : Action

    data class UpdateInstallReady(
        val filePath: String,
    ) : Action

    data class InstallUpdate(
        val filePath: String,
    ) : Action

    data object UpdateInstallComplete : Action

    data object RestartApplication : Action

}
