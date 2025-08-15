package plugins.device

import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import plugins.PluginMiddleware

class DevicePluginMiddleware(
    pluginName: String
) : PluginMiddleware(pluginName) {
    override suspend fun asyncProcess(
        action: IAction,
        state: AppState,
        processor: IProcessor<AppState>
    ) {
        when (action) {
            is Action.StartPlugin -> {
                processor.reduce(Action.DeliverPluginResult(pluginName, emptyList<Any>()))
            }
        }
    }
}
