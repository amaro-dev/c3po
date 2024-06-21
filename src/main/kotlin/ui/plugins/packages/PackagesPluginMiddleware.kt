package ui.plugins.packages

import commands.ClearDataCommand
import commands.ListPackagesCommand
import commands.StopAppCommand
import commands.UninstallAppCommand
import core.Action
import core.AppState
import dev.amaro.sonic.IAction
import dev.amaro.sonic.IMiddleware
import dev.amaro.sonic.IProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackagesPluginMiddleware(private val name: String) : IMiddleware<AppState> {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    override fun process(action: IAction, state: AppState, processor: IProcessor<AppState>) {

        when (action) {
            is Action.StartPlugin,
            PackagesPlugin.Actions.List -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        processor.reduce(
                            Action.DeliverPluginResult(name, ListPackagesCommand(this).run())
                        )
                    }
                }
            }

            is PackagesPlugin.Actions.Stop -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        StopAppCommand(this, action.packageInfo).run()
                    }
                }
            }

            is PackagesPlugin.Actions.Uninstall -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        UninstallAppCommand(this, action.packageInfo).run()
                    }
                }
            }

            is PackagesPlugin.Actions.ClearData -> {
                coroutineScope.launch {
                    state.currentDevice?.run {
                        ClearDataCommand(this, action.packageInfo).run()
                    }
                }
            }
        }
    }

}
