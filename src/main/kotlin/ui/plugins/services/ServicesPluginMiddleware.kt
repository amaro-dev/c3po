package ui.plugins.services

import commands.ListServicesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class ServicesPluginMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ServicesPlugin.Actions.LIST -> {
                state.currentDevice?.run {
                    val activities = ListServicesCommand(this).run()
                    processor.reduce(Action.DeliverPluginResult(pluginName, activities))
                }
            }
        }
    }

}
