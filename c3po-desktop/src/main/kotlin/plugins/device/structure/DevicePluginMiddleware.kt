package plugins.device.structure

import core.command.CommandExecutor
import core.command.GetFullDeviceInfoCommand
import core.handle
import core.model.Action
import core.model.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware

class DevicePluginMiddleware(
    pluginName: String,
    private val executor: CommandExecutor
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        when (action) {
            is Action.StartPlugin -> {
                if (action.pluginName == pluginName && state.currentDevice != null) {
                    execute(GetFullDeviceInfoCommand(), state, executor).handle(processor) { deviceInfo ->
                        processor.reduce(Action.DeliverPluginResult(pluginName, listOf(deviceInfo)))
                    }
                } else {
                    processor.reduce(Action.DeliverPluginResult(pluginName, emptyList<Any>()))
                }
            }
        }
    }
}