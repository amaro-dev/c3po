package ui.plugins.activities

import commands.CommandExecutor
import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class ActivitiesPluginMiddleware(
    pluginName: String,
    executor: CommandExecutor
) : PluginMiddleware(pluginName, executor) {

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListActivitiesCommand(), state, processor) {
                    processor.reduce(Action.DeliverPluginResult(pluginName, it, searchTerm))
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                execute(StartActivityCommand(action.activityInfo, action.forDebug), state, processor) { }
            }
        }
    }
}
