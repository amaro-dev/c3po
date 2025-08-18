package plugins.attrs.structure

import core.command.CommandExecutor
import core.command.DeviceInfoCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware
import plugins.attrs.definition.DeviceAttrsPlugin

class DeviceAttrsMiddleware(
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
            DeviceAttrsPlugin.Actions.List,
                -> {
                execute(DeviceInfoCommand(), state, executor).handle(processor) { deviceInfo ->
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, deviceInfo),
                    )
                }
            }
        }
    }
}