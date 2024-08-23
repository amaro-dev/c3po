package ui.plugins.attrs

import commands.CommandExecutor
import commands.DeviceInfoCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import ui.plugins.PluginMiddleware

class DeviceAttrsMiddleware(
    pluginName: String,
    executor: CommandExecutor
) : PluginMiddleware(pluginName, executor) {

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            DeviceAttrsPlugin.Actions.List -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                execute(DeviceInfoCommand(), state, processor) { deviceInfo ->
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, deviceInfo.toList().sortedBy { it.first }, searchTerm)
                    )
                }
            }
        }
    }
}
