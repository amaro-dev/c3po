package ui.plugins.services

import commands.CommandExecutor
import commands.ListServicesCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class ServicesPluginMiddleware(
    pluginName: String,
    executor: CommandExecutor
) : PluginMiddleware(pluginName,executor) {

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {

        when (action) {
            is Action.StartPlugin,
            ServicesPlugin.Actions.LIST -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListServicesCommand(),state,processor) { activities ->
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, activities.toList().sortedBy { it.first }, searchTerm)
                    )
                }
            }
        }
    }
}
