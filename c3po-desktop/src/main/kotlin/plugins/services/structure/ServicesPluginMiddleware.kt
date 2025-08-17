package plugins.services.structure

import core.command.CommandExecutor
import core.command.StartServiceCommand
import core.model.Action
import core.model.ActivityInfo
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware
import plugins.services.definition.ServicesPlugin

class ServicesPluginMiddleware(
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
            ServicesPlugin.Actions.LIST,
                -> {
                processor.perform(
                    Action.SendSocketRequest(
                        ServicesPlugin.LIST_SERVICE_SOCKET_COMMAND,
                        null,
                    ),
                )
            }

            is ServicesPlugin.Actions.Launch -> {
                execute(StartServiceCommand(action.activityInfo, state.currentDevice!!), state, executor)
                    .handle(processor)
            }

            is Action.DeliverSocketResponse -> {
                if (action.reference.command == ServicesPlugin.LIST_SERVICE_SOCKET_COMMAND) {
                    val response =
                        action.content
                            .map {
                                val (pkg, service) = it.split(' ')
                                ActivityInfo(pkg, service.removePrefix(pkg))
                            }.sortedBy { it.packageName }
                            .groupBy { it.packageName }
                            .toList()

                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, response),
                    )
                }
            }
        }
    }
}
