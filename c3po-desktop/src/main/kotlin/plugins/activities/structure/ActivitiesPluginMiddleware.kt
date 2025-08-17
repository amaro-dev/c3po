package plugins.activities.structure

import core.command.CommandExecutor
import core.command.ListActivitiesCommand
import core.command.StartActivityCommand
import core.model.Action
import core.model.ActivityInfo
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware

class ActivitiesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>,
    ) {
        when (action) {
            is Action.StartPlugin,
            plugins.activities.definition.ActivitiesPlugin.Actions.List,
                -> {
                if (true) { // Use ADB or companion
                    execute(ListActivitiesCommand(), state, executor).handle(processor) {
                        processor.reduce(Action.DeliverPluginResult(pluginName, it))
                    }
                } else {
                    processor.perform(
                        Action.SendSocketRequest(
                            plugins.activities.definition.ActivitiesPlugin.LIST_ACTIVITY_SOCKET_COMMAND,
                            null,
                        ),
                    )
                }
            }

            is plugins.activities.definition.ActivitiesPlugin.Actions.Launch -> {
                execute(StartActivityCommand(action.activityInfo, action.forDebug), state, executor).handle(processor)
            }

            is Action.DeliverSocketResponse -> {
                if (action.reference.command == plugins.activities.definition.ActivitiesPlugin.LIST_ACTIVITY_SOCKET_COMMAND) {
                    val response =
                        action.content
                            .map {
                                val (pkg, service) = it.split(' ')
                                ActivityInfo(pkg, service.removePrefix(pkg))
                            }.sortedBy { it.packageName }
                            .toList()

                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, response),
                    )
                }
            }
        }
    }
}