package ui.plugins.attrs

import commands.DeviceInfoCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor

class DeviceAttrsMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        when (action) {
            is Action.StartPlugin,
            DeviceAttrsPlugin.Actions.List -> {
                state.currentDevice?.run {
                    val deviceInfo = DeviceInfoCommand(this).run()
                    processor.reduce(
                        Action.DeliverPluginResult(pluginName, deviceInfo.toList().sortedBy { it.first })
                    )
                }
            }
        }
    }
}
