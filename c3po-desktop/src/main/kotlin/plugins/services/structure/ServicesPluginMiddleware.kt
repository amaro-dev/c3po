package plugins.services.structure

import core.command.CommandExecutor
import core.command.ListServicesByPackageCommand
import core.command.StartServiceCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
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
                execute(ListServicesByPackageCommand(), state, executor)
                    .handle(processor) {
                        processor.reduce(
                            Action.DeliverPluginResult(pluginName, it),
                        )
                    }
            }

            is ServicesPlugin.Actions.Launch -> {
                execute(StartServiceCommand(action.activityInfo, state.currentDevice!!), state, executor)
                    .handle(processor)
            }

        }
    }
}
