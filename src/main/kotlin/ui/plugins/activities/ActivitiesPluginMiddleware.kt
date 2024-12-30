package ui.plugins.activities

import commands.CommandExecutor
import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import models.ActivityInfo
import ui.plugins.PluginMiddleware

class ActivitiesPluginMiddleware(
    pluginName: String,
    executor: CommandExecutor,
) : PluginMiddleware(pluginName, executor) {
    override fun process(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List,
                -> {
//                processor.perform(
//                    Action.SendSocketRequest(
//                        ActivitiesPlugin.LIST_ACTIVITY_SOCKET_COMMAND,
//                        UUID.randomUUID().toString(),
//                        null
//                    )
//                )
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(ListActivitiesCommand(), state, processor) {
                    processor.reduce(Action.DeliverPluginResult(pluginName, it, searchTerm))
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                execute(StartActivityCommand(action.activityInfo, action.forDebug), state, processor) { }
            }

            is Action.DeliverSocketResponse -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                if (action.reference.command == ActivitiesPlugin.LIST_ACTIVITY_SOCKET_COMMAND) {
                    val response =
                        action.content
                            .map {
                                val (pkg, service) = it.split(' ')
                                ActivityInfo(pkg, service.removePrefix(pkg))
                            }.sortedBy { it.packageName }
                            .toList()

                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, response, searchTerm),
                    )
                }
            }
        }
    }
}
