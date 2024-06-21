package ui.plugins.services

import commands.ListServicesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ServicesPluginMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ServicesPlugin.Actions.LIST -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val activities = ListServicesCommand(this).run()
                        processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                    }
                }
            }
        }
    }

}
