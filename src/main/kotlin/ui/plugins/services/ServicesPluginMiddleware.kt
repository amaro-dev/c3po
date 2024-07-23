package ui.plugins.services

import Settings
import commands.ListServicesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.plugins.PluginMiddleware

class ServicesPluginMiddleware(
    private val pluginName: String
) : PluginMiddleware(pluginName) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)

        when (action) {
            is Action.StartPlugin,
            ServicesPlugin.Actions.LIST -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val activities = ListServicesCommand(this).run(adbPath).toList().sortedBy { it.first }
                        println(activities)
                        processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                    }
                }
            }
        }
    }

}
