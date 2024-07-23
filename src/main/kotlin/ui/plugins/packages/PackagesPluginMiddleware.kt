package ui.plugins.packages

import Settings
import commands.ClearDataCommand
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.plugins.PluginMiddleware

class PackagesPluginMiddleware(private val pluginName: String) : PluginMiddleware(pluginName) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {
        val adbPath = state.settings.getProperty(Settings.ADB_PATH_PROP)
        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List -> {
                val searchTerm = state.windows[pluginName]?.searchTerm ?: ""
                coroutineScope.launch {
                    state.currentDevice?.run {
                        processor.reduce(
                            Action.DeliverPluginResult(
                                pluginName,
                                ListPackagesCommand(this).run(adbPath),
                                searchTerm
                            )
                        )
                    }
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        StopAppCommand(this, action.packageInfo).run(adbPath)
                    }
                    processor.reduce(Action.SetCommandCompleted)
                }
            }

            is PackagesPlugin.Actions.Uninstall -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        UninstallAppCommand(this, action.packageInfo).run(adbPath)
                    }
                    processor.reduce(Action.SetCommandCompleted)
                }
            }

            is PackagesPlugin.Actions.ClearData -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        ClearDataCommand(this, action.packageInfo).run(adbPath)
                    }
                    processor.reduce(Action.SetCommandCompleted)
                }
            }
        }
    }

}
