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
                val device = state.currentDevice
                if (device == null) {
                    processor.reduce(Action.SetCommandError("No device connected for service launch"))
                    return
                }

                execute(StartServiceCommand(action.activityInfo, device), state, executor)
                    .onSuccess {
                        processor.reduce(Action.SetSuccess("Service '${action.activityInfo.fullPath}' started successfully"))
                    }
                    .handle(processor)
            }

        }
    }
}
