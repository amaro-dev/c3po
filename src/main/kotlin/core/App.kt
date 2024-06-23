package core

import Settings
import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IAction
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin
import ui.plugins.attrs.DeviceAttrsPlugin
import ui.plugins.packages.PackagesPlugin
import java.awt.datatransfer.Clipboard
import java.io.File

class App(clipboard: Clipboard) {
    val plugins: List<Plugin<out Any>> = listOf(
        ActivitiesPlugin(),
        PackagesPlugin(),
        DeviceAttrsPlugin()
    )

    private val resourcesPath =
        if (Settings.isDebug())
            File(System.getProperty(Settings.RESOURCES_PROP)).parentFile
        else
            File(System.getProperty(Settings.RESOURCES_PROP))

    private val stateManager = AppStateManager(
        CommandMiddleware(),
        PluginSelectorMiddleware(plugins),
        ClipboardMiddleware(clipboard),
        ConditionedDirectMiddleware(
            Action.SelectPlugin::class,
            Action.ClosePlugin::class
        ),
        SettingsMiddleware(resourcesPath, Settings.FILE_NAME)
    )

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()
}
