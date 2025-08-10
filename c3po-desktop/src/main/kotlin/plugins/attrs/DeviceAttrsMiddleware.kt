package plugins.attrs

import commands.CommandExecutor
import commands.DeviceInfoCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import handle
import plugins.PluginMiddleware

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
