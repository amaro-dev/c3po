package core

import Settings
import commands.CommandExecutor
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IAction
import socket.SocketClient
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin
import ui.plugins.attrs.DeviceAttrsPlugin
import ui.plugins.intents.pending.PendingIntentsPlugin
import ui.plugins.packages.PackagesPlugin
import ui.plugins.permissions.PermissionsPlugin
import ui.plugins.services.ServicesPlugin
import java.awt.datatransfer.Clipboard
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

class App(
    clipboard: Clipboard,
) {
    private val socketClient = SocketClient()

    private val executor = CommandExecutor()

    val plugins: List<Plugin<*>> =
        listOf(
            ActivitiesPlugin(executor),
            PackagesPlugin(executor),
            DeviceAttrsPlugin(executor),
            ServicesPlugin(executor),
            PermissionsPlugin(executor),
            PendingIntentsPlugin(executor),
        )

    private val resourcesPath =
        if (Settings.isDebug()) {
            File(Paths.get("build").absolutePathString())
        } else {
            File(Settings.productionSettingsFolder()).apply {
                if (!exists()) Files.createDirectory(toPath())
            }
        }

    private val stateManager =
        AppStateManager(
            DeviceMiddleware(executor),
            PluginSelectorMiddleware(plugins),
            ClipboardMiddleware(clipboard),
            ConditionedDirectMiddleware(
                Action.SelectPlugin::class,
                Action.ClosePlugin::class,
                Action.ChangeFilter::class,
                Action.ClearError::class,
            ),
            SettingsMiddleware(resourcesPath, Settings.FILE_NAME),
            CompanionMiddleware(executor),
            SocketMiddleware(socketClient),
        )

    fun start() {
        perform(Action.LoadSettings)
    }

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()

    fun exit() {
        socketClient.close()
    }
}

fun debug(message: String) {
    //println("[DEBUG] $message")
}
