package ui.plugins.services

import commands.CommandExecutor
import commands.StartServiceCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import models.ActivityInfo
import ui.plugins.PluginMiddleware
import java.util.UUID

class ServicesPluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor,
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            ServicesPlugin.Actions.LIST,
                -> {
                processor.perform(
                    Action.SendSocketRequest(
                        ServicesPlugin.LIST_SERVICE_SOCKET_COMMAND,
                        UUID.randomUUID().toString(),
                        null,
                    ),
                )
            }

            is ServicesPlugin.Actions.Launch -> {
                execute(StartServiceCommand(action.activityInfo, state.currentDevice!!), state, executor)
            }

            is Action.DeliverSocketResponse -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
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
                        Action.DeliverPluginResult(pluginName, response, searchTerm),
                    )
                }
            }
        }
    }
}
