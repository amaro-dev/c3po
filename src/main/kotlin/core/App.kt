package core

import dev.amaro.sonic.ConditionedDirectMiddleware
import dev.amaro.sonic.IAction
import ui.plugins.Plugin
import ui.plugins.activities.ActivitiesPlugin
import ui.plugins.attrs.DeviceAttrsPlugin
import ui.plugins.packages.PackagesPlugin

class App {
    val plugins: List<Plugin<out Any>> = listOf(
        ActivitiesPlugin(),
        PackagesPlugin(),
        DeviceAttrsPlugin()
    )

    private val stateManager = AppStateManager(
        CommandMiddleware(),
        PluginSelectorMiddleware(plugins),
        ConditionedDirectMiddleware(
            Action.SelectPlugin::class,
            Action.ClosePlugin::class
        )
    )

    fun perform(action: IAction) = stateManager.perform(action)

    fun listen() = stateManager.listen()
}
