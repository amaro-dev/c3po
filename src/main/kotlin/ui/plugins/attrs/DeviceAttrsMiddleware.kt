package ui.plugins.attrs

import Settings
import commands.DeviceInfoCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeviceAttrsMiddleware(
    private val pluginName: String
) : IMiddleware<AppState> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.StartPlugin,
            DeviceAttrsPlugin.Actions.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        val deviceInfo = DeviceInfoCommand(this@run).run(adbPath)
                        processor.reduce(
                            Action.DeliverPluginResult(pluginName, deviceInfo.toList().sortedBy { it.first })
                        )
                    }
                }
            }
        }
    }
}
