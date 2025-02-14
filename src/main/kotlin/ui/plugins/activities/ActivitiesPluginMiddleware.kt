package ui.plugins.activities

import commands.CommandExecutor
import commands.ListActivitiesCommand
import commands.StartActivityCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import models.ActivityInfo
import ui.plugins.PluginMiddleware
import java.util.UUID

class ActivitiesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ActivitiesPlugin.Actions.List,
                -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                if (true) { // Use ADB or companion
                    execute(ListActivitiesCommand(), state, executor).handle(processor) {
                        processor.reduce(Action.DeliverPluginResult(pluginName, it, searchTerm))
                    }
                } else {
                    processor.perform(
                        Action.SendSocketRequest(
                            ActivitiesPlugin.LIST_ACTIVITY_SOCKET_COMMAND,
                            UUID.randomUUID().toString(),
                            null,
                        ),
                    )
                }
            }

            is ActivitiesPlugin.Actions.Launch -> {
                execute(StartActivityCommand(action.activityInfo, action.forDebug), state, executor).handle(processor)
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
